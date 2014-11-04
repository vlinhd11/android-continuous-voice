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

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.sound.AudioService;

public class AttWebServiceRecognitionService extends AbstractWebServiceRecognitionService {

    public static final String TAG = "AttWebServiceRecognitionService";
    private String key;
    protected long RECORDING_MAX_DURATION = 5 * 1000;

    public AttWebServiceRecognitionService(String apiKey, AudioService audioService) {
        super(audioService);
        this.key = apiKey;
    }

    private String getUrl() {
        return "https://api.att.com/speech/v3/speechToText";
    }

    @Override
    public String request(File f) {
        /*
         * POST /speech/v3/speechToText HTTP/1.1
         *  Host: api.att.com
         *  Authorization: Bearer [oauth-key]
         *  Accept: application/xml
         *  Content-length: 5655
         *  Connection: keep-alive
         *  Content-Type: audio/amr
         *  X-SpeechContext: BusinessSearch
         *  X-Arg: ClientApp=NoteTaker,ClientVersion=1.0.1,DeviceType=Android
         */
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getUrl());

        String transcript = "";
        try {
            httppost.addHeader("Authorization", "Bearer " + key);
            httppost.addHeader("Accept", "application/json");
            //only with fixed grammars httppost.addHeader("Content-Language", "de-DE");
            httppost.addHeader("X-SpeechContect", "Generic");
            httppost.setEntity(new FileEntity(f, "audio/amr"));
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

        Log.i(TAG, response);
        
        String result = response;
//        
//        if (response != null) {
//            String cleansedResult = response.replace("{\"result\":[]}", "");
//            cleansedResult = cleansedResult.replace("\n", "").replace("\r", "").trim();
//            if (cleansedResult.length() != 0) { //empty result
//                JSONObject json = new JSONObject(cleansedResult);
//                
//                try {
//                    result = json.getJSONArray("result").getJSONObject(0).getJSONArray("alternative").getJSONObject(0).getString("transcript");
//                } catch (NullPointerException npe) {
//                }
//            }
//        }
        return result;
    }

    @Override
    public String getName() {
        return "AT&T Webservice Recognizer";
    }
}