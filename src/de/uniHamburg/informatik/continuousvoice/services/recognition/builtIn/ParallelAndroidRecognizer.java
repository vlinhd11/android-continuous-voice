package de.uniHamburg.informatik.continuousvoice.services.recognition.builtIn;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.IAudioService;

public class ParallelAndroidRecognizer extends AbstractRecognizer {

    private static final String TAG = "ParallelAndroidRecognizer";
    private List<SpeechRecognizer> recognizers = new LinkedList<SpeechRecognizer>();
    private AudioManager audioManager;
    private boolean beepOff = false;
    private Context context;
    private IAudioService audioService;
    private int recognizerId = 0;
    private static final int NUMBER_OF_INSTANCES = 2;
    
    public ParallelAndroidRecognizer(Context context, IAudioService audioService) {
        this.context = context;
        this.audioService = audioService;
    }

    @Override
    public void initialize() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    // Service control ->
    @Override
    public void start() {
        super.startTranscription();

        if (audioService.isRunning()) {
            audioService.shutdown();
            setStatus("AudioService turn off");
        }

        turnBeepOff();

        //create recognizers
        for (int i = 0; i < NUMBER_OF_INSTANCES; i++) {
            startANewRecognizer();
        }
        
        setStatus("started");
    }

    @Override
    public void stop() {
        super.stop();
        for (SpeechRecognizer r : recognizers) {
            if (r != null) {
                r.destroy();
            }
        }
        recognizers.clear();

        turnBeepOn();

        if (!audioService.isRunning()) {
            audioService.initialize();
            setStatus("AudioService turn on");
        }
    }

    /**
     * Destroy the recognizer.
     */
    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void shutdown() {
        if (isRunning()) {
            stop();
        }
        super.shutdown();
    }
    

    private void startANewRecognizer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = createRecognizerIntent();
                SpeechRecognizer r = SpeechRecognizer.createSpeechRecognizer(context);
                r.setRecognitionListener(new ParallelAbstractAndroidRecognitionListener(r, intent));
                r.startListening(intent);
                Log.i(TAG, "Recognizer created, now listening!");
                recognizers.add(r);
            }
        }).run();
    }

    private Intent createRecognizerIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, settings.getLanguage().getCode4());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "de-DE");
        return intent;
    }

    private void turnBeepOff() {
        if (!beepOff) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
            beepOff = true;
        }
    }

    private void turnBeepOn() {
        if (beepOff) {
            audioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
            beepOff = false;
        }
    }

    @Override
    public String getName() {
        return "Multi Android Recognizer";
    }
    
    private class ParallelAbstractAndroidRecognitionListener extends AbstractAndroidRecognitionListener {
        protected SpeechRecognizer currentRecognizer;
        private Intent intent;
        private int id;

        protected ParallelAbstractAndroidRecognitionListener(SpeechRecognizer thisRecognizer, Intent intent) {
            currentRecognizer = thisRecognizer;
            this.intent = intent;
            this.id = ++recognizerId ;
        }
        
        @Override
        public void onError(int error) {
            super.onError(error);

            setStatus(translateError(error) + "!");
            Log.w(TAG, id + "Received error: " + translateError(error));
            if (super.restartWhenError(error)) {
                currentRecognizer.cancel();
                restart();
                setStatus("restart");
            }
        }

        @Override
        public void onResults(Bundle results) {
            setStatus("got results, restart");
            Log.i(TAG, id + ": Got results.");
            restart();

            StringBuilder scores = new StringBuilder();
            for (int i = 0; i < results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES).length; i++) {
                scores.append(results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)[i] + " ");
            }

            // Add results and notify
            if (results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) != null) {
                String chunk = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
                if (chunk != null) {
                    finishTranscription(currentTranscriptionId, chunk);
                }
            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.i(TAG, id + " onReadyForSpeech");
        }
        
        private void restart() {
            Log.i(TAG, id + ": Restart listening for this recognizer.");
            currentRecognizer.startListening(intent);
        }
        
        @Override
        public void onBeginningOfSpeech() {
            Log.i(TAG, id + " onBeginningOfSpeech");
        }

        @Override
        public void onEndOfSpeech() {
            Log.i(TAG, id + " onEndOfSpeech");
        }
    }

}
