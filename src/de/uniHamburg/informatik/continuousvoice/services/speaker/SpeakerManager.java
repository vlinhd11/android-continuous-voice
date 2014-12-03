package de.uniHamburg.informatik.continuousvoice.services.speaker;

import java.util.HashSet;
import java.util.Set;

import android.graphics.Color;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;

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
    private boolean fixed = false;
    public static final Speaker STATIC_SPEAKER = new Speaker(0, null, Color.GRAY);
    private static final String TAG = "SpeakerManager";
    
    public SpeakerManager() {
        speakers = new HashSet<Speaker>();
    }
    
    /**
     * fixed speaker manager with given speakers - no new speakers will be added
     */
    public SpeakerManager(Speaker... fixedSpeakers) {
        speakers = new HashSet<Speaker>();
        for (Speaker s: fixedSpeakers) {
            speakers.add(s);
        }
        fixed = true;
    }
    
    public Speaker assign(AbstractSpeakerFeature feature, boolean merge) {
        Speaker speaker = null;

        if (fixed) {
            double lastBestDistance = Double.MAX_VALUE;
            for (Speaker s: speakers) {
                double currentDistance = s.getReferenceFeature().getDistanceTo(feature);
                if (currentDistance < lastBestDistance) {
                    speaker = s;
                    lastBestDistance = currentDistance;
                }
            }
            //Log.i(TAG, "Best match: " + speaker.toString() + ": " + lastBestDistance);
        } else {
            
            if (speakers.isEmpty()) {
                return addNewSpeaker(feature);
            }
            
            Speaker bestMatch = null;
            double bestDistance = 0;
            for (Speaker s: speakers) {
                bestDistance = s.getReferenceFeature().getDistanceTo(feature);
                if (bestDistance <= AudioConstants.MAX_SPEAKER_DISTANCE) {
                    bestMatch = s;
                    break;
                }
            }
            
            if (bestMatch == null) {
                speaker = addNewSpeaker(feature);
            } else {
            	if (merge) {
            		bestMatch.mergeReferenceFeature(feature);
            	}
                speaker = bestMatch;
            }
        }
        
        return speaker;
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
}
