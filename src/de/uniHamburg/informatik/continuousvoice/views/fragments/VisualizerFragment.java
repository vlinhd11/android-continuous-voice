package de.uniHamburg.informatik.continuousvoice.views.fragments;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.uniHamburg.informatik.continuousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.sound.analysis.SoundMeter;

public class VisualizerFragment extends Fragment {

    protected static final String TAG = VisualizerFragment.class.getCanonicalName();
    private ProgressBar progressBar;
    private TextView aplitudeText;
    private SoundMeter soundMeter;
    private static final double precision = 1000.0;
    private ScheduledExecutorService scheduleTaskExecutor;
    private double max;
    private Handler handler;
    private boolean running = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.visualizer, container, false);

        aplitudeText = (TextView) view.findViewById(R.id.amplitudeText);
        progressBar = (ProgressBar) view.findViewById(R.id.soundlevel);
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
                final int percent = (int) ((amp * 100.0) / max);
                //Android Dev Rule 42: Use a handler to update UI stuff from a timer task
                handler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress((int) amp);
                        aplitudeText.setText(percent + "%");
                    }
                });
            }
        }, 0, 80, TimeUnit.MILLISECONDS);
        running = true;
    }

    @Override
    public void onPause() {
        if (scheduleTaskExecutor != null) {
            scheduleTaskExecutor.shutdown();
            scheduleTaskExecutor = null;
            Log.i(TAG, "timer: shutdown");
        }
        if (soundMeter != null) {
            soundMeter.stop();
            running = false;
            Log.i(TAG, "soundmeter: stop");
        }
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