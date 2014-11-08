package de.uniHamburg.informatik.continuousvoice.services.recognition.dummy;

import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;

public class DummyRecognitionService extends AbstractRecognitionService {

    @Override
    public void start() {
        super.start();
        
        addWords("start a timer: ");
        long endTime = System.currentTimeMillis() + 5 * 1000;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                    addWords(" 5sec passed");
                } catch (Exception e) {
                }
            }
        }
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
