package de.uniHamburg.informatik.continuousvoice.views.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.Loudness;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.SpeakerPosition;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAudioServiceStartStopListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.IAudioService;

public class VisualizerFragment extends Fragment {

    protected static final String TAG = VisualizerFragment.class.getName();
    private static final int PROGRESSBAR_GRANULARITY = 1000;
    private Switch audioServiceSwitch;
    private Handler handler = new Handler();
    private IAudioService audioService;
	private ProgressBar progressBarLeft;
	private ImageView recordingIconLeft;
	private ProgressBar progressBarRight;
	private ImageView recordingIconRight;
    
    public VisualizerFragment(IAudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visualizer, container, false);

        audioServiceSwitch = (Switch) view.findViewById(R.id.audioServiceSwitch);
        audioServiceSwitch.setChecked(audioService.isRunning());
        progressBarLeft = (ProgressBar) view.findViewById(R.id.soundlevelLeft);
        recordingIconLeft = (ImageView) view.findViewById(R.id.silenceStateLeft);
        
        progressBarRight = (ProgressBar) view.findViewById(R.id.soundlevelRight);
        recordingIconRight = (ImageView) view.findViewById(R.id.silenceStateRight);
        
        progressBarRight.setMax(PROGRESSBAR_GRANULARITY); //percent
        progressBarLeft.setMax(PROGRESSBAR_GRANULARITY); //percent

        createListeners();

        return view;
    }

    private void createListeners() {
        audioServiceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!audioService.isRunning()) {
                        audioService.initialize();
                    }
                } else {
                    if (audioService.isRunning()) {
                        audioService.shutdown();
                        progressBarRight.setProgress(0);
                        progressBarLeft.setProgress(0);
                    }
                }
            }
        });

        audioService.addAmplitudeListener(new IAmplitudeListener() {
            @Override
            public void onSilence() {
            }

            @Override
            public void onSpeech() {
            }

            @Override
            public void onAmplitudeUpdate(double percentLeft, double percentRight) {
                progressBarLeft.setProgress((int) (percentLeft * PROGRESSBAR_GRANULARITY));
                progressBarRight.setProgress((int) (percentRight * PROGRESSBAR_GRANULARITY));
                
                if (percentLeft <= AudioConstants.SILENCE_AMPLITUDE_THRESHOLD) {
                	switchRecordingIcon(true, Loudness.SILENCE);
                } else {
                	switchRecordingIcon(true, Loudness.SPEECH);
                }

                if (percentRight <= AudioConstants.SILENCE_AMPLITUDE_THRESHOLD) {
                	switchRecordingIcon(false, Loudness.SILENCE);
                } else {
                	switchRecordingIcon(false, Loudness.SPEECH);
                }
            }

			@Override
			public void onSpeakerChange(SpeakerPosition position) {
			}
        });
        
        audioService.addStartStopListener(new IAudioServiceStartStopListener() {
            @Override
            public void onAudioServiceStateChange() {
                handler.post(new Runnable() {
                    public void run() {
                        audioServiceSwitch.setChecked(audioService.isRunning());
                    }
                });
            }
        });
    }

    private void switchRecordingIcon(final boolean left, Loudness state) {
        final int imageId;
        if (state == Loudness.SPEECH) {
            imageId = R.drawable.mic;
        } else {
            imageId = R.drawable.mic_muted;
        }
        handler.post(new Runnable() {
            public void run() {
            	ImageView recordingIcon;
            	if (left) {
            		recordingIcon = recordingIconLeft;
            	} else {
            		recordingIcon = recordingIconRight;
            	}
                recordingIcon.setImageDrawable(getResources().getDrawable(imageId));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}