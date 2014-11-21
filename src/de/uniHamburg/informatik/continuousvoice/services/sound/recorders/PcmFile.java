package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PcmFile extends File {
    
    private static final long serialVersionUID = 1L;
    private List<short[]> pcmData = new ArrayList<short[]>();
    
    public PcmFile(String path) {
        super(path);
    }
    
    public void addSample(short[] sample) {
        pcmData.add(sample);
    }

    public List<short[]> getPcmData() {
        return pcmData;
    }
    
    public short[] getConcatenatedPcmData() {
        int length = 0;
        
        for (short[] sample: pcmData) {
            length += sample.length;
        }
        short[] result = new short[length];
        int i = 0;
        for (short[] sample: pcmData) {
            for (short s: sample) {
                result[i] = s;
                i++;
            }
        }
        return result;
    }
    
}
