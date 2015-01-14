package de.uniHamburg.informatik.continuousvoice.services.sound.recorders.timeShift;

import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.services.sound.PcmBuffer;

public class TimeShiftBuffer {

	private PcmBuffer<short[]> buffer;

	public TimeShiftBuffer() {
		this.buffer = new PcmBuffer<short[]>(AudioConstants.TIMESHIFT_BUFFER_CHUNKS);
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
	public TimeShiftAudioData getPastAudioData() {
		if (buffer.isEmpty()) {
			return null;
		}
		TimeShiftAudioData completeBufferData = new TimeShiftAudioData(buffer.getCompleteBufferData());
		
		buffer.clear();
        return completeBufferData;
	}
	
}
