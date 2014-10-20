package de.uniHamburg.informatik.continuousvoice.services.sound.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.media.MediaRecorder;

public class SoundMeter {

    public static final double MAXIMUM_AMPLITUDE = (32768/2700.0); //from: http://stackoverflow.com/a/15613051/1686216
    private static final String TAG = SoundMeter.class.getCanonicalName();
    private MediaRecorder mRecorder = null;
    private List<SilenceListener> listeners = new ArrayList<SilenceListener>();
    private ScheduledExecutorService scheduleTaskExecutor;
    private boolean running;
    private int state;
    private int silenceSince = 0;
    private double currentAmplitude = 0;
    
    /** STATES **/
    public static final int SILENT = 0;
    public static final int LOUD = 1;
    
    public static final double SILENCE_THRESHOLD = 2.0;
    public static final int SILENCE_TIME = 2000;
    private static final long SILENCE_POLLING_TIME = 30;
    
    public SoundMeter() {
        mRecorder = createRecorder();
    }

    private MediaRecorder createRecorder() {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile("/dev/null");
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recorder;
    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = createRecorder();
        }
        mRecorder.start();
        running = true;
        startMeasurement();
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        running = false;
    }

    public double getAmplitude() {
        return currentAmplitude;
    }
    
    private int getCurrentState() {
        double amplitude = getAmplitude();
        if (amplitude < SILENCE_THRESHOLD) {
            return SILENT;
        } else {
            return LOUD;
        }
    }
    
    public void addSilenceListener(SilenceListener sl) {
        listeners.add(sl);
    }
    
    private void updateAmplitude() {
        if (mRecorder != null) {
            //We have to skip 0.0 here
            double newAplitude = mRecorder.getMaxAmplitude() / 2700.0;
            if (newAplitude != 0.0) {
                currentAmplitude =  newAplitude;
            }
        } else {
            currentAmplitude = 0;
        }
    }
    
    private void notifyListeners(boolean silent) {
        for (SilenceListener l: listeners) {
            if (silent) {
                l.onSilence();
            } else {
                l.onSpeech();
            }
        }
    }
    
    private void startMeasurement() {
        if (scheduleTaskExecutor == null) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        }

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (running) {
                    updateAmplitude();
                    int current = getCurrentState();
                    if (current == SILENT) {
                        silenceSince += SILENCE_POLLING_TIME;
                        if (silenceSince >= SILENCE_TIME) {
                            switchToState(SILENT);
                            silenceSince = 0;
                        }
                    } else {
                        silenceSince = 0;
                        switchToState(LOUD);
                    }
                } else {
                    scheduleTaskExecutor.shutdown();
                }
            }
        }, 0, SILENCE_POLLING_TIME, TimeUnit.MILLISECONDS);
        running = true;
    }
    
    private void switchToState(int newState) {
        if (state != newState) {
            notifyListeners(newState == SILENT);
            state = newState;
        }
    }
    
}
