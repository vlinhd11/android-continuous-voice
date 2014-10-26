package de.uniHamburg.informatik.continuousvoice.views.fragments;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.constants.BroadcastIdentifiers;
import de.uniHamburg.informatik.continuousvoice.services.sound.analysis.SilenceListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.analysis.SoundMeter;

public class VisualizerFragment extends Fragment {

    protected static final String TAG = VisualizerFragment.class.getName();
    private ProgressBar progressBar;
    private SoundMeter soundMeter;
    private static final double precision = 1000.0;
    private ScheduledExecutorService scheduleTaskExecutor;
    private double max;
    private Handler handler;
    private boolean running = false;
    private ImageView recordingIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.visualizer, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.soundlevel);
        recordingIcon = (ImageView) view.findViewById(R.id.silenceState);
        max = SoundMeter.MAXIMUM_AMPLITUDE * precision;
        progressBar.setMax(((int) max) + 1);
        handler = new Handler();
        soundMeter = new SoundMeter();
        startMeasurement();

        return view;
    }

    private void startMeasurement() {
        soundMeter.start();
        if (scheduleTaskExecutor == null) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        }

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                final double amp = soundMeter.getAmplitude() * precision;
                progressBar.setProgress((int) amp);
            }
        }, 0, 80, TimeUnit.MILLISECONDS);
        
        running = true;
        
        soundMeter.addSilenceListener(new SilenceListener() {
            @Override
            public void onSilence() {
                switchRecordingIcon(SoundMeter.SILENT);
                sendBroadcast(SoundMeter.SILENT);
            }
            
            @Override
            public void onSpeech() {
                switchRecordingIcon(SoundMeter.LOUD);
                sendBroadcast(SoundMeter.LOUD);
            }
        });
    }

    private void sendBroadcast(int state) {
        Intent i = new Intent(BroadcastIdentifiers.SILENCE_BROADCAST);
        i.putExtra("SILENCE", (state == SoundMeter.SILENT));
        i.putExtra("LOUD", (state == SoundMeter.LOUD));
        getActivity().sendBroadcast(i);
    }

    private void switchRecordingIcon(int state) {
        final int imageId;
        if (state == SoundMeter.LOUD) {
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
        if (scheduleTaskExecutor != null) {
            scheduleTaskExecutor.shutdownNow();
            scheduleTaskExecutor = null;
        }
        if (soundMeter != null) {
            soundMeter.stop();
        }
        running = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!running) {
            startMeasurement();
        }
    }

}