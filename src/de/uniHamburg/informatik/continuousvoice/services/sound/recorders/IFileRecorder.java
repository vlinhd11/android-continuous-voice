package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

public interface IFileRecorder {

	public int initAudio();

	public int getFrameSize();

	public int close();

	public String getFilename();

	public PcmFile getPcmFile();

}