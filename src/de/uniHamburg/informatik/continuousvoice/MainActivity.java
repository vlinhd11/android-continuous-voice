package de.uniHamburg.informatik.continuousvoice;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.uniHamburg.informatik.continousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.recognition.builtIn.AndroidRecognitionService;
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
        
        visualizationFragment = new VisualizerFragment();
        
        //TODO DELETEME
        BroadcastReceiver voiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(intent.getStringExtra("filename"))));
                shareIntent.setType("audio/amr");
                Intent createChooser = Intent.createChooser(shareIntent, "Share record");
                startActivity(createChooser);
            }
        };
        registerReceiver(voiceReceiver, new IntentFilter("DEBUGFILESHARE"));
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
                        .add(R.id.visualizationFragmentContainer, visualizationFragment)
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
