package de.uniHamburg.informatik.continuousvoice.services.recognition;

import java.util.ArrayList;
import java.util.List;

import de.uniHamburg.informatik.continuousvoice.settings.GeneralSettings;
import de.uniHamburg.informatik.continuousvoice.settings.Language;
import de.uniHamburg.informatik.continuousvoice.settings.SettingsChangedListener;

public abstract class AbstractRecognizer implements IRecognizerControl {

    private final String TAG = this.getClass().getSimpleName();
    protected boolean running = false;
    private String recognizedText = "";
    private List<ITranscriptionResultListener> transcriptionResultListeners;
    private List<IStatusListener> statusListeners;
    protected final GeneralSettings settings;

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

    /**
     * override this method if needed but then remember to call super.start();
     */
    public void start() {
        running = true;
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

    protected void addWords(String words) {
        recognizedText += " " + words;
        
        for (ITranscriptionResultListener trl: transcriptionResultListeners) {
            trl.onTranscriptResult(words);
        }
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