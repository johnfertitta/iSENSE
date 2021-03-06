package edu.uml.cs.isense;

import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.experiments.Experiments;
import edu.uml.cs.isense.people.People;
import edu.uml.cs.isense.profile.Profile;
import edu.uml.cs.isense.sensors.Sensors;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
 
public class Isense extends Activity {	
	static final public int EXPERIMENT_CODE = 0;
	static final public int PEOPLE_CODE = 1;
	static final public int PROFILE_CODE = 2;
	static final public int DATA_COLLECT_CODE = 3;
	static final public int SESSION_CODE = 4;
	static final public int LOGIN_CODE = 5;
	static final public int CAMERA_PIC_REQUESTED = 6;
	static final public int PICTURE_BROWSE_REQUESTED = 7;
	
	static final public int DIALOG_LOGIN_ID = 0;
	static final public int DIALOG_LOGIN_ID_WITH_MSG = 1;
	static final public int DIALOG_UPLOAD_ID = 2;
	static final public int DIALOG_SAVE_ID = 3;
	static final public int DIALOG_QUESTION = 4;
	static final public int DIALOG_UPLOAD_ERROR = 5;
	static final public int DIALOG_RECORD_ERROR = 6;
	static final public int DIALOG_NO_SENSORS = 7;
	static final public int DIALOG_NO_FIELDS = 8;
	static final public int DIALOG_X_AXIS = 9;
	static final public int DIALOG_Y_AXIS = 10;
	static final public int DIALOG_GRAPH_OPTIONS = 11;
	static final public int DIALOG_SET_RATE = 12;
	static final public int DIALOG_NO_MATCH = 13;
	static final public int DIALOG_STREAM_OPTIONS = 14;
	static final public int DIALOG_READY_TO_STREAM = 15;
	static final public int DIALOG_MAP_MODE = 16;
	static final public int DIALOG_CUSTOM_RATE_ID = 17;
	static final public int DIALOG_GET_IMG_FOR_OLD = 18;
	static final public int DIALOG_GET_IMG_FOR_NEW = 19;
	
	static final private int MENU_ITEM_LOGIN = 1;
	static final private int MENU_ITEM_LOGOUT = 2;

	
	public static Context mContext;
	private RestAPI rapi;
	
	private TextView ls;
	
	// references to the listeners for our buttons
    private OnClickListener[] mThumbListeners = {
    		new OnClickListener() {
    			public void onClick(View v) {
    				startActivity(new Intent(mContext, Experiments.class));
    			}
    		},
    		new OnClickListener() {
    			public void onClick(View v) {
    				Intent i = new Intent(mContext, Sensors.class);
    				i.putExtra("edu.uml.cs.isense.sensors.exp_id", -1);
    				startActivityForResult(i, Isense.DATA_COLLECT_CODE);
    			}
    		},
    		new OnClickListener() {
    			public void onClick(View v) {
    				startActivityForResult(new Intent(mContext, People.class), Isense.PEOPLE_CODE);
    			}
    		},
    		new OnClickListener() {
    			public void onClick(View v) {
    				Intent i = new Intent(mContext, Profile.class);
    				i.putExtra("edu.uml.cs.isense.people.user_id", rapi.getUID());
    				i.putExtra("edu.uml.cs.isense.people.name", rapi.getLoggedInUsername());
    				
    				startActivityForResult(i, Isense.PROFILE_CODE);
    			}
    		}
    };
	
	//Preferences menu.
	private SharedPreferences settings;

	protected Dialog onCreateDialog(final int id) {
	    Dialog dialog;
	    switch(id) {
	    case DIALOG_LOGIN_ID:
	    	LoginActivity la = new LoginActivity(mContext);
	        dialog = la.getDialog(new Handler() {
			      public void handleMessage(Message msg) { 
			    	  switch (msg.what) {
			    	  	case LoginActivity.LOGIN_SUCCESSFULL:
			    	  	  ls.setText("You are logged in as " + rapi.getLoggedInUsername());
			    		  break;
			    	  	case LoginActivity.LOGIN_CANCELED:
			    		  break;
			    	  	case LoginActivity.LOGIN_FAILED:
			    		  ls.setText("You are not logged in");
			    		  break;
			    	  }
			      }
        		});
                
	        break;
	    default:
	        dialog = null;
	    }
	    
	    if (dialog != null) {
	    	dialog.setOnDismissListener(new OnDismissListener() {
            	@Override
            	public void onDismiss(DialogInterface dialog) {
            		removeDialog(id);
            	}
            });
	    }
	    
	    return dialog;
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.main);

        rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), getApplicationContext());
        
    	settings = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String username = settings.getString("username", "");
    	String password = settings.getString("password", "");
    	    	
    	ls = (TextView) findViewById(R.id.LoginState);
    	
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new GridAdapter(getBaseContext())); 
        
        gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				// TODO Auto-generated method stub
				mThumbListeners[position].onClick(v);
			}
        	
        });
    	
    	if (username != "" && password != "") {
    		Thread thread =  new Thread(null, new Runnable() {
    			@Override
    			public void run() {
    				rapi.login(settings.getString("username", ""), settings.getString("password", ""));
    				if (rapi.isLoggedIn()) {
    		    		gridview.postInvalidate();
    		    		runOnUiThread(updateText);
    				}
    			}
    		}, "MagentoBackground");
	        thread.start();
    	}
    	
    	ls.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (rapi.isLoggedIn()) {
					rapi.logout();
	        		Editor edit = settings.edit();
	        		edit.putString("username", "");
	        		edit.putString("password", "");
	        		edit.commit();
	        		ls.setText("You are not logged in");
				} else {
	        		showDialog(DIALOG_LOGIN_ID);
				}
			}
    		
    	});
    }
    
    Runnable updateText = new Runnable() {
    	@Override
    	public void run() {
	    	if (rapi.isLoggedIn()) {
	    		ls.setText("You are logged in as " + rapi.getLoggedInUsername());
	    	} else {
	    		ls.setText("You are not logged in");
	    	}
    	}
    };
    
    @Override
	protected void onResume() {
		super.onResume();
		if (rapi.isLoggedIn()) {
    		ls.setText("You are logged in as " + rapi.getLoggedInUsername());
    	} else {
    		ls.setText("You are not logged in");
    	}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
			case (EXPERIMENT_CODE):
			case (PEOPLE_CODE):
			case (PROFILE_CODE):
			case (DATA_COLLECT_CODE):
			case (SESSION_CODE):
			default:
				break;
		}
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {		
		menu.clear();

		if (rapi.isLoggedIn()) {
			menu.add(Menu.NONE, MENU_ITEM_LOGOUT, Menu.NONE, R.string.logout);
		} else {
			menu.add(Menu.NONE, MENU_ITEM_LOGIN, Menu.NONE, R.string.login);
		}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case MENU_ITEM_LOGIN:
        		showDialog(DIALOG_LOGIN_ID);
        		return true;
        	case MENU_ITEM_LOGOUT:
        		rapi.logout();
        		Editor edit = settings.edit();
        		edit.putString("username", "");
        		edit.putString("password", "");
        		edit.commit();
        		ls.setText("You are not logged in");
        		return true;
        }
        return false;
    }
}