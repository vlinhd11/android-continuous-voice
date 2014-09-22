package de.uniHamburg.informatik.continuousvoice.services;

public interface IRecognitionControl {

    public void start();

    public boolean isRunning();

    public void stop();

    public void reset();

}