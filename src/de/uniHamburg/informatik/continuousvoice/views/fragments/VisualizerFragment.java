package de.uniHamburg.informatik.continuousvoice.views.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.uniHamburg.informatik.continousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.visualizer.CDrawer;
import de.uniHamburg.informatik.continuousvoice.services.visualizer.CSampler;
import de.uniHamburg.informatik.continuousvoice.services.visualizer.IBufferReceiver;

public class VisualizerFragment extends Fragment {

    private CDrawer.CDrawThread mDrawThread;
    private CDrawer mdrawer;

    private View.OnClickListener listener;
    private Boolean m_bStart = Boolean.valueOf(false);
    private Boolean recording;
    private CSampler sampler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.visualizer, container, false);

        mdrawer = (CDrawer) view.findViewById(R.id.drawer);
        m_bStart = Boolean.valueOf(false);

        recording = false;
        run();

        return view;
    }

    /**
     * Pause the visualizer when the app is paused
     */
    @Override
    public void onPause() {
        System.out.println("onPause");
        sampler.SetRun(Boolean.valueOf(false));
        mDrawThread.setRun(Boolean.valueOf(false));
        sampler.SetSleeping(Boolean.valueOf(true));
        mDrawThread.SetSleeping(Boolean.valueOf(true));
        Boolean.valueOf(false);
        super.onPause();
    }

    /**
     * Resume the visualizer when the app resumes
     */
    @Override
    public void onResume() {
        System.out.println("onResume");
        int i = 0;
        while (true) {
            if ((sampler.GetDead2().booleanValue()) && (mdrawer.GetDead2().booleanValue())) {
                System.out.println(sampler.GetDead2() + ", " + mdrawer.GetDead2());
                sampler.Restart();
                if (!m_bStart.booleanValue())
                    mdrawer.Restart(Boolean.valueOf(true));
                sampler.SetSleeping(Boolean.valueOf(false));
                mDrawThread.SetSleeping(Boolean.valueOf(false));
                m_bStart = Boolean.valueOf(false);
                super.onResume();
                return;
            }
            try {
                Thread.sleep(500L);
                System.out.println("Hang on..");
                i++;
                if (!sampler.GetDead2().booleanValue())
                    System.out.println("sampler not DEAD!!!");
                if (!mdrawer.GetDead2().booleanValue()) {
                    System.out.println("mDrawer not DeAD!!");
                    mdrawer.SetRun(Boolean.valueOf(false));
                }
                if (i <= 4)
                    continue;
                mDrawThread.SetDead2(Boolean.valueOf(true));
            } catch (InterruptedException localInterruptedException) {
                localInterruptedException.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        System.out.println("onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        System.out.println("onStop");
        super.onStop();
    }

    /**
     * Recives the buffert from the sampler
     * 
     * @param buffert
     */
    public void setBuffer(short[] paramArrayOfShort) {
        mDrawThread = mdrawer.getThread();
        mDrawThread.setBuffer(paramArrayOfShort);
    }

    /**
     * Called by OnCreate to get everything up and running
     */
    public void run() {
        try {
            if (mDrawThread == null) {
                mDrawThread = mdrawer.getThread();
            }
            if (sampler == null) {
                sampler = new CSampler(new IBufferReceiver() {
                    public void receiveBuffer(short[] paramArrayOfShort) {
                        VisualizerFragment.this.setBuffer(paramArrayOfShort);
                    }
                });
            }

            mdrawer.setOnClickListener(listener);
            if (sampler != null) {
                try {
                    sampler.Init();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sampler.StartRecording();
                sampler.StartSampling();
            }
        } catch (NullPointerException e) {
            Log.e("Main_Run", "NullPointer: " + e.getMessage());
        }
    }

}