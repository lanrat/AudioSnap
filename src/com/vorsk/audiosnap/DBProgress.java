package com.vorsk.audiosnap;

import android.app.ProgressDialog;
import com.dropbox.client2.ProgressListener;

//this does not work, I would like to know why
public class DBProgress extends ProgressListener{
	ProgressDialog dialog;
	public DBProgress(ProgressDialog d){
		super();
		this.dialog = d;
		//dialog.setCancelable(false);
		//dialog.show();
		//Log.v(TAG, "creating progress updater");
	}

	@Override
	public void onProgress(long bytes, long total) {
		dialog.setProgress((int) (bytes/total));
	}
}
