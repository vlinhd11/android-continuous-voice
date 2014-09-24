package de.uniHamburg.informatik.continuousvoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.uniHamburg.informatik.continousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.builtIn.AndroidRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.dummy.DummyRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.pocketSphinx.PocketSphinxRecognitionService;
import de.uniHamburg.informatik.continuousvoice.views.fragments.RecognizerFragment;

public class MainActivity extends Activity {

    private RecognizerFragment androidVoiceRecognitionFragment;
    private RecognizerFragment sphinxVoiceRecognitionFragment;

    public void createServices() {
        androidVoiceRecognitionFragment = new RecognizerFragment("Android Built-in", AndroidRecognitionService.class);
        sphinxVoiceRecognitionFragment = new RecognizerFragment("PocketSphinx", PocketSphinxRecognitionService.class);
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
                        .add(R.id.voiceRecognitionFragmentContainer1, androidVoiceRecognitionFragment)
                        .add(R.id.voiceRecognitionFragmentContainer2, sphinxVoiceRecognitionFragment)
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
