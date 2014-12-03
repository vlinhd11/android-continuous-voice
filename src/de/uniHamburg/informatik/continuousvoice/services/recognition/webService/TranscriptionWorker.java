package de.uniHamburg.informatik.continuousvoice.services.recognition.webService;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;
import de.uniHamburg.informatik.continuousvoice.services.speaker.Speaker;

public class TranscriptionWorker {

	
	private static final String TAG = "TranscriptionWorker";
	private Queue<Job> queue;
	private IWebServiceRecognizer recognizer;
	private boolean working = false;
	private IJobDoneListener callback;

	public TranscriptionWorker(IWebServiceRecognizer recognizer, IJobDoneListener callback) {
		this.recognizer = recognizer;
		this.queue = new LinkedBlockingQueue<Job>();
		this.callback = callback;
	}
	
	public void enqueueJob(int id, File file, Speaker speaker) {
		queue.add(new Job(id, file, speaker));
		
		//Log.w(TAG, queue.size() + " jobs waiting");
		workQueue();
	}
	
	private synchronized void doJob(final Job job) {
		working = true;

		new Thread(new Runnable() {
			@Override
			public void run() {
				recognizer.transcribe(job.file, new IWebServiceTranscriptionDoneCallback() {
					@Override
					public void transcriptionDone(String result) {
						working = false;
						callback.jobDone(job.id, result, job.speaker);
						Log.i(TAG, "job #" + job.id + " done (" + result.split(" ").length + " words). " + queue.size() + " jobs waiting");
						workQueue();
					}
				});
			}
		}).start();
	}
	
	private void workQueue() {
		if (!working) {
			Job toDo = queue.poll();
			if (toDo != null) {
				doJob(toDo);
			}
		}
	}
	
	private class Job {
		int id;
		File file;
		Speaker speaker;
		protected Job(int id, File file, Speaker speaker) {
			this.id = id;
			this.file = file;
			this.speaker = speaker;
		}
	}
}
