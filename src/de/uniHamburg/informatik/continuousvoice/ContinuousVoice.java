package de.uniHamburg.informatik.continuousvoice;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import de.uniHamburg.informatik.continuousvoice.services.recognition.AbstractRecognizer;
import de.uniHamburg.informatik.continuousvoice.services.recognition.ITranscriptionResultListener;
import de.uniHamburg.informatik.continuousvoice.services.recognition.webService.GoogleWebServiceRecognizer;
import de.uniHamburg.informatik.continuousvoice.services.sound.IAmplitudeListener;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.IAudioService;
import de.uniHamburg.informatik.continuousvoice.services.sound.recorders.PcmAudioService;
import de.uniHamburg.informatik.continuousvoice.services.speaker.ISpeakerChangeListener;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerManager;
import de.uniHamburg.informatik.continuousvoice.services.speaker.SpeakerRecognizer;
import de.uniHamburg.informatik.continuousvoice.settings.GeneralSettings;
import de.uniHamburg.informatik.continuousvoice.settings.Language;

/**
 * This it the resulting continuous voice library.
 * @author marius
 *
 */
public class ContinuousVoice {

    private IAudioService audioService;
    private AbstractRecognizer currentRecognizer;
    private SpeakerManager speakerManager;
    private Speaker[] speakers;
    
    public ContinuousVoice(Context context, Language language, String googleApiKey) {
        //the ffmpeg library needs the current context!
        GeneralSettings s = GeneralSettings.getInstance();
        s.setApplicationContext(context);
        s.setLanguage(language);
        
        speakerManager = new SpeakerManager();
        SpeakerRecognizer speakerRecognizer = new SpeakerRecognizer(speakerManager);
        
        audioService = new PcmAudioService(speakerRecognizer);
        currentRecognizer = new GoogleWebServiceRecognizer(googleApiKey, audioService);
    }

    /*
     * CONTROL COMMANDS
     * use: initialize(), start(), stop(), start(), stop(), ..., shutdown()
     */
    public void start() {
        audioService.initialize();
        currentRecognizer.start();
    }
    public void initialize() {
        currentRecognizer.initialize();
    }
    
    public void stop() {
        currentRecognizer.stop();
        audioService.shutdown();
    }

    //TODO wait for last transcription result
//    public void stop(Runnable afterLastTranscriptionReceived) {
//        audioService.shutdown();
//        currentRecognizer.stop();
//    }
    
    public void shutdown() {
        currentRecognizer.shutdown();
    }

    /*
     * SPEAKER MANAGEMENT
     * (optionally) set fixed speakers or let ContinuousVoice create them 
     */
    
    /**
     * You can set predefined Speakers like this:
     * Speaker front = new Speaker(1, new SoundPositionSpeakerFeature(0.9, 0.1), 0xff00ff00);
     * If you don't predefine any speakers, the system will guess and create its own.
     * 
     * @param speakers the only speakers possible
     */
    public void setSpeakers(Speaker[] speakers) {
        this.speakers = speakers;
        speakerManager.setFixedSpeakers(speakers);
    }
    
    public Speaker[] getPossibleSpeakers() {
        return speakers;
    }
    
    /*
     * THE LISTENERS
     * Whenever something happens, get noticed by adding an event listener
     */
    public void addSpeakerChangeListener(ISpeakerChangeListener scl) {
        audioService.addSpeakerChangeListener(scl);
    }
    
    public void addTranscriptionResultListener(ITranscriptionResultListener trl) {
        currentRecognizer.addTranscriptionListener(trl);
    }

    public void addAmplitudeListener(IAmplitudeListener iAmplitudeListener) {
        audioService.addAmplitudeListener(iAmplitudeListener);
    }
}
