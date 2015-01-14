package de.uniHamburg.informatik.continuousvoice.constants;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioConstants {
	
    private static final String TAG = "AudioConstants";
    /**
     * Recorder Config
     */
    //44100Hz is guaranteed to work on all devices, 22050, 16000, and 11025 possible
    public static final int AUDIO_RATE = 44100;
    public static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int AUDIO_ENCODING_BITS = 16;
    //mono: AudioFormat.CHANNEL_IN_MONO; stereo: AudioFormat.CHANNEL_IN_STEREO;
    public static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    //mono: MediaRecorder.AudioSource.MIC; stereo: MediaRecorder.AudioSource.CAMCORDER
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER;
    //2 for stereo
    public static final int AUDIO_CHANNELS = 2;
    //Frame size (buffer size) is in bytes, for short: /2
    public static final int AUDIO_MIN_BUFFER_SIZE_BYTES = AudioRecord.getMinBufferSize(AUDIO_RATE,
            AUDIO_CHANNEL, AUDIO_ENCODING);
    public static final int AUDIO_MIN_BUFFER_SIZE_SHORTS = AUDIO_MIN_BUFFER_SIZE_BYTES / 2;
    
    public static final double AUDIO_BUFFER_LENGTH_SEC = (double) AUDIO_MIN_BUFFER_SIZE_BYTES / (2.0 * 2.0 * (double) AUDIO_RATE);
    public static final int AUDIO_BUFFER_LENGTH_MILLIS = (int) (AUDIO_BUFFER_LENGTH_SEC*1000.0);

    /**
     * Silence and Speaker Recognition
     */
    //Sound Level in percent 0.0 - 1.0
	public static final double SILENCE_AMPLITUDE_THRESHOLD = 0.2;                       //<=== CONFIG
	//The duration it must remain silent before sending a SILENCE event.
    public static int SILENCE_OFFSET_MILLIS = 2000;                                     //<=== CONFIG

    //Speaker distance threshold in percent 0.0 - 1.0
    public static final double MAX_SPEAKER_DISTANCE = 0.3;                              //<=== CONFIG
    
    /**
     * AUDIO CUT/EDIT
     */
    //when to throw away recordings instead of sending them to the server
    public static int MIN_TRANSCRIPTION_AUDIO_LENGTH_MILLIS = 1200;                     //<=== CONFIG
    //time shift
    public static final int TIMESHIFT_BUFFER_MILLIS = 800;                              //<=== CONFIG
    public static final int TIMESHIFT_BUFFER_CHUNKS = TIMESHIFT_BUFFER_MILLIS / AUDIO_BUFFER_LENGTH_MILLIS;
    //end cut-off 1sec
    public static final int SOUNDFILE_END_CUTOFF_MILLIS = 1500;                         //<=== CONFIG
    public static final int SOUNDFILE_END_CUTOFF_CHUNKS = SOUNDFILE_END_CUTOFF_MILLIS / AUDIO_BUFFER_LENGTH_MILLIS; //before: 35
    
    /**
     * General enums
     */
    public enum SpeakerPosition {
        LEFT, RIGHT 
    }
    
    public enum Loudness {
        SPEECH, SILENCE;
    }
    
    public static void print() {
        Log.d(TAG, "AUDIO_RATE: " + AUDIO_RATE);
        Log.d(TAG, "AUDIO_ENCODING: " + AUDIO_ENCODING);
        Log.d(TAG, "AUDIO_CHANNEL: " + AUDIO_CHANNEL);
        Log.d(TAG, "AUDIO_SOURCE: " + AUDIO_SOURCE);
        Log.d(TAG, "AUDIO_MIN_BUFFER_SIZE: " + AUDIO_MIN_BUFFER_SIZE_BYTES);
        Log.d(TAG, "AUDIO_MIN_BUFFER_SIZE_SHORTS: " + AUDIO_MIN_BUFFER_SIZE_SHORTS);
        Log.d(TAG, "AUDIO_BUFFER_LENGTH_SEC: " + AUDIO_BUFFER_LENGTH_SEC);
        Log.d(TAG, "AUDIO_BUFFER_LENGTH_MILLIS: " + AUDIO_BUFFER_LENGTH_MILLIS);
        Log.d(TAG, "SILENCE_AMPLITUDE_THRESHOLD: " + SILENCE_AMPLITUDE_THRESHOLD);
        Log.d(TAG, "SILENCE_OFFSET_MILLIS: " + SILENCE_OFFSET_MILLIS);
        Log.d(TAG, "MAX_SPEAKER_DISTANCE: " + MAX_SPEAKER_DISTANCE);
        Log.d(TAG, "MIN_TRANSCRIPTION_AUDIO_LENGTH_MILLIS: " + MIN_TRANSCRIPTION_AUDIO_LENGTH_MILLIS);
        Log.d(TAG, "TIMESHIFT_BUFFER_MILLIS: " + TIMESHIFT_BUFFER_MILLIS);
        Log.d(TAG, "TIMESHIFT_BUFFER_CHUNKS: " + TIMESHIFT_BUFFER_CHUNKS);
        Log.d(TAG, "SOUNDFILE_END_CUTOFF_MILLIS: " + SOUNDFILE_END_CUTOFF_MILLIS);
        Log.d(TAG, "SOUNDFILE_END_CUTOFF_CHUNKS: " + SOUNDFILE_END_CUTOFF_CHUNKS);
    }
    
}
