package de.uniHamburg.informatik.continuousvoice.services.sound;

public interface IAmplitudeListener {

    public void onSilence();
    
    public void onSpeech();
    
    /**
     * @param percent 0..1
     */
    public void onAmplitudeUpdate(double percent);
}
