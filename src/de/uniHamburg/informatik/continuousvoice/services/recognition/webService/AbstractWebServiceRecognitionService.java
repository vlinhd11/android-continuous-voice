package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.BroadcastIdentifiers;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.sound.analysis.SilenceListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.analysis.SoundMeter;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorder.SoundRecordingService;

public abstract class AbstractWebServiceRecognitionService extends AbstractRecognitionService implements
        SilenceListener {

    public final String TAG = this.getClass().getSimpleName();
    private SoundRecordingService recorder;
    private SoundMeter soundMeter;
    private boolean recording;
    private String baseName;
    public final static int RECORDING_MAX_DURATION = 10 * 1000;

    public AbstractWebServiceRecognitionService(String baseName) {
        this.baseName = baseName;
    }
    
    @Override
    public void onCreate() {
        recorder = new SoundRecordingService(baseName);
        
        BroadcastReceiver statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (running) {
                    boolean silent = intent.getBooleanExtra("SILENCE", true);
                    if (silent) {
                        AbstractWebServiceRecognitionService.this.onSilence();
                    } else {
                        AbstractWebServiceRecognitionService.this.onSpeech();
                    }
                }
            }
        };
        registerReceiver(statusReceiver, new IntentFilter(BroadcastIdentifiers.SILENCE_BROADCAST));
    }

    @Override
    public void start() {
        super.start();
        //soundMeter = new SoundMeter();
        recorder.start(); // initially start the recording service
        setStatus("started");

//        soundMeter.addSilenceListener(this);
//        soundMeter.start();
    }

    @Override
    public void stop() {
        super.stop();
        if (soundMeter != null) {
            soundMeter.stop();
            //not necessary soundMeter.removeSilenceListener(this);
            soundMeter = null;
        }
        File toTranscribe = recorder.shutdownAndRelease();
        setStatus("stopped, transcribing");
        transcribeAsync(toTranscribe);
    }

    /**
     * Calls the abstract method "request(File)" in background. Sends the
     * recognized words to the UI on result.
     * 
     * @param f
     *            the audio/amr file to transcribe (bitrate 8000)
     */
    protected void transcribeAsync(File f) {
        AsyncTask<File, Void, String> asyncTask = new AsyncTask<File, Void, String>() {

            @Override
            protected String doInBackground(File... params) {
                return request(params[0]);
            }

            @Override
            protected void onPostExecute(String result) {
                if (running) {
                    setStatus("success (" + result.split(" ").length + " words)");
                    addWords(result);
                }
            }
        };
        asyncTask.execute(f);
    }

    public abstract String request(File audioFile);

    /*
     * Recording procedure
     * a) split at least after 13s
     * b) split on silence (silence is delayed by SoundMeter, files are never under 3s)
     * c) start on Loud if not running
     * 
     * onLoud ------> startIfNotRunning incl. startTimer --13s--> stopIfRunning -,
     *             ^-------------------------------------------------------------'
     * onSilence ------> stopIfRunning
     */
    private ScheduledExecutorService maxRecordingTimeScheduler;

    @Override
    public void onSpeech() {
        Log.e(TAG, "record >=======");
        startRecording();
    }
    
    @Override
    public void onSilence() {
        Log.e(TAG, "                =======| stop ");
        stopRecording();
    }

    private void startRecording() {
        if (!recording) {
            //start recorder
            Log.e(TAG, "Recorder: " + recorder.currentRecorder);
            recorder.start();
            recording = true;
            startMaxTimeScheduler();
        }
    }

    private void stopRecording() {
        if (recording) {
            //Stop recorder
            File toTranscribe = recorder.split(false);
            //transcribe
            transcribeAsync(toTranscribe);
            //stopTimer
            maxRecordingTimeScheduler.shutdownNow();
            recording = false;
        }
    }

    private void startMaxTimeScheduler() {
        //start 13s Timer
        maxRecordingTimeScheduler = Executors.newScheduledThreadPool(1);
        maxRecordingTimeScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setStatus("split");
                        File f = recorder.split(true);
                        //transcribeAsync(f);
                        startMaxTimeScheduler();
                    }
                });
            }
        }, RECORDING_MAX_DURATION, TimeUnit.MILLISECONDS);
    }
    
    private final Handler handler = new Handler();
}
