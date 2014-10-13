package de.uniHamburg.informatik.continuousvoice.services;

public interface IServiceControl {

    public void start();

    public boolean isRunning();

    public void stop();

    public void reset();

}