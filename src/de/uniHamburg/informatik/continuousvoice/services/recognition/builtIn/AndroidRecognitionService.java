package de.uniHamburg.informatik.continuousvoice.services.recognition.builtIn;

import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class AndroidRecognitionService extends AbstractRecognitionService {

    private static final String TAG = "AndroidRecognitionService";
    private SpeechRecognizer speech = null; // Speech recognizer instance
    private AudioManager audioManager;
    private boolean beepOff = false;

    private RecognitionListener recognitionListener = new AbstractAndroidRecognitionListener() {

        @Override
        public void onError(int error) {
            super.onError(error);

            Log.i(TAG, "Restarting Android Speech Recognizer");
            if (super.restartWhenError(error)) {
                getSpeechRecognizer().cancel();
                startVoiceRecognitionCycle();
            }
        }

        @Override
        public void onResults(Bundle results) {
            startVoiceRecognitionCycle(); // Restart new dictation cycle

            StringBuilder scores = new StringBuilder();
            for (int i = 0; i < results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES).length; i++) {
                scores.append(results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)[i] + " ");
            }
            Log.d(TAG, "onResults: " + results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) + " scores: "
                    + scores.toString());

            // Add results and notify
            if (results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) != null) {
                String chunk = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
                if (chunk != null) {
                    addWords(chunk);
                }
            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    // Service control ->
    @Override
    protected void onStart() {
        super.onStart();
        turnBeepOff();
        startVoiceRecognitionCycle();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();
            speech = null;
        }
        turnBeepOn();
    }

    /**
     * Destroy the recognizer.
     */
    @Override
    public void onReset() {
        super.onReset();

        if (speech != null) {
            speech.destroy();
            speech = null;
        }
    }

    // <- Service control

    /**
     * Lazy instantiation method for getting the speech recognizer
     * 
     * @return the android speech recognizer
     */
    private SpeechRecognizer getSpeechRecognizer() {
        if (speech == null) {
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(recognitionListener);
        }

        return speech;
    }

    /**
     * Fire an intent to start the voice recognition process.
     */
    public void startVoiceRecognitionCycle() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        getSpeechRecognizer().startListening(intent);
    }
    
    @Override
    public void onDestroy()
    {
        onStop();
        super.onDestroy();
    }

    private void turnBeepOff() {
        if (!beepOff) {
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

}
