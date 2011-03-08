package edu.uml.cs.isense.sensors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uml.cs.isense.pincushion.BluetoothService;
import edu.uml.cs.isense.pincushion.BluetoothWrapper;
import edu.uml.cs.isense.pincushion.DeviceListActivity;
import edu.uml.cs.isense.Isense;
import edu.uml.cs.isense.LoginActivity;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.pincushion.pinpointInterface;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.IsenseSensor;
import edu.uml.cs.isense.sessions.SessionList;
import edu.uml.cs.isense.visualizations.ChartHandler;
import edu.uml.cs.isense.visualizations.Visualizations;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;

public class Sensors extends Activity {
	// Bluetooth states
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 4;
    private static final int REQUEST_ENABLE_BT = 5;
    
    private BluetoothAdapter mBluetoothAdapter = null;

    private static BluetoothService mBluetoothService = null;
    
    private ArrayAdapter<String> mMessageArrayAdapter;
    
    private TextView mTitle;
    
    private static String mConnectedDeviceName = null;
    
    static pinpointInterface pinpoint;
	
	public static final int TYPE_LOCATION = -2;
	public static final int TYPE_TIME = -1;
	public static final int TYPE_PINPOINT = -3;
	
	private static final int REQUEST_CODE = 100;
	public static final int SENSOR_COUNT = 10;
	
	// Intent request codes
    private static final int REQUEST_DATA_LIST_FOR_VIEWING = 1;
	private static final int REQUEST_DATA_LIST_FOR_UPLOAD = 2;
	private static final int REQUEST_FOR_STREAMING_MODE = 3;
	// Sensor and location manager setup.
	private static LocationManager lm;
	private static SensorManager sensorMan;

	//List of available Sensors
	static List<Sensor> sensors;
	private Button recordButton;
		
	public static HashMap<Integer, IsenseSensor> mSensorMap;
	private HashMap<Integer, Integer> indexMap;
	
	static final public int DIALOG_OK = 1;
	static final public int DIALOG_CANCELED = 0;
	static final public int DIALOG_SAVE = 2;
	static final public int DATA_UPDATE = 42;
	
	private RestAPI rapi;
		
	private DataDbAdapter mDbHelper;
	private Context mContext;
	
	private static ISensorsService mSensorsService;

	private boolean mIsBound = false;
	
	private final static int MENU_ITEM_LOGIN = 1;
	private final static int MENU_ITEM_LOGOUT = 2;
	private final static int MENU_ITEM_RATE = 3;
	private final static int MENU_ITEM_VIEW = 4;
	private final static int MENU_ITEM_UPLOAD = 5;
	private final static int MENU_ITEM_STREAM = 6;
	
	private int exp_id;
	private int sid;

	private static int rate = 1000;
	
	private String dataString = "";
	private String title = "";
	
	private boolean recording = false;
	private boolean streamingMode = false;
	private static boolean mBluetoothAvailable;
	
    static {
    	try {
    		BluetoothWrapper.checkAvailable();
    		mBluetoothAvailable = true;
    	} catch (Throwable t) {
    		Log.d("huh", "not avail?");
    		mBluetoothAvailable = false;
    	}
    }
	
