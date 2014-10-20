package de.uniHamburg.informatik.continuousvoice.services.sound.analysis;

public interface SilenceListener {

    public void onSilence();
    
    public void onSpeech();
}
