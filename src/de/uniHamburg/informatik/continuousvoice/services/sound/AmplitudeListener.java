package de.uniHamburg.informatik.continuousvoice.services.sound;

public interface AmplitudeListener {

    public void onSilence();
    
    public void onSpeech();
    
    /**
     * @param percent 0..1
     */
    public void onAmplitudeUpdate(double percent);
}
