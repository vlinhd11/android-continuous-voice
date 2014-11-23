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
     * Extract highest value out of pcm data
     * unfortunately this: http://stackoverflow.com/a/8766420 doesn't work.
     * 
     * @param pcmData
     *            the raw audio buffer
     * @return a sound level 0..Short.MAX_VALUE
     */
    public static double pcmToSoundLevel(short[] pcmData) {
        
        double max = 0;
        
        for (short s: pcmData) {
            max = Math.max(s, max);
        }

        return max;
    }
    
    /**
     * uses ffmpeg to convert a mp3 into a amr
     * ffmpeg -i testwav.wav -ar 8000 -ab 12.2k audio.amr 
     * @param mp3File
     * @return
     * @throws Exception 
     */
    public static void convertMp3ToCompressedWav(Context context, final File mp3File, final ConversionDoneCallback callback) throws Exception {
     
        FfmpegController c = new FfmpegController(context, new File("ffMpegTempFile"));
        String inPath = mp3File.getAbsolutePath();
        final String outPath = inPath.substring(0, inPath.lastIndexOf('.')) + ".wav";
        //“ffmpeg -i Ahmad_Amr.wav -ar 8000 -ac 1 -acodec pcm_u8 output.wav’

        ShellCallback shellCallback = new ShellCallback() {
            @Override
            public void shellOut(String shellLine) {
                Log.i(TAG, "  >  " + shellLine);
            }

            @Override
            public void processComplete(int exitValue) {
                Log.w(TAG, "  >  Conversion finished with exit-value:" + exitValue);
                callback.conversionDone(mp3File, new File(outPath));
            }
        };
        
        c.convertToSmallWav(new Clip(inPath), outPath, shellCallback);
    }
    
    public interface ConversionDoneCallback {
        public void conversionDone(File origin, File converted);
    }

    public static short[][] splitStereo(short[] audioData) {
        int length = audioData.length;
        short[] leftChannelAudioData = new short[length];
        short[] rightChannelAudioData = new short[length];
                
        for(int i = 0; i < length/2; i = i + 2) {
            //split stereo: http://stackoverflow.com/a/20624845/1686216
            //leftChannelAudioData[i] = audioData[2*i];
            //leftChannelAudioData[i+1] = audioData[2*i+1];
            //rightChannelAudioData[i] =  audioData[2*i+2];
            //rightChannelAudioData[i+1] = audioData[2*i+3];
            
            //split stereo right?: http://stackoverflow.com/a/15418720/1686216 comment 2
            leftChannelAudioData[i] = audioData[2*i];
            leftChannelAudioData[i + 1] = audioData[2 * i + 2];

            rightChannelAudioData[i] = audioData[2 * i + 1];
            rightChannelAudioData[i + 1] = audioData[2 * i + 3];
        }
        
       return new short[][] {leftChannelAudioData, rightChannelAudioData};
    }
    
    /**
     * Converts alternating audio sample array values by adding left and right channel
     * [l1,r1,l2,r2,...] => [l1+r1, l2+r2]
     * @param stereo alternating audio samples [l,r,l,r,l,r,...]
     * @return
     */
    public static short[] convertStereoToMono(short[] stereo) {
    	int length = stereo.length / 2;
    	short[] mono = new short[length];
    	
    	for(int i = 0; i < length; i++) {
    		int sum = stereo[2*i] + stereo[2*i+1];
    		
    		mono[i] = (short) stereo[2*i];//(sum / 2);
        }
    	
    	return mono;
    }
    
    /**
     * 
     * @param audioFile
     * @return 0 = 100% left, 1 = 100% right, 0.5 = center
     */
    public static double soundPosition(PcmFile audioFile) {
        short[][] stereo = AudioHelper.splitStereo(audioFile.getConcatenatedPcmData());
        
        double left = pcmToSoundLevel(stereo[0]);
        double right = pcmToSoundLevel(stereo[1]);
        String maxInfo = "";
        double position = (left / (left + right));
        if (left >= right) {
            maxInfo = "L: " + position; 
        } else {
            maxInfo = "R: " + right / (left + right);
        }
        
        Log.e(TAG, "POS: " + maxInfo);
        
        return position;
    }
}
