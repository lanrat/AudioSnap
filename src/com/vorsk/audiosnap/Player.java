package com.vorsk.audiosnap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Thread which listens on the MIC input and passes captured audio to the
 * decoder
 * 
 * @author Ian Foster
 */
class Player extends AsyncTask<ByteArrayOutputStream, String, Integer> {
	private final String TAG = "player";
	private final int FREQUENCY = 11025;
	private long startTime;

	// Gui Var, there should be a better way to do this
	private PlayActivity activity;

	// boolean value used to stop the recording if the activity is closed
	private boolean isPlaying = true;
	
	public Player(PlayActivity activity){
		super();
		this.activity = activity;
	}


	protected Integer doInBackground(ByteArrayOutputStream... f) {
		// kinda ctor-ish, there must be another way of doing this
		Log.d(TAG, "Thread started");
		
		//buffer shits
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(((ByteArrayOutputStream) f[0]).toByteArray()));

		startTime = (System.currentTimeMillis()/1000);
		
		// Get the length of the audio stored in the file (16 bit so 2 bytes per
		// short)
		// and create a short array to store the recorded audio.
		//int musicLength = (int) (dis. / 2);
		//ArrayList<Short> music = new ArrayList<Short>();
		
		//Log.v(TAG,"buffer size of: "+musicLength);

		try {
			// Create a DataInputStream to read the audio data back from the
			// saved file.
			//InputStream is = new FileInputStream(temp);
			//BufferedInputStream bis = new BufferedInputStream(is);
			//DataInputStream dis = new DataInputStream(new PipedInputStream(f[0]));
			// Read the file into the music array.
			/*int i = 0;
			while (dis.available() > 0 && isPlaying) {
				music.add(dis.readShort());
				i++;
			}*/

			// Close the input streams.
			//dis.close();

			// Create a new AudioTrack object using the same parameters as the
			// AudioRecord
			// object used to create the file.
			
			int minBuffSize = AudioTrack.getMinBufferSize(FREQUENCY, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
			
			
			AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					FREQUENCY, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minBuffSize,
					AudioTrack.MODE_STREAM);
			// Start playback
			audioTrack.play();

			// Write the music buffer to the AudioTrack object
			//audioTrack.write((short[])music.toArray(new Short[0]), 0, (music.size()/2));
			short[] data;
			
			while(isPlaying && dis.available() > 0){
				data = new short[512];
				for (int i = 0; dis.available()>0 && i<data.length; i++){
					data[i] = dis.readShort();
				}
				
				//data = {dis.readShort()};
				audioTrack.write(data, 0, data.length);
				publishProgress();
			}
			Log.d(TAG,"done playing");
			audioTrack.stop();
			

		} catch (Throwable t) {
			Log.e("AudioTrack", "Playback Failed: "+t.getMessage());
		}
		return null;
	}

	
	protected void onPostExecute(Integer i) {
		Log.d(TAG,"post play");
		activity.donePlaying();

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
		isPlaying = false;
	}

}