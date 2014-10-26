package de.uniHamburg.informatik.continuousvoice.services.recognition;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import de.uniHamburg.informatik.continuousvoice.constants.ServiceControlConstants;
import de.uniHamburg.informatik.continuousvoice.services.IServiceControl;

public class RecognitionControlHandler extends Handler {

    public static final String TAG = RecognitionControlHandler.class.getName();
    private IServiceControl recognitionControl;

    public RecognitionControlHandler(IServiceControl recognitionControl) {
        this.recognitionControl = recognitionControl;
    }

    @Override
    public void handleMessage(Message msg) {
        int messageType = msg.what;
        
        switch (messageType) {
        case ServiceControlConstants.START: {
            if (recognitionControl.isRunning()) {
                reply(msg, false);
            } else {
                recognitionControl.start();
                reply(msg, true);
            }
            break;
        }
        case ServiceControlConstants.STOP: {
            if (recognitionControl.isRunning()) {
                recognitionControl.stop();
                reply(msg, true);
            } else {
                reply(msg, false);
            }
            break;
        }
        case ServiceControlConstants.RESET: {
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
            Message resp = Message.obtain(null, ServiceControlConstants.SERVICE_CONTROL_RESPONSE);
            Bundle bResp = new Bundle();
            bResp.putBoolean("success", success);
            resp.setData(bResp);
            msg.replyTo.send(resp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
