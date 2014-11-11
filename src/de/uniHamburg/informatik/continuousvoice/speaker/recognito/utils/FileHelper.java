/*
 * (C) Copyright 2014 Amaury Crickx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uniHamburg.informatik.continuousvoice.speaker.recognito.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility methods for loding voice samples from files
 * 
 * @author Amaury Crickx
 */
public class FileHelper {

    /**
     * Converts the given audio file to an array of doubles with values between -1.0 and 1.0
     * @param voiceSampleFile the file to convert
     * @return an array of doubles
     * @throws UnsupportedAudioFileException when the JVM does not support the file format
     * @throws IOException when an I/O exception occurs
     */
    public static double[] readAudioInputStream(File file) 
            throws IOException {
        
//        AudioFormat originalFormat = is.getFormat(); 
//        AudioFormat format = new AudioFormat(originalFormat.getSampleRate(), 16, 1, true, true);
//
//        AudioInputStream localIs = null;
//
//        if(!originalFormat.matches(format)) {
//            if(AudioSystem.isConversionSupported(format, originalFormat)) {
//                localIs = AudioSystem.getAudioInputStream(format, is);
//            } else {
//                throw new UnsupportedAudioFileException("Alas, the system could not decode your file type." +
//                		"Try converting your file to some PCM 16bit 16000 Hz mono file format using dedicated " +
//                		"software. (Hint : http://sox.sourceforge.net/");
//            }
//        } else {
//            localIs = is;
//        }
//        
//        
//        double[] audioSample = new double[(int)localIs.getFrameLength()];
//        byte[] buffer = new byte[8192];
//        int bytesRead = 0;
//        int offset = 0;
//        
//        while((bytesRead = localIs.read(buffer)) > -1) {
//            int wordCount = (bytesRead / 2) + (bytesRead % 2);
//            for (int i = 0; i < wordCount; i++) {
//                double d = (double) byteArrayToShort(buffer, 2 * i, format.isBigEndian()) / 32768;
//                audioSample[offset + i] = d;
//            }
//            offset += wordCount;
//        }
//        return audioSample;
        

        InputStream inStream;
        inStream = new FileInputStream(file);
        AmrInputStream aStream = new AmrInputStream(inStream);

        file.createNewFile();
        OutputStream out = new FileOutputStream(file); 

        byte[] x = new byte[1024];
        int len;
        while ((len=aStream.read(x)) > 0) {
            out.write(x,0,len);
        }

        aStream.close();
        out.close();
        
        
        
        return null;
    }
    
    private static short byteArrayToShort(byte[] bytes, int offset, boolean bigEndian) {
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
