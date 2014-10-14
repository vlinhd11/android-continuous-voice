package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorder.SoundRecordingService;

public abstract class AbstractWebServiceRecognitionService extends AbstractRecognitionService {

    public static final String TAG = AbstractWebServiceRecognitionService.class.getCanonicalName();
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
        startSplitting(); // begin splitting the records
        addWords(" [START] ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = true;
        addWords(" [STOP] ");
        recorder.terminate();

        // File file = recorder.getCurrentFile();
        // Intent i = new Intent("DEBUGFILESHARE");
        // i.putExtra("filename", file.getAbsolutePath());
        // sendBroadcast(i);
        
        //transcribe
    }

    private void startSplitting() {
        if (scheduleTaskExecutor == null) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        }

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (active) {
                    File f = recorder.split();
                    addWords(transcribe(f));
                } else {
                    scheduleTaskExecutor.shutdown();
                }
            }
        }, (RECORDING_DURATION - 1), RECORDING_DURATION, TimeUnit.SECONDS);
    }

    protected String transcribe(File f) {
        String result = request(f);
        return result;
    }

    public abstract String request(File audioFile);
    
}
