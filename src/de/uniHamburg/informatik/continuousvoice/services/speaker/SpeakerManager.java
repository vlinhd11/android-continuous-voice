package de.uniHamburg.informatik.continuousvoice.services.speaker;

import java.util.HashSet;
import java.util.Set;

import android.graphics.Color;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;

public class SpeakerManager {

    //CONFIG
    private Set<Speaker> speakers;
    private int counter = 0;
    private int[] colors = new int[] {
    		0xff1abc9c,
    		0xff9b59b6,
    		0xff2980b9,
    		0xffe74c3c,
    		0xff34495e,
    		0xff2ecc71,
    		0xffe67e22
    		//http://flatuicolors.com/
    };
    private ISpeakerRecognizer speakerRecognizer;
    private double maxDistance;
    public static final Speaker STATIC_SPEAKER = new Speaker(0, null, Color.GRAY);
    
    public SpeakerManager(ISpeakerRecognizer speakerRecognizer) {
        speakers = new HashSet<Speaker>();
        this.speakerRecognizer = speakerRecognizer;
        this.maxDistance = speakerRecognizer.getMaxDistance();
    }
    
    public SpeakerAssignResult assign(PcmFile file) {
        AbstractSpeakerFeature feature = speakerRecognizer.extractFeature(file);
        
        if (speakers.isEmpty()) {
            return new SpeakerAssignResult(addNewSpeaker(feature), 1.0);
        }
        
        Speaker bestMatch = null;
        double bestDistance = 0;
        for (Speaker s: speakers) {
            bestDistance = s.getReferenceFeature().getDistanceTo(feature);
            if (bestDistance <= maxDistance) {
                bestMatch = s;
                break;
            }
        }
        
        if (bestMatch == null) {
            return new SpeakerAssignResult(addNewSpeaker(feature), 1.0);
        } else {
            bestMatch.mergeReferenceFeature(feature);
            return new SpeakerAssignResult(bestMatch, 1.0 - bestDistance);
        }
    }
    
    private Speaker addNewSpeaker(AbstractSpeakerFeature feature) {
    	counter += 1;
        Speaker s = new Speaker(counter, feature, generateSpeakerColor());
        speakers.add(s);
        return s;
    }

    private int generateSpeakerColor() {
        int i = counter % colors.length;
        return colors[i];
    }

    public ISpeakerRecognizer getSpeakerRecognizer() {
        return speakerRecognizer;
    }
}
