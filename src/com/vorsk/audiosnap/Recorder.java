package com.vorsk.audiosnap;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Thread which listens on the MIC input and passes captured audio to the
 * decoder
 * 
 * @author Ian Foster
 */
class Recorder extends AsyncTask<RecordActivity, String, File> {
	private final String TAG = "AudioRecord";
	private final int FREQUENCY = 11025;
	private long startTime;

	// Gui Var, there should be a better way to do this
	private RecordActivity activity;

	// boolean value used to stop the recording if the activity is closed
	private boolean isRecording = true;

	/*
	 * (non-Javadoc) Main thread method Listens to the audio input and passes
	 * audio data to the Decoder when it finds any
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected File doInBackground(RecordActivity... a) {
		// kinda ctor-ish, there must be another way of doing this
		activity = a[0]; // TODO find a better way!!!

		Log.d(TAG, "Thread started");

		// configure stuff
		int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

		File temp;
		try {
			temp = File.createTempFile("audiosnap", "dat", activity.getCacheDir());
		} catch (IOException e1) {
			throw new IllegalStateException("Failed to create temp file");
		}

		try {
			// Create a DataOuputStream to write the audio data into the saved
			// file.
			OutputStream os = new FileOutputStream(temp);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			DataOutputStream dos = new DataOutputStream(bos);

			// Create a new AudioRecord object to record the audio.
			int bufferSize = AudioRecord.getMinBufferSize(FREQUENCY,
					channelConfiguration, audioEncoding);
			AudioRecord audioRecord = new AudioRecord(
					MediaRecorder.AudioSource.MIC, FREQUENCY,
					channelConfiguration, audioEncoding, bufferSize);

			short[] buffer = new short[bufferSize];
			startTime = (System.currentTimeMillis()/1000);
			audioRecord.startRecording();

			while (isRecording) {
				int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
				for (int i = 0; i < bufferReadResult; i++)
					dos.writeShort(buffer[i]);
				//too much?
				publishProgress();
			}

			audioRecord.stop();
			dos.close();

		} catch (Throwable t) {
			Log.e(TAG, "Recording Failed");
		}
		
		//buffer error here
		//this.play(temp);

		return temp;
	}
	
	protected void onPostExecute(final File result) {
		activity.uploadFinished(result);

	}
	

	/*
	 * (non-Javadoc) Push a string to the GUI to display
	 * 
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(String... s) {
		// Log.d(TAG, "GUI: "+s[0]);
		// activity.addToGUI(s[0]);
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