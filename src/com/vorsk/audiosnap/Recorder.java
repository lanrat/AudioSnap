package com.vorsk.audiosnap;

import java.io.File;
import java.io.IOException;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Thread which listens on the MIC input and passes captured audio to the
 * decoder
 * 
 * @author Ian Foster
 */
class Recorder extends AsyncTask<Integer, String, File> {
	private final String TAG = "AudioRecord";
	private long startTime;

	// Gui Var, there should be a better way to do this
	private RecordActivity activity;

	// boolean value used to stop the recording if the activity is closed
	private boolean isRecording = true;
	
	//constructor to set the activity
	public Recorder(RecordActivity activity){
		this.activity = activity;
	}

	/*
	 * (non-Javadoc) Main thread method Listens to the audio input and passes
	 * audio data to the Decoder when it finds any
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected File doInBackground(Integer... a) {
		Log.d(TAG, "Thread started");

		File temp;
		try {
			temp = File.createTempFile("audiosnap", "dat", activity.getCacheDir());
		} catch (IOException e1) {
			throw new IllegalStateException("Failed to create temp file");
		}

		try {
			MediaRecorder recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setMaxFileSize(10485760); //10MB
			recorder.setOutputFile(temp.getPath());

			//TODO: the above should be moved to the ctor
			recorder.prepare();
			recorder.start();
			startTime = (System.currentTimeMillis()/1000);

			while (isRecording) {
				Thread.sleep(500);
				publishProgress();
			}

			recorder.stop();

		} catch (Throwable t) {
			Log.e(TAG, "Recording Failed");
		}

		return temp;
	}
	
	protected void onPostExecute(final File result) {
		activity.uploadFinished(result);
	}
	

	/*
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(String... s) {;
		long diff = (System.currentTimeMillis()/1000)-startTime;
		activity.updateTime(String.format("%d:%02d", (diff/60), (diff%60)));
	}

	/**
	 * Stops the recording, to re-start re-create the object
	 */
	public void stop() {
		isRecording = false;
	}

}