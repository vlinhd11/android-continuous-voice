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
import android.widget.TextView;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.sound.AmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioService;

public class VisualizerFragment extends Fragment {

    protected static final String TAG = VisualizerFragment.class.getName();
    private static final int PROGRESSBAR_GRANULARITY = 1000;
    private ProgressBar progressBar;
    private ImageView recordingIcon;
    private TextView amplitudeText;
    private Switch audioServiceSwitch;
    private Handler handler = new Handler();
    private AudioService audioService;
    
    public VisualizerFragment(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visualizer, container, false);

        audioServiceSwitch = (Switch) view.findViewById(R.id.audioServiceSwitch);
        audioServiceSwitch.setActivated(audioService.isRunning());
        //amplitudeText = (TextView) view.findViewById(R.id.amplitudeText);
        progressBar = (ProgressBar) view.findViewById(R.id.soundlevel);
        recordingIcon = (ImageView) view.findViewById(R.id.silenceState);
        progressBar.setMax(PROGRESSBAR_GRANULARITY); //percent

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
                        progressBar.setProgress(0);
                    }
                }
            }
        });

        audioService.addAmplitudeListener(new AmplitudeListener() {
            @Override
            public void onSilence() {
                switchRecordingIcon(AudioService.State.SILENCE);
            }

            @Override
            public void onSpeech() {
                switchRecordingIcon(AudioService.State.SPEECH);
            }

            @Override
            public void onAmplitudeUpdate(double percent) {
                progressBar.setProgress((int) (percent * PROGRESSBAR_GRANULARITY));
            }
        });
    }

    private void switchRecordingIcon(AudioService.State state) {
        final int imageId;
        if (state == AudioService.State.SPEECH) {
            imageId = R.drawable.mic;
        } else {
            imageId = R.drawable.mic_muted;
        }
        handler.post(new Runnable() {
            public void run() {
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