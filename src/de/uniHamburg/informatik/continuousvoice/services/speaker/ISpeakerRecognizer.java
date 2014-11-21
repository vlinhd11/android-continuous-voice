package de.uniHamburg.informatik.continuousvoice.services.speaker;

import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;

public interface ISpeakerRecognizer {

    public AbstractSpeakerFeature extractFeature(PcmFile sample);
    
    public double getDistance(AbstractSpeakerFeature a, AbstractSpeakerFeature b);
    
    public double getMaxDistance();
    
}
