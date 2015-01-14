package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.constants.AudioConstants;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.timeShift.TimeShiftAudioData;

public class PcmFile extends File {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "PcmFile";
    private List<short[]> pcmData = new LinkedList<short[]>();
    private String filename;

    public PcmFile(String path) {
        super(path);
        this.filename = path;
    }

    public synchronized void addSample(short[] sample) {
        pcmData.add(sample);
    }

    public short[] getConcatenatedPcmData() {
        int length = 0;

        for (short[] sample : pcmData) {
            length += sample.length;
        }
        short[] result = new short[length];
        int i = 0;
        for (short[] sample : pcmData) {
            for (short s : sample) {
                result[i] = s;
                i++;
            }
        }
        return result;
    }

    public int getAudioLength() {
        return pcmData.size() * AudioConstants.AUDIO_BUFFER_LENGTH_MILLIS;
    }

    public long byteSize() {
        if (pcmData.isEmpty()) {
            return 0l;
        }

        // #segments * segment-length * 2 (1 short = 2 bytes)
        return pcmData.size() * pcmData.get(0).length * 2;
    }

    public Iterable<short[]> getAudioSegments() {
        return pcmData;
    }

    public void prepend(TimeShiftAudioData prepend) {
        pcmData.addAll(0, prepend.getData());
        Log.i(TAG, prepend.getData().size() * AudioConstants.AUDIO_BUFFER_LENGTH_SEC + "sec prepended");
    }

    public void cutOffAtEnd(int sampleCount) {
        if (pcmData.size() - sampleCount < 10) {
            pcmData = pcmData.subList(0, pcmData.size() - sampleCount);
            Log.i(TAG, sampleCount * AudioConstants.AUDIO_BUFFER_LENGTH_SEC + "sec cutted off");
        }
    }

    public void save() throws IOException {
        /*
         * 1 PEPARE READER/WRITER
         */
        FileOutputStream fos = new FileOutputStream(filename);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream out = new DataOutputStream(bos);

        /*
         * 2 CALCULATE BYTE LENGTH OF FINAL FILE
         */
        long finalSize = byteSize();

        /*
         * 3 CREATE WAVE/RIFF HEADER
         */
        long byteRate = AudioConstants.AUDIO_ENCODING_BITS * AudioConstants.AUDIO_RATE * AudioConstants.AUDIO_CHANNELS
                / 8;
        WaveFileHeader header = new WaveFileHeader(finalSize, AudioConstants.AUDIO_RATE, AudioConstants.AUDIO_CHANNELS,
                byteRate, (byte) AudioConstants.AUDIO_ENCODING_BITS);

        /*
         * 4 WRITE TO FILE
         */
        header.writeToFile(out);

        for (short[] segment : pcmData) {
            for (short s : segment) {
                writeShortLE(out, s);
            }
        }
        out.close();
    }

    /**
     * writes a short value as two bytes in Little Endian Format to the given
     * data output. http://stackoverflow.com/a/27215737/1686216
     * 
     * @throws IOException
     */
    private void writeShortLE(OutputStream out, short value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }
}
