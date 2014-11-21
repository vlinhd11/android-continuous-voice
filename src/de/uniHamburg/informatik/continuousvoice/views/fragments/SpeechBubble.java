package de.uniHamburg.informatik.continuousvoice.views.fragments;

import android.app.Fragment;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerAssignResult;

public class SpeechBubble extends Fragment {
	
	public static final short ORIENTATION_LEFT = 0;
	public static final short ORIENTATION_RIGHT = 1;
	private short orientation;
	private String text;
	private SpeakerAssignResult speaker;
	private TextView textView;
	private ImageView imageView;
	
	
	public SpeechBubble(SpeakerAssignResult speaker, String text) {
		this.speaker = speaker;
		this.text = text;
		this.orientation = (short) ((short) speaker.getSpeaker().getIndex() % 2);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view;
		switch (orientation) {
		case ORIENTATION_LEFT:
			view = inflater.inflate(R.layout.speech_bubble_left, container, false);
			break;
		case ORIENTATION_RIGHT:
		default:
			view = inflater.inflate(R.layout.speech_bubble_right, container, false);
			break;
		}

        imageView = (ImageView) view.findViewById(R.id.speakerIcon);
        textView = (TextView) view.findViewById(R.id.text);
        textView.setText(text);
        imageView.getDrawable().setColorFilter( speaker.getSpeaker().getColor(), Mode.MULTIPLY);

        return view;
    }
	
	public void addText(String text) {
		textView.append("\n" + text);
	}
	
}
