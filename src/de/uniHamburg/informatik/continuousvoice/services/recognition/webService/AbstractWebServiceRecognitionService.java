package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorder.SoundRecordingService;

public abstract class AbstractWebServiceRecognitionService extends AbstractRecognitionService {

    public static final String TAG = AbstractWebServiceRecognitionService.class.getName();
    private SoundRecordingService recorder;
    private boolean active = false;
    private ScheduledExecutorService scheduleTaskExecutor;
    public final static int RECORDING_DURATION = 13;

    public AbstractWebServiceRecognitionService(String baseName) {
        recorder = new SoundRecordingService(baseName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recorder.start(); // initially start the recording service
        active = true;
        setStatus("started");
        startSplitting(); // begin splitting and continously sending the records
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        recorder.terminate();
        setStatus("stopped, transcribing");
        
        transcribeAsync(recorder.getCurrentFile());

        // File file = recorder.getCurrentFile();
        // Intent i = new Intent("DEBUGFILESHARE");
        // i.putExtra("filename", file.getAbsolutePath());
        // sendBroadcast(i);
    }

    private void startSplitting() {
        if (scheduleTaskExecutor == null) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        }

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (active) {
                    setStatus("split, transcribe");
                    File f = recorder.split();
                    transcribeAsync(f);
                } else {
                    scheduleTaskExecutor.shutdown();
                }
            }
        }, (RECORDING_DURATION - 1), RECORDING_DURATION, TimeUnit.SECONDS);
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
                setStatus("success (" + result.split(" ").length + " words)");
                addWords(result);
            }
        };
        asyncTask.execute(f);
    }

    public abstract String request(File audioFile);

}
