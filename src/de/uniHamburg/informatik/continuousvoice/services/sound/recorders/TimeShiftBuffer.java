package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.util.LinkedList;

public class TimeShiftBuffer {

	private LinkedList<short[]> buffer = new LinkedList<short[]>();
	private int size;
	private int seconds;
	
	public TimeShiftBuffer(int seconds) {
		//audio comes every 40-50ms 1sec has 20 data arrays
		this.size = seconds * 20;
		this.seconds = seconds;
	}
	
	public synchronized void write(short[] audioData) {
		buffer.add(audioData);
		while (buffer.size() > size) {
			buffer.remove(0);
		}
	}
	
	public void clear() {
		buffer.clear();
	}
	
	public void getOldAudioData(int secondsToRetrieve) {
		if (secondsToRetrieve > seconds) {
			throw new IllegalArgumentException("TimeShift MAX = " + seconds + ". You can't have: " + secondsToRetrieve);
		}
	}
	
}
