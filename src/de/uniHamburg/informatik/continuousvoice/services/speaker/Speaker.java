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
    
    public int getIndex() {
    	return index;
    }

	@Override
	public String toString() {
		return getId() + feature.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Speaker other = (Speaker) obj;
		if (index != other.index)
			return false;
		return true;
	}
}
