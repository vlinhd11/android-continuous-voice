package de.uniHamburg.informatik.continuousvoice.speaker;

public class Speaker {

    private double[][] referenceMatrix; //or list?
    private final String id;
    private int color;
    
    public Speaker(String id, double[][] referenceMatrix, int color) {
        this.referenceMatrix = referenceMatrix;
        this.id = id;
        this.color = color;
    }
    
    public double[][] getReferenceMatrix() {
        return referenceMatrix;
    }
    
    public String getId() {
        return id;
    }
    
    public int getColor() {
        return color;
    }
}
