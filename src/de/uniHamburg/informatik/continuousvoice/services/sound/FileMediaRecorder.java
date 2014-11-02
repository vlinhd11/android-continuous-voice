package de.uniHamburg.informatik.continuousvoice.services.sound;

import java.io.File;

import android.media.MediaRecorder;

/**
 * Media recorder with ability to return its recorded file and file name
 * @author marius
 *
 */
public class FileMediaRecorder extends MediaRecorder {

    private String filename;
    
    /**
     * @param filename null means transient recorder
     */
    public FileMediaRecorder(String filename) {
        super();
        if (filename == null) {
            this.filename = "/dev/null";
        } else {
            this.filename = filename;
        }
        
        super.setOutputFile(this.filename);
    }
    
    public String getFilename() {
        return filename;
    }
    
    public File getFile() {
        return new File(filename);
    }
    
    public void setOutputFile(String filename) {
        throw new IllegalArgumentException("Please use the constructor to set the file.");
    }
    
    
}
