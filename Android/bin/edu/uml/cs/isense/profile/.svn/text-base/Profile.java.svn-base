package edu.uml.cs.isense.profile;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.ImageManager;
import edu.uml.cs.isense.Isense;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.objects.Item;
import edu.uml.cs.isense.objects.Session;
import edu.uml.cs.isense.sessions.Sessions;
import edu.uml.cs.isense.visualizations.Visualizations;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

public class Profile extends TabActivity {	
	private Item m_item = null;
	private ProgressDialog m_ProgressDialog = null;
	private ImageManager im = null;
	private Runnable viewItems;
	private int userID;
	public String name;
	private RestAPI rapi;
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final Item items = m_item;
	    return items;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		im = ImageManager.getInstance();
		rapi = RestAPI.getInstance();
		
		Bundle extras = getIntent().getExtras();
		userID = extras.getInt("edu.uml.cs.isense.people.user_id");
		name = extras.getString("edu.uml.cs.isense.people.name");

		setTitle("iSENSE - " + name);
		
		TabHost mTabHost = getTabHost();
		mTabHost.addTab(mTabHost.newTabSpec("experimentlist").setIndicator(
				"Experiments").setContent(R.id.experiments));
		mTabHost.addTab(mTabHost.newTabSpec("sessionlist").setIndicator(
				"Sessions").setContent(R.id.sessions));

		mTabHost.setCurrentTab(0);
		
		TabListener tl = new TabListener();

		mTabHost.setOnTabChangedListener(tl);
		
		final Object data = getLastNonConfigurationInstance();
		if (data != null) {
			m_item = (Item) data;
			runOnUiThread(loadExperimentList);
		} else {
			m_item = new Item();
			
			viewItems = new Runnable() {
				@Override
				public void run() {
					try {
						m_item = rapi.getProfile(userID);
						runOnUiThread(loadExperimentList);
					} catch (Exception e) {
						Log.e("BACKGROUND_PROC", e.getMessage());
					}
				}
			};
			
			Thread thread = new Thread(null, viewItems, "MagentoBackground");
			thread.start();
			m_ProgressDialog = ProgressDialog.show(Profile.this, "Please wait...", "Retrieving data ...", true, true);
		}
	}

	class TabListener implements OnTabChangeListener {
		public void onTabChanged(String tabID) {
			if (tabID == "sessionlist") {
				SessionList sl = new SessionList();
				sl.loadSessionList();
			}
		}
	}
	
	private Runnable loadExperimentList = new Runnable() {
		private ProfileExperimentAdapter e_adapter = null;
		private ListView lv;

		public void run() {
			lv = (ListView) findViewById(R.id.experimentlist);			

			if (userID != rapi.getUID()) {
				int length = m_item.e.size();
				for (int i = 0; i < length; i++) {
					if (m_item.e.get(i).hidden != 0) {
						m_item.e.remove(i--);
						length--;
					}
				}
			}
			
			if (e_adapter == null) {
				this.e_adapter = new ProfileExperimentAdapter(getBaseContext(), R.layout.experimentrow, m_item.e, im);
			}

			lv.setAdapter(this.e_adapter);
			
			lv.requestFocus();
			
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> l, View v, int position, long id) {					
					Experiment e = m_item.e.get(position);
					
					Intent i = new Intent(getBaseContext(), Sessions.class);
					i.putExtra("edu.uml.cs.isense.experiments.exp_id", e.experiment_id);
					i.putExtra("edu.uml.cs.isense.experiments.name", e.name);
				
					startActivityForResult(i, Isense.SESSION_CODE);
					
				}
				
			});
			
            if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) m_ProgressDialog.dismiss();
		}
	};
	
	class SessionList {
		private ProfileSessionAdapter s_adapter = null;
		private ListView lv;
		
		public void loadSessionList() {
			lv = (ListView) findViewById(R.id.sessionlist);			
			
			if (s_adapter == null) {
				this.s_adapter = new ProfileSessionAdapter(getBaseContext(), R.layout.profilesessionrow, new ArrayList<Session>());
			}

			lv.setAdapter(this.s_adapter);
			
			lv.requestFocus();
			
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> l, View v, int position, long id) {					
					int sid = s_adapter.getItem(position).session_id;
					
					final Intent i = new Intent(getBaseContext(), Visualizations.class);
					i.putExtra("edu.uml.cs.isense.visualizations.session_list", sid + "");
					startActivity(i);
				}
				
			});
			
			
			runOnUiThread(Populator);
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
			}
		};
	}
}
