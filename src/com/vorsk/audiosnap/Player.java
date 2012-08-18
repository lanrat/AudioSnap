package com.vorsk.audiosnap;

import java.io.File;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Thread which listens on the MIC input and passes captured audio to the
 * decoder
 * 
 * @author Ian Foster
 */
class Player extends AsyncTask<File, String, Integer> {
	private final String TAG = "player";
	private long startTime;

	// Gui Var, there should be a better way to do this
	private PlayActivity activity;

	// boolean value used to stop the recording if the activity is closed
	//private boolean isPlaying = true;
	
	MediaPlayer player;
	
	public Player(PlayActivity activity){
		super();
		this.activity = activity;
	}


	protected Integer doInBackground(File... f) {
		Log.d(TAG, "Thread started");
		
		player = new MediaPlayer();
		try {
			player.setDataSource(f[0].getAbsolutePath());
			player.prepare();
		} catch (Exception e) {
			Log.e(TAG,"Unable to setup temp file playback");
		}

		player.start();
			
		startTime = (System.currentTimeMillis()/1000);
		try{	
			while (player.isPlaying()){
				Thread.sleep(100);
				publishProgress();
			}
		}catch (Exception e) {
			Log.e(TAG,"Unable to play temp file");
		}
		Log.d(TAG,"done playing");
		
		player.stop();
		
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
		//isPlaying = false;
		if (player != null){
			player.stop();
		}
	}

}