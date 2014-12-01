package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AlternativeWavFileRecorder {

    private static final int RECORDER_BPP = 16; // 16
    private static final String TAG = "WavFileRecorder";
    short[] audioData;
    private String filename;
    private int sampleRate;
    private PcmFile finalFile;

    public AlternativeWavFileRecorder(String filename, int sampleRate) {
        this.filename = filename;
        this.sampleRate = sampleRate;
        this.finalFile = new PcmFile(filename);
    }

    public void writeAudioFrame(short[] frame) {
        finalFile.addSample(frame);
    }

    public int close() {
        createWaveFile();
        return 0;
    }

    public String getFilename() {
        return filename;
    }

    public PcmFile getPcmFile() {
        return finalFile;
    }

    private void createWaveFile() {
        long longSampleRate = sampleRate;
        int channels = 2;
        long byteRate = RECORDER_BPP * sampleRate * channels / 8;

        try {
            FileOutputStream os = new FileOutputStream(filename);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);

            long totalAudioLen = finalFile.byteSize();
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(dos, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

            for (short[] frame : finalFile.getAudioFrames()) {
                for (int i = 0; i < frame.length; i++) {
                    writeShortLE(dos, frame[i]);
                }
            }

            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * writes a short value as two bytes in Little Endian Format to the given
     * data output. http://stackoverflow.com/a/27215737/1686216
     * 
     * @throws IOException
     */
    public static void writeShortLE(DataOutputStream out, short value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >> 8) & 0xFF);
    }

    private void writeWaveFileHeader(OutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
            int channels, long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

}
