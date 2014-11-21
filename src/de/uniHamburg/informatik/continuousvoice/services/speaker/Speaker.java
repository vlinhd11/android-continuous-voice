package de.uniHamburg.informatik.continuousvoice.services.speaker;

public class Speaker {

    private AbstractSpeakerFeature feature;
    private final int index;
    private int color;
    
    public Speaker(int index, AbstractSpeakerFeature feature, int color) {
        this.feature = feature;
        this.index = index;
        this.color = color;
    }
    
    public AbstractSpeakerFeature getReferenceFeature() {
        return feature;
    }
    
    public void mergeReferenceFeature(AbstractSpeakerFeature other) {
        this.feature.merge(other);
    }
    
    public String getId() {
        return "Speaker " + index;
    }
    
    public int getColor() {
        return color;
    }
    
    @Override
    public boolean equals(Object other) {
    	if (other instanceof Speaker) {
    		return ((Speaker) other).getId().equals(getId());
    	} else {
    		return false;
    	}
    }
    
    public int getIndex() {
    	return index;
    }
}
