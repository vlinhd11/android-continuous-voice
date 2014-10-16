package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.R;

public class GoogleWebServiceRecognitionService extends AbstractWebServiceRecognitionService {

    public static final String TAG = GoogleWebServiceRecognitionService.class.getCanonicalName();
    private String key;

    public GoogleWebServiceRecognitionService() {
        super("google_soundfile_" + System.currentTimeMillis());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        key = getString(R.string.googleApiKey);
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

        InputStreamEntity reqEntity;
        String transcript = "";
        try {
            reqEntity = new InputStreamEntity(new FileInputStream(f), -1);
            reqEntity.setContentType("audio/amr; rate=8000");
            reqEntity.setChunked(false); // Send in multiple parts if needed
            httppost.setEntity(reqEntity);
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

            Log.e(TAG, "RESULT: " + transcript);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "b: " + e.getMessage().toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "c: " + e.getMessage().toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "d: " + e.getMessage().toString());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e(TAG, "e: " + e.getMessage().toString());
        } catch (JSONException e) {
            e.printStackTrace();
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

        Log.e(TAG, response);
        
        String cleansedResult = response.replace("{\"result\":[]}", "");
        JSONObject json = new JSONObject(cleansedResult);

        String result = "";
        try {
            result = json.getJSONArray("result").getJSONObject(0).getJSONArray("alternative").getJSONObject(0).getString("transcript");
        } catch (NullPointerException npe) {
        }
        return result;
    }
}