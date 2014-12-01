package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.Loudness;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAudioServiceStartStopListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.IRecorder;
import de.uniHamburg.informatik.continuousvoice.services.speaker.ISpeakerChangeListener;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;

public interface IAudioService extends IRecorder {

    public Loudness getCurrentSilenceState();

    public void addAmplitudeListener(IAmplitudeListener sl);

    public void removeAmplitudeListener(IAmplitudeListener sl);

    public boolean isRunning();

    public boolean isRecording();

    public void addStartStopListener(IAudioServiceStartStopListener l);
    
    public String getMimeType();

	public void addSpeakerChangeListener(
			ISpeakerChangeListener iSpeakerChangeListener);

	public Speaker identifySpeaker(PcmFile f);

}