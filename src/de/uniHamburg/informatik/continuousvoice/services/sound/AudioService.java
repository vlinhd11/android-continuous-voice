package de.uniHamburg.informatik.continuousvoice.services.sound;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

/**
 * sampletrates: possible but not garantueed: 8000, 11025, 16000, 22050, 44100
 * http://stackoverflow.com/a/9223936/1686216
 * 
 * According to the javadocs, all devices are guaranteed to support this format
 * (for recording): 44100, AudioFormat.CHANNEL_IN_MONO,
 * AudioFormat.ENCODING_PCM_16BIT.
 * 
 * max amplitude: http://stackoverflow.com/a/15613051/1686216
 * 
 * @author marius
 * 
 */
public abstract class AudioService implements IAudioService {
    //config
    private final String        TAG = "AudioService";
    private static final int    SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int    FORMAT = MediaRecorder.OutputFormat.AMR_NB;
    private static final int    ENCODER = MediaRecorder.AudioEncoder.AMR_NB;
    public static final String  MIME_TYPE = "audio/amr; rate=8000";
    public static final String  BASE_FILENAME = "audioservice";
    public static final double  SILENCE_AMPLITUDE_THRESHOLD = 2.0;
    public static final int     SILENCE_POLLING_TIME = 100;
    public static final int     SILENCE_OFFSET_TIME = 2000;
    public static int           SILENCE_OFFSET_MILLIS = 2000;

    //media recorders
    private FileMediaRecorder currentRecorder = null;
    private FileMediaRecorder persistentRecorder = null;
    private FileMediaRecorder transientRecorder = null;

    //soundfile recording
    private int recorderIteration = 0;
    private boolean recording = false;
    private List<IAmplitudeListener> listeners = new ArrayList<IAmplitudeListener>();
    private List<IAudioServiceStartStopListener> startStopListeners = new ArrayList<IAudioServiceStartStopListener>();
    public static final String SUFFIX = "amr";

    //aplitude measurement
    public static final double MAXIMUM_AMPLITUDE = (32768 / 2700.0);
    private boolean running = false;
    private ScheduledExecutorService scheduleTaskExecutor;
    private double currentAmplitude = 0.0;
    private int silenceSince = 0;
    private Loudness lastNotificationState;

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#initialize()
     */
    @Override
    public void initialize() {
        persistentRecorder = createRecorder(true);
        transientRecorder = createRecorder(false);
        currentRecorder = createRecorder(false);

        startRecorder(currentRecorder);
        running = true;
        notifyStartStopListeners();
        startAmplitudeMeasurement();
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#shutdown()
     */
    @Override
    public void shutdown() {
        running = false;
        notifyStartStopListeners();
        scheduleTaskExecutor = null;
        terminateRecorder(currentRecorder); //no matter if persistent or transient

        currentRecorder = null;
        persistentRecorder = null;
        transientRecorder = null;
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#startRecording()
     */
    @Override
    public void startRecording() {
        if (recording) {
            throw new IllegalStateException("Already recording.");
        }

        //1 release the transient recorder (current with no file)
        terminateRecorder(currentRecorder);
        //2 set the ready-to-start persistent recorder as current
        currentRecorder = persistentRecorder;
        //3 start the current recorder
        startRecorder(currentRecorder);
        recording = true;
        //4 create a new persistentRecorder in advance
        persistentRecorder = createRecorder(true);
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#stopRecording()
     */
    @Override
    public File stopRecording() {
        if (!recording) {
            throw new IllegalStateException("Currently not recording.");
        }

        //1 terminate the current recorder (which is a persistent recorder)
        terminateRecorder(currentRecorder);
        recording = false;
        //2 get the file
        File currentFile = currentRecorder.getFile();
        Log.i(TAG, "Soundfile: " + currentFile.getName() + " exists: " + currentFile.exists() + " size: " + (currentFile.length()/1024.0) + "kb");
        //3 set the ready-to-start transient recorder as current
        currentRecorder = transientRecorder;
        //4 start the current transient recorder
        startRecorder(currentRecorder);
        //5 create a new 
        transientRecorder = createRecorder(false);
        //5 check if file exists
        if (!currentFile.exists()) {
            throw new IllegalStateException("The soundfile " + currentFile.getAbsolutePath() + " does not exist.");
        }

        return currentFile;
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#splitRecording()
     */
    @Override
    public File splitRecording() {
        if (!recording) {
            throw new IllegalStateException("Cannot split. Currently not recording.");
        }
        // 1 terminate current recorder
        terminateRecorder(currentRecorder);
        // 2 get the file
        File file = currentRecorder.getFile();
        // 3 start next recorder immediately
        currentRecorder = persistentRecorder;
        startRecorder(persistentRecorder);
        // 4 create new recorder
        persistentRecorder = createRecorder(true);
        // 5 return the file
        return file;
    }

    private FileMediaRecorder createRecorder(boolean persistent) {
        String currentFileName = null;
        if (persistent) {
            currentFileName = getNextFileName();
        }

        FileMediaRecorder recorder = new FileMediaRecorder(currentFileName);
        recorder.setAudioSource(SOURCE);
        recorder.setOutputFormat(FORMAT);
        recorder.setAudioEncoder(ENCODER);


        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recorder;
    }

    private String getNextFileName() {
        recorderIteration++;
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String basePath = dir.getAbsolutePath();
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        return basePath + "/" + BASE_FILENAME + "_" + date + "_" + recorderIteration + "." + SUFFIX;
    }

    private void terminateRecorder(MediaRecorder recorder) {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.reset();
                recorder.release();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "ERROR: cannot stop recorder. Already stopped / not started yet?" + e.getMessage());
        }
    }

    private void startRecorder(MediaRecorder recorder) {
        try {
            if (recorder != null) {
                recorder.start();
            }
        } catch (IllegalStateException e) {
            //already started
            Log.e(TAG, "ERROR: Cannot start recorder. Already started? " + e.getMessage());
        }
    }

    private void startAmplitudeMeasurement() {
        if (scheduleTaskExecutor == null) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        }

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (running) {
                    updateAmplitude();
                    updateSilenceState();
                } else {
                    scheduleTaskExecutor.shutdown();
                }
            }
        }, 0, SILENCE_POLLING_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * update amplitude and notifly listeners if valid amplitude
     */
    private void updateAmplitude() {
        if (currentRecorder != null) {
            //We have to skip 0.0 to prevent flickering.
            double newAplitude = currentRecorder.getMaxAmplitude() / 2700.0;
            if (newAplitude != 0.0) {
                currentAmplitude = newAplitude;
                //notify listeners
                for (IAmplitudeListener al : listeners) {
                    al.onAmplitudeUpdate(newAplitude / MAXIMUM_AMPLITUDE);
                }
            }
        } else {
            currentAmplitude = 0;
        }
    }

