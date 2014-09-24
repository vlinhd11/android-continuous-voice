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
- build/compile with Android v16
- launch