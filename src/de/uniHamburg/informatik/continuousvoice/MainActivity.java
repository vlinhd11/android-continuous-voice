package de.uniHamburg.informatik.continuousvoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.IAudioService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmAudioService;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SoundPositionSpeakerFeature;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerManager;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerRecognizer;
import de.uniHamburg.informatik.continuousvoice.settings.GeneralSettings;
import de.uniHamburg.informatik.continuousvoice.views.fragments.RecognizerFragment;
import de.uniHamburg.informatik.continuousvoice.views.fragments.VisualizerFragment;

public class MainActivity extends Activity {

    //private String TAG = this.getClass().getSimpleName();
    private RecognizerFragment voiceRecognitionFragment;
    private VisualizerFragment visualizationFragment;

    public void createServices() {
        GeneralSettings.getInstance().setApplicationContext(this); //dirty java hack :-(

        //Set 3 fixed speakers
        Speaker front = new Speaker(1, new SoundPositionSpeakerFeature(0.9, 0.1), 0xff00ff00);
        Speaker rear = new Speaker(2, new SoundPositionSpeakerFeature(0.1, 0.9), 0xff0000ff);
        Speaker middle = new Speaker(3, new SoundPositionSpeakerFeature(0.5, 0.5), 0xffff0000);
        
        SpeakerManager speakerManager = new SpeakerManager();
        speakerManager.setFixedSpeakers(new Speaker[] {front, rear, middle});
        SpeakerRecognizer speakerRecognizer = new SpeakerRecognizer(speakerManager);
        
        IAudioService audioService = new PcmAudioService(speakerRecognizer);
        
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
