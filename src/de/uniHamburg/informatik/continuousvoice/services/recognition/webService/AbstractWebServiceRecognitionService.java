package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.sound.AmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioService;
import de.uniHamburg.informatik.continuousvoice.services.sound.IRecorder;

public abstract class AbstractWebServiceRecognitionService extends AbstractRecognitionService implements
        AmplitudeListener {

    public final String TAG = "AbstractWebServiceRecognitionService";
    public final static int RECORDING_MAX_DURATION_MILLIS = 10 * 1000;
    private IRecorder recorder;
    private AudioService audioService;

    public AbstractWebServiceRecognitionService(AudioService audioService) {
        this.audioService = audioService;
        this.recorder = audioService;
    }

    @Override
    public void initialize() {
        //1 ensure audioService is running
        if (!audioService.isRunning()) {
            audioService.initialize();
        }

    }

    @Override
    public void shutdown() {
        if (running) {
            stop();
        }
    }

    @Override
    public void start() {
        super.start();
        audioService.addAmplitudeListener(this);
        if (audioService.getCurrentSilenceState() == AudioService.State.SPEECH) {
            startRecording();
        }
    }

    @Override
    public void stop() {
        if (maxRecordingTimeScheduler != null) {
            maxRecordingTimeScheduler.shutdownNow();
        }
        audioService.removeAmplitudeListener(this);
        if (recorder.isRecording()) {
            File toTranscribe = recorder.stopRecording();
            setStatus("stopped, transcribing");
            transcribeAsync(toTranscribe);
        } else {
            setStatus("stopped");
        }
        recorder.shutdown();

        super.stop();
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

    private ScheduledExecutorService maxRecordingTimeScheduler;

    @Override
    public void onSpeech() {
        startRecording();
    }

    @Override
    public void onSilence() {
        stopRecording();
    }

    @Override
    public void onAmplitudeUpdate(double percent) {
        //nothing
    }

    private void startRecording() {
        Log.e(TAG, "is rec: " + recorder.isRecording());
        Log.e(TAG, "❰ record ❱─────┐");
        if (!recorder.isRecording()) {
            //start recorder
            recorder.startRecording();
            startMaxTimeScheduler();
        }
    }

    private void stopRecording() {
        Log.e(TAG, "               └─────❰ stop ❱");
        Log.e(TAG, "is rec: " + recorder.isRecording());
        if (recorder.isRecording()) {
            //Stop recorder
            File toTranscribe = recorder.stopRecording();
            //transcribe
            transcribeAsync(toTranscribe);
            //stopTimer
            maxRecordingTimeScheduler.shutdownNow();
        }
    }

    private void startMaxTimeScheduler() {
        //start 13s Timer
        maxRecordingTimeScheduler = Executors.newScheduledThreadPool(1);
        maxRecordingTimeScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                setStatus("splitting");
                File f = recorder.splitRecording();
                transcribeAsync(f);
                startMaxTimeScheduler();                
            }
        }, RECORDING_MAX_DURATION_MILLIS, TimeUnit.MILLISECONDS);
    }

    public abstract String request(File audioFile);
}
