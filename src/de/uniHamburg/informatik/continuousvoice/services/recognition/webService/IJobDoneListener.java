package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;

public interface IJobDoneListener {
	
	public void jobDone(int id, String result, Speaker speaker);
}
