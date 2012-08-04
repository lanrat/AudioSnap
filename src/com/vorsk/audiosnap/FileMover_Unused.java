package com.vorsk.audiosnap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Thread which can upload and download files from dropbox
 * decoder
 * 
 * @author Ian Foster
 */
class FileMover_Unused extends AsyncTask<Object, String, Void> {
	private final String TAG = "File Mover";
	public DropboxAPI<AndroidAuthSession> mDBApi;

	// Gui Var, there should be a better way to do this
	public Activity activity;
	public File file;
	public static enum Action {UPLOAD, DOWNLOAD};
	public String uploadName;



	@Override
	protected Void doInBackground(Object... o) {
		// kinda ctor-ish, there must be another way of doing this
		//activity = (Activity) o[0];
		//mDBApi = (DropboxAPI<AndroidAuthSession>) o[1];
		//file = (File) o[2];
		//String uploadName = (String) o[3];
		Action action = (Action) o[0];

		Log.d(TAG, "Thread started");
		
		if (action == Action.UPLOAD){
			upload(uploadName);
		}else if (action == Action.DOWNLOAD){
			//Code here
		}else{
			Log.w(TAG,"unknown action");
		}
		
		return null;
	}
	
	
	
	private void upload(String path){
		publishProgress("Uploading to dropbox");
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			publishProgress("Cannot Read File");
			return;

		}

		try {
		   Entry newEntry = mDBApi.putFile(path, inputStream,
		           file.length(), null, null);
		   Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
		   publishProgress("Upload Finished");
		} catch (DropboxUnlinkedException e) {
		   // User has unlinked, ask them to link again here.
		   Log.e("DbExampleLog", "User has unlinked.");
		   publishProgress("Your Dropbox accoutn is Unlinked!");
		} catch (DropboxException e) {
		   Log.e("DbExampleLog", "Something went wrong while uploading.");
		   publishProgress("Unknown Error while Uploading");
		}
		
		
	}
	
	protected void onPostExecute() {
		//this.showToast("Upload Finished");
	}
	
	@Override
	protected void onProgressUpdate(String... s) {
		Log.i(TAG,s[0]);
		showToast(s[0]);
		
	}
	
    public void showToast(String msg) {
        Toast error = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
        error.show();
    }
	



}