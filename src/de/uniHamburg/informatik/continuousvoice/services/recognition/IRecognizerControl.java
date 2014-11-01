package de.uniHamburg.informatik.continuousvoice.services.recognition;

public interface IRecognizerControl {

    public void initialize();

    public void start();

    public boolean isRunning();

    public void stop();

    public void shutdown();
    
    public void addStatusListener(StatusListener sl);
    
    public void addTranscriptionListener(TranscriptionResultListener trl);

}