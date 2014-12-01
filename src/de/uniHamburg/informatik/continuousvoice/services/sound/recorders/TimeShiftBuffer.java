package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.util.List;

import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.services.sound.PcmBuffer;

public class TimeShiftBuffer {

	private PcmBuffer<short[]> buffer;

	public TimeShiftBuffer() {
		this.buffer = new PcmBuffer<short[]>(AudioConstants.TIMESHIFT_BUFFER_SIZE);
	}
	
	public void write(short[] audioData) {
		buffer.write(audioData);
	}
	
	public void clear() {
		buffer.clear();
	}
	
	/**
	 * retrieves the complete buffer data and clears it.
	 * @return
	 */
	public short[][] getPastAudioData() {
		if (buffer.isEmpty()) {
			return null;
		}
		List<short[]> completeBufferData = buffer.getCompleteBufferData();
		
		short[][] result = completeBufferData.toArray(new short[completeBufferData.size()][completeBufferData.get(0).length]);
		buffer.clear();
        return result;
	}
	
}
