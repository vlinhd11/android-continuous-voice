package de.uniHamburg.informatik.continuousvoice.services.recognition;

import java.util.ArrayList;
import java.util.List;

import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerManager;
import de.uniHamburg.informatik.continuousvoice.settings.GeneralSettings;

public abstract class AbstractRecognizer implements IRecognizerControl {

    private final String TAG = this.getClass().getSimpleName();
    protected boolean running = false;
    private String recognizedText = "";
    protected List<ITranscriptionResultListener> transcriptionResultListeners;
    private List<IStatusListener> statusListeners;
    protected final GeneralSettings settings;
    protected int currentTranscriptionId = 0;

    public AbstractRecognizer() {
        statusListeners = new ArrayList<IStatusListener>();
        transcriptionResultListeners = new ArrayList<ITranscriptionResultListener>();
        settings = GeneralSettings.getInstance();
    }

    /**
     * override this method if needed but then remember to call super.stop();
     */
    public void stop() {
        running = false;
    }

    public int startTranscription() {
        running = true;
        
        currentTranscriptionId++;
    	
    	for (ITranscriptionResultListener trl: transcriptionResultListeners) {
            trl.onTranscriptionStart(currentTranscriptionId);
        }
    	return currentTranscriptionId;
    }

    @Deprecated
    protected void finishTranscription(int id, String words) {
    	finishTranscription(id, words, SpeakerManager.STATIC_SPEAKER);
    }
    
    protected void finishTranscription(int id, String words, Speaker s) {
    	recognizedText += " " + words;
    	
    	for (ITranscriptionResultListener trl: transcriptionResultListeners) {
    		trl.onTranscriptResult(id, words, (!words.trim().equals("(?)")), s);
    	}
    }

    /**
     * override this method if needed but then remember to call super.reset();
     */
    public void reset() {
        stop();
        recognizedText = "";
        clearStatus();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public void clearStatus() {
        setStatus("");
        setStatus("");
    }
    
    protected void setStatus(String status) {
        for (IStatusListener sl: statusListeners) {
            sl.onStatusUpdate(status);
        }
    }

    /**
     * @return the complete text since the last reset
     */
    public String getRecognizedText() {
        return recognizedText;
    }
    public abstract String getName();
    
    public void addStatusListener(IStatusListener sl) {
        statusListeners.add(sl);
    }
    
    public void addTranscriptionListener(ITranscriptionResultListener trl) {
        transcriptionResultListeners.add(trl);
    }
    
    @Override
    public void shutdown() {
        statusListeners.clear();
        transcriptionResultListeners.clear();
    }
}