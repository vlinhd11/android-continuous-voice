package de.uniHamburg.informatik.continuousvoice.services.recognition;

import de.uniHamburg.informatik.continuousvoice.services.IServiceControl;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public abstract class AbstractRecognitionService extends Service implements IServiceControl {

    private static final String TAG = AbstractRecognitionService.class.getName();
    private Messenger messenger;
    protected boolean running = false;
    private String recognizedText = "";
    private String broadcastIdentifier;
    private String statusBroadcastIdentifier;

    public AbstractRecognitionService() {
        messenger = new Messenger(new RecognitionControlHandler(this));
    }
    
    /**
     * override this method if needed
     * but then remember to call super.stop();
     */
    public void stop() {
        running = false;
    }
    
    /**
     * override this method if needed
     * but then remember to call super.start();
     */
    public void start() {
        Log.i(TAG, "START");
        running = true;
    }
    
    /**
     * override this method if needed
     * but then remember to call super.reset();
     */
    public void reset() {
        Log.i(TAG, "RESET");
        stop();
        recognizedText = "";
        clearStatus();
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }

    public void clearStatus() {
        setStatus("");
        setStatus("");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "ON BIND");
        broadcastIdentifier = intent.getStringExtra("broadcastIdentifier");
        if (broadcastIdentifier == null) {
            Log.e(TAG, "missing \"broadcastIdentifier\" extra in Service intent");
        }
        
        statusBroadcastIdentifier = intent.getStringExtra("statusBroadcastIdentifier");
        if (statusBroadcastIdentifier == null) {
            Log.e(TAG, "missing \"statusBroadcastIdentifier\" extra in Service intent");
        }
        
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "ON DESTROY");
        running = false;
    }    
    
    protected void addWords(String words) {
        recognizedText += " " + words;
        
        Intent i = new Intent(broadcastIdentifier);
        i.putExtra("words", words);
        sendBroadcast(i);
    }
    
    protected void setStatus(String status) {
        Intent i = new Intent(statusBroadcastIdentifier);
        i.putExtra("message", status);
        sendBroadcast(i);
    }
    
    /**
     * @return the complete text since the last reset
     */
    public String getRecognizedText() {
        return recognizedText;
    }
}