package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.io.File;
import java.io.IOException;

import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.timeShift.TimeShiftAudioData;

public class WaveFileRecorder {

    private String filename;
    private PcmFile finalFile;
    private TimeShiftAudioData prepend;
    
    public WaveFileRecorder(String filename) {
        this.filename = filename;
        this.finalFile = new PcmFile(filename);
        
        new File(filename);
    }

    public void writeAudioFrame(short[] segment) {
        finalFile.addSample(segment);
    }

    public PcmFile writeFile(boolean timeShift, boolean cut) {
        
        if (timeShift && prepend != null) {
            finalFile.prepend(prepend);
        }
        
        if (cut) {
            finalFile.cutOffAtEnd(AudioConstants.SOUNDFILE_END_CUTOFF_CHUNKS);
        }
        
        try {
            finalFile.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return finalFile;
    }

    public String getFilename() {
        return filename;
    }

    public void prepend(TimeShiftAudioData pastAudioData) {
        prepend = pastAudioData;
    }
    
}