    private void updateSilenceState() {
        Loudness current = getCurrentSilenceState();

        if (current == Loudness.SILENCE) {
            silenceSince += SILENCE_POLLING_TIME;
            if (silenceSince >= SILENCE_OFFSET_TIME) {
                notifySilenceListeners(Loudness.SILENCE);
                silenceSince = 0;
            }
        } else {
            silenceSince = 0;
            notifySilenceListeners(Loudness.SPEECH);
        }
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#getCurrentSilenceState()
     */
    @Override
    public Loudness getCurrentSilenceState() {
        if (currentAmplitude < SILENCE_AMPLITUDE_THRESHOLD) {
            return Loudness.SILENCE;
        } else {
            return Loudness.SPEECH;
        }
    }

    private void notifySilenceListeners(Loudness state) {
        if (state != lastNotificationState) {
            if (state == Loudness.SPEECH) {
                for (IAmplitudeListener sl : listeners) {
                    sl.onSpeech();
                }
            } else {
                for (IAmplitudeListener sl : listeners) {
                    sl.onSilence();
                }
            }
        }
        lastNotificationState = state;
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#addAmplitudeListener(de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener)
     */
    @Override
    public void addAmplitudeListener(IAmplitudeListener sl) {
        listeners.add(sl);
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#removeAmplitudeListener(de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener)
     */
    @Override
    public void removeAmplitudeListener(IAmplitudeListener sl) {
        listeners.remove(sl);
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#isRunning()
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#isRecording()
     */
    @Override
    public boolean isRecording() {
        return recording;
    }
    
    /* (non-Javadoc)
     * @see de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService#addStartStopListener(de.uniHamburg.informatik.continuousvoice.services.sound.IAudioServiceStartStopListener)
     */
    @Override
    public void addStartStopListener(IAudioServiceStartStopListener l) {
        startStopListeners.add(l);
    }
    
    private void notifyStartStopListeners() {
        for (IAudioServiceStartStopListener l: startStopListeners) {
            l.onAudioServiceStateChange();
        }
    }
}
