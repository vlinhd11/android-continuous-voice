package de.uniHamburg.informatik.continuousvoice.services.recognition.dummy;

import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;

public class DummyRecognizer extends AbstractRecognizer {

    @Override
    public void start() {
        setStatus("START!");
        
        setStatus("END!");
    }

    @Override
    public String getName() {
        return "Dummy Recognizer";
    }

    @Override
    public void initialize() {

    }

    @Override
    public void stop() {

        super.stop();
    }
    
    @Override
    public void shutdown() {
        
        super.shutdown();
    }
}
