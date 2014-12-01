package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import com.example.libavndkdemo.Mp3Encoder;

/**
 * This is a wrapper and the reference to the libavndkdemo project
 * 
 * @author marius
 */
public final class Mp3FileRecorder implements IFileRecorder {

    private String filename;
    public Mp3Encoder encoder;
    private PcmFile file;
    
    public Mp3FileRecorder(String filename) {
        this.filename = filename;
        encoder = new Mp3Encoder();
        file = new PcmFile(filename);
    }

    @Override
	public int initAudio(){
        return encoder.initAudio(filename);
    }

	public void writeAudioFrame(final short[] samples, final int length) {
		encoder.writeAudioFrame(samples, length);	
        //file.addSample(samples);
    }

    @Override
	public int getFrameSize() {
        return encoder.getFrameSize();
    }

    @Override
	public int close() {
        return encoder.close();
    }

    @Override
	public String getFilename() {
        return filename;
    }
    
    @Override
	public PcmFile getPcmFile() {
        return file;
    }
}
