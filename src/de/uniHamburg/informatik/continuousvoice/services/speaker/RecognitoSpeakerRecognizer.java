package de.uniHamburg.informatik.continuousvoice.services.speaker;

import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;

public class RecognitoSpeakerRecognizer implements ISpeakerRecognizer {

    @Override
    public AbstractSpeakerFeature extractFeature(PcmFile sample) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getDistance(AbstractSpeakerFeature a, AbstractSpeakerFeature b) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getMaxDistance() {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
