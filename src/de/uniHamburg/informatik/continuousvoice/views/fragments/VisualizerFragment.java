package de.uniHamburg.informatik.continuousvoice.views.fragments;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import de.uniHamburg.informatik.continousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.sound.analysis.SoundMeter;

public class VisualizerFragment extends Fragment {

    protected static final String TAG = VisualizerFragment.class.getCanonicalName();
    private ProgressBar progressBar;
    private SoundMeter soundMeter;
    private static final double precision = 1000.0;
    private ScheduledExecutorService scheduleTaskExecutor;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.visualizer, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.soundlevel);
        progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#333333"), Mode.SRC);
        progressBar.setMax((int)(SoundMeter.MAXIMUM_AMPLITUDE*precision));
        
        startMeasurement();

        return view;
    }

    private void startMeasurement() {
        if (soundMeter == null) {
            soundMeter = new SoundMeter();
        }
        soundMeter.start();
        if (scheduleTaskExecutor == null) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        }

        // This schedule a runnable task every 2 minutes
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                progressBar.setProgress((int)(soundMeter.getAmplitude()*precision));
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onPause() {
        if (scheduleTaskExecutor != null) {
            scheduleTaskExecutor.shutdown();
        }
        if (soundMeter != null) {
            soundMeter.stop();
            soundMeter = null;
        }
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        //startMeasurement();
    }
    
}