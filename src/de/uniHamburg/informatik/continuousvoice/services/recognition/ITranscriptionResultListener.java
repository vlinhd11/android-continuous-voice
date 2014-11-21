package de.uniHamburg.informatik.continuousvoice.services.recognition;

import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerAssignResult;

public interface ITranscriptionResultListener {

    public void onTranscriptResult(String transcriptResult, SpeakerAssignResult speaker);
}
