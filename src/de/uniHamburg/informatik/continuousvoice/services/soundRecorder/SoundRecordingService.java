package de.uniHamburg.informatik.continuousvoice.services.soundRecorder;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;

public class SoundRecordingService extends AbstractRecognitionService {

    private static final String TAG = "SoundRecordingService";
    private MediaRecorder currentRecorder;
    private int recorderIteration = 0;
    private String baseFileName;
    private String currentFileName;

    public SoundRecordingService(String baseFileName) {
        this.baseFileName = baseFileName;
    }

    public void record() {
        try {
            if (currentRecorder == null) {
                currentRecorder = createRecorder();
            }
            currentRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (currentRecorder != null) {
            currentRecorder.stop();
            currentRecorder.reset();
            currentRecorder.release();

            Log.e(TAG, "stopped -file: " + currentFileName);            
            
            currentFileName = null;
        }
    }

    private MediaRecorder createRecorder() throws IllegalStateException, IOException {
        stop(); // release if recorder is not null

        currentFileName = getNewFileName();
        
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(currentFileName);
        recorder.prepare();

        return recorder;
    }

    private String getNewFileName() {
        recorderIteration++;
        File dir = Environment.getExternalStorageDirectory(); 
        String basePath = dir.getAbsolutePath();
        String suffix = "3gp";

        String msg = basePath + "/" + baseFileName + "_" + recorderIteration + "." + suffix;
        Log.e(TAG, msg);
        return msg;
    }
}
