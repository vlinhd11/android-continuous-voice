package de.uniHamburg.informatik.continuousvoice.services.sound.recorders;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class WavFileRecorder {

    private static final String TAG = "WavFileRecorder";
    private static final int RECORDER_BPP = 16; // 16
    private String filename;
    private int sampleRate;
    private PcmFile finalFile;
    private DataOutputStream tempDos;
    private int bufferSize;
    private String tempFilename;
    
    public WavFileRecorder(String filename, int sampleRate, int bufferSize) {
        this.filename = filename;
        this.sampleRate = sampleRate;
        this.finalFile = new PcmFile(filename);
        this.tempFilename = filename + "_temp";
        this.bufferSize = bufferSize;
        Log.e(TAG, "condtrucotr" + filename);
        
        createTempFile();
        createFile();
        
        FileOutputStream tempOs;
        try {
            tempOs = new FileOutputStream(tempFilename);
            BufferedOutputStream bos = new BufferedOutputStream(tempOs);
            tempDos = new DataOutputStream(bos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeAudioFrame(short[] frame) {
        try {
            for (int i = 0; i < frame.length; i++) {
                writeShortLE(tempDos, frame[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PcmFile writeFile() {
        Log.e(TAG, "write temp");
        try {
            tempDos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        copyWaveFile(tempFilename, filename);
        return finalFile;
    }

    public String getFilename() {
        return filename;
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRate;
        int channels = 2;
        long byteRate = RECORDER_BPP * sampleRate * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.i(TAG, "File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
            Log.e(TAG, "final wirtten " + finalFile.exists() + " " + finalFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {

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
    
    private void createFile() {
        new File(filename);
    }

    private void createTempFile() {
        new File(tempFilename);
    }
    
}
