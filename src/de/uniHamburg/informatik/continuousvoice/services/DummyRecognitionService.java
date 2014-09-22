package de.uniHamburg.informatik.continuousvoice.services;

public class DummyRecognitionService extends AbstractRecognitionService {

    @Override
    protected void start() {
        super.start();
        
        long endTime = System.currentTimeMillis() + 5 * 1000;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                    addWords("this is a test", true);
                } catch (Exception e) {
                }
            }
        }
    }
}
