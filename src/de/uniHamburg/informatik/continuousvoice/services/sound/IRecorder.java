package de.uniHamburg.informatik.continuousvoice.services.sound;

import java.io.File;

public interface IRecorder {

    /*
     * Workflow:
     * 1 start recorder
     * 2 stop or split and get file
     * 3 shutdown recorder
     */
    
    public void initialize();

    /**
     * Shuts down this recorder. You have to call initialize again if you plan to continue.
     * @return the last file.
     */
    public void shutdown();
    
    public void startRecording();
    
    public File stopRecording();
    
    /**
     * Stops the current recorder, releases and returns the file but keeps on
     * recording to a new file until terminate() or split() is called.
     * 
     * @return the recorded file
     */
    public File splitRecording();

    public boolean isRecording();
}
