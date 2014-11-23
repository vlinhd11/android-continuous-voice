package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import com.example.libavndkdemo.Mp3Encoder;

import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper;

/**
 * This is a wrapper and the reference to the libavndkdemo project
 * 
 * @author marius
 */
public final class Mp3FileRecorder {

    private String filename;
    public Mp3Encoder encoder;
    private PcmFile file;
    
    public Mp3FileRecorder(String filename) {
        this.filename = filename;
        encoder = new Mp3Encoder();
        file = new PcmFile(filename);
    }

    public int initAudio(){
        return encoder.initAudio(filename);
    }

    public void writeAudioFrame(short[] samples, int length) {
        encoder.writeAudioFrame(samples, length);
        file.addSample(samples);
    }

    public int getFrameSize() {
        return encoder.getFrameSize();
    }

    public int close() {
        return encoder.close();
    }

    public String getFilename() {
        return filename;
    }
    
    public PcmFile getPcmFile() {
        return file;
    }
}
