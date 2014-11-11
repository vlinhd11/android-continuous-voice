package de.uniHamburg.informatik.continuousvoice.services.recognition.nuance;

import android.content.Context;
import android.os.Handler;

import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.Recognizer.Listener;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService;

public class NuanceRecognizer extends AbstractRecognizer implements Listener {

    public final String TAG = "NuanceRecognitionService";
    private final static int MAX_UNSUCCESSFUL_ATTEMPTS = 3;

    private SpeechKit speechKit;
    private final Context context;
    private Recognizer currentRecognizer;
    private int attempts = 0;
    private IAudioService audioService;
    private Handler handler = new Handler();
    private boolean audioServiceBeforeState = false;

    public NuanceRecognizer(Context context, IAudioService audioService) {
        this.context = context;
        this.audioService = audioService;
    }

    @Override
    public void initialize() {

        if (speechKit == null) {
            speechKit = SpeechKit.initialize(context, NuanceCredentials.SpeechKitAppId,
                    NuanceCredentials.SpeechKitServer, NuanceCredentials.SpeechKitPort, NuanceCredentials.SpeechKitSsl,
                    NuanceCredentials.SpeechKitApplicationKey);
            speechKit.connect();
        }
    }

    @Override
    public void start() {
        super.start();
        audioServiceBeforeState = audioService.isRunning();

        restartRecognitionCycle();
    }

    private void restartRecognitionCycle() {
        if (running) {
            if (audioService.isRunning()) {
                audioService.shutdown();
                setStatus("AudioService turn off");
            }

            if (currentRecognizer != null) {
                currentRecognizer.cancel();
            }
            //Long: Detect the end of a longer phrase, sentence or sentences that may have brief pauses.
            //None: Do not detect the end of speech.
            //Short: Detect the end of a short phrase with no pauses.
            currentRecognizer = speechKit.createRecognizer(Recognizer.RecognizerType.Dictation,
                    Recognizer.EndOfSpeechDetection.Long, settings.getLanguage().getCode6(), this, handler);
            currentRecognizer.start();
        }
    }

    @Override
    public void stop() {
        currentRecognizer.cancel();

        if (audioServiceBeforeState && !audioService.isRunning()) {
            audioService.initialize();
            setStatus("AudioService turn on");
        }

        super.stop();
    }

    @Override
    public String getName() {
        return "Nuance NDEV Speech Kit 1.4.7";
    }

    @Override
    public void shutdown() {
        if (speechKit != null) {
            speechKit.release();
            speechKit = null;
        }

        super.shutdown();
    }

    @Override
    public void onError(Recognizer arg0, SpeechError err) {
        attempts += 1;
        if (attempts > MAX_UNSUCCESSFUL_ATTEMPTS) {
            stop();
            setStatus("stopped after " + attempts + " attempts");
        } else {
            setStatus("ERR: " + err.getErrorDetail());
            restartRecognitionCycle();
        }
    }

    @Override
    public void onRecordingBegin(Recognizer arg0) {
    }

    @Override
    public void onRecordingDone(Recognizer arg0) {
    }

    @Override
    public void onResults(Recognizer arg0, Recognition result) {
        attempts = 0;
        if (running) {
            if (result.getResultCount() > 0) {
                addWords(result.getResult(0).getText());
                setStatus(result.getResultCount() + " results, score: " + result.getResult(0).getScore());
                restartRecognitionCycle();
            } else {
                setStatus("no results");
            }
        }
    }
}
