package de.uniHamburg.informatik.continuousvoice.services.sound.analysis;

import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class SoundMeter {

    public static final double MAXIMUM_AMPLITUDE = (32768/2700.0); //from: http://stackoverflow.com/a/15613051/1686216
    private static final String TAG = SoundMeter.class.getCanonicalName();
    private MediaRecorder mRecorder = null;

    public SoundMeter() {
        mRecorder = createRecorder();
    }

    private MediaRecorder createRecorder() {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile("/dev/null");
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recorder;
    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = createRecorder();
        }
        mRecorder.start();
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
            return mRecorder.getMaxAmplitude() / 2700.0;
        } else {
            return 0;
        }
    }
}
