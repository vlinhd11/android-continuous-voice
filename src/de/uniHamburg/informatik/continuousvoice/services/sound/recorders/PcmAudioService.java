package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.io.File;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.Loudness;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAudioServiceStartStopListener;

public class PcmAudioService extends Activity implements IAudioService {

    private static final String TAG = "PcmAudioService";
    public static final String BASE_FILENAME = "audioservice";
    public static final String MIME_TYPE = "audio/wav"; //http://en.wikipedia.org/wiki/MP3
    private static final String SUFFIX = "mp3";
    private static final int CONFIG_AUDIO_RATE = 48000; //44100;
    public int CONFIG_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public int CONFIG_AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    public int CONFIG_AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER; //MediaRecorder.AudioSource.MIC;

    private boolean running = false; //amplitude
    private boolean recording = false; //recording, needs running
    private int recorderIteration = 0;
    private int frameSize;
    private short[] pending = new short[0];
    private volatile Mp3FileRecorder currentRecorder;
    private volatile Mp3FileRecorder alternateRecorder;
    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;
    private Thread audioThread;
    private List<IAmplitudeListener> listeners = new ArrayList<IAmplitudeListener>();
    private List<IAudioServiceStartStopListener> startStopListeners = new ArrayList<IAudioServiceStartStopListener>();

    //aplitude measurement
    private double currentSoundLevel = 0.0; //percent 0-1
    private long silenceStartedAt = -1l;
    private Loudness lastNotificationState;
    private Context context;
    
    public PcmAudioService(Context context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        currentRecorder = createMp3Recorder();
        alternateRecorder = createMp3Recorder();

        notifyStartStopListeners();

        //start the thread which runs continuously!!
        running = true;
        try {
            audioRecordRunnable = new AudioRecordRunnable();
            audioThread = new Thread(audioRecordRunnable);
            audioThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        recording = false;
        running = false;
        notifyStartStopListeners();
        
        currentSoundLevel = 0;
        notifySilenceListeners(Loudness.SILENCE);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void startRecording() {
        recording = true;
        //init
        if (currentRecorder.initAudio() == 0) {
            frameSize = currentRecorder.getFrameSize();
            if (frameSize == 0) {
                Log.e(TAG, "currentRecorder initialize failure");
            }
        } else {
            Log.i(TAG, "currentRecorder initialize success");
        }
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public PcmFile stopRecording() {
        Log.w(TAG, "STOP RECORDING");

        PcmFile f = null;
        if (currentRecorder != null && recording) {
            recording = false;
            Log.i(TAG, "Finishing recording, calling stop and release on recorder");
            f = currentRecorder.getPcmFile();
            try {
                currentRecorder.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //new recorder
            currentRecorder = null;
            currentRecorder = alternateRecorder;
            alternateRecorder = createMp3Recorder();
        } else {
            Log.e(TAG, "Can't stop. Is not recording.");
        }

        if (!f.exists()) {
            throw new IllegalStateException("The soundfile " + f.getAbsolutePath() + " does not exist.");
        }
        
        return f;
    }

    @Override
    public PcmFile splitRecording() {
        if (!recording) {
            throw new IllegalStateException("Cannot split. Currently not recording.");
        }

        PcmFile file = stopRecording();
        //start again immediately
        startRecording();

        if (!file.exists()) {
            throw new IllegalStateException("The soundfile " + file.getAbsolutePath() + " does not exist.");
        }

        return file;
    }

    private String getNextFileName() {
        recorderIteration++;
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String basePath = dir.getAbsolutePath();
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        return basePath + "/" + BASE_FILENAME + "_" + date + "_" + recorderIteration + "." + SUFFIX;
    }

    private Mp3FileRecorder createMp3Recorder() {
        Log.i(TAG, "init recorder");
        Mp3FileRecorder recorder = new Mp3FileRecorder(getNextFileName());

        return recorder;
    }

    //---------------------------------------------
    // audio thread, gets and encodes audio data
    //---------------------------------------------
    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // Audio
            int bufferSize;
            short[] audioData;
            int bufferReadResult;

            bufferSize = AudioRecord.getMinBufferSize(CONFIG_AUDIO_RATE, CONFIG_AUDIO_CHANNEL, CONFIG_AUDIO_ENCODING);
            audioRecord = new AudioRecord(CONFIG_AUDIO_SOURCE, CONFIG_AUDIO_RATE, CONFIG_AUDIO_CHANNEL,
                    CONFIG_AUDIO_ENCODING, bufferSize);
            
            //start the android recorder
            audioRecord.startRecording();
            
            audioData = new short[bufferSize];

            /* ffmpeg_audio encoding loop */
            long last = System.currentTimeMillis();
            while (running) {
                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
                long curr = System.currentTimeMillis();
                long diff = curr - last;
                last = curr;
                
                Log.w(TAG, diff + "");
                //save buffer if recording
                if (bufferReadResult > 0) {
                    if (recording) {
                        try {
                            writeAudioSamples(audioData, bufferReadResult);
                        } catch (Exception e) {
                            Log.v(TAG, "m: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    //analyse buffer for amplitude
                    saveToTimeshiftBuffer(audioData.clone());
                    updateAmplitude(audioData);
                    updateSilenceState();
                } else {
                    Log.e(TAG, "audio record error: " + bufferReadResult);
                }
                
            }
            Log.v(TAG, "AudioThread Finished, release audioRecord");

            /* encoding finish, release recorder */
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                Log.v(TAG, "audioRecord released");
            }
        }

    }

    private void saveToTimeshiftBuffer(short[] clone) {
    	// TODO create a LinkedList<short[]>(x) buffer with the last audio data
    	
    }

    private void writeAudioSamples(short[] buffer, int bufferReadResult) {

        int pendingArrLength = pending.length;
        short[] newArray = new short[bufferReadResult + pendingArrLength];

        System.arraycopy(pending, 0, newArray, 0, pendingArrLength);
        System.arraycopy(buffer, 0, newArray, pendingArrLength, bufferReadResult);

        int len = newArray.length;
        int q = Math.abs(len / frameSize);
        int r = len % frameSize;

        ShortBuffer shortBuffer = ShortBuffer.wrap(newArray);
        for (int i = 0; i < q && recording; i++) {
            short dst[] = new short[frameSize];
            shortBuffer.get(dst);
            currentRecorder.writeAudioFrame(dst, dst.length);
        }
        pending = new short[r];
        shortBuffer.get(pending);
    }

    /**
     * update amplitude and notifly listeners if valid amplitude
     * 
     * @param s
     */
    private void updateAmplitude(final short[] audioData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (running) {
                    double soundLevel = AudioHelper.pcmToSoundLevel(audioData);
                    
                    //notify listeners
                    currentSoundLevel = (soundLevel / (double) AudioHelper.MAX_SOUND_LEVEL);
                    
                    short[][] stereo = AudioHelper.splitStereo(audioData);
                    double currentSoundLevelLeft = (AudioHelper.pcmToSoundLevel(stereo[0]) / (double) AudioHelper.MAX_SOUND_LEVEL);
                    double currentSoundLevelRight = (AudioHelper.pcmToSoundLevel(stereo[1]) / (double) AudioHelper.MAX_SOUND_LEVEL);
                    
                    for (IAmplitudeListener al : listeners) {
                        al.onAmplitudeUpdate(currentSoundLevelLeft, currentSoundLevelRight);
                    }
                }
            }
        }).start();
    }
    
    private void updateSilenceState() {
        Loudness current = getCurrentSilenceState();

        if (current == Loudness.SILENCE) {
            if (silenceStartedAt == -1l) {
                //start silence time
                silenceStartedAt = System.currentTimeMillis();
            } else {
                long silenceSince = System.currentTimeMillis() - silenceStartedAt;

                if (silenceSince >= AudioConstants.SILENCE_OFFSET_MILLIS) {
                    notifySilenceListeners(Loudness.SILENCE);
                    silenceStartedAt = -1l; //reset
                }
            }
        } else {
            silenceStartedAt = -1l; //reset
            notifySilenceListeners(Loudness.SPEECH);
        }
    }

    public Loudness getCurrentSilenceState() {
        if (currentSoundLevel < AudioConstants.SILENCE_AMPLITUDE_THRESHOLD) {
            return Loudness.SILENCE;
        } else {
            return Loudness.SPEECH;
        }
    }

    private void notifySilenceListeners(Loudness state) {
        if (state != lastNotificationState) {
            if (state == Loudness.SPEECH) {
                for (IAmplitudeListener sl : listeners) {
                    sl.onSpeech();
                }
            } else {
                for (IAmplitudeListener sl : listeners) {
                    sl.onSilence();
                }
            }
        }
        lastNotificationState = state;
    }

    public void addAmplitudeListener(IAmplitudeListener sl) {
        listeners.add(sl);
    }

    public void removeAmplitudeListener(IAmplitudeListener sl) {
        listeners.remove(sl);
    }

    public void addStartStopListener(IAudioServiceStartStopListener l) {
        startStopListeners.add(l);
    }

    private void notifyStartStopListeners() {
        for (IAudioServiceStartStopListener l : startStopListeners) {
            l.onAudioServiceStateChange();
        }
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }
}
