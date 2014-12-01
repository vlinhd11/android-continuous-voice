package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.Loudness;
import de.uniHamburg.informatik.continuousvoice.constants.RecognitionConstants;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.IConversionDoneCallback;
import de.uniHamburg.informatik.continuousvoice.services.sound.IRecorder;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.IAudioService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;

public abstract class AbstractWebServiceRecognizer extends AbstractRecognizer implements IAmplitudeListener,
        IWebServiceRecognizer {

    public final String TAG = "AbstractWebServiceRecognitionService";
    private ScheduledExecutorService maxRecordingTimeScheduler;
    private IRecorder recorder;
    protected IAudioService audioService;
    private Handler handler = new Handler();
    private TranscriptionWorker worker;

    public AbstractWebServiceRecognizer(IAudioService audioService) {
        this.audioService = audioService;
        this.recorder = audioService;
    }

    @Override
    public void initialize() {
        worker = new TranscriptionWorker(this, new IJobDoneListener() {

            @Override
            public void jobDone(final int id, final String result, final Speaker speaker) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        finishTranscription(id, result, speaker);
                    }
                });
            }
        });

    }

    @Override
    public void shutdown() {
        if (running) {
            stop();
        }
    }

    public void start() {
        // 1 ensure audioService is running
        if (!audioService.isRunning()) {
            audioService.initialize();
            setStatus("AudioService turn on");
        }

        audioService.addAmplitudeListener(this);
        if (audioService.getCurrentSilenceState() == Loudness.SPEECH) {
            startRecording();
        }
    }

    @Override
    public void stop() {
        stopRecording();

        stopMaxTimeScheduler();
        audioService.removeAmplitudeListener(this);
        super.stop();
    }

    protected void transcribe(final int id, final PcmFile f) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AudioHelper.compress(settings.getApplicationContext(), f, new ConversionDoneCallback(id, f));
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

            }
        }).run();
    }
    
    private class ConversionDoneCallback implements IConversionDoneCallback {
        private int id;
        private PcmFile pcmFile;
        public ConversionDoneCallback(int id, PcmFile pcm) {
            this.id = id;
            this.pcmFile = pcm;
        }
        @Override
        public void conversionDone(File origin, File converted, long took) {
            Log.w(TAG, "conversion done: " + took + "ms, "
                    + (origin.length() / 1024.0) + "kB -> " + (converted.length() / 1024.0) + "kB, "
                    + "(" + (1.0 - ((double) converted.length() / (double) origin.length())) * 100
                    + "%))");
            // when done: request
            worker.enqueueJob(id, converted, audioService.identifySpeaker(pcmFile));
        }
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
        // nothing
    }

    private void startRecording() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (!recorder.isRecording()) {
                    int id = AbstractWebServiceRecognizer.super.startTranscription();

                    Log.e(TAG, "❰ record " + id + " ❱───┐");
                    startMaxTimeScheduler();
                    // start recorder
                    recorder.startRecording();
                }
            }
        });
    }

    private void stopRecording() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (recorder.isRecording()) {
                    Log.e(TAG, "abstract stop rec");
                    // Stop recorder
                    final File toTranscribe = recorder.stopRecording();
                    Log.e(TAG, "               └─────❰ stop " + currentTranscriptionId + " ❱");
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            transcribe(currentTranscriptionId, (PcmFile) toTranscribe);
                        }
                    }).start();
                    // stopTimer
                    stopMaxTimeScheduler();
                }
            }
        });
    }

    private void startMaxTimeScheduler() {
        stopMaxTimeScheduler();

        maxRecordingTimeScheduler = Executors.newScheduledThreadPool(1);
        maxRecordingTimeScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (recorder.isRecording()) {
                            setStatus("splitting");
                            stopRecording();
                            startRecording();
                        }
                    }
                });
            }
        }, RecognitionConstants.MAX_LENGTH_AUDIO_RECORD, TimeUnit.MILLISECONDS);
    }

    private void stopMaxTimeScheduler() {
        if (maxRecordingTimeScheduler != null) {
            maxRecordingTimeScheduler.shutdownNow();
        }
    }

}
