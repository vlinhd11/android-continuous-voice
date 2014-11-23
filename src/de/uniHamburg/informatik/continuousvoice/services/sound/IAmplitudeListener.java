package de.uniHamburg.informatik.continuousvoice.services.sound;

import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;

public interface IAmplitudeListener {

    public void onSilence();
    
    public void onSpeech();
    
    /**
     * @param soundLevel 0..1
     */
    public void onAmplitudeUpdate(double soundLevelLeft, double soundLevelRight);
}
