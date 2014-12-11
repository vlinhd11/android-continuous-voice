package de.uniHamburg.informatik.continuousvoice.services.speaker;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.PcmBuffer;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;

public class SpeakerRecognizer implements IAmplitudeListener {

	private static final String TAG = "SpeakerRecognizer";
	private boolean running = false;
	private Speaker currentSpeaker;
	private List<ISpeakerChangeListener> listeners = new LinkedList<ISpeakerChangeListener>();

	private PcmBuffer<Double> levelLeftBuffer = new PcmBuffer<Double>(
			AudioConstants.TIMESHIFT_BUFFER_FRAMES);
	private PcmBuffer<Double> levelRightBuffer = new PcmBuffer<Double>(
			AudioConstants.TIMESHIFT_BUFFER_FRAMES);
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
		} else if (currentSpeaker == null) {
			updateSpeaker(soundLevelLeft, soundLevelRight);
		}
	}

	private void updateSpeakerState() {
		
		double left = maxFromBuffer(levelLeftBuffer);
		double right = maxFromBuffer(levelRightBuffer);
		
		boolean leftAboveThreshold = left > AudioConstants.SILENCE_AMPLITUDE_THRESHOLD;
		boolean rightAboveThreshold = right > AudioConstants.SILENCE_AMPLITUDE_THRESHOLD;
		
		if (leftAboveThreshold || rightAboveThreshold) {
			updateSpeaker(left, right);
		}
		
	}
	
	private void updateSpeaker(double left, double right) {
		AbstractSpeakerFeature feat = new SoundPositionSpeakerFeature(left, right);
		
		Speaker newSpeaker = speakerManager.assign(feat, false);
		if (!newSpeaker.equals(currentSpeaker)) {
			
			Log.i(TAG, "Speaker change: " + currentSpeaker + " => " + newSpeaker);
			
			currentSpeaker = newSpeaker;
			notifySpeakerListener();
		}
	}

	private double maxFromBuffer(PcmBuffer<Double> buffer) {
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

	public Speaker getSpeakerFromFile(PcmFile f) {
		double[] levels = AudioHelper.stereoLevelsFromFile(f);
		SoundPositionSpeakerFeature feature = new SoundPositionSpeakerFeature(levels[0], levels[1]);
		Speaker speaker = speakerManager.assign(feature, false);
		Log.i(TAG, "This files sound position feature: " + feature.toString());
		return speaker;
	}

    public void remvoveSpeakerChangeListener(ISpeakerChangeListener iSpeakerChangeListener) {
        listeners.remove(iSpeakerChangeListener);
    }

}
