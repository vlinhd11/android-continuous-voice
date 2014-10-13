package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.soundRecorder.SoundRecordingService;

//TODO abstract
public  class AbstractWebServiceRecognitionService extends AbstractRecognitionService {

    private static final String TAG = "AbstractWebServiceRecognitionService";
    private SoundRecordingService recorder;

    public AbstractWebServiceRecognitionService() {
        //recorder = new SoundRecordingService("soundfile_" + System.currentTimeMillis());
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        //recorder.record();
        addWords("start", true);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        addWords("stop", true);
        //recorder.stop();
        Log.e(TAG, "### STOP (3)");
    }
    
    
}
