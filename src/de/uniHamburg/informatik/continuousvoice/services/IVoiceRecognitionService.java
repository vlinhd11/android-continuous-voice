package de.uniHamburg.informatik.continuousvoice.services;

public interface IVoiceRecognitionService {

	public void addRecognitionListener(IVoiceRecognitionListener listener);
	
	/**
	 * @return the complete recognized text since the last reset
	 */
	public String getRecognizedText();
	
	/**
	 * Returns the text from cursor to end and moves the cursor to the end of the buffer text.
	 * @return the recognized text since the last call of this method.
	 */
	public String getRecognizedChunk();
	
	public void start();
	
	public void stop();
	
	public void reset();

}
