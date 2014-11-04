package de.uniHamburg.informatik.continuousvoice.views.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.constants.BroadcastIdentifiers;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.recognition.StatusListener;
import de.uniHamburg.informatik.continuousvoice.services.recognition.TranscriptionResultListener;
import de.uniHamburg.informatik.continuousvoice.services.recognition.builtIn.AndroidRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.recognition.webService.AttWebServiceRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.recognition.webService.GoogleWebServiceRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioService;

public class RecognizerFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private String minutesStringSchema;
    private String wordsStringSchema;
    private int seconds = 0;
    private String completeText = "";
    private int words = 0;

    private ImageButton playBtn;
    private ImageButton stopBtn;
    private ImageButton clearBtn;
    private ImageButton shareBtn;

    private Spinner serviceSpinner;
    private TextView timeText;
    private TextView statusTextLine1;
    private TextView statusTextLine2;
    private TextView wordCountText;
    private TextView contentText;
    private ScrollView scrollWrapper;

    private short currentState = STATE_1_READY;
    public static final short STATE_1_READY = 1;
    public static final short STATE_2_WORKING = 2;
    public static final short STATE_3_DONE = 3;

    private Handler handler;
    private boolean running = false;
    private Runnable timeUpdateRunner;
    private AudioService audioService;
    private List<AbstractRecognitionService> availableRecognizers;
    private AbstractRecognitionService currentRecognizer;  
    
    public RecognizerFragment(AudioService audioService) {
        this.audioService = audioService;
        
        timeUpdateRunner = new Runnable() {
            @Override
            public void run() {
                if (running) {
                    seconds++;
                    updateTimeText();
                    handler.postDelayed(this, 1000);
                }
            }
        };
        
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.voice_recognizer, container, false);

        playBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnPlay);
        stopBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnStop);
        clearBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnClear);
        shareBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnShare);

        timeText = (TextView) view.findViewById(R.id.voiceRecognizerTime);
        wordCountText = (TextView) view.findViewById(R.id.voiceRecognizerWordCount);
        contentText = (TextView) view.findViewById(R.id.voiceRecognizerContent);
        scrollWrapper = (ScrollView) view.findViewById(R.id.voiceRecognizerScrollWrapper);
        statusTextLine1 = (TextView) view.findViewById(R.id.voiceRecognizerState1);
        statusTextLine2 = (TextView) view.findViewById(R.id.voiceRecognizerState2);

        availableRecognizers = new ArrayList<AbstractRecognitionService>();
        availableRecognizers.add(new AndroidRecognitionService(getActivity(), audioService));
        availableRecognizers.add(new GoogleWebServiceRecognitionService(getString(R.string.googleApiKey), audioService));
        availableRecognizers.add(new AttWebServiceRecognitionService(getString(R.string.attApiOauthKey), audioService));
        
        serviceSpinner = (Spinner) view.findViewById(R.id.serviceSpinner);
        List<String> list = new ArrayList<String>();
        for (AbstractRecognitionService ars: availableRecognizers) {
            list.add(ars.getName());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        dataAdapter.setDropDownViewResource
                     (android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(dataAdapter);
        
        Resources res = getResources();
        minutesStringSchema = res.getString(R.string.minutes);
        wordsStringSchema = res.getString(R.string.words);

        createListeners();
        resetTexts();
        updateButtonState();
        

        return view;
    }

    private void switchService(AbstractRecognitionService newRecognizer) {
        if (currentRecognizer != null) {
            currentRecognizer.shutdown();
        }
        currentRecognizer = newRecognizer;
        initializeRecognizer(currentRecognizer);
    }
    
    private void initializeRecognizer(AbstractRecognitionService recognizer) {
        recognizer.initialize();
        recognizer.addTranscriptionListener(new TranscriptionResultListener() {
            @Override
            public void onTranscriptResult(String transcriptResult) {
                addTextToView(transcriptResult);
            }
        });
        recognizer.addStatusListener(new StatusListener() {
            @Override
            public void onStatusUpdate(String newStatus) {
                setStatus(newStatus);
            }
        });
    }
    
    
    
    private void startTimer() {
        running = true;
        handler.postDelayed(timeUpdateRunner, 1000);
    }

    private void stopTimer() {
        running = false;
    }

    private void resetTime() {
        seconds = 0;
        updateTimeText();
    }

    private void createListeners() {
        playBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        stopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        clearBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });
        shareBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        serviceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switchService(availableRecognizers.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void resetTexts() {
        contentText.setHint(R.string.fragment_hint);
        timeText.setText(String.format(minutesStringSchema, "00:00:00"));
        wordCountText.setText(String.format(wordsStringSchema, "0"));
    }

    private void updateWordCount() {
        wordCountText.setText(String.format(wordsStringSchema, words + ""));
    }

    private void updateTimeText() {
        int h = seconds / 3600;
        int min = (seconds % 3600) / 60;
        int sec = seconds % 60;
        String formattedTime = String.format("%02d:%02d:%02d", h, min, sec);
        timeText.setText(String.format(minutesStringSchema, formattedTime));
    }

    private void setStatus(final String newStatus) {
        statusTextLine2.setText(statusTextLine1.getText());
        statusTextLine1.setText(newStatus);

        //Animation inAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        //statusTextLine1.setAnimation(inAnim);
    }

    private void switchState(short currentState) {
        this.currentState = currentState;
        updateButtonState();
    }

    private void updateButtonState() {
        switch (this.currentState) {
        case STATE_1_READY:
            playBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            clearBtn.setVisibility(View.GONE);
            shareBtn.setVisibility(View.GONE);
            toggleSpinnerState(true);
            break;
        case STATE_2_WORKING:
            playBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);
            clearBtn.setVisibility(View.GONE);
            shareBtn.setVisibility(View.GONE);
            toggleSpinnerState(false);
            break;
        case STATE_3_DONE:
            playBtn.setVisibility(View.GONE);
            stopBtn.setVisibility(View.GONE);
            clearBtn.setVisibility(View.VISIBLE);
            shareBtn.setVisibility(View.VISIBLE);
            toggleSpinnerState(false);
            break;
        }
    }

    private void toggleSpinnerState(boolean b) {
        serviceSpinner.setClickable(b);
        serviceSpinner.setEnabled(b);
    }

    private void scrollDown() {
        scrollWrapper.post(new Runnable() {
            @Override
            public void run() {
                scrollWrapper.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void addTextToView(String toAdd) {
        contentText.append(" " + toAdd);
        completeText.replaceAll("\\s+", " ");
        words = completeText.trim().split("\\s+").length;
        updateWordCount();
        scrollDown();
    }

    public void play() {
        currentRecognizer.start();
        switchState(STATE_2_WORKING);
        resetTime();
        startTimer();
    }

    public void stop() {
        currentRecognizer.stop();
        switchState(STATE_3_DONE);
        stopTimer();
    }

    public void clear() {
        if (currentRecognizer.isRunning()) {
            currentRecognizer.stop();
        }
        
        switchState(STATE_1_READY);
        resetTime();
        completeText = "";
        words = 0;
        updateWordCount();
        contentText.setText("");
    }

    public void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, completeText);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onPause() {
        stop();
        currentRecognizer.shutdown();
        
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        stop();
        currentRecognizer.shutdown();

        super.onDestroy();
    }

}