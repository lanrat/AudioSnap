package com.vorsk.audiosnap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.exception.DropboxException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends Activity {
	private static final String TAG = "Play Activity";
	boolean playing = false;
	Player playThread = null;
	
	File myFile;
	String file;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		//get our file to play var
		Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
		{
			file = extras.getString("fileName");
		}else{
			Log.w(TAG,"could not determine file name");
			return;
		}
		
		Log.i(TAG, "playing: "+file);
		
		setContentView(R.layout.play);
		
		TextView fileName = (TextView) findViewById(R.id.fileName);
		fileName.setText(file);

	}
	
	public void donePlaying(){
		Button button = (Button) findViewById(R.id.play_button);
		button.setText(R.string.play);
		playing = false;
		//this.togglePlayActivity(null);
	}
	
	protected void onStop() {
		super.onStop();
		if (playing){
			playThread.stop();
		}
		myFile.delete();
	}

	
    public void togglePlayActivity(View v){
    	Log.v(TAG,"State Toggle");
    	Button button = (Button) findViewById(R.id.play_button);
		if (!playing){
			//download the file to play
			if (myFile == null){
				Log.d(TAG, "file does not exist, downloading");
				myFile = downloadFile(file);
			}
			if (myFile == null){
				return;
			}
			playThread = new Player(this);
			button.setText(R.string.stop);
			playThread.execute(myFile);
		}else{
			button.setText(R.string.play);
			playThread.stop();
		}
		playing = !playing;
    }
    
    private File downloadFile(String fileName) {
    	//ProgressDialog dialog = ProgressDialog.show(this, "",  "Loading. Please wait...", true);
    	showToast("Downloading Audio");
    	
    	File temp = null;

    	try {
    		temp = File.createTempFile("audiosnap", "dat", getCacheDir());
    		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
    		DropboxFileInfo info = AudioSnapActivity.mApi.getFile(fileName, null, out, null);
    		Log.i(TAG, "The file's rev is: " + info.getMetadata().rev);
		} catch (DropboxException e) {
			showToast("Something went wrong while downloading");
			Log.e(TAG, "Something went wrong while downloading.");
			finish();
		} catch (FileNotFoundException e) {
			showToast("Something went wrong while downloading");
			Log.e(TAG, "could not find temp file");
		} catch (IOException e) {
			showToast("Something went wrong while downloading");
			Log.e(TAG, "could not create temp file");
		}
    	    	
    	return temp;	
	}
    
    public void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

	protected void updateTime(String time) {
		((TextView) findViewById(R.id.running_time)).setText(time);
	}
	
	
}
