package de.uniHamburg.informatik.continuousvoice.services.recognition;

import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;


public interface ITranscriptionResultListener {

	public void onTranscriptionStart(int id);
	
    public void onTranscriptResult(int id, String transcriptResult, Speaker s);
}
