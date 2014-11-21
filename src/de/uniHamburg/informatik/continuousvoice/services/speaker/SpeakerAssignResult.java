package de.uniHamburg.informatik.continuousvoice.services.speaker;

public class SpeakerAssignResult {

    private final double confidence;
    private final Speaker speaker;
    
    public SpeakerAssignResult(Speaker speaker, double confidence) {
        this.speaker = speaker;
        this.confidence = confidence;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public Speaker getSpeaker() {
        return speaker;
    }
}
