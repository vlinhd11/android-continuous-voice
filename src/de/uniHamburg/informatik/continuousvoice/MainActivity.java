package de.uniHamburg.informatik.continuousvoice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import de.uniHamburg.informatik.continousvoice.R;
import de.uniHamburg.informatik.continuousvoice.services.AndroidVoiceRecognitionService;
import de.uniHamburg.informatik.continuousvoice.services.IVoiceRecognitionService;
import de.uniHamburg.informatik.continuousvoice.views.fragments.VoiceRecognizerFragment;

public class MainActivity extends Activity {
	
	private VoiceRecognizerFragment androidVoiceRecognitionFragment;
	private VoiceRecognizerFragment sphinxVoiceRecognitionFragment;
	
	public MainActivity() {
		IVoiceRecognitionService androidService = new AndroidVoiceRecognitionService();
		androidVoiceRecognitionFragment = new VoiceRecognizerFragment("Android Built-in", androidService);
		IVoiceRecognitionService sphinxService = new AndroidVoiceRecognitionService();
		sphinxVoiceRecognitionFragment = new VoiceRecognizerFragment("PocketSphinx", sphinxService);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			if (savedInstanceState == null) {
				getFragmentManager()
					.beginTransaction()
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
	
	public void startService(View view) {
		Context context = getApplicationContext();
		
		// use this to start and trigger a service
		Intent i = new Intent(context, AndroidVoiceRecognitionService.class);
		// potentially add data to the intent
		i.putExtra("KEY2", "Value to be used by the service");
		context.startService(i);
	}
}
