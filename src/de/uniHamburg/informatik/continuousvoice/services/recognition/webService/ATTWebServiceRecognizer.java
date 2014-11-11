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
import de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService;
import de.uniHamburg.informatik.continuousvoice.settings.Language;

public class ATTWebServiceRecognizer extends AbstractWebServiceRecognizer {

    public static final String TAG = "AttWebServiceRecognitionService";
    private String key;
    protected long RECORDING_MAX_DURATION = 5 * 1000;

    public ATTWebServiceRecognizer(String apiKey, IAudioService audioService) {
        super(audioService);
        this.key = apiKey;
    }
    
    @Override
    public void start() {
        //reset language to en-us if not en-us!
        if (!settings.getLanguage().equals(Language.EnUs)) {
            settings.setLanguage(Language.EnUs);
            setStatus("ONLY EN-US!");
        }
        
        super.start();
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
            transcript = "[JSON ERROR]";
            Log.e(TAG, "f: " + e.getMessage().toString());
        }

        return transcript;
    }

    private String parseResponse(String response) throws IllegalStateException, IOException, JSONException {
        String result = response;

        if (result != null) {
            result = result.trim();
            if (result.length() != 0) { //empty result
                JSONObject json = new JSONObject(result);

                try {
                    if (json.has("Recognition")) {
                        JSONObject info = json.getJSONObject("Recognition");
                        if (info.has("NBest")) {
                            result = info.getJSONArray("NBest")
                            .getJSONObject(0).getString("ResultText");
                        } else {
                            result = "(?)";
                        }
                    } else if (json.has("RequestError")) {
                       result = json.getJSONObject("RequestError").toString(2);
                    } else {
                        result = "(?)";
                    }
                } catch (NullPointerException npe) {
                    result = "(?)";
                }
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "AT&T Webservice Recognizer";
    }
}