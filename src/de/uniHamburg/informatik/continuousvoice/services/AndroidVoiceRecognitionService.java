package de.uniHamburg.informatik.continuousvoice.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AndroidVoiceRecognitionService extends Service implements IVoiceRecognitionService{

	private static final String TAG = "AndroidVoiceRecognitionService";

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public AndroidVoiceRecognitionService() {
		Log.i(TAG, "AndroidVoiceRecognitionService Constructor");
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");

		try {
			for (int i = 0; i < 10; ++i) {
				Log.i(TAG, "wörk >_ " + i);
				//synchronized (this) {
					wait(1000);
				//}
			}
			//this.stopSelf();
		} catch (Exception e) {
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}