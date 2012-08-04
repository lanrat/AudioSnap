package com.vorsk.audiosnap;

import java.io.ByteArrayOutputStream;
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
	
	ByteArrayOutputStream myFile;
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
		
	}

	
    public void togglePlayActivity(View v){
    	Log.v(TAG,"State Toggle");
    	Button button = (Button) findViewById(R.id.play_button);
		if (!playing){
			//donload the file to play
			if (myFile == null){
				Log.d(TAG, "file does not exist, downloading");
				myFile = downloadFile(file);
			}
			
			
			playThread = new Player(this);
			button.setText(R.string.stop);
			playThread.execute(myFile);
			//recordThread.execute(this);
		}else{
			button.setText(R.string.play);
			playThread.stop();
			//recordThread.cancel(false);
			//recordThread = null;
		}
		playing = !playing;
    }
    
    private ByteArrayOutputStream downloadFile(String fileName) {
    	//ProgressDialog dialog = ProgressDialog.show(this, "",  "Loading. Please wait...", true);
    	showToast("Downloading Audio");
    	
    	
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    	try {
		   DropboxFileInfo info = AudioSnapActivity.mApi.getFile(fileName, null, outputStream, null);
		   Log.i("DbExampleLog", "The file's rev is: " + info.getMetadata().rev);
		} catch (DropboxException e) {
			showToast("Something went wrong while downloading");
		   Log.e("DbExampleLog", "Something went wrong while downloading.");
		   finish();
		}
    	    	
    	//return temp;
    	return outputStream;
    	
	}
    
    public void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

	protected void updateTime(String time) {
		((TextView) findViewById(R.id.running_time)).setText(time);
	}
	
	
}
