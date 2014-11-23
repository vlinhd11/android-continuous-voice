package de.uniHamburg.informatik.continuousvoice.services.recognition;

import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;


public interface ITranscriptionResultListener {

    public void onTranscriptResult(String transcriptResult, Speaker s);
}
