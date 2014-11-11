package de.uniHamburg.informatik.continuousvoice.services.sound;


public class AudioHelper {

    public static int MAX_SOUND_LEVEL = 32768;
    
    /**
     * Extract Decibel value out of pcm data with black magic from:
     * http://stackoverflow.com/a/8766420
     * 
     * @param pcmData
     *            the raw audio buffer
     * @return the decibel
     */
    public static double pcmToSoundLevel(short[] pcmData) {
        
        double max = 0;
        
        for (short s: pcmData) {
            max = Math.max(s, max);
        }

        return max;
    }
}
