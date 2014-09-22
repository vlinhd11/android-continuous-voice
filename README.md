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
###Sep. 22
- Whole new working UI with split screen for Android and Pocket Sphinx (Dummy Implementation)
- Working Google Continuous Speech Recognition
- No beeping sounds for Google Speech Recognition

##Features (planned for final version)
- simultaneously transcript spoken language with help of multiple voice recognition services
- find similarities and resemblances in transcripts
- share (export) results
- use either [*Android Built-In*](http://developer.android.com/reference/android/speech/SpeechRecognizer.html) Voice recognition or [*Pocket Sphinx*](http://cmusphinx.sourceforge.net/wiki/tutorialandroid) **or both**    
- completely decoupled services - easily use them in other applications (publication as library projects)
- licensed under [MIT](http://opensource.org/licenses/MIT) license 

##Setup
- clone this project
- open with Android IDE (Eclipse)
- build/compile with Android v16
- launch