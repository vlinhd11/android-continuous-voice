/* Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 * BSD-License
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */
package de.uniHamburg.informatik.continuousvoice.services.recognition.pocketSphinx;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.widget.Toast;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;
import de.uniHamburg.informatik.continuousvoice.settings.SettingsChangedListener;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class PocketSphinxRecognizer extends AbstractRecognizer implements RecognitionListener {

    private SpeechRecognizer recognizer;
    private File assetsDir;

    @Override
    public void start() {
        super.startTranscription();
        continueRecognizing();
    }

    @Override
    public void stop() {
        super.stop();
        recognizer.stop();
    }

    private void continueRecognizing() {
        recognizer.stop();
        recognizer.startListening("freespeech");
    }

    @Override
    public void initialize() {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(settings.getApplicationContext());
                    File assetDir = assets.syncAssets();
                    assetsDir = assetDir;
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    String text = "Failed to init PocketSphinx: " + result;
                    Toast.makeText(settings.getApplicationContext(), text, Toast.LENGTH_LONG).show();
                } else {
                    // continueRecognizing();
                    Toast.makeText(settings.getApplicationContext(), "PocketSphinx file successfully copied", Toast.LENGTH_SHORT).show();
                    setUp();
                }
            }
        }.execute();

        settings.addSettingsChangedListener(new SettingsChangedListener() {
            @Override
            public void settingChanged() {
                initialize();
            }
        });
    }

    private void setUp() {
        Map<String, String> deSphinx = new HashMap<String, String>();
        deSphinx.put("acousticModel", "hmm/de-de-voxforge-sphinx");
        deSphinx.put("dictionary", "dict/voxforge_de_sphinx.dic");
        deSphinx.put("languageModel", "lm/voxforge_de_sphinx.lm");
        Map<String, String> deFull = new HashMap<String, String>();
        deFull.put("acousticModel", "hmm/de-de-voxforge");
        deFull.put("dictionary", "dict/voxforge_de.dic");
        deFull.put("languageModel", "lm/voxforge_de.dmp");
        Map<String, String> enUs = new HashMap<String, String>();
        enUs.put("acousticModel", "hmm/en-us");
        enUs.put("dictionary", "dict/voxforge_en_us.dic");
        enUs.put("languageModel", "lm/en-us.dmp");

        // SET LANGUAGE HERE!
        Map<String, String> language = null;
        switch (settings.getLanguage()) {
        case DeDe:
            language = deFull;
            break;
        default:
            language = enUs;
            break;
        }

        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()

        .setAcousticModel(new File(modelsDir, language.get("acousticModel")))
                .setDictionary(new File(modelsDir, language.get("dictionary")))
                // try 1e-45, 1e-20, 1e-10f: http://stackoverflow.com/questions/24463721/keyword-is-not-detected-using-pocketsphinx-on-android
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-45f).getRecognizer();
        recognizer.addListener(this);

        File languageModel = new File(modelsDir, language.get("languageModel"));
        recognizer.addNgramSearch("freespeech", languageModel);
        //try fsg
        
        //recognizer.addFsgSearch("freespeech", languageModel);
        Toast.makeText(settings.getApplicationContext(), "PocketSphinx ready!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        // String result = hypothesis.getHypstr();
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            finishTranscription(currentTranscriptionId, text);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        continueRecognizing();
    }

    @Override
    public String getName() {
        return "PocketSphinx";
    }
}
