package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.util.List;

import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.services.sound.Buffer;

public class TimeShiftBuffer {

	private Buffer<short[]> buffer;

	public TimeShiftBuffer() {
		this.buffer = new Buffer<short[]>(AudioConstants.AUDIO_BUFFER_SIZE);
	}
	
	public void write(short[] audioData) {
		buffer.write(audioData);
	}
	
	public void clear() {
		buffer.clear();
	}
	
	public short[][] getPastAudioData() {
		List<short[]> completeBufferData = buffer.getCompleteBufferData();
		
		return completeBufferData.toArray(new short[completeBufferData.size()][completeBufferData.get(0).length]);
	}
	
}
