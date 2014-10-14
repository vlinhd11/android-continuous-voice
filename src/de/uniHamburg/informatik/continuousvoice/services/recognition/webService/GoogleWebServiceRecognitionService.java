package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Environment;

public class GoogleWebServiceRecognitionService extends AbstractWebServiceRecognitionService {

    public GoogleWebServiceRecognitionService() {
        super("google_soundfile_" + System.currentTimeMillis());
    }

    @Override
    public String request(File f) {
        /*
         * curl -X POST --data-binary @soundfile_1413203798952_1.amr --header
         * 'Content-Type: audio/amr; rate=8000;'
         * 'https://www.google.com/speech-api/v2/recognize?output=json&lang=en-us&key=[KEY]'
         */
        String url = "http://yourserver";
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(url);
            
            InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(f), -1);
            reqEntity.setContentType("binary/octet-stream");
            reqEntity.setChunked(false); // Send in multiple parts if needed
            httppost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httppost);
            // Do something with response...

        } catch (Exception e) {
            // show error
        }

        return "done";
    }

}
