package edu.uml.cs.isense.sensors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import edu.uml.cs.isense.Isense;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.experiments.Experiments;
import edu.uml.cs.isense.objects.ExperimentField;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Upload extends Activity {
	private ProgressDialog m_ProgressDialog = null;
	
	int expID = -1;
	JSONArray data;
	RestAPI rapi;
	
	HashMap<Integer, Integer> indexMap;
	
	EditText nameInput;
	EditText procedureInput;
	EditText streetInput;
	EditText citystateInput;
	EditText experimentInput;
	
    private static final int REQUEST_FIELD_MATCH = 1;
    private boolean streamingMode;

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
        	case Isense.EXPERIMENT_CODE:
        		if (resultCode == Activity.RESULT_OK) {
        			expID = data.getExtras().getInt("edu.uml.cs.isense.experiments.exp_id");
        			experimentInput.setText("" + expID);
        		}
        		break;
        	case REQUEST_FIELD_MATCH: 
        		if (resultCode == Activity.RESULT_CANCELED) {
        			showDialog(Isense.DIALOG_NO_MATCH);
        			return;
        		}

    			Bundle extras = data.getExtras();
        		
        		if (!streamingMode) {        		
        			int length = extras.getInt("edu.uml.cs.isense.fieldmatch.length");

        			indexMap = new HashMap<Integer, Integer>();
    			
        			for (int i = 0; i < length; i++) {
        				indexMap.put(i, extras.getInt("edu.uml.cs.isense.fieldmatch." + i));
        			}
    			
        			rebuildDataSet();
        		}else {
    				int sessionId = rapi.createSession(experimentInput.getText().toString(), nameInput.getText().toString(), procedureInput.getText().toString(), streetInput.getText().toString(), citystateInput.getText().toString(), "");
    				Intent intent = new Intent();
    				intent.putExtras(data);
    				intent.putExtra("edu.uml.cs.isense.fieldmatch.sid", sessionId);
    				intent.putExtra("edu.uml.cs.isense.fieldmatch.eid", Integer.parseInt(experimentInput.getText().toString()));
    				setResult(Activity.RESULT_OK, intent);
    		        finish();
    			}
    			
        		break;
        }   
    }
	
	private void rebuildDataSet() {
		int length = indexMap.size();
		
		JSONArray newData = new JSONArray();
		int datalength = this.data.length();
		
		for (int i = 0; i < length; i++) {
			int index = indexMap.get(i);
			
			for (int j = 0; j < datalength - 1; j++) {
				try {
					JSONArray row = newData.getJSONArray(j);
					row.put(this.data.getJSONArray(j+1).get(index));
					newData.put(j, row);
				} catch (JSONException e) {
					JSONArray row = new JSONArray();
					try {
						row.put(this.data.getJSONArray(j+1).get(index));
					} catch (JSONException e1) {
						e1.printStackTrace();
						continue;
					}
					newData.put(row);
				}
			}
		}
		
		this.data = newData;
		
		uploader up = new uploader();
		
		Thread thread =  new Thread(null, up, "MagentoBackground");
		thread.start();
    
		m_ProgressDialog = ProgressDialog.show(Upload.this, "Please wait...", "Uploading data...", true, true);
	}
	
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    switch(id) {
	    	case Isense.DIALOG_UPLOAD_ERROR:
	    		builder.setTitle("An error has occured!")
			  	   .setMessage("You're data may not have been uploaded to iSENSE.  Sorry.")
			       .setCancelable(false)
			       .setPositiveButton("Ok", null);
	    		dialog = builder.create();
	    		break;
	    	case Isense.DIALOG_NO_FIELDS:
	    		builder.setTitle("Incomplete form")
			  	   .setMessage("Please fill out every field")
			       .setCancelable(false)
			       .setPositiveButton("Ok", null);
	    		dialog = builder.create();
	    		break;
	    	case Isense.DIALOG_NO_MATCH:
	    		builder.setTitle("Field match failed")
	    			.setMessage("Sorry, field matching failed, please try again.")
	    			.setCancelable(false)
	    			.setPositiveButton("Ok", null);
	    		dialog = builder.create();
	    	default:
	    		dialog = null;
	    }
	    return dialog;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);
		
		rapi = RestAPI.getInstance();
		
		Bundle extras = getIntent().getExtras();
		expID = extras.getInt("edu.uml.cs.isense.upload.exp_id", -1);
		streamingMode = extras.getBoolean("edu.uml.cs.isense.upload.stream", false);
		try {
			if (!streamingMode)
				data = new JSONArray((String)extras.getString("edu.uml.cs.isense.upload.data"));
			else {
				data = new JSONArray();
				data.put(new JSONArray((String)extras.getString("edu.uml.cs.isense.upload.header")));
			}	
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		nameInput = (EditText) findViewById(R.id.NameInput);
		procedureInput = (EditText) findViewById(R.id.ProcedureInput);
		streetInput = (EditText) findViewById(R.id.StreetInput);
		citystateInput = (EditText) findViewById(R.id.CityStateInput);
		experimentInput = (EditText) findViewById(R.id.ExperimentInput);
		experimentInput.setEnabled(false);
		
		Button uploadButton = (Button) findViewById(R.id.UploadButton);
		
		if (streamingMode) uploadButton.setText("Ok");

		uploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
	    		try {
					ArrayList<ExperimentField> fields = rapi.getExperimentFields(expID);
		    		JSONArray remoteFields = new JSONArray();
		    		
		    		indexMap = new HashMap<Integer, Integer>();
		    		
		    		int length = fields.size();
		    		JSONArray header = data.getJSONArray(0);
		    		int headerLength = header.length();
		    		
		    		Intent intent = new Intent(getApplicationContext(), FieldMatch.class);
		    		
		    		for (int i = 0; i < length; i++) {
		    			String fieldName = fields.get(i).field_name.toLowerCase();
		    			
		    			remoteFields.put(fieldName);
		    			
		    			for (int j = 0; j < headerLength; j++) {
		    				String headerString = header.getString(j).toLowerCase();
		    				if (fieldName.contains(headerString) || headerString.contains(fieldName)) {
		    					indexMap.put(i, j);
		    		    		intent.putExtra("edu.uml.cs.isense.fieldmatch." + i, j);
		    				}
		    			}
		    		}
		    		
		    		if (indexMap.size() == length) {
		    			if (!streamingMode) {
		    				rebuildDataSet();
		    			} else {
		    				Intent newIntent = new Intent();
		    				newIntent.putExtras(intent);
		    				int sessionId = rapi.createSession(experimentInput.getText().toString(), nameInput.getText().toString(), procedureInput.getText().toString(), streetInput.getText().toString(), citystateInput.getText().toString(), "");
		    				intent.putExtra("edu.uml.cs.isense.fieldmatch.sid", sessionId);
		    				intent.putExtra("edu.uml.cs.isense.fieldmatch.eid", Integer.parseInt(experimentInput.getText().toString()));
			    			intent.putExtra("edu.uml.cs.isense.fieldmatch.length", indexMap.size());
		    				setResult(Activity.RESULT_OK, intent);
		    		        finish();
		    			}
		    		} else {
		    			intent.putExtra("edu.uml.cs.isense.fieldmatch.header", data.getJSONArray(0).toString());
		    			intent.putExtra("edu.uml.cs.isense.fieldmatch.remoteheader", remoteFields.toString());
		    			intent.putExtra("edu.uml.cs.isense.fieldmatch.length", indexMap.size());
		    			startActivityForResult(intent, REQUEST_FIELD_MATCH);
					
		    			expID = Integer.parseInt(experimentInput.getText().toString());
		    		}
	    		} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		Button browseButton = (Button) findViewById(R.id.BrowseButton);
		browseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent experimentIntent = new Intent(getApplicationContext(), Experiments.class);
				
				experimentIntent.putExtra("edu.uml.cs.isense.experiments.prupose", Isense.EXPERIMENT_CODE);
				
				startActivityForResult(experimentIntent, Isense.EXPERIMENT_CODE);
			}
			
		});

		if (expID != -1) {
			experimentInput.setText("" + expID);
		}
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (lm != null) {
			Criteria c = new Criteria();
			c.setAccuracy(Criteria.ACCURACY_COARSE);
			c.setAltitudeRequired(false);
			c.setBearingRequired(false);
			c.setCostAllowed(false);
			c.setPowerRequirement(Criteria.NO_REQUIREMENT);
			c.setSpeedRequired(false);
			String provider = lm.getBestProvider(c, true);
			if (provider != null) {
				Location l = lm.getLastKnownLocation(provider);
				if (l != null) {
					Geocoder g = new Geocoder(getBaseContext());
					try {
						List<Address> addrList = g.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
						if (addrList != null && addrList.size() != 0) {
							Address a = addrList.get(0);
							streetInput.setText(a.getAddressLine(0) + "");
							citystateInput.setText(a.getLocality() + "");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private class uploader implements Runnable {
		@Override
		public void run() {
    		int sessionId;
    		
    		String eid = experimentInput.getText().toString();
    		
    		sessionId = rapi.createSession(eid, nameInput.getText().toString(), procedureInput.getText().toString(), streetInput.getText().toString(), citystateInput.getText().toString(), "");

    		Boolean result = rapi.putSessionData(sessionId, eid, data);
    		
    		final Message Success = Message.obtain();
			Success.setTarget(handler);
			Success.what = 1;
			
			final Message Failure = Message.obtain();
			Failure.setTarget(handler);
			Failure.what = 0;
    		
    		if (result) {
    			Success.sendToTarget();
    		} else {
    			Failure.sendToTarget();
    		}
		}
	}
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        		if (msg.what == 1) {
        			Intent mIntent = new Intent();
        			setResult(RESULT_OK, mIntent);
    			
        			m_ProgressDialog.dismiss();
    			
        			finish();
        		} else if (msg.what == 0) {
        			m_ProgressDialog.dismiss();
    				showDialog(Isense.DIALOG_UPLOAD_ERROR);
        		}
        }
	};
	
}
