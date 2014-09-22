package de.uniHamburg.informatik.continuousvoice.services;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.RecognitionConstants;

public class RecognitionControlHandler extends Handler {

    private static final String TAG = "RecognitionControlHandler";
    private IRecognitionControl recognitionControl;

    public RecognitionControlHandler(IRecognitionControl recognitionControl) {
        this.recognitionControl = recognitionControl;
    }

    @Override
    public void handleMessage(Message msg) {
        int messageType = msg.what;
        Log.i(TAG, "MESSAGE received: " + messageType);
        
        switch (messageType) {
        case RecognitionConstants.START_RECOGNIZING: {
            if (recognitionControl.isRunning()) {
                reply(msg, false);
            } else {
                recognitionControl.start();
                reply(msg, true);
            }
            break;
        }
        case RecognitionConstants.STOP_RECOGNIZING: {
            if (recognitionControl.isRunning()) {
                recognitionControl.stop();
                reply(msg, true);
            } else {
                reply(msg, false);
            }
            break;
        }
        case RecognitionConstants.RESET_SERVICE: {
            if (recognitionControl.isRunning()) {
                reply(msg, false);
            } else {
                recognitionControl.reset();
                reply(msg, true);
            }
            break;
        }
        default:
            super.handleMessage(msg);
        }
    }

    protected void reply(Message msg, boolean success) {
        try {
            Message resp = Message.obtain(null, RecognitionConstants.SERVICE_CONTROL_RESPONSE);
            Bundle bResp = new Bundle();
            bResp.putBoolean("success", success);
            resp.setData(bResp);
            msg.replyTo.send(resp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