	private OnCheckedChangeListener checkChanged = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			if (v.getId() == TYPE_LOCATION) {
				if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					buildAlertMessageNoGps();
				} else {
					mSensorsService.enableSensor(TYPE_LOCATION);
				}
				if (!isChecked) {
					mSensorsService.disableSensor(v.getId());
				}
			} else if (v.getId() == TYPE_PINPOINT) {
				if(isChecked) {
					if (mBluetoothAvailable) {
						if (!BluetoothWrapper.isEnabled()) {
							Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
							startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
						} else {
							setupConnection();
						}
					}
				} else {
					mSensorsService.disableSensor(v.getId());
				}
			} else {
				if (isChecked) {
					mSensorsService.enableSensor(v.getId());
				} else {
					mSensorsService.disableSensor(v.getId());
				}
			}
		}
	};
	
	private void setupConnection() {
        // Initialize the BluetoothService to perform bluetooth connections
        mBluetoothService = new BluetoothService(this, mHandler);
        if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
            // Start the Bluetooth service
            mBluetoothService.start();
        }
        Intent serverIntent = new Intent(getBaseContext(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
	
    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	case DATA_UPDATE:
            		Collection<IsenseSensor> collection = mSensorMap.values();
            		
            		for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
            			IsenseSensor s = it.next();
               		 	s.updateView();
            		}
            	    break;
            	case MESSAGE_STATE_CHANGE:
            		switch (msg.arg1) {
	            		case BluetoothService.STATE_CONNECTED:
	                		mSensorMap.get(TYPE_PINPOINT).setText("Connected");	               

	                		pinpoint = new pinpointInterface(mBluetoothService);
	                        mSensorsService.setPinpoint(pinpoint);
	                        
	                        mSensorsService.enableSensor(TYPE_PINPOINT);
	                        
	                		break;
	                	case BluetoothService.STATE_CONNECTING:
	                		mSensorMap.get(TYPE_PINPOINT).setText("Connecting...");	               
	                		break;
	                	case BluetoothService.STATE_LISTEN:
	                		
	                	case BluetoothService.STATE_NONE:	
	                		break;
            		}
            }
        }
    };

	
	private  ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			 mSensorsService = (ISensorsService) service;
			 mSensorsService.setup(lm, sensorMan, mHandler, rate);
			 
			 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

		    recording = settings.getBoolean("recording", false);

			rate = settings.getInt("rate", 1000);
				
		    if (recording) {
		        setProgressBarIndeterminateVisibility(true);
		    }
			 
			Collection<IsenseSensor> collection = mSensorMap.values();
			int i = 0;
			for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
				IsenseSensor s = it.next();
				if (settings.getInt("checkBox" + i, 0) == 1) {
					s.setChecked(true);
					if (recording) s.setEnabled(false);
				}
		   		 i++;
		   	}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSensorsService = null;
		}
	};
	
	void doBindService() {
        if (!mIsBound) bindService(new Intent(Sensors.this, SensorsService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
    
    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
	public Object onRetainNonConfigurationInstance() {    	
    	final String[] state = new String[3];
    	
    	state[0] = dataString;
    	
    	if (recording) {
    		state[1] = "1";
    	} else {
    		state[1] = "0";
    	}
    	
    	if (streamingMode) {
    		state[2] = "1";
    	} else {
    		state[2] = "0";
    	}
    	    	
    	return state;
	}
    
    
	protected Dialog onCreateDialog(final int id) {
	    Dialog dialog;
    	LoginActivity la = new LoginActivity(mContext);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    switch(id) {
	    case Isense.DIALOG_LOGIN_ID:
    		dialog = la.getDialog(new Handler() {
		      public void handleMessage(Message msg) { 
		    	  switch (msg.what) {
		    	  	case LoginActivity.LOGIN_SUCCESSFULL:
		    		  break;
		    	  	case LoginActivity.LOGIN_CANCELED:
		    		  break;
		    	  	case LoginActivity.LOGIN_FAILED:
		    		  break;
		    	  }
		      }
    		});
	        break;
	    case Isense.DIALOG_LOGIN_ID_WITH_MSG:
			dialog = la.getDialog(new Handler() {
			      public void handleMessage(Message msg) { 
			    	  switch (msg.what) {
			    	  	case LoginActivity.LOGIN_SUCCESSFULL:
			    	  	  Intent dIntent = new Intent(mContext, DataListActivity.class);
		                  startActivityForResult(dIntent, REQUEST_DATA_LIST_FOR_UPLOAD);
			    		  break;
			    	  	case LoginActivity.LOGIN_CANCELED:
			    		  break;
			    	  	case LoginActivity.LOGIN_FAILED:
			    		  break;
			    	  }
			      }
			}, "You need to be logged in to do that.");
			break;
	    case Isense.DIALOG_SAVE_ID:
	    	dialog = getSavePrompt(new Handler() {
				public void handleMessage(Message msg) { 
			    	  switch (msg.what) {
			    	  	case DIALOG_OK:
			    	  	  mDbHelper.createData(title, dataString);
			    	      break;
			    	  	case DIALOG_CANCELED:
			    		  break;
			    	  }
			      }
			});
	    	break;
	    case Isense.DIALOG_RECORD_ERROR:
	        builder.setMessage("There was an error recording your data, or you havn't started to record yet.")
	        	   .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        		   public void onClick(DialogInterface dialog, int id) {
	        			   dialog.dismiss();
	        		   }
	        	   })
	        	   .setCancelable(true);
	        dialog = builder.create();
	        break;
	    case Isense.DIALOG_NO_SENSORS:
	    	builder.setMessage("Please select at least one sensor before starting recording.")
	    		   .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        		   public void onClick(DialogInterface dialog, int id) {
	        			   dialog.dismiss();
	        		   }
	        	   })
	        	   .setCancelable(true);
	    	dialog = builder.create();
	    	break;
	    case Isense.DIALOG_SET_RATE:
	    	dialog = getRatePrompt();
	    	break;
	    case Isense.DIALOG_CUSTOM_RATE_ID:
	    	dialog = getRateInputPrompt();
	    	break;
	    case Isense.DIALOG_STREAM_OPTIONS:
	    	dialog = getStreamOptionsPrompt();
	    	break;
	    case Isense.DIALOG_READY_TO_STREAM:
	    	builder.setTitle("All set!")
	    	    .setMessage("When you're ready to start sending data to iSENSE just press the \"Start Streaming\" button!")
	    		.setPositiveButton("Ok", null)
     	   		.setCancelable(true);
	    	dialog = builder.create();
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
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		rapi = RestAPI.getInstance();
		mContext = this;
		
		Bundle extras = getIntent().getExtras();
		exp_id = extras.getInt("edu.uml.cs.isense.sensors.exp_id", -1);
		
		final String[] data = (String[]) getLastNonConfigurationInstance();
        
        if (data == null) {
        	recording = false;
        	dataString = "";
        	streamingMode = false;
        } else {
        	recording = Integer.parseInt(data[1]) == 1 ? true : false;        	
        	streamingMode = Integer.parseInt(data[2]) == 1 ? true : false;
        	dataString = data[0];
        	
        }
		
		mDbHelper = new DataDbAdapter(this);
		mSensorMap = new HashMap<Integer, IsenseSensor>();
		
		// Set up sensor and location managers
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		sensorMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensors = sensorMan.getSensorList(Sensor.TYPE_ALL);
				
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT );
		LinearLayout l = new LinearLayout(this);
		l.setOrientation(LinearLayout.VERTICAL);
		
		ScrollView sv = new ScrollView(this);
		sv.setBackgroundColor(Color.WHITE);
		LinearLayout.LayoutParams inlp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout inl = new LinearLayout(this);
		inl.setOrientation(LinearLayout.VERTICAL);
		inl.setBackgroundColor(Color.WHITE);
		
		int size = sensors.size();
		
		for (int i = 0; i < size; i++) {
			Sensor s = sensors.get(i);
			IsenseSensor temp = new IsenseSensor(mContext, s);
			inl.addView(temp.getLayout());
			
			temp.setListener(checkChanged);
			
			mSensorMap.put(s.getType(), temp);
		}
		
		IsenseSensor temp = new IsenseSensor(mContext, TYPE_TIME);
		inl.addView(temp.getLayout());
			
		temp.setListener(checkChanged);
		mSensorMap.put(TYPE_TIME, temp);
		
		if (lm != null) {
			temp = new IsenseSensor(mContext, TYPE_LOCATION);
			inl.addView(temp.getLayout());
				
			temp.setListener(checkChanged);
			mSensorMap.put(TYPE_LOCATION, temp);
		}
		
		if (mBluetoothAvailable) {
	    	//We should support bluetooth in versions greater than ECLAIR (API level 5).
	    	mBluetoothAdapter = BluetoothWrapper.getDefaultAdapter();
		    // If the adapter is null, then Bluetooth is not supported
		    if (mBluetoothAdapter != null) {
		    	temp = new IsenseSensor(mContext, TYPE_PINPOINT);
		    	inl.addView(temp.getLayout());
		    	
		    	temp.setListener(checkChanged);
		    	mSensorMap.put(TYPE_PINPOINT, temp);
		    }
		}

		recordButton = new Button(this);
		recordButton.setText("Start Recording");
		recordButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				toggleRecording();
			}
			
		});
		
		if (!streamingMode) {
			if (recording) recordButton.setText("Stop Recording");
		} else {
			if (recording) recordButton.setText("Stop Streaming");
			else recordButton.setText("Start Streaming");
		}
		
		inl.addView(recordButton, inlp);
		sv.addView(inl, lp);
		l.addView(sv, lp);
		
		setContentView(l);
		
		startService(new Intent(Sensors.this, SensorsService.class));
	}
	
	private void toggleRecording() {
		Collection<IsenseSensor> collection = mSensorMap.values();

		if (recording) {
			recording = false;
			setProgressBarIndeterminateVisibility(false);

			dataString = mSensorsService.stop();
			sid = 0;
			exp_id = 0;			
    		for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
    			IsenseSensor s = it.next();
       		 	s.setEnabled(true);
    		}

			if (dataString == "") {
				showDialog(Isense.DIALOG_RECORD_ERROR);
			} else {
				showDialog(Isense.DIALOG_SAVE_ID);
			}
			
			streamingMode = false;

			recordButton.setText("Start Recording");
		} else {
			JSONArray header = new JSONArray();
			    		
    		for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
    			IsenseSensor s = it.next();
       		 	s.setEnabled(false);
       		 	
       		 	if (s.getChecked()) {
       		 		for (Iterator<String> is = s.getHeader().iterator(); is.hasNext();) {
       		 			header.put(is.next());
       		 		}
       		 	}
    		}

			if (header.length() == 0) {
				for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
	    			IsenseSensor s = it.next();
	       		 	s.setEnabled(true);
	    		}
				showDialog(Isense.DIALOG_NO_SENSORS);
				return;
			}
			
			recording = true;
			
			setProgressBarIndeterminateVisibility(true);
			
			mSensorsService.start(rate, header, indexMap, sid, exp_id, streamingMode);
			
			if (!streamingMode) recordButton.setText("Stop Recording");
			else recordButton.setText("Stop Streaming");
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		doUnbindService();
		//stopService(new Intent(Sensors.this, SensorsService.class));
	}
	
	@Override
	protected void onPause() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = settings.edit();
		
		editor.putBoolean("recording", recording);
    
		Collection<IsenseSensor> collection = mSensorMap.values();
		int i = 0;
		for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
			IsenseSensor s = it.next();
   		 	if (s.getChecked()) {
   		 		editor.putInt("checkBox" + i, 1);
   		 	} else {
   		 		editor.putInt("checkBox" + i, 0);
   		 	}
   		 	i++;
   		 }
    	
    	editor.putInt("rate", rate);

    	editor.commit();
    	
		super.onPause();
	}

	@Override
	protected void onResume() {
		doBindService();
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mDbHelper.open();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mDbHelper.close();
		doUnbindService();
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		
		menu.add(Menu.NONE, MENU_ITEM_RATE, Menu.NONE, R.string.setRate);

        menu.add(Menu.NONE, MENU_ITEM_VIEW, Menu.NONE, R.string.viewData);
        menu.findItem(MENU_ITEM_VIEW).setIcon(android.R.drawable.ic_menu_view);
        
        menu.add(Menu.NONE, MENU_ITEM_UPLOAD, Menu.NONE, R.string.uploadData);
		menu.findItem(MENU_ITEM_UPLOAD).setIcon(android.R.drawable.ic_menu_upload);
		
		if (!streamingMode) 
			menu.add(Menu.NONE, MENU_ITEM_STREAM, Menu.NONE, R.string.TurnDataStreamOn);
		else
			menu.add(Menu.NONE, MENU_ITEM_STREAM, Menu.NONE, R.string.TurnDataStreamOff);
		
		if (recording) {
        	menu.findItem(MENU_ITEM_RATE).setEnabled(false);
			menu.findItem(MENU_ITEM_STREAM).setEnabled(false);
        } else {
        	menu.findItem(MENU_ITEM_RATE).setEnabled(true);
			menu.findItem(MENU_ITEM_STREAM).setEnabled(true);
        }
		
        if (rapi.isLoggedIn()) {
			menu.add(Menu.NONE, MENU_ITEM_LOGOUT, Menu.NONE, R.string.logout);
			menu.findItem(MENU_ITEM_UPLOAD).setEnabled(true);
			menu.findItem(MENU_ITEM_STREAM).setEnabled(true);
		} else {
			menu.add(Menu.NONE, MENU_ITEM_LOGIN, Menu.NONE, R.string.login);
			menu.findItem(MENU_ITEM_UPLOAD).setEnabled(false);
			menu.findItem(MENU_ITEM_STREAM).setEnabled(false);
		}
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case MENU_ITEM_LOGIN:
        		showDialog(Isense.DIALOG_LOGIN_ID);
        		return true;
        	case MENU_ITEM_LOGOUT:
        		rapi.logout();
        		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        		Editor edit = settings.edit();
        		edit.putString("username", "");
        		edit.putString("password", "");
        		edit.commit();
        		return true;	
        	case MENU_ITEM_RATE:
        		showDialog(Isense.DIALOG_SET_RATE);
        		return true;
        	case MENU_ITEM_VIEW:		
        		Intent dataIntent = new Intent(this, DataListActivity.class);
                startActivityForResult(dataIntent, REQUEST_DATA_LIST_FOR_VIEWING);
        		return true;
        	case MENU_ITEM_UPLOAD:
        		if (!rapi.isLoggedIn()) {
        			showDialog(Isense.DIALOG_LOGIN_ID_WITH_MSG);
        		} else {
        			Intent intent = new Intent(this, DataListActivity.class);
                    startActivityForResult(intent, REQUEST_DATA_LIST_FOR_UPLOAD);
        		}
                return true;
        	case MENU_ITEM_STREAM:
    			Collection<IsenseSensor> collection = mSensorMap.values();

        		for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
        			IsenseSensor s = it.next();
           		 	s.setEnabled(true);
           		 	s.setChecked(false);
        		}
        		
        		indexMap = new HashMap<Integer, Integer>();
    			sid = 0;
    			exp_id = 0;
    			
        		if (streamingMode) {
        			streamingMode = false;
        			recordButton.setText("Start Recording");
        		} else {
        			showDialog(Isense.DIALOG_STREAM_OPTIONS);
        		}
        		return true;
        }
        return false;
    }
    
    private Dialog getRatePrompt() {
    	final String[] items = new String[4];
    	int checked = 0;
    	
    	items[0] = "1";
    	items[1] = "10";
    	items[2] = "60";
    	items[3] = "Other (Currently " + rate + ")";
    	
    	switch (rate) {
    		case 1000:
    			checked = 0;
    			break;
    		case 10000:
    			checked = 1;
    			break;
    		case 60000:
    			checked = 2;
    			break;
    		default:
    			checked = 3;
    			break;
    	}

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select the record rate in seconds");
    	builder.setSingleChoiceItems(items, checked, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which < 3) {
					rate = Integer.parseInt(items[which]) * 1000;
					mSensorsService.updateRate(rate);
				} else {
	        		showDialog(Isense.DIALOG_CUSTOM_RATE_ID);
				}
				dialog.dismiss();
			}
    	});
    	AlertDialog alert = builder.create();
    	return alert;
    }
    
    private AlertDialog getRateInputPrompt() {
    	final View v;
		LayoutInflater vi = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(R.layout.sensorsavedialog, null);
		
        final EditText titleInput = (EditText) v.findViewById(R.id.titleInput);
		
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setView(v);
        
        builder.setMessage("Input rate in milliseconds")
        	   .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        		   public void onClick(DialogInterface dialog, int id) {
        			   rate = Integer.parseInt(titleInput.getText().toString());
   					   mSensorsService.updateRate(rate);
   					   dialog.dismiss();
        		   }
        	   })
        	   .setCancelable(true)
        	   .setNegativeButton("Cancel", null);
        
        return builder.create();
    }
    
    private AlertDialog getSavePrompt(final Handler h) {		
		final View v;
		LayoutInflater vi = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(R.layout.sensorsavedialog, null);
		
        final EditText titleInput = (EditText) v.findViewById(R.id.titleInput);
		
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setView(v);
        
        builder.setMessage("Save Data to phone")
        	   .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        		   public void onClick(DialogInterface dialog, int id) {
        			   final Message dialogOk = Message.obtain();
        			   dialogOk.setTarget(h);
        			   dialogOk.what = DIALOG_OK;
        				
        			   title = titleInput.getText().toString();
        			   dialogOk.sendToTarget();
        			   
        			   dialog.dismiss();
        		   }
        	   })
        	   .setCancelable(true)
        	   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
       			@Override
    			public void onClick(DialogInterface dialog, int id) {
       				final Message rejectMsg = Message.obtain();
       				rejectMsg.setTarget(h);
       				rejectMsg.what = DIALOG_CANCELED;
    				rejectMsg.sendToTarget();
    			}
        	   })
        	   .setOnCancelListener(new OnCancelListener() {
        		   public void onCancel(DialogInterface dialog) {
        			   final Message rejectMsg = Message.obtain();
        			   rejectMsg.setTarget(h);
        			   rejectMsg.what = DIALOG_CANCELED;
        			   rejectMsg.sendToTarget();
        		   }
        	   });
        
        return builder.create();
	}
    
    private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	            	   Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	            	   startActivityForResult(intent, REQUEST_CODE);
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	            	   mSensorMap.get(TYPE_LOCATION).setChecked(false); 
	            	   dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
    
    private Dialog getStreamOptionsPrompt() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Would you like to start a new session or append to an existing one?");
		builder.setPositiveButton("New", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                Intent uploadIntent = new Intent(mContext, Upload.class);
                
                JSONArray header = new JSONArray();
				
                Collection<IsenseSensor> collection = mSensorMap.values();
				
				for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
	    			IsenseSensor s = it.next();
	       		 	
	       		 	for (Iterator<String> is = s.getHeader().iterator(); is.hasNext();) {
	       		 		header.put(is.next());
	       		 	}
	    		}
				
				uploadIntent.putExtra("edu.uml.cs.isense.upload.stream", true);
				uploadIntent.putExtra("edu.uml.cs.isense.upload.header", header.toString());
				
				startActivityForResult(uploadIntent, REQUEST_FOR_STREAMING_MODE);

			}
		});
		builder.setNeutralButton("Append", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent sessionListIntent = new Intent(mContext, SessionList.class);
				
				JSONArray header = new JSONArray();
				
				Collection<IsenseSensor> collection = mSensorMap.values();
				
				for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
	    			IsenseSensor s = it.next();
	       		 	
	       		 	for (Iterator<String> is = s.getHeader().iterator(); is.hasNext();) {
	       		 		header.put(is.next());
	       		 	}
	    		}
				
				sessionListIntent.putExtra("edu.uml.cs.isense.sessionlist.header", header.toString());
                				
				startActivityForResult(sessionListIntent, REQUEST_FOR_STREAMING_MODE);
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.setCancelable(true);
		return builder.create();
    }
    
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
        	case REQUEST_DATA_LIST_FOR_VIEWING:
        		if (resultCode == Activity.RESULT_OK) {
        			Long rowId = data.getExtras().getLong(DataListActivity.DATA_ID);
        			Cursor dataCursor = mDbHelper.fetchData(rowId);
                    startManagingCursor(dataCursor);
                    dataString = dataCursor.getString(dataCursor.getColumnIndexOrThrow(DataDbAdapter.KEY_DATA));
                    String title = dataCursor.getString(dataCursor.getColumnIndexOrThrow(DataDbAdapter.KEY_TITLE));
                	try {
	                    JSONArray array = new JSONArray();
	                    JSONArray dataJSON = new JSONArray(dataString);
	                    
	                    JSONArray header = (JSONArray) dataJSON.get(0);
	                    int fieldCount = header.length();
	                    
	                    dataString = dataString.replace(header.toString() + ",", "");
	                    dataJSON = new JSONArray(dataString);
	                    	                    
	                    JSONArray fieldsJSON = new JSONArray();
	                    JSONArray metaJSON = new JSONArray();
	                    
	                    JSONObject temp = new JSONObject();

	                    for (int i = 0; i < fieldCount; i++) {
	                    	temp = new JSONObject();
	                    	String thisHeader = header.getString(i);
	                    	temp.put("field_name", thisHeader);
	                    	
	                    	if (thisHeader.contains("Time")) {
	                    		temp.put("type_id", ChartHandler.FIELD_TYPE.TIME);
		                    	temp.put("unit_abbreviation", "ms");
	                    	} else if (thisHeader.contains("Latitude")) {
	                    		temp.put("type_id", ChartHandler.FIELD_TYPE.GEO_LAT);
		                    	temp.put("unit_abbreviation", "");
	                    	} else if (thisHeader.contains("Longitude")) {
	                    		temp.put("type_id", ChartHandler.FIELD_TYPE.GEO_LON);
		                    	temp.put("unit_abbreviation", "");
	                    	} else {
	                    		temp.put("type_id", ChartHandler.FIELD_TYPE.NONE);
		                    	temp.put("unit_abbreviation", "");
	                    	}
			                        
	                    	fieldsJSON.put(temp);
	                    	
		                }
	                    	                    
	                    temp = new JSONObject();
	                    
	                    temp.put("name", title);
	                    metaJSON.put(temp);
	                    
	                    temp = new JSONObject();
	                    
	                    temp.put("fields", fieldsJSON);
	                    array.put(temp);
	                    
	                    temp = new JSONObject();
	                    temp.put("meta", metaJSON);
	                    
	                    array.put(temp);
	                    
	                    temp = new JSONObject();
	                    temp.put("data", dataJSON);
	                    
	                    array.put(temp);
	                    	                    	                    	                    
	                    Intent i = new Intent(getBaseContext(), Visualizations.class);
	                    i.putExtra("edu.uml.cs.isense.visualizations.session_list", "-1");
	            		i.putExtra("edu.uml.cs.isense.visualizations.RawJSON", array.toString());
	            		
	            		startActivity(i);	
	                    
                    } catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
        		}
        		break;
        	case REQUEST_DATA_LIST_FOR_UPLOAD:
        		if (resultCode == Activity.RESULT_OK) {
        			Long rowId = data.getExtras().getLong(DataListActivity.DATA_ID);
        			Cursor dataCursor = mDbHelper.fetchData(rowId);
                    startManagingCursor(dataCursor);
                    dataString = dataCursor.getString(dataCursor.getColumnIndexOrThrow(DataDbAdapter.KEY_DATA));
                    Intent uploadIntent = new Intent(mContext, Upload.class);
                    uploadIntent.putExtra("edu.uml.cs.isense.upload.data", dataString);
                    uploadIntent.putExtra("edu.uml.cs.isense.upload.exp_id", exp_id);
            		startActivity(uploadIntent);
        		}
        		break;
        	case REQUEST_CODE:
        		if (resultCode == 0) {
                    String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if(provider != null){
                        mSensorsService.enableSensor(TYPE_LOCATION);
                    }else{
                        mSensorMap.get(TYPE_LOCATION).setChecked(false);
                    }
        		}
        		break;
        	case REQUEST_FOR_STREAMING_MODE:
        		/*
        		 * Everything contained within this if statement is magic.
        		 * The code is not commented for a reason.  You are not suppose to try to figure it out.
        		 * Do not try to figure it out.
        		 * Do not try to change it.
        		 * I am not responsible for your sanity if you do not follow those instructions.
        		 */
        		if (resultCode == Activity.RESULT_OK) {
        			Bundle extras = data.getExtras();
        		
        			int length = extras.getInt("edu.uml.cs.isense.fieldmatch.length");
        			int place = 0;
        			sid = extras.getInt("edu.uml.cs.isense.fieldmatch.sid");
        			exp_id = extras.getInt("edu.uml.cs.isense.fieldmatch.eid");
        			indexMap = new HashMap<Integer, Integer>();

        			Collection<IsenseSensor> collection = mSensorMap.values();
        			for (int i = 0; i < length; i++) {
        				int j = extras.getInt("edu.uml.cs.isense.fieldmatch." + i);
            			indexMap.put(i, j);
        			}
        			    				
    				for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
    	    			IsenseSensor s = it.next();
    	       		 	s.setEnabled(false);
    	       		 	
    	       		 	int size = s.getHeader().size();
    	       		 	
    	       		 	for (int i = place; i < place+size; i++) {
    	       		 		if (indexMap.containsValue(i)) {
    	       		 			s.setChecked(true);
    	       		 			break;
    	       		 		}
    	       		 	}
    	       		 	
    	       		 	place += size;
    	    		}
        			
        			streamingMode = true;
        			recordButton.setText("Start Streaming");
        			showDialog(Isense.DIALOG_READY_TO_STREAM);
        		}   			
    			break;
        	case REQUEST_ENABLE_BT:
        		if (resultCode == Activity.RESULT_OK) {
        			setupConnection();
        		} else {
        			Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
        		}
        		break;
        	case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                	if (mBluetoothAvailable) {
                		BluetoothDevice device = BluetoothWrapper.getRemoteDevice(address);
                		// Attempt to connect to the device
                		mBluetoothService.connect(device);
                	}
                } else {
                    mSensorMap.get(TYPE_PINPOINT).setChecked(false);
                }
                break;
        }
    }
}