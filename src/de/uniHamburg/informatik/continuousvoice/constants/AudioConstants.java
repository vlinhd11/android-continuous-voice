package de.uniHamburg.informatik.continuousvoice.constants;

public class AudioConstants {
	public static final double SILENCE_AMPLITUDE_THRESHOLD = 0.2; //percent 0-1
    public static int SILENCE_OFFSET_MILLIS = 2000;
    
    public enum SpeakerPosition {
        LEFT, RIGHT 
    }
    
    public enum Loudness {
        SPEECH, SILENCE;
    }
    
}
