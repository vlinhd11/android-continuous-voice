package de.uniHamburg.informatik.continuousvoice.services.speaker;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.services.sound.Buffer;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;

public class SpeakerRecognizer implements IAmplitudeListener {

	private static final String TAG = "SpeakerRecognizer";
	private boolean running = false;
	private Speaker currentSpeaker;
	private List<ISpeakerChangeListener> listeners = new LinkedList<ISpeakerChangeListener>();

	private Buffer<Double> levelLeftBuffer = new Buffer<Double>(
			AudioConstants.AUDIO_BUFFER_SIZE);
	private Buffer<Double> levelRightBuffer = new Buffer<Double>(
			AudioConstants.AUDIO_BUFFER_SIZE);
	private SpeakerManager speakerManager;

	public SpeakerRecognizer(SpeakerManager speakerManager) {
		this.speakerManager = speakerManager;
	}

	@Override
	public void onSilence() {
		running = false;
	}

	@Override
	public void onSpeech() {
		running = true;
	}

	@Override
	public void onAmplitudeUpdate(double soundLevelLeft, double soundLevelRight) {
		if (running) {
			levelLeftBuffer.write(soundLevelLeft);
			levelRightBuffer.write(soundLevelRight);

			updateSpeakerState();
		}
	}

	private void updateSpeakerState() {
		
		double left = maxFromBuffer(levelLeftBuffer);
		double right = maxFromBuffer(levelRightBuffer);
		
		boolean leftAboveThreshold = left > AudioConstants.SILENCE_AMPLITUDE_THRESHOLD;
		boolean rightAboveThreshold = right > AudioConstants.SILENCE_AMPLITUDE_THRESHOLD;
		
		if (leftAboveThreshold || rightAboveThreshold) {
			AbstractSpeakerFeature feat = new SoundPositionSpeakerFeature(left, right);
			
			Speaker newSpeaker = speakerManager.assign(feat);
			if (!newSpeaker.equals(currentSpeaker)) {
				
				Log.i(TAG, "Speaker change: " + currentSpeaker + " => " + newSpeaker);
				
				currentSpeaker = newSpeaker;
				notifySpeakerListener();
			}
		}
		
	}

	private double maxFromBuffer(Buffer<Double> buffer) {
		double max = 0.0;
		for (double d: buffer.getCompleteBufferData()) {
			max = Math.max(d, max);
		}
		
		return max;
	}

	public void addSpeakerChangeListener(ISpeakerChangeListener listener) {
		listeners.add(listener);
	}

	private void notifySpeakerListener() {
		for (ISpeakerChangeListener listener : listeners) {
			listener.onSpeakerChange(currentSpeaker);
		}
	}

	public void clear() {
		levelLeftBuffer.clear();
		levelRightBuffer.clear();
	}

	public Speaker getCurrentSpeaker() {
		return currentSpeaker;
	}

}
