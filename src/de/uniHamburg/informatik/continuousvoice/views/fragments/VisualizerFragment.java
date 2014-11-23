package de.uniHamburg.informatik.continuousvoice.views.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants.Loudness;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAudioServiceStartStopListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.IAudioService;
import de.uniHamburg.informatik.continuousvoice.services.speaker.ISpeakerChangeListener;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;

public class VisualizerFragment extends Fragment {

    protected static final String TAG = VisualizerFragment.class.getName();
    private static final int PROGRESSBAR_GRANULARITY = 1000;
    private Switch audioServiceSwitch;
    private Handler handler = new Handler();
    private IAudioService audioService;
	private ProgressBar progressBarLeft;
	private ImageView thresholdIconLeft;
	private ProgressBar progressBarRight;
	private ImageView thresholdIconRight;
	private ImageView switchSpeakerIcon;
	private ImageView recordingIcon;
	private float inactiveAlpha = 0.2f;
	private float activeAlpha = 1.0f;
    
    public VisualizerFragment(IAudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visualizer, container, false);

        audioServiceSwitch = (Switch) view.findViewById(R.id.audioServiceSwitch);
        audioServiceSwitch.setChecked(audioService.isRunning());
        progressBarLeft = (ProgressBar) view.findViewById(R.id.soundlevelLeft);
        thresholdIconLeft = (ImageView) view.findViewById(R.id.silenceStateLeft);
        
        recordingIcon = (ImageView) view.findViewById(R.id.recordingIcon);
        switchSpeakerIcon = (ImageView) view.findViewById(R.id.switchSpeakerIcon);
        
        progressBarRight = (ProgressBar) view.findViewById(R.id.soundlevelRight);
        thresholdIconRight = (ImageView) view.findViewById(R.id.silenceStateRight);
        
        progressBarRight.setMax(PROGRESSBAR_GRANULARITY); //percent
        progressBarLeft.setMax(PROGRESSBAR_GRANULARITY); //percent

        createListeners();
        
        recordingAnimation(false); //reset recording
        switchSpeakerIcon.setVisibility(View.VISIBLE);
        switchSpeakerIcon.setAlpha(inactiveAlpha);
        
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
				recordingAnimation(false);
            }

			@Override
            public void onSpeech() {
            	recordingAnimation(true);
            }

            @Override
            public void onAmplitudeUpdate(double percentLeft, double percentRight) {
            	progressBarLeft.setProgress((int) (percentLeft * PROGRESSBAR_GRANULARITY));
                progressBarRight.setProgress((int) (percentRight * PROGRESSBAR_GRANULARITY));
                
                if (percentLeft <= AudioConstants.SILENCE_AMPLITUDE_THRESHOLD) {
                	switchThresholdIcon(true, Loudness.SILENCE);
                } else {
                	switchThresholdIcon(true, Loudness.SPEECH);
                }

                if (percentRight <= AudioConstants.SILENCE_AMPLITUDE_THRESHOLD) {
                	switchThresholdIcon(false, Loudness.SILENCE);
                } else {
                	switchThresholdIcon(false, Loudness.SPEECH);
                }
            }
        });
        
        audioService.addSpeakerChangeListener(new ISpeakerChangeListener() {
			@Override
			public void onSpeakerChange(Speaker newSpeaker) {
				speakerAnimation(newSpeaker.getColor());
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

    private void switchThresholdIcon(final boolean left, Loudness state) {
        final int imageId;
        if (state == Loudness.SPEECH) {
            imageId = R.drawable.mic;
        } else {
            imageId = R.drawable.mic_muted;
        }
        handler.post(new Runnable() {
            public void run() {
            	ImageView thresholdIcon;
            	if (left) {
            		thresholdIcon = thresholdIconLeft;
            	} else {
            		thresholdIcon = thresholdIconRight;
            	}
                thresholdIcon.setImageDrawable(getResources().getDrawable(imageId));
            }
        });
    }

    private void recordingAnimation(final boolean active) {
    	handler.post(new Runnable() {
            public void run() {
            	if (active) {
            		recordingIcon.setImageDrawable(getResources().getDrawable(R.drawable.recording));
            		recordingIcon.setAlpha(activeAlpha);
            	} else {
            		recordingIcon.setImageDrawable(getResources().getDrawable(R.drawable.recording_off));
            		recordingIcon.setAlpha(inactiveAlpha);
            	}
            }
		});
	}

	private void speakerAnimation(final int color) {
		handler.post(new Runnable() {
            public void run() {
            	
				Animation fadeIn = new AlphaAnimation(inactiveAlpha, activeAlpha);
				fadeIn.setInterpolator(new DecelerateInterpolator());
				fadeIn.setDuration(250);
		
				Animation fadeOut = new AlphaAnimation(activeAlpha, inactiveAlpha);
				fadeOut.setInterpolator(new AccelerateInterpolator());
				fadeOut.setStartOffset(750);
				fadeOut.setDuration(250);
		
				final AnimationSet animation = new AnimationSet(false);
				animation.addAnimation(fadeIn);
				animation.addAnimation(fadeOut);
				
				animation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
						switchSpeakerIcon.setAlpha(activeAlpha);
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						switchSpeakerIcon.setAlpha(inactiveAlpha);
					}
				});
				
				switchSpeakerIcon.startAnimation(animation);
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