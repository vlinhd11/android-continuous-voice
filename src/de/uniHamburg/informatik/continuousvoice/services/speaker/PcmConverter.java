package de.uniHamburg.informatik.continuousvoice.services.speaker;

import java.util.List;

import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;

public class PcmConverter {

    public static double[] convertPcmFile(PcmFile file) {
        List<short[]> pcm = file.getPcmData();
        
        int frameLength = pcm.get(0).length;
        System.out.println("frameLenght: " + frameLength);
        double[] audioSample = new double[frameLength];
        int bytesRead = 0;
        int offset = 0;
        boolean bigEndian = false; //see http://stackoverflow.com/a/2727775/1686216

        for (short[] sample: pcm) {
            bytesRead += sample.length;
            int wordCount = (bytesRead / 2) + (bytesRead % 2);
            for (int i = 0; i < wordCount; i++) {
                double d = (double) shortArrayToShort(sample, 2 * i, bigEndian) / 32768;
                audioSample[offset + i] = d;
            }
            offset += wordCount;
        }
        return audioSample;
    }

    private static short shortArrayToShort(short[] bytes, int offset, boolean bigEndian) {
        int low, high;
        if (bigEndian) {
            low = bytes[offset + 1];
            high = bytes[offset + 0];
        } else {
            low = bytes[offset + 0];
            high = bytes[offset + 1];
        }
        return (short) ((high << 8) | (0xFF & low));
    }
}
