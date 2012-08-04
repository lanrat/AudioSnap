package com.vorsk.audiosnap;

import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.client2.session.Session.AccessType;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class AudioSnapActivity extends ListActivity {
	private static final String TAG = "AudioSnap";
	
	//DB stuff
	final static private String APP_KEY = "f4nj12exvbn6qcw";
	final static private String APP_SECRET = "aadeh5g8te8jsz5";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    //private DropboxAPI<AndroidAuthSession> mDBApi;
    SharedPreferences DBsettings;
    final static private String ACCOUNT_PREFS_NAME = "AudioSnap";
    final static private String ACCESS_KEY_NAME = "gen";
    final static private String ACCESS_SECRET_NAME = "pickle";
    private boolean mLoggedIn;
    public static DropboxAPI<AndroidAuthSession> mApi;
    
    List<Entry> values;

	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "create me");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        //for list context view
        ListView list = getListView();
        registerForContextMenu(list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                onListItemClick(v,pos,id);
            }
        });

	    this.startDBAuth();
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      int menuItemIndex = item.getItemId();
      String[] menuItems = getResources().getStringArray(R.array.action_menu);
      String menuItemName = menuItems[menuItemIndex];
      //Log.d(TAG,info.position+" # "+menuItemName);
      if (menuItemName.equals("Delete")){
    	  this.delete(values.get(info.position));
      }else if (menuItemName.equals("Rename")){
    	  this.rename(values.get(info.position));
      }else if (menuItemName.equals("Play")){
    	  this.launchPlayActivity(values.get(info.position));
      }
      //String listItemName = Countries[info.position];

      //TextView text = (TextView)findViewById(R.id.footer);
      //text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));
      return true;
    }
    
    protected void onListItemClick(View v, int pos, long id) {
        //Log.i(TAG, "onListItemClick id=" + id);
        this.launchPlayActivity(values.get(pos));
    }


    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == getListView().getId() ) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(values.get(info.position).fileName());
			String[] menuItems = getResources().getStringArray(R.array.action_menu);
			for (int i = 0; i < menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}
	
	protected void delete(final Entry file) {
		
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.delete)
        .setMessage(getString(R.string.delete_message)+" "+file.fileName()+"?")
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
        		try {
        			mApi.delete(file.fileName());
        			listDir();
        		} catch (DropboxException e) {
        			showToast("Error deleting file");
        			//e.printStackTrace();
        		}
            }
        })
        .setNegativeButton(R.string.no, null)
        .show();
		
	}
	
	
	protected void rename(final Entry file){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.rename);
		//alert.setMessage("Message");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);
		input.setSingleLine(true);
		input.setText(file.fileName());

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  Editable value = input.getText();
		  Log.d(TAG,"renaming: "+value.toString());
		  try {
			mApi.move(file.fileName(), value.toString());
			} catch (DropboxException e) {
				showToast("Error renaming File");
			}
		  	listDir();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
		
	}
    
    private void startDBAuth(){
    	Log.d(TAG, "init DB");
    	
    	
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
    	
        //this should be in a pop-up.....
        Log.d(TAG, "start auth");
        if (!mLoggedIn){
        	if (!mApi.getSession().isLinked()){
        		mApi.getSession().startAuthentication(this);
        	}
        }
        Log.d(TAG, "end auth");
    }
    
    
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "resumeing");
        super.onResume();
        Log.d(TAG, "getting session");
        AndroidAuthSession session = mApi.getSession();
        Log.d(TAG, "got session");
        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
        	Log.d(TAG, "attempting resume auth");
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }else{
        	//this is run if you simply press back at the DB prompt
        	//showToast("Dropbox Auth Error");
        	//Log.i(TAG, "Auth Error");
        } 
        Log.d(TAG, "done resumeing");
        
        
        this.listDir();
    }
    
    
	//make the menu work
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d(TAG,"making menu");
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return true;
	}
    //when a user selects a menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
		case R.id.menu_refresh:
			//refresh the networks (the easy way)
			this.listDir();
			return true;
		/*case R.id.menu_logout:

			updateLoginButton(item);
			
			//startActivity(new Intent());
			return true;*/
		//possibly add more menu items here
		default:
			return false;
		}
    }
    
    //borked
    /*
    private void updateLoginButton(MenuItem accountToggle){
    	//MenuItem accountToggle = (MenuItem) findViewById(R.id.menu_logout);
    	Button runButton = (Button) findViewById(R.id.new_recording_button);
    	if (mLoggedIn) {
    		this.logOut();
    		accountToggle.setTitle(R.string.menu_logout);
    		runButton.setEnabled(true);
    	} else {
    		this.startDBAuth(); //testing
    		runButton.setEnabled(false);
    		accountToggle.setTitle(R.string.menu_login);
    		
    	}
    }*/
    
    private void listDir(){
        //testing
        if (mApi.getSession().isLinked()){
        	Log.d(TAG, "valid auth");
	        String hash = null; //TODO use
	        try {
	        	Entry dir = mApi.metadata("/", 0, hash, true, null);
	        	values = dir.contents;
	        	Log.d(TAG,"# of files: "+values.size());
	        	DBFileArrayAdapter adapter = new DBFileArrayAdapter(this, values);  //new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_1, values);
	        	setListAdapter(adapter);
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				values = null;
				setListAdapter(null);
				Log.w(TAG,"Error searching");
				this.showConnectErrorMessage();
				
				//e.printStackTrace();
			}
	        //Log.d(TAG, "test done");
        }else{
        	setListAdapter(null);
        	Log.d(TAG, "invalid auth");
        }
    }
    
    
    private void showConnectErrorMessage(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Could not Connect")
    	       .setCancelable(false)
    	       .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                AudioSnapActivity.this.listDir(); //cool
    	           }
    	       })
    	       .setNeutralButton("Reset Login", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   AudioSnapActivity.this.logOut();
    	        	   //restart the activity
    	        	   Intent intent = getIntent();
    	        	   AudioSnapActivity.this.finish();
    	        	   startActivity(intent);
    	           }
    	       })
    	       .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	                AudioSnapActivity.this.finish();
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    public void launchRecordActivity(View v){
		startActivityForResult(new Intent(this,RecordActivity.class), 5); //5 is the request code, I don't really use it
    }
    
    public void launchPlayActivity(Entry e){
    	Intent playIntent = new Intent(this,PlayActivity.class);
    	playIntent.putExtra("fileName", e.fileName());
		startActivityForResult(playIntent, 6); //6 is the request code, I don't really use it
    }
    
    
    //DB stuff below
    private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
        setListAdapter(null);
        //finish();
    }

    /**
     * Convenience function to change UI state based on being logged in
     */
    private void setLoggedIn(boolean loggedIn) {
    	mLoggedIn = loggedIn;

    }


    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
        	Log.d(TAG,"found saved certs");
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
        	Log.d(TAG,"need new certs");
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
}