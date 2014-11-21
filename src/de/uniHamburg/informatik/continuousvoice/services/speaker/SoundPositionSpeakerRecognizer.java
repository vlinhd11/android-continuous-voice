package de.uniHamburg.informatik.continuousvoice.services.speaker;

import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;

public class SoundPositionSpeakerRecognizer  implements ISpeakerRecognizer {

    public static final double MAX_DIST = 0.1;
    
    @Override
    public AbstractSpeakerFeature extractFeature(PcmFile sample) {
        double pos = AudioHelper.soundPosition(sample);
        return new SoundPositionSpeakerFeature(pos);
    }

    @Override
    public double getDistance(AbstractSpeakerFeature a, AbstractSpeakerFeature b) {
        return a.getDistanceTo(b);
    }

    @Override
    public double getMaxDistance() {
        return MAX_DIST;
    }

}