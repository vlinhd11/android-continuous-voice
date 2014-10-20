package de.uniHamburg.informatik.continuousvoice.services.sound.recorder;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;

public class SoundRecordingService extends AbstractRecognitionService {

    private static final String TAG = SoundRecordingService.class.getCanonicalName();
    private MediaRecorder currentRecorder;
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

    public void terminate() {
        terminateRecorder(currentRecorder);
    }

    public File getCurrentFile() {
        File file = new File(currentFileName);
        return file;
    }

    /**
     * Stops the current recorder, releases and returns the file but keeps on
     * recording to a new file until stopAndGetFile() oder split() is called.
     * 
     * @return the recorded file
     */
    public File split() {
        // 1 terminate current recorder
        terminateRecorder(currentRecorder);
        // 2 create file
        File file = new File(currentFileName);
        // 3 start next recorder immediately
        startRecorder(nextRecorder);
        // 4 create new next recorder and filename
        String newFileName = getNewFileName();
        MediaRecorder newRecorder = createRecorder(newFileName);
        // 5 switch recorders and filenames
        currentRecorder = nextRecorder;
        currentFileName = nextFileName;
        nextRecorder = newRecorder;
        nextFileName = newFileName;
        // 6 return file
        return file;
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
            if (recorder != null) {
                recorder.stop();
                recorder.reset();
                recorder.release();
                running = false;
                // DEBUG
                Log.i(TAG, "stopped recording - file: " + currentFileName + " (exists: " + new File(currentFileName).exists() + ")");
            }
        } catch(IllegalStateException e) {
            //already stopped / not started yet
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
}
