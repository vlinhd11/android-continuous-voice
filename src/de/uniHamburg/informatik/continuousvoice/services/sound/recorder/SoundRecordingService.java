package de.uniHamburg.informatik.continuousvoice.services.sound.recorder;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
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
            
            File file = new File(currentFileName);
            Log.i(TAG, "stopped recording - file: " +
                    currentFileName +
                    " (exists: " +
                    file.exists() +
                    ")");            
            currentFileName = null;
        }
    }
    
    public File stopAndReturnFile() {
        File file = new File(currentFileName);
        stop();
        return file;
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
        File dir;
        //public dir
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //private dir
        //dir = getApplicationContext().getExternalFilesDir(null);
        String basePath = dir.getAbsolutePath();
        String suffix = "amr";

        String msg = basePath + "/" + baseFileName + "_" + recorderIteration + "." + suffix;
        return msg;
    }
}
