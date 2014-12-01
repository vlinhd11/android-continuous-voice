package de.uniHamburg.informatik.continuousvoice.services.sound;

import java.io.File;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmFile;

public class AudioHelper {

    protected static final String TAG = "AudioHelper";
    public static int MAX_SOUND_LEVEL = 32768;

    /**
     * Extract highest value out of pcm data unfortunately this:
     * http://stackoverflow.com/a/8766420 doesn't work.
     * 
     * @param pcmData
     *            the raw audio buffer
     * @return a sound level 0..Short.MAX_VALUE
     */
    public static double pcmToSoundLevel(short[] pcmData) {

        double max = 0;

        for (short s : pcmData) {
            max = Math.max(s, max);
        }

        return max;
    }

    /**
     * uses ffmpeg to compress a wav file
     * 
     * @param toCompress
     * @return
     * @throws Exception
     */
    public static void compress(Context context, final File toCompress,
            final IConversionDoneCallback callback) throws Exception {

        final long start = System.currentTimeMillis();
        
        FfmpegController c = new FfmpegController(context, new File("ffMpegTempFile"));
        String inPath = toCompress.getAbsolutePath();
        final String outPath = inPath.substring(0, inPath.lastIndexOf('.')) + ".wav";
        // “ffmpeg -i Ahmad_Amr.wav -ar 8000 -ac 1 -acodec pcm_u8 output.wav’

        ShellCallback shellCallback = new ShellCallback() {
            @Override
            public void shellOut(String shellLine) {
                Log.i(TAG, "  >  " + shellLine);
            }

            @Override
            public void processComplete(int exitValue) {
                Log.i(TAG, "  >  Conversion finished with exit-value:" + exitValue);
                callback.conversionDone(toCompress, new File(outPath), System.currentTimeMillis() - start);
            }
        };

        c.convertToSmallWav(new Clip(inPath), outPath, shellCallback);
    }

    public static short[][] splitStereo(short[] audioData) {
        int length = audioData.length;
        short[] leftChannelAudioData = new short[length];
        short[] rightChannelAudioData = new short[length];

        for (int i = 0; i < length / 2; i = i + 2) {
            // split stereo right?: http://stackoverflow.com/a/15418720/1686216 (comment #2)
            leftChannelAudioData[i] = audioData[2 * i];
            leftChannelAudioData[i + 1] = audioData[2 * i + 2];

            rightChannelAudioData[i] = audioData[2 * i + 1];
            rightChannelAudioData[i + 1] = audioData[2 * i + 3];
        }

        return new short[][] { leftChannelAudioData, rightChannelAudioData };
    }

    public static double[] stereoLevelsFromFile(PcmFile audioFile) {
        short[][] stereo = AudioHelper.splitStereo(audioFile.getConcatenatedPcmData());

        double left = pcmToSoundLevel(stereo[0]);
        double right = pcmToSoundLevel(stereo[1]);

        return new double[] { left, right };
    }
}
