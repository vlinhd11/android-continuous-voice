package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.Loudness;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.SpeakerPosition;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper.ConversionDoneCallback;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.IRecorder;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.IAudioService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerAssignResult;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerManager;

public abstract class AbstractWebServiceRecognizer extends AbstractRecognizer implements IAmplitudeListener {

    public final String TAG = "AbstractWebServiceRecognitionService";
    private ScheduledExecutorService maxRecordingTimeScheduler;
    protected long RECORDING_MAX_DURATION = 10 * 1000;
    private IRecorder recorder;
    protected IAudioService audioService;
    private Runnable splitRunnable;
    private Handler handler = new Handler();
    private SpeakerManager speakerManager;

    public AbstractWebServiceRecognizer(IAudioService audioService, SpeakerManager speakerManager) {
        this.audioService = audioService;
        this.speakerManager = speakerManager;
        this.recorder = audioService;
        this.splitRunnable = new Runnable() {
            @Override
            public void run() {
                setStatus("splitting");
                Log.e(TAG, "          ┌────┴────┐");
                Log.e(TAG, "          │❰ split ❱│");
                Log.e(TAG, "          └────┬────┘");
                File f = recorder.splitRecording();
                transcribeAsync((PcmFile) f);
                startMaxTimeScheduler();
            }
        };
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
        if (running) {
            stop();
        }
    }

    @Override
    public void start() {
        //1 ensure audioService is running
        if (!audioService.isRunning()) {
            audioService.initialize();
            setStatus("AudioService turn on");
        }

        super.start();

        audioService.addAmplitudeListener(this);
        if (audioService.getCurrentSilenceState() == Loudness.SPEECH) {
            startRecording();
        }
    }

    @Override
    public void stop() {
        stopMaxTimeScheduler();
        audioService.removeAmplitudeListener(this);
        if (recorder.isRecording()) {
            PcmFile toTranscribe = (PcmFile) recorder.stopRecording();
            setStatus("stopped, transcribing");
            transcribeAsync(toTranscribe);
        } else {
            setStatus("stopped");
        }

        super.stop();
    }

    /**
     * Calls the abstract method "request(File)" in background. Sends the
     * recognized words to the UI on result.
     * 
     * @param f
     *            the audio file (mp3) to transcribe
     */
    protected void transcribeAsync(final PcmFile f) {

        Thread transcriptionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final SpeakerAssignResult sar = speakerManager.assign(f);
                
                final long start = System.currentTimeMillis();
                try {
                    AudioHelper.convertMp3ToCompressedWav(settings.getApplicationContext(), f, new ConversionDoneCallback() {
                        @Override
                        public void conversionDone(File mp3, final File wav) {
                            Log.i(TAG, "conversion done:" 
                                    + (System.currentTimeMillis() - start) + "ms, " 
                                    + (mp3.length() / 1024.0) + "kB -> "
                                    + (wav.length() / 1024.0) + "kB, " 
                                    + "(" + (1.0 - ((double)wav.length() / (double)mp3.length()))*100 + "%))");
                            //2: when done: request
                            new TranscriptionAsyncTask(wav, sar).start();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        transcriptionThread.start();
    }

    @Override
    public void onSpeech() {
        startRecording();
    }

    @Override
    public void onSilence() {
        stopRecording();
    }

    @Override
    public void onAmplitudeUpdate(double percentLeft, double percentRight) {
        //nothing
    }

    @Override
    public void onSpeakerChange(SpeakerPosition pos) {
        //nothing
    }

    private void startRecording() {
        if (!recorder.isRecording()) {
            Log.e(TAG, "❰ record ❱─────┐");
            startMaxTimeScheduler();
            //start recorder
            recorder.startRecording();
        }
    }

    private void stopRecording() {
        if (recorder.isRecording()) {
            //Stop recorder
            File toTranscribe = recorder.stopRecording();

            Log.e(TAG, "               └───┬─❰ stop ❱");
            Log.e(TAG, "                   └\"" + toTranscribe.getName() + "\" " + toTranscribe.length() / 1024.0
                    + "kb");

            //transcribe
            transcribeAsync((PcmFile) toTranscribe);
            //stopTimer
            stopMaxTimeScheduler();
        }
    }

    private void startMaxTimeScheduler() {
        stopMaxTimeScheduler();

        maxRecordingTimeScheduler = Executors.newScheduledThreadPool(1);
        maxRecordingTimeScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                handler.post(splitRunnable);
            }
        }, RECORDING_MAX_DURATION, TimeUnit.MILLISECONDS);
    }

    private void stopMaxTimeScheduler() {
        if (maxRecordingTimeScheduler != null) {
            maxRecordingTimeScheduler.shutdownNow();
        }
    }
    
    private class TranscriptionAsyncTask extends Thread {
    	public TranscriptionAsyncTask(final File file, final SpeakerAssignResult sar) {
			super(new Runnable() {
				public void run() {
					final String result = request(file);
					final AbstractWebServiceRecognizer me = AbstractWebServiceRecognizer.this;
					handler.post(new Runnable() {
                        @Override
                        public void run() {
                        	me.addWordsForSpeaker(result, sar);
                        }
                    });
				}
			});
		}
    }
    
    
    public abstract String request(File audioFile);
}
