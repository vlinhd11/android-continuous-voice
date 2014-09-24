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
package de.uniHamburg.informatik.continuousvoice.services.pocketSphinx;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;

import android.os.AsyncTask;
import android.widget.Toast;
import de.uniHamburg.informatik.continuousvoice.services.AbstractRecognitionService;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class PocketSphinxRecognitionService extends AbstractRecognitionService implements RecognitionListener {

    private SpeechRecognizer recognizer;

    @Override
    protected void start() {
        super.start();

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxRecognitionService.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    String text = "Failed to init PocketSphinx: " + result;
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                } else {
                    continueRecognizing();
                    Toast.makeText(getApplicationContext(), "PocketSphinx Ready!", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
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

    private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                //en: .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setAcousticModel(new File(modelsDir, "hmm/de-de-voxforge"))
                //en: .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setDictionary(new File(modelsDir, "dict/voxforge_de_sphinx.dic")).setRawLogDir(assetsDir)
                .setKeywordThreshold(1e-20f).getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search
        // recognizer.addKeyphraseSearch("keyphrase", "AKTIVIEREN");
        // Create grammar-based searches
        // File grammar = new File(modelsDir, "grammar/menu.gram");
        // recognizer.addGrammarSearch("grammarSearch", grammar);

        // Create language model search.
        File languageModel = new File(modelsDir, "lm/voxforge_de_sphinx.lm");
        // File languageModel = new File(modelsDir, "lm/weather.dmp");
        recognizer.addNgramSearch("freespeech", languageModel);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        // String result = hypothesis.getHypstr();
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            addWords(text, true);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        continueRecognizing();
    }
}
