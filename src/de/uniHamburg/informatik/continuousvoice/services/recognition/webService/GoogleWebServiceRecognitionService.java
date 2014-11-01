package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioService;

public class GoogleWebServiceRecognitionService extends AbstractWebServiceRecognitionService {

    public static final String TAG = GoogleWebServiceRecognitionService.class.getName();
    private String key;

    public GoogleWebServiceRecognitionService(String apiKey, AudioService audioService) {
        super(audioService);
        this.key = apiKey;
    }

    private String getLanguageId() {
        // en-us en-en fr es ...
        return "de";
    }

    private String getUrl() {
        // 'https://www.google.com/speech-api/v2/recognize?output=json&lang=en-us&key=[KEY]'
        return "https://www.google.com/speech-api/v2/recognize?output=json&lang=" + getLanguageId() + "&key=" + key
                + "&client=chromium&maxresults=1&pfilter=2";
    }

    @Override
    public String request(File f) {
        /*
         * curl -X POST --data-binary @soundfile_1413203798952_1.amr --header
         * 'Content-Type: audio/amr; rate=8000;'
         * 'https://www.google.com/speech-api/v2/recognize?output=json&lang=en-us&key=[KEY]'
         */
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getUrl());

        String transcript = "";
        try {
            httppost.setEntity(new FileEntity(f, recording_mime_type));
            HttpResponse response;
            response = httpclient.execute(httppost);

            InputStream inputStream = response.getEntity().getContent();
            Reader in = new InputStreamReader(inputStream);
            BufferedReader bufferedreader = new BufferedReader(in);
            StringBuilder stringBuilder = new StringBuilder();
            String stringReadLine = null;
            while ((stringReadLine = bufferedreader.readLine()) != null) {
                stringBuilder.append(stringReadLine + "\n");
            }

            transcript = parseResponse(stringBuilder.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "b: " + e.getMessage().toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "c: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "d: " + e.getMessage().toString());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e(TAG, "e: " + e.getMessage().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            transcript = "(Is your API key valid?)";
            Log.e(TAG, "f: " + e.getMessage().toString());
        }

        return transcript;
    }

    private String parseResponse(String response) throws IllegalStateException, IOException, JSONException {
        // "{\"result\":[]}{"result":[{"alternative":[{"transcript":"Hola
        // OpenDomo","confidence":0.95670336
        // },{"transcript":"holaaa OpenDomo"},{"transcript":"Olga OpenDomo"},{"transcript":
        // "Hola a OpenDomo"},{"transcript":"hola a OpenDomo"}],"final":true}],"result_inde
        // x":0}

        String result = "(?)";
        
        if (response != null) {
            String cleansedResult = response.replace("{\"result\":[]}", "");
            cleansedResult = cleansedResult.replace("\n", "").replace("\r", "").trim();
            if (cleansedResult.length() != 0) { //empty result
                JSONObject json = new JSONObject(cleansedResult);
                
                try {
                    result = json.getJSONArray("result").getJSONObject(0).getJSONArray("alternative").getJSONObject(0).getString("transcript");
                } catch (NullPointerException npe) {
                }
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Google Webservice Recognizer";
    }
}