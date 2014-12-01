package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import de.uniHamburg.informatik.continuousvoice.services.speaker.ISpeakerChangeListener;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerRecognizer;

public class PcmAudioService extends Activity implements IAudioService {

    private static final String TAG = "PcmAudioService";
    public static final String BASE_FILENAME = "pcm";
    public static final String MIME_TYPE = "audio/wav";
    private static final String SUFFIX = "wav";
    private static final int CONFIG_AUDIO_RATE = 44100; // 44100 vs 48000;
    public static final int CONFIG_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int CONFIG_AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO; // AudioFormat.CHANNEL_IN_STEREO;
    // for stereo, for mono: MediaRecorder.AudioSource.MIC; vs CAMCORDER
    public static final int CONFIG_AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER;
    public static final int CONFIG_MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(CONFIG_AUDIO_RATE,
            CONFIG_AUDIO_CHANNEL, CONFIG_AUDIO_ENCODING);

    private boolean running = false; // amplitude
    private boolean recording = false; // recording, needs running
    private int recorderIteration = 0;
    private WavFileRecorder currentRecorder;
    private WavFileRecorder alternateRecorder;
    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;
    private TimeShiftBuffer timeShift;
    private boolean includeTimeShift;
    private Thread audioThread;
    private SpeakerRecognizer speakerRecognizer;

    // listeners
    private List<IAmplitudeListener> listeners = new ArrayList<IAmplitudeListener>();
    private List<IAudioServiceStartStopListener> startStopListeners = new ArrayList<IAudioServiceStartStopListener>();

    // aplitude measurement
    private double currentSoundLevel = 0.0; // percent 0-1
    private long silenceStartedAt = -1l;
    private Loudness lastNotificationState;

    public PcmAudioService(SpeakerRecognizer speakerRecognizer) {
        this.timeShift = new TimeShiftBuffer();
        this.speakerRecognizer = speakerRecognizer;
        addAmplitudeListener(speakerRecognizer);
    }

    @Override
    public void initialize() {
        currentRecorder = createRecorder();
        alternateRecorder = createRecorder();

        notifyStartStopListeners();

        // start the thread which runs continuously!!
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
        timeShift.clear();
        speakerRecognizer.clear();

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
        includeTimeShift = true;
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public PcmFile stopRecording() {
        PcmFile f = null;
        if (currentRecorder != null && recording) {
            recording = false;
            includeTimeShift = false;
            f = currentRecorder.writeFile();

            // new recorder
            currentRecorder = null;
            currentRecorder = alternateRecorder;
            alternateRecorder = createRecorder();
        } else {
            Log.e(TAG, "Can't stop. Is not recording.");
        }

        if (!f.exists()) {
            throw new IllegalStateException("The soundfile " + f.getAbsolutePath() + " does not exist.");
        }

        return f;
    }

    @SuppressLint("SimpleDateFormat")
    private String getNextFileName() {
        recorderIteration++;
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String basePath = dir.getAbsolutePath();
        String date = new SimpleDateFormat("HH-mm-ss").format(new Date());
        return basePath + "/" + BASE_FILENAME + "_" + date + "_" + recorderIteration + "." + SUFFIX;
    }

    private WavFileRecorder createRecorder() {
        return new WavFileRecorder(getNextFileName(), CONFIG_AUDIO_RATE, CONFIG_MIN_BUFFER_SIZE);
    }

    // ---------------------------------------------
    // audio thread, gets and encodes audio data
    // ---------------------------------------------
    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            int bufferReadResult;

            audioRecord = new AudioRecord(CONFIG_AUDIO_SOURCE, CONFIG_AUDIO_RATE, CONFIG_AUDIO_CHANNEL,
                    CONFIG_AUDIO_ENCODING, CONFIG_MIN_BUFFER_SIZE);

            // start the android recorder
            audioRecord.startRecording();
            short[] audioData = new short[CONFIG_MIN_BUFFER_SIZE / 2];

            /* ffmpeg_audio encoding loop */
            while (running) { // running
                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);

                if (bufferReadResult != AudioRecord.ERROR_INVALID_OPERATION) {
                    // save buffer if recording
                    if (recording) {
                        try {
                            // turn back the time ♫
                            if (includeTimeShift) {
                                for (short[] timeShiftData : timeShift.getPastAudioData()) {
                                    currentRecorder.writeAudioFrame(timeShiftData);
                                }
                                includeTimeShift = false; // done, set flag!
                            }
                            currentRecorder.writeAudioFrame(audioData);
                        } catch (Exception e) {
                            Log.v(TAG, "m: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    // analyse buffer for amplitude
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
        timeShift.write(clone);
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

                    // notify listeners
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
                // start silence time
                silenceStartedAt = System.currentTimeMillis();
            } else {
                long silenceSince = System.currentTimeMillis() - silenceStartedAt;

                if (silenceSince >= AudioConstants.SILENCE_OFFSET_MILLIS) {
                    notifySilenceListeners(Loudness.SILENCE);
                    silenceStartedAt = -1l; // reset
                }
            }
        } else {
            silenceStartedAt = -1l; // reset
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

    @Override
    public void addSpeakerChangeListener(ISpeakerChangeListener iSpeakerChangeListener) {
        speakerRecognizer.addSpeakerChangeListener(iSpeakerChangeListener);
    }

    @Override
    public Speaker identifySpeaker(PcmFile f) {
        return speakerRecognizer.getSpeakerFromFile(f);
    }
}
