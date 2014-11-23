package de.uniHamburg.informatik.continuousvoice.services.sound;

import java.util.LinkedList;
import java.util.List;

public class Buffer<T> {

	private List<T> buffer;
	private int size;
	
	public Buffer (int size) {
		this.size = size;
		buffer = new LinkedList<T>();
	}
	
	public synchronized void write(T data) {
		buffer.add(data);
		while (buffer.size() > size) {
			buffer.remove(0);
		}
	}
	
	public synchronized void clear() {
		buffer.clear();
	}
	
	public synchronized List<T> getCompleteBufferData() {
		return new LinkedList<T>(buffer);
	}
	
//	public short[][] getPastAudioData(int secondsToRetrieve) {
//	if (secondsToRetrieve > seconds) {
//		throw new IllegalArgumentException("TimeShift MAX = " + seconds + ". You can't have: " + secondsToRetrieve);
//	}
//	
//	int size = Math.min(secondsToRetrieve * 20, buffer.size());
//	short[][] result = new short[size][buffer.getFirst().length];
//
//	for (int i = 0; i < size; i ++) {
//		result[i] = buffer.get(buffer.size() - 1 - i);
//	}
//	
//	return result;
//}
	
	
}
