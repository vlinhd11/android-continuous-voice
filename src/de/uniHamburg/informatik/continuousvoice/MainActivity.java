package de.uniHamburg.informatik.continuousvoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAudioService;
import de.uniHamburg.informatik.continuousvoice.services.sound.PcmAudioService;
import de.uniHamburg.informatik.continuousvoice.views.fragments.RecognizerFragment;
import de.uniHamburg.informatik.continuousvoice.views.fragments.VisualizerFragment;

public class MainActivity extends Activity {

    //private String TAG = this.getClass().getSimpleName();
    private RecognizerFragment voiceRecognitionFragment;
    private VisualizerFragment visualizationFragment;

    public void createServices() {
        IAudioService audioService = new PcmAudioService();
        
        voiceRecognitionFragment = new RecognizerFragment(audioService);
        visualizationFragment = new VisualizerFragment(audioService);
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
                        .add(R.id.voiceRecognitionFragmentContainer, voiceRecognitionFragment)
                        .add(R.id.visualizationFragmentContainer, visualizationFragment)
                        .commit();
            }
        }

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }
    
}
