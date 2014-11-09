package de.uniHamburg.informatik.continuousvoice.services.recognition;

public interface ITranscriptionResultListener {

    public void onTranscriptResult(String transcriptResult);
}
