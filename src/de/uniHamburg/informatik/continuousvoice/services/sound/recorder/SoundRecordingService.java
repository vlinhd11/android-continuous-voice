package de.uniHamburg.informatik.continuousvoice.services.sound.recorder;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class SoundRecordingService extends Service {

    private static final String TAG = SoundRecordingService.class.getName();
    public MediaRecorder currentRecorder; //TODO private
    private MediaRecorder nextRecorder;
    private String currentFileName;
    private String nextFileName;

    private int recorderIteration = 0;
    private String baseFileName;
    private boolean running = false;

    public SoundRecordingService(String baseFileName) {
        this.baseFileName = baseFileName;

        currentFileName = getNewFileName();
        currentRecorder = createRecorder(currentFileName);

        nextFileName = getNewFileName();
        nextRecorder = createRecorder(nextFileName);
    }

    public void start() {
        startRecorder(currentRecorder);
    }

    /**
     * Shuts down this recorder. You have to create a new instance of this class if you plan to continue.
     * @return the last file.
     */
    public File shutdownAndRelease() {
        terminateRecorder(currentRecorder);
        File file = new File(currentFileName);
        return file;
    }

    /**
     * Stops the current recorder, releases and returns the file but keeps on
     * recording to a new file until terminate() or split() is called.
     * 
     * @return the recorded file
     */
    public File split(boolean startDirectly) {
        // 1 terminate current recorder
        terminateRecorder(currentRecorder);
        // 2 create file
        File file = new File(currentFileName);
        // 3 start next recorder immediately
        if (startDirectly) {
            startRecorder(nextRecorder);
        }
        // 4 switch recorders and filenames
        switchRecorders();
        // 5 return file
        return file;
    }

    private void switchRecorders() {
        // a) create new next recorder and filename
        String newFileName = getNewFileName();
        MediaRecorder newRecorder = createRecorder(newFileName);
        // b) switch recorders and filenames
        currentRecorder = nextRecorder;
        currentFileName = nextFileName;
        nextRecorder = newRecorder;
        nextFileName = newFileName;
    }

    private MediaRecorder createRecorder(String fileName) {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recorder;
    }

    private String getNewFileName() {
        recorderIteration++;
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String basePath = dir.getAbsolutePath();
        String suffix = "amr";
        String path = basePath + "/" + baseFileName + "_" + recorderIteration + "." + suffix;
        return path;
    }

    private void terminateRecorder(MediaRecorder recorder) {
        try {
            if (recorder != null && running) {
                recorder.stop();
                recorder.reset();
                recorder.release();
                running = false;
                // DEBUG
                Log.w(TAG, "stopped recording - file: " + currentFileName + " (exists: " + new File(currentFileName).exists() + ")");
            }
        } catch(IllegalStateException e) {
            //already stopped / not started yet
            terminateRecorder(recorder); //TODO ugly: infinite loop possible!
        }
    }

    private void startRecorder(MediaRecorder recorder) {
        try {
            if (!running && recorder != null) {
                recorder.start();
                running = true;
            }
        } catch(IllegalStateException e) {
            //already started
            Log.e(TAG, "started? " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
