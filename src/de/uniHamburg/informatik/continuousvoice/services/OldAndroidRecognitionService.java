package de.uniHamburg.informatik.continuousvoice.services;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class OldAndroidRecognitionService implements RecognitionListener {

	private static final String TAG = "AndroidVoiceRecognitionService";
	private SpeechRecognizer speech = null; // Speech recognizer instance
	private Timer speechTimeout = null; // Timer used as timeout for recognition
	private Activity activity;

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public OldAndroidRecognitionService(Activity activity) {
		this.activity = activity;
		Log.i(TAG, "AndroidVoiceRecognitionService Constructor");
	}

//	@Override
//	public void start() {
//		startVoiceRecognitionCycle();
//	}
//
//	/**
//	 * Stop the voice recognition process and destroy the recognizer.
//	 */
//	@Override
//	public void stop() {
//		speechTimeout.cancel();
//	}
//
//	/**
//	 * Destroy the recognizer.
//	 */
//	@Override
//	public void reset() {
//		super.reset();
//
//		if (speech != null) {
//			speech.destroy();
//			speech = null;
//		}
//	}

	/*
	 * @Override public void onDestroy() { Log.i(TAG, "onDestroy"); }
	 * 
	 * @Override public int onStartCommand(Intent intent, int flags, int
	 * startId) { Log.i(TAG, "onStartCommand"); start(); return
	 * super.onStartCommand(intent, flags, startId); }
	 */
	// Lazy instantiation method for getting the speech recognizer
	private SpeechRecognizer getSpeechRecognizer() {
		if (speech == null) {
			speech = SpeechRecognizer.createSpeechRecognizer(activity);
			speech.setRecognitionListener(this);
		}

		return speech;
	}

	/**
	 * Fire an intent to start the voice recognition process.
	 */
	public void startVoiceRecognitionCycle() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		getSpeechRecognizer().startListening(intent);
	}

	// --> METHODS FROM INTERFACE android.speech.RecognitionListener
	@Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech");
        // create and schedule the input speech timeout
        speechTimeout = new Timer();
        speechTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                onError(SpeechRecognizer.ERROR_SPEECH_TIMEOUT);
            }
        }, 3000);
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int error) {
        String message;
        boolean restart = true;
        switch (error) {
        case SpeechRecognizer.ERROR_AUDIO:
            message = "Audio recording error";
            break;
        case SpeechRecognizer.ERROR_CLIENT:
            message = "Client side error";
            restart = false;
            break;
        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
            message = "Insufficient permissions";
            restart = false;
            break;
        case SpeechRecognizer.ERROR_NETWORK:
            message = "Network error";
            break;
        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            message = "Network timeout";
            break;
        case SpeechRecognizer.ERROR_NO_MATCH:
            message = "No match";
            break;
        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
            message = "RecognitionService busy";
            break;
        case SpeechRecognizer.ERROR_SERVER:
            message = "error from server";
            break;
        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            message = "No speech input";
            break;
        default:
            message = "Not recognised";
            break;
        }
        Log.d(TAG, "onError code:" + error + " message: " + message);

        // THIS WAS ASYNC - WHY?
        if (restart) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    getSpeechRecognizer().cancel();
                    startVoiceRecognitionCycle();
                }
            });
        }
    }

    @Override
    public void onResults(Bundle results) {
        // Restart new dictation cycle
        startVoiceRecognitionCycle();

        StringBuilder scores = new StringBuilder();
        for (int i = 0; i < results
                .getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES).length; i++) {
            scores.append(results
                    .getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)[i] + " ");
        }
        Log.d(TAG,
                "onResults: "
                        + results
                                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        + " scores: " + scores.toString());

        // Add results and notify
        if (results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) != null) {
            String chunk = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION).get(0);
            if (chunk != null) {
                //recognizedText += " " + chunk;
                //notifyListeners();
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }
	// <--

}