package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioService;
import de.uniHamburg.informatik.continuousvoice.services.sound.IRecorder;

public abstract class AbstractWebServiceRecognizer extends AbstractRecognizer implements
        IAmplitudeListener {

    public final String TAG = "AbstractWebServiceRecognitionService";
    private ScheduledExecutorService maxRecordingTimeScheduler;
    protected long RECORDING_MAX_DURATION = 10 * 1000;
    private IRecorder recorder;
    private AudioService audioService;
    private Runnable splitRunnable;
    private Handler handler = new Handler();

    public AbstractWebServiceRecognizer(AudioService audioService) {
        this.audioService = audioService;
        this.recorder = audioService;
        this.splitRunnable = new Runnable() {
            @Override
            public void run() {
                setStatus("splitting");
                Log.e(TAG, "          ┌────┴────┐");
                Log.e(TAG, "          │❰ split ❱│");
                Log.e(TAG, "          └────┬────┘");
                File f = recorder.splitRecording();
                transcribeAsync(f);
                startMaxTimeScheduler();
            }
        };
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
        if (running) {
            stop();
        }
    }

    @Override
    public void start() {
        //1 ensure audioService is running
        if (!audioService.isRunning()) {
            audioService.initialize();
            setStatus("AudioService turn on");
        }

        super.start();

        audioService.addAmplitudeListener(this);
        if (audioService.getCurrentSilenceState() == AudioService.State.SPEECH) {
            startRecording();
        }
    }

    @Override
    public void stop() {
        stopMaxTimeScheduler();
        audioService.removeAmplitudeListener(this);
        if (recorder.isRecording()) {
            File toTranscribe = recorder.stopRecording();
            setStatus("stopped, transcribing");
            transcribeAsync(toTranscribe);
        } else {
            setStatus("stopped");
        }

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
        if (!recorder.isRecording()) {
            Log.e(TAG, "❰ record ❱─────┐");
            startMaxTimeScheduler();
            //start recorder
            recorder.startRecording();
        }
    }

    private void stopRecording() {
        if (recorder.isRecording()) {
            Log.e(TAG, "               └─────❰ stop ❱");
            //Stop recorder
            File toTranscribe = recorder.stopRecording();
            //transcribe
            transcribeAsync(toTranscribe);
            //stopTimer
            stopMaxTimeScheduler();
        }
    }

    private void startMaxTimeScheduler() {
        stopMaxTimeScheduler();

        maxRecordingTimeScheduler = Executors.newScheduledThreadPool(1);
        maxRecordingTimeScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                handler.post(splitRunnable);
            }
        }, RECORDING_MAX_DURATION, TimeUnit.MILLISECONDS);
    }

    private void stopMaxTimeScheduler() {
        if (maxRecordingTimeScheduler != null) {
            maxRecordingTimeScheduler.shutdownNow();
        }
    }

    public abstract String request(File audioFile);
}
