package de.uniHamburg.informatik.continuousvoice.services.speaker;

public class SoundPositionSpeakerFeature extends AbstractSpeakerFeature {

    private double soundPosition;

    public SoundPositionSpeakerFeature(double soundPosition) {
        this.soundPosition = soundPosition;
    }
    
    @Override
    public double getDistanceTo(AbstractSpeakerFeature other) {
        if (other instanceof SoundPositionSpeakerFeature) {
            return Math.abs(((SoundPositionSpeakerFeature) other).getSoundPosition() - soundPosition);
        } else {
            return 1.0;
        }
    }
    
    public double getSoundPosition() {
        return soundPosition;
    }

    @Override
    public void merge(AbstractSpeakerFeature other) {
        if (other instanceof SoundPositionSpeakerFeature) {
            //average
            this.soundPosition = (((SoundPositionSpeakerFeature) other).getSoundPosition() + soundPosition) / 2;
        }
    }

    
}
