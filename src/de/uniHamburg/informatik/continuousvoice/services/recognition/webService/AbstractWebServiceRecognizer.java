package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.Loudness;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioHelper.ConversionDoneCallback;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.IRecorder;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.IAudioService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;

public abstract class AbstractWebServiceRecognizer extends AbstractRecognizer implements IAmplitudeListener {

    public final String TAG = "AbstractWebServiceRecognitionService";
    private ScheduledExecutorService maxRecordingTimeScheduler;
    protected long RECORDING_MAX_DURATION = 10 * 1000;
    private IRecorder recorder;
    protected IAudioService audioService;
    private Runnable splitRunnable;
    private Handler handler = new Handler();
	private Speaker currentSpeaker;

    public AbstractWebServiceRecognizer(IAudioService audioService) {
        this.audioService = audioService;
        this.recorder = audioService;
        this.splitRunnable = new Runnable() {
            @Override
            public void run() {
                setStatus("splitting");
                Log.e(TAG, "          ┌────┴────┐");
                Log.e(TAG, "          │❰ split ❱│");
                Log.e(TAG, "          └────┬────┘");
                File f = recorder.splitRecording();
                transcribeAsync(currentTranscriptionId, currentSpeaker, (PcmFile) f);
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

    public void start() {
        //1 ensure audioService is running
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

    /**
     * Calls the abstract method "request(File)" in background. Sends the
     * recognized words to the UI on result.
     * 
     * @param f
     *            the audio file (mp3) to transcribe
     */
    protected void transcribeAsync(final int id, final Speaker speaker, final PcmFile f) {

        Thread transcriptionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //final Speaker speaker = speakerManager.assign(f);
                
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
                            //Speaker
                            new TranscriptionAsyncTask(id, wav, speaker).getThread().start();
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

    private void startRecording() {
        if (!recorder.isRecording()) {
        	currentSpeaker = audioService.getCurrentSpeaker();
        	Log.e(TAG, "curr speak: "  + currentSpeaker);
        	super.startTranscription(currentSpeaker);
            
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
            transcribeAsync(currentTranscriptionId, currentSpeaker, (PcmFile) toTranscribe);
            
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
    
    protected class TranscriptionAsyncTask {
    	private int id;
    	private File file;
    	final AbstractWebServiceRecognizer me = AbstractWebServiceRecognizer.this;
		private Speaker speaker;
		
    	public TranscriptionAsyncTask(int id, final File file, final Speaker speaker) {
    		this.id = id;
    		this.file = file;
    		this.speaker = speaker;
    	}
    	
    	public Thread getThread() {
    		return new Thread(new Runnable() {
				public void run() {
					final String result = request(file);
					handler.post(new Runnable() {
                        @Override
                        public void run() {
                        	me.finishTranscription(id, result, speaker);
                        }
                    });
				}
			});
		}
    }
    
    
    public abstract String request(File audioFile);
}
