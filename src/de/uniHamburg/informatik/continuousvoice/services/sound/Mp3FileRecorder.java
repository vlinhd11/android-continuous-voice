package de.uniHamburg.informatik.continuousvoice.services.sound;

import com.example.libavndkdemo.Mp3Encoder;

public final class Mp3FileRecorder {

    private String filename;
    private int state;
    public Mp3Encoder encoder;
    
    public Mp3FileRecorder(String filename) {
        this.filename = filename;
        encoder = new Mp3Encoder();
        this.state = initAudio(filename);
    }

    public int initAudio(String filePath){
        return encoder.initAudio(filePath);
    }

    public void writeAudioFrame(short[] samples, int length) {
        encoder.writeAudioFrame(samples, length);
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

    public int getInitState() {
        return state;
    }

}
