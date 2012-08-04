package com.vorsk.audiosnap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends Activity {
	private static final String TAG = "Record Activity";
	public static final String DATE_FORMAT_NOW = "MM-dd_HH.mm.ss";
	EditText fileName = null;
	boolean recording = false;
	Recorder recordThread = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		
		Log.v(TAG,"record create");
		
		//set the default name
		if (fileName == null){
			fileName = (EditText) findViewById(R.id.file_name);
			fileName.setText(now());
		}
		

	}
	
	public void uploadFinished(final File file){
		
		//upload(file);

		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.save_title)
        .setMessage(R.string.save_message)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	//dialog.cancel();
                upload(file); //TODO this does not work right, fixing later
                //finish();
            }
        })
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish(); //go back
            }
        })
        .show();
	}
	
	protected void upload(File file){
		/*ProgressDialog dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage("Uploading...");
		dialog.setCancelable(false);
		dialog.setProgress(0);
		dialog.show();*/
		showToast("Uploading in the background");
		
		//start DB upload code
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			showToast("Cannot Read File");
			return;
		}

		try {
		   Entry newEntry = AudioSnapActivity.mApi.putFile(fileName.getText().toString(), inputStream,
		           file.length(), null, null);//new DBProgress(dialog));
		   Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
		   //showToast("Upload Finished");
		} catch (DropboxUnlinkedException e) {
		   // User has unlinked, ask them to link again here.
		   Log.e("DbExampleLog", "User has unlinked.");
		   showToast("Your Dropbox accoutn is Unlinked!");
		} catch (DropboxException e) {
		   Log.e("DbExampleLog", "Something went wrong while uploading.");
		   showToast("Unknown Error while Uploading");
		}
		//end db upload code
		
		//Log.d(TAG,"dismissing");
		//dialog.dismiss();
		finish();
		
		/*
    	//save the file
    	FileMover mover = new FileMover();
    	mover.activity = this;
    	mover.mDBApi = AudioSnapActivity.mApi;
    	mover.file = file;
    	mover.uploadName = fileName.getText().toString();
    	mover.execute(FileMover.Action.UPLOAD);
    	finish();*/
    	
	}
	
	
    public void toggleRecordActivity(View v){
    	recording = !recording;
    	Button button = (Button) findViewById(R.id.record_button);
		fileName.setEnabled(!recording);
		if (recording){
			recordThread = new Recorder();
			button.setText(R.string.stop_recording);
			recordThread.execute(this);
		}else{
			button.setText(R.string.start_recording);
			recordThread.stop();
			
			
			//save and upload the file
			
			//recordThread.cancel(false);
			//recordThread = null;
		}
    }
    
    protected void updateTime(String time) {
		((TextView) findViewById(R.id.running_time)).setText(time);
	}
	
	
	private static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
	
	
    public void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    

}
