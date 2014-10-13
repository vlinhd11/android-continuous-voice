package de.uniHamburg.informatik.continuousvoice;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import de.uniHamburg.informatik.continousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.recognition.builtIn.AndroidRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.recognition.builtIn.AndroidRecognitionService2;
import de.uniHamburg.informatik.continuousvoice.services.recognition.builtIn.AndroidRecognitionService3;
import de.uniHamburg.informatik.continuousvoice.services.recognition.pocketSphinx.PocketSphinxRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.recognition.webService.AbstractWebServiceRecognitionService;
import de.uniHamburg.informatik.continuousvoice.views.fragments.RecognizerFragment;
import de.uniHamburg.informatik.continuousvoice.views.fragments.VisualizerFragment;

public class MainActivity extends Activity {

    private RecognizerFragment androidVoiceRecognitionFragment;
    private RecognizerFragment androidVoiceRecognitionFragment2;
    private RecognizerFragment androidVoiceRecognitionFragment3;
    private RecognizerFragment sphinxVoiceRecognitionFragment;
    private RecognizerFragment googleSpeechApiVoiceRecognitionFragment;
    private VisualizerFragment visualizationFragment;

    public void createServices() {
        androidVoiceRecognitionFragment = new RecognizerFragment("Android Built-in", AndroidRecognitionService.class);

        googleSpeechApiVoiceRecognitionFragment = new RecognizerFragment("Google Speech-To-text API", AbstractWebServiceRecognitionService.class);
        
        //sphinxVoiceRecognitionFragment = new RecognizerFragment("PocketSphinx", PocketSphinxRecognitionService.class);
        //androidVoiceRecognitionFragment2 = new RecognizerFragment("Android Built-in (2)", AndroidRecognitionService2.class);
        //androidVoiceRecognitionFragment3 = new RecognizerFragment("Android Built-in (3)", AndroidRecognitionService3.class);
        
        //visualizationFragment = new VisualizerFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createServices();

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        //.add(R.id.visualizationFragmentContainer, visualizationFragment)
                        .add(R.id.voiceRecognitionFragmentContainer1, androidVoiceRecognitionFragment)
                        .add(R.id.voiceRecognitionFragmentContainer2, googleSpeechApiVoiceRecognitionFragment)
                        //.add(R.id.voiceRecognitionFragmentContainer3, androidVoiceRecognitionFragment3)
                        .commit();
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
