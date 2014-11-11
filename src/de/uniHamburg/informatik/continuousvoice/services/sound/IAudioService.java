package de.uniHamburg.informatik.continuousvoice.services.sound;

import java.io.File;

public interface IAudioService extends IRecorder {

    public void initialize();

    public void shutdown();

    public void startRecording();

    public File stopRecording();

    public File splitRecording();

    public Loudness getCurrentSilenceState();

    public void addAmplitudeListener(IAmplitudeListener sl);

    public void removeAmplitudeListener(IAmplitudeListener sl);

    public boolean isRunning();

    public boolean isRecording();

    public void addStartStopListener(IAudioServiceStartStopListener l);

}