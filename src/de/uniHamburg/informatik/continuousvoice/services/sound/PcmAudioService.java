package de.uniHamburg.informatik.continuousvoice.services.sound;

import java.io.File;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class PcmAudioService extends Activity implements IAudioService {

    private static final String TAG = "AudioRecordActivity";
    public static final String BASE_FILENAME = "audioservice";
    private static final String SUFFIX = "mp3";
    private static final int CONFIG_AUDIO_RATE = 44100;
    public static final double SILENCE_AMPLITUDE_THRESHOLD = 0.2; //percent 0-1
    public static int SILENCE_OFFSET_MILLIS = 2000;
    public int CONFIG_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public int CONFIG_AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public int CONFIG_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    private boolean running = false; //amplitude
    private boolean recording = false; //recording, needs audiopollThreadactive
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
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public File stopRecording() {

        File f = null;
        if (currentRecorder != null && recording) {
            recording = false;
            Log.i(TAG, "Finishing recording, calling stop and release on recorder");
            f = new File(currentRecorder.getFilename());
            try {
                currentRecorder.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //new recorder
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
    public File splitRecording() {
        if (!recording) {
            throw new IllegalStateException("Cannot split. Currently not recording.");
        }

        File file = stopRecording();
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
        if (recorder.getInitState() == 0) {
            frameSize = recorder.getFrameSize();
            if (frameSize == 0) {
                Log.e(TAG, "recorder initialize failure");
            }
        } else {
            Log.i(TAG, "recorder initialize success");
        }

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
            while (running) {
                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);

                //save buffer if recording
                if (bufferReadResult > 0) {
                    if (recording) {
                        try {
                            writeAudioSamples(audioData, bufferReadResult);
                        } catch (Exception e) {
                            Log.v(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    //analyse buffer for amplitude
                    updateAmplitude(audioData.clone());
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
                    
                    for (IAmplitudeListener al : listeners) {
                        al.onAmplitudeUpdate(currentSoundLevel);
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

                if (silenceSince >= SILENCE_OFFSET_MILLIS) {
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
        if (currentSoundLevel < SILENCE_AMPLITUDE_THRESHOLD) {
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
}
