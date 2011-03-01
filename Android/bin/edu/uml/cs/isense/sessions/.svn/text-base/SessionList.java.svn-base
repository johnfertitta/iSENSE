package edu.uml.cs.isense.sessions;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.objects.Item;
import edu.uml.cs.isense.objects.Session;
import edu.uml.cs.isense.profile.ProfileSessionAdapter;
import edu.uml.cs.isense.sensors.FieldMatch;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class SessionList extends ListActivity {
	private ProfileSessionAdapter s_adapter = null;
	private ProgressDialog m_ProgressDialog = null;
	private RestAPI rapi;
	private ArrayList<Session> m_sessions;
	private Item m_item;
	private JSONArray header;
    private static final int REQUEST_FIELD_MATCH = 1;

    private static int sid;
    private static int eid;

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
        	case REQUEST_FIELD_MATCH:
        		if (resultCode == Activity.RESULT_OK) {
        			Bundle extras = data.getExtras();
    			
        			Intent intent = new Intent();
        			intent.putExtras(extras);
        			intent.putExtra("edu.uml.cs.isense.fieldmatch.sid", sid);
        			intent.putExtra("edu.uml.cs.isense.fieldmatch.eid", eid);
	            
        			setResult(Activity.RESULT_OK, intent);
        			finish();
        		}
        }
    }
    
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final Item items = m_item;
	    return items;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sessionlist);
        rapi = RestAPI.getInstance();
        
        setResult(Activity.RESULT_CANCELED);
        
        Bundle extras = getIntent().getExtras();
        try {
			header = new JSONArray(extras.getString("edu.uml.cs.isense.sessionlist.header"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                
    	m_sessions = new ArrayList<Session>();

    	this.s_adapter = new ProfileSessionAdapter(getBaseContext(), R.layout.profilesessionrow, m_sessions);
        
        setListAdapter(this.s_adapter); 
    	
        final Object data = getLastNonConfigurationInstance();
        // The activity is starting for the first time, load the data from the site
        if (data != null) {
            // The activity was destroyed/created automatically
        	m_item = (Item) data;
			runOnUiThread(Populator);
        } else {
        	m_item = new Item();
             
     		Runnable getItems = new Runnable() {
     			@Override
     			public void run() {
     				try {
     					m_item = rapi.getProfile(rapi.getUID());
     					runOnUiThread(Populator);
     				} catch (Exception e) {
     					Log.e("BACKGROUND_PROC", e.getMessage());
     				}
     			}
     		};
     		
     		Thread thread = new Thread(null, getItems, "MagentoBackground");
     		thread.start();
     		m_ProgressDialog = ProgressDialog.show(SessionList.this, "Please wait...", "Retrieving data ...", true, true);
        }

    }
	
	private Runnable Populator = new Runnable() {
		@Override
		public void run() {
			if (m_item != null && m_item.s.size() > 0) {
				s_adapter.notifyDataSetChanged();
				int size = m_item.s.size();
				for (int i = size - 1; i >= 0; i--) 
					s_adapter.add(m_item.s.get(i));
			}
			s_adapter.notifyDataSetChanged();
			if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) m_ProgressDialog.dismiss();
		}
	};
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		sid = s_adapter.getItem(position).session_id;
		eid = s_adapter.getItem(position).experiment_id;

		ArrayList<ExperimentField> fields = rapi.getExperimentFields(eid);
		JSONArray remoteFields = new JSONArray();
		
		HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
		
		int length = fields.size();
		int headerLength = header.length();
		
		Intent intent = new Intent(getApplicationContext(), FieldMatch.class);

		try {	
			for (int i = 0; i < length; i++) {
				String fieldName = fields.get(i).field_name.toLowerCase();
				remoteFields.put(fieldName);
			
				for (int j = 0; j < headerLength; j++) {
					String headerString;
					headerString = header.getString(j).toLowerCase();

					if (fieldName.contains(headerString) || headerString.contains(fieldName)) {
						indexMap.put(i, j);
						intent.putExtra("edu.uml.cs.isense.fieldmatch." + i, j);
					}
				}
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (indexMap.size() == length) {
			Intent newIntent = new Intent();
			newIntent.putExtras(intent);
			newIntent.putExtra("edu.uml.cs.isense.fieldmatch.length", length);
			newIntent.putExtra("edu.uml.cs.isense.fieldmatch.sid", sid);
            newIntent.putExtra("edu.uml.cs.isense.fieldmatch.eid", eid);

            setResult(Activity.RESULT_OK, newIntent);
            finish();
		} else {
			intent.putExtra("edu.uml.cs.isense.fieldmatch.header", header.toString());
			intent.putExtra("edu.uml.cs.isense.fieldmatch.remoteheader", remoteFields.toString());
			intent.putExtra("edu.uml.cs.isense.fieldmatch.length", indexMap.size());
			startActivityForResult(intent, REQUEST_FIELD_MATCH);		
		}
	}
}
