package de.uniHamburg.informatik.continuousvoice.services.sound;

import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;

public interface IRecorder {

    public void initialize();

    /**
     * Shuts down this recorder. You have to call initialize again if you plan to continue.
     * @return the last file.
     */
    public void shutdown();
    
    public void startRecording();
    
    public PcmFile stopRecording(boolean timeshift, boolean cutoff);
    
    public boolean isRecording();
}
