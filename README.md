#android-continuous-voice

Part of my master thesis in winter term 2014/2015:
"*Continuous voice recognition on mobile devices*"

```
  Marius Fink

  Universität Hamburg 
  in Hamburg, Germany
  MIN-Faculty - Department of Informatics
  Distributed Systems and Information Systems (VSIS)
```

##Changelog

###Nov 23.
- chat-like transcription UI
- stereo audio service
- continuous speaker detection: icon shows speaker changes
- using native libraries for converting raw PCM audio samples into .mp3 and .wav
- time shift (buffer that will be prepended on loudness detection not to lose the initialization time when recording starts)

###Okt 20.
- new UI with only one but choosable recognizer
- silence notification in visualizer (perfect for automatic audio file splitting) 
- Google Speech API recognition service
- fixed some visualizer bugs 

###Okt 14.
- Removed PocketSphinx
- Framework for HTTP web service recognizer implementations (for the coming soon: [Google Speech To Text API v2](https://github.com/gillesdemey/google-speech-v2) Recognition Service)
- Audio Recorder that creates continuously small files with minimal downtime
- Amplitude visualizer   
- Enhanced Design

###Sep 24.
- implement basic PocketSphinx Service with (Voxforge german language model)[http://sourceforge.net/projects/cmusphinx/files/Acoustic%20and%20Language%20Models/]  
- now simultaneous voice recognition is possible (but the PocketSphinx recognition quality is very low at the moment)
- share functionality

###Sep 22.
- Whole new working UI with split screen for Android and Pocket Sphinx (Dummy Implementation)
- Working Google Continuous Speech Recognition
- No beeping sounds for Google Speech Recognition

##Features (planned for final version)
- simultaneously transcript spoken language with help of multiple voice recognition services
- find similarities and resemblances in transcripts
- share (export) results
- use either [*Android Built-In*](http://developer.android.com/reference/android/speech/SpeechRecognizer.html) Voice recognition or [*Pocket Sphinx*](http://cmusphinx.sourceforge.net/wiki/tutorialandroid) **or both**    
- completely decoupled services - easily use them in other applications (publication as library projects)
- licensed under [BSD-3](http://opensource.org/licenses/BSD-3-Clause) license due to PocketSphinx restrictions

##Setup
- clone this project
- open with Android IDE (Eclipse)
- You also need to have Eclipse Java development tools plugin installed, if you are using stock ADT from eclipse, go to Help→Install New Software. Select Eclipse Repository and install from Programming Languages section “Eclipse Java Development Tools”. (from: [PocketSphinx Tutorial for Android](http://cmusphinx.sourceforge.net/wiki/tutorialandroid))
- move and rename the `/keys.template` to `/res/values/keys.xml` and set your API keys there
- clone the following repo: https://github.com/guardianproject/android-ffmpeg-java
- import them into your workspace (import as android project, not existing eclipse project)
- set them as library (project -> properties -> Android -> [x] is library)
- reference them from ContinuousVoice (project -> properties -> project references)
- include libraries in build path (project -> properties -> Java build path -> projects -> add ...)
- build/compile with Android v16
- launch