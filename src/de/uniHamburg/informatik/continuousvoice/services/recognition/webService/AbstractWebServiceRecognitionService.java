package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;

import android.content.Intent;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorder.SoundRecordingService;

//TODO abstract
public  class AbstractWebServiceRecognitionService extends AbstractRecognitionService {

    public static final String TAG = AbstractWebServiceRecognitionService.class.getCanonicalName();
    private SoundRecordingService recorder;

    public AbstractWebServiceRecognitionService() {
        recorder = new SoundRecordingService("soundfile_" + System.currentTimeMillis());
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        recorder.record();
        addWords("start", true);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        addWords("stop", true);
        File file = recorder.stopAndReturnFile();
        
        Intent i = new Intent("DEBUGFILESHARE");
        i.putExtra("filename", file.getAbsolutePath());
        sendBroadcast(i);
    }
    
    
}
