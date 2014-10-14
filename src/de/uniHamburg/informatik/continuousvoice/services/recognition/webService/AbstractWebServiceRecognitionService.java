package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorder.SoundRecordingService;

//TODO abstract
public class AbstractWebServiceRecognitionService extends AbstractRecognitionService {

    public static final String TAG = AbstractWebServiceRecognitionService.class.getCanonicalName();
    private SoundRecordingService recorder;
    private boolean active = false;
    private ScheduledExecutorService scheduleTaskExecutor;
    public final static int RECORDING_DURATION = 13;

    public AbstractWebServiceRecognitionService() {
        recorder = new SoundRecordingService("soundfile_" + System.currentTimeMillis());
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
    }

    private void startSplitting() {
        if (scheduleTaskExecutor == null) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        }

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (active) {
                    File f = recorder.split();
                    Log.w(TAG, f.getAbsolutePath());
                } else {
                    scheduleTaskExecutor.shutdown();
                }
            }
        }, (RECORDING_DURATION - 1), RECORDING_DURATION, TimeUnit.SECONDS);
    }

}
