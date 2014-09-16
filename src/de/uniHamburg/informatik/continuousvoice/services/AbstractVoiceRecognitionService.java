package de.uniHamburg.informatik.continuousvoice.services;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class AbstractVoiceRecognitionService extends Service implements
		IVoiceRecognitionService {

	private List<IVoiceRecognitionListener> listeners = new ArrayList<IVoiceRecognitionListener>();
	protected String recognizedText = "";
	protected int cursor = 0;
	
	@Override
	public void addRecognitionListener(IVoiceRecognitionListener listener) {
		listeners.add(listener);
	}
	
	protected void notifyListeners() {
		for (IVoiceRecognitionListener listener : listeners) {
			listener.voiceRecognized();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public String getRecognizedText() {
		return recognizedText;
	}

	@Override
	public String getRecognizedChunk() {
		String chunk = recognizedText.substring(cursor);
		cursor = recognizedText.length();
		return chunk;
	}
	
	@Override
	public void reset() {
		recognizedText = "";
		cursor = 0;
	}

}
