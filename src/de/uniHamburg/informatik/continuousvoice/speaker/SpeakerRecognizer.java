package de.uniHamburg.informatik.continuousvoice.speaker;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.uniHamburg.informatik.continuousvoice.speaker.recognito.Recognito;

import android.graphics.Color;

public class SpeakerRecognizer {

    //CONFIG
    private static final double MAX_DIST = 1.0;
    
    private Recognito<Integer> recognito;
    private Set<Speaker> speakers;
    private int counter = 0;
    private int[] colors = new int[] {Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.WHITE};
    
    public SpeakerRecognizer() {
        speakers = new HashSet<Speaker>();
        recognito = new Recognito<Integer>(8000f);
    }
    
    public Speaker assign(File soundfile) {
        //recognito.createVoicePrint(counter, soundfile);
        
        //Recognito call here!!!
        return null;
    }
    
    private Speaker getSpeakerForVoicePrint(double[][] voiceprint) {
        if (speakers.isEmpty()) {
            return addNewSpeaker(voiceprint);
        }
        
        Speaker bestMatch = null;
        double currentDist = Double.MAX_VALUE;
        for (Speaker s: speakers) {
            if (getDistance(s.getReferenceMatrix(), voiceprint) < currentDist) {
                bestMatch = s;
            }
        }
        
        if (bestMatch == null) {
            return addNewSpeaker(voiceprint);
        } else {
            return bestMatch;
        }
        
    }
    
    private double getDistance(double[][] reference, double[][] compare) {
        return 1.0;
    }
    
    private Speaker addNewSpeaker(double[][] referenceMatrix) {
        Speaker s = new Speaker(createId(), referenceMatrix, createColor());
        speakers.add(s);
        return s;
    }

    private int createColor() {
        return Color.BLACK;
    }

    private String createId() {
        counter += 1;
        return "s" + counter;
    }
    
}
