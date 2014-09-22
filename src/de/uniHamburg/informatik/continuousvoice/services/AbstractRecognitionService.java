package de.uniHamburg.informatik.continuousvoice.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public abstract class AbstractRecognitionService extends Service implements IRecognitionService {

    private static final String TAG = "AndroidVoiceRecognitionService";
    private Messenger messenger;
    private boolean running = false;
    private String recognizedText = "";
    private String broadcastIdentifier;

    public AbstractRecognitionService() {
        Log.i(TAG, "CONSTRUCTOR");

        IRecognitionControl control = new IRecognitionControl() {
            @Override
            public void start() {
                AbstractRecognitionService.this.start();
            }
            
            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public void stop() {
                AbstractRecognitionService.this.stop();
            }

            @Override
            public void reset() {
                AbstractRecognitionService.this.reset();
            }
        };
        messenger = new Messenger(new RecognitionControlHandler(control));
    }
    
    /**
     * override this method if needed
     * remember to call super.stop();
     */
    protected void stop() {
        Log.i(TAG, "STOP");
        running = false;
    }
    
    /**
     * override this method if needed
     * remember to call super.start();
     */
    protected void start() {
        Log.i(TAG, "START");
        running = true;
    }
    
    /**
     * override this method if needed
     * remember to call super.reset();
     */
    protected void reset() {
        Log.i(TAG, "RESET");
        stop();
        recognizedText = "";
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "ON BIND");
        broadcastIdentifier = intent.getStringExtra("broadcastIdentifier");
        if (broadcastIdentifier == null) {
            Log.e(TAG, "missing broadcastIdentifier extra in Service intent");
        }
        
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "ON DESTROY");
        running = false;
    }    
    
    protected void addWords(String words, boolean sendBroadcast) {
        recognizedText += " " + words;
        
        if (sendBroadcast) {
            Intent i = new Intent(broadcastIdentifier);
            i.putExtra("words", words);
            sendBroadcast(i);
            Log.i(TAG, "SEND BROADCAST");
        }
    }
    
    /**
     * @return the complete text since the last reset
     */
    public String getRecognizedText() {
        return recognizedText;
    }
}