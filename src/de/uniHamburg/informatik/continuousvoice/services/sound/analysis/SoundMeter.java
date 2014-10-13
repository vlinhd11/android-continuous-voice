package de.uniHamburg.informatik.continuousvoice.services.sound.analysis;

import java.io.IOException;

import android.media.MediaRecorder;

public class SoundMeter {

    public static final double MAXIMUM_AMPLITUDE = (32768/2700.0); //from: http://stackoverflow.com/a/15613051/1686216
    private MediaRecorder mRecorder = null;

    public SoundMeter() {
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (mRecorder != null) {
            mRecorder.start();
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null) {
            return (mRecorder.getMaxAmplitude() / 2700.0);
        } else {
            return 0;
        }
    }
}
