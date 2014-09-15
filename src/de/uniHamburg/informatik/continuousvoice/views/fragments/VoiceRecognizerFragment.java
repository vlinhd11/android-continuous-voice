package de.uniHamburg.informatik.continuousvoice.views.fragments;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import de.uniHamburg.informatik.continousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.IVoiceRecognitionService;

public class VoiceRecognizerFragment extends Fragment {

	private String name;
	private IVoiceRecognitionService recognitionService;
	private Resources res;
	private String minutesStringSchema;
	private String wordsStringSchema;
	
	private ImageButton playBtn;
	private ImageButton stopBtn;
	private ImageButton clearBtn;
	private ImageButton shareBtn;
	
	private TextView titleText;
	private TextView timeText;
	private TextView wordCountText;
	
	private short currentState = STATE_1_READY; 
	public static final short STATE_1_READY = 1;
	public static final short STATE_2_WORKING = 2;
	public static final short STATE_3_DONE = 3;
	
	public VoiceRecognizerFragment(String name, IVoiceRecognitionService recognitionService) {
		this.name = name;
		this.recognitionService = recognitionService;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.voice_recognizer, container, false);

        playBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnPlay);
        stopBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnStop);
        clearBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnClear);
        shareBtn = (ImageButton) view.findViewById(R.id.voiceRecognizerBtnShare);
        
        titleText = (TextView) view.findViewById(R.id.voiceRecognizerTitle);
        timeText = (TextView) view.findViewById(R.id.voiceRecognizerTime);
        wordCountText = (TextView) view.findViewById(R.id.voiceRecognizerWordCount);
        
        Resources res = getResources();
        minutesStringSchema = res.getString(R.string.minutes);
        wordsStringSchema = res.getString(R.string.words);
        
        createListeners();
        resetTexts();
        updateButtonState();
        
        return view;
    }
    
    private void createListeners() {
    	playBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				play(v);
			}
		});
    	stopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stop(v);
			}
		});
    	clearBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clear(v);
			}
		});
    	shareBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				share(v);
			}
		});
	}
    
    private void resetTexts() {
    	titleText.setText(name);
    	timeText.setText(String.format(minutesStringSchema, "0:00"));
    	wordCountText.setText(String.format(wordsStringSchema, "0"));
    }

    private void switchState(short currentState) {
    	this.currentState = currentState;
    	updateButtonState();
    }
    
	private void updateButtonState() {
    	switch(this.currentState) {
    	case STATE_1_READY:
    		playBtn.setVisibility(View.VISIBLE);
    		stopBtn.setVisibility(View.GONE);
    		clearBtn.setVisibility(View.GONE);
    		shareBtn.setVisibility(View.GONE);
    		break;
    	case STATE_2_WORKING:
    		playBtn.setVisibility(View.GONE);
    		stopBtn.setVisibility(View.VISIBLE);
    		clearBtn.setVisibility(View.GONE);
    		shareBtn.setVisibility(View.GONE);
    		break;
    	case STATE_3_DONE:
    		playBtn.setVisibility(View.GONE);
    		stopBtn.setVisibility(View.GONE);
    		clearBtn.setVisibility(View.VISIBLE);
    		shareBtn.setVisibility(View.VISIBLE);
    		break;
    	}
    }
    
    public void play(View view) {
    	switchState(STATE_2_WORKING);
    	Toast.makeText(getActivity(), "PLAY " + getId(), Toast.LENGTH_SHORT).show();
    }
    
    public void stop(View view) {
    	switchState(STATE_3_DONE);
    	Toast.makeText(getActivity(), "STOP " + getId(), Toast.LENGTH_SHORT).show();
    }
    
    public void clear(View view) {
    	switchState(STATE_1_READY);
    	Toast.makeText(getActivity(), "CLEAR " + getId(), Toast.LENGTH_SHORT).show();
    }
    
    public void share(View view) {
    	Toast.makeText(getActivity(), "SHARE " + getId(), Toast.LENGTH_SHORT).show();
    }
    
    public short getCurrentState() {
    	return this.currentState;
    }
    
}