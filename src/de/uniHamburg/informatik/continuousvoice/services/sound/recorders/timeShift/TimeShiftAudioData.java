package de.uniHamburg.informatik.continuousvoice.services.sound.recorders.timeShift;

import java.util.ArrayList;
import java.util.List;

public class TimeShiftAudioData {

    private final List<short[]> data;

    public TimeShiftAudioData(final List<short[]> completeBufferData) {
        this.data = completeBufferData;
    }
    
    public long getByteSize() {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        //Log.e("TimShiftAudioData", "FRAMESIZE: " + data.get(0).length);
        //     (            shorts            ) * 2 = bytes
        return data.size() * data.get(0).length * 2;
    }
    
    public Iterable<short[]> getData() {
        return new ArrayList<short[]>(data);
    }

}
