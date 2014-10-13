package de.uniHamburg.informatik.continuousvoice.services.recognition.dummy;

import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognitionService;

public class DummyRecognitionService extends AbstractRecognitionService {

    @Override
    protected void onStart() {
        super.onStart();
        
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
