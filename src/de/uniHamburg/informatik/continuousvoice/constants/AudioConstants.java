package de.uniHamburg.informatik.continuousvoice.constants;

public class AudioConstants {
	
	public static final double SILENCE_AMPLITUDE_THRESHOLD = 0.2; //percent 0-1
    public static int SILENCE_OFFSET_MILLIS = 2000;
    public static final double MAX_SPEAKER_DISTANCE = 0.3;
    //AUDIO EDIT
    /**
     * prepend size 1sec
     */
    public static final int TIMESHIFT_BUFFER_FRAMES = (int) (0.8 * 20);  //sec * ~20
    /**
     * end cut-off 1sec
     */
    public static final int SOUNDFILE_END_CUTOFF_FRAMES = (int) (1.5 * 20);  //sec * ~20
    
    public enum SpeakerPosition {
        LEFT, RIGHT 
    }
    
    public enum Loudness {
        SPEECH, SILENCE;
    }
    
}
