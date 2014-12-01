package de.uniHamburg.informatik.continuousvoice.constants;

public class AudioConstants {
	
	public static final double SILENCE_AMPLITUDE_THRESHOLD = 0.2; //percent 0-1
    public static int SILENCE_OFFSET_MILLIS = 2000;
    public static final double MAX_SPEAKER_DISTANCE = 0.3;
    public static final int AUDIO_BUFFER_SIZE = 20;  //sec * 20
    
    public enum SpeakerPosition {
        LEFT, RIGHT 
    }
    
    public enum Loudness {
        SPEECH, SILENCE;
    }
    
}
