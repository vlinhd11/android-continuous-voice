package de.uniHamburg.informatik.continuousvoice.views.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;

public class SpeechBubble extends Fragment {
	
	public static final short ORIENTATION_LEFT = 0;
	public static final short ORIENTATION_RIGHT = 1;
	private static final String TAG = "SpeechBubble";
	private short orientation;
	private String text = "";
	private Speaker speaker;
	private ImageView imageView;
	private SparseArray<ViewGroup> components = new SparseArray<ViewGroup>();
	private LinearLayout transcriptionsContainer;
	private Activity context;
	
	public SpeechBubble(Speaker speaker, Activity context) {
		this.speaker = speaker;
		this.orientation = (short) ((short) speaker.getIndex() % 2);
		this.context = context;
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

		transcriptionsContainer = (LinearLayout) view.findViewById(R.id.transcriptions);
        imageView = (ImageView) view.findViewById(R.id.speakerIcon);
        imageView.getDrawable().setColorFilter( speaker.getColor(), Mode.MULTIPLY);
        
        return view;
    }
	
	public void setTextForId(int id, String text) {
		//find layout manager with id from map
		ViewGroup vg = components.get(id);
		//remove existing components
		vg.removeAllViews();
		//create text view
		TextView textView = new TextView(getActivity());
		textView.setText(text);
		//add text view
		vg.addView(textView);
		//add text to var
		text += "\n " + text;
	}
	
	public void addPlaceholder(int id) {
		Log.e(TAG, id + " palceholder!");
		//add simple linear layout
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams linLayoutParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(linLayoutParam);
		//save in map with id
		components.put(id, layout);
		//add indeterminate progressbar to layout
		ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
		bar.setIndeterminate(true);
		bar.setVisibility(View.VISIBLE);
		bar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		layout.addView(bar);
		//add to main view
		layout.setVisibility(View.VISIBLE);

		transcriptionsContainer.addView(layout);
	}

	public boolean hasPlaceholder(int id) {
		return components.get(id) != null;
	}
	
}
