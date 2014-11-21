package de.uniHamburg.informatik.continuousvoice.services.speaker;

public abstract class AbstractSpeakerFeature {
    
    /**
     * Calculates distance between two speaker features.
     * @param other 
     * @return a value between 0 and 1 - 0: 100% same, 1: 100% different
     */
    public abstract double getDistanceTo(AbstractSpeakerFeature other);
    
    public abstract void merge(AbstractSpeakerFeature other);
}
