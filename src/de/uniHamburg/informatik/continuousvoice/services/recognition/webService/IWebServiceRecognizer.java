package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;

public interface IWebServiceRecognizer {

	public void transcribe(File file, IWebServiceTranscriptionDoneCallback callback);
	
}
