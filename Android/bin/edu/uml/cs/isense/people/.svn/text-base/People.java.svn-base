package edu.uml.cs.isense.people;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.ImageManager;
import edu.uml.cs.isense.Isense;
import edu.uml.cs.isense.LoginActivity;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Person;
import edu.uml.cs.isense.profile.Profile;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class People extends ListActivity {
	private ArrayList<Person> m_people = null;
	private PeopleAdapter m_adapter;
	private RestAPI rapi;
	private Context mContext;
	private ImageManager im;
	
	static final private int MENU_ITEM_LOGIN = 1;
	static final private int MENU_ITEM_LOGOUT = 2;
	
	protected Dialog onCreateDialog(final int id) {
	    Dialog dialog;
	    switch(id) {
	    case Isense.DIALOG_LOGIN_ID:
	    	LoginActivity la = new LoginActivity(mContext);
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
	
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final ArrayList<Person> list = m_adapter.items;
	    final int loaded = m_adapter.itemsLoaded;
	    final boolean allLoaded = m_adapter.allItemsLoaded;
	    final int page = m_adapter.page;
	    Object[] objs = new Object[4];
	    objs[0] = list;
	    objs[1] = loaded;
	    objs[2] = allLoaded;
	    objs[3] = page;
	    return objs;
	}
	
	/** Called when the activity is first created. */
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people);
        m_people = new ArrayList<Person>();
        rapi = RestAPI.getInstance();
        mContext = this;

        im = ImageManager.getInstance();

        final Object data = getLastNonConfigurationInstance();
        final Object[] dataList = (Object[]) data;
        // The activity is starting for the first time, load the data from the site
        if (data != null) {
            // The activity was destroyed/created automatically
        	m_people = (ArrayList<Person>) dataList[0];
        } else {
        	m_people = new ArrayList<Person>();
        }
        
        this.m_adapter = new PeopleAdapter(getBaseContext(), R.layout.peoplerow, R.layout.loadrow, m_people, im);
        
        if (data != null) {
        	m_adapter.itemsLoaded = (Integer) dataList[1];
        	m_adapter.allItemsLoaded = (Boolean) dataList[2];
        	m_adapter.page = (Integer) dataList[3];
        }
                
        setListAdapter(this.m_adapter);
        
        EditText et = (EditText) findViewById(R.id.PeopleSerchInput);
        et.setSingleLine(true);
        et.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s == null || s.length() == 0) {
					m_people = new ArrayList<Person>();
					m_adapter = new PeopleAdapter(getBaseContext(), R.layout.peoplerow, R.layout.loadrow, m_people, im);
			        setListAdapter(m_adapter);
				} else {
					m_people = new ArrayList<Person>();
					m_adapter = new PeopleAdapter(getBaseContext(), R.layout.peoplerow, R.layout.loadrow, m_people, im);
					m_adapter.action = "search";
					m_adapter.query = s.toString();
			        setListAdapter(m_adapter);
				}
			}
        	
        });
    }

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Person p = m_people.get(position);
		
		Intent i = new Intent(getBaseContext(), Profile.class);
		i.putExtra("edu.uml.cs.isense.people.user_id", p.user_id);
		i.putExtra("edu.uml.cs.isense.people.name", p.firstname);
		
		startActivityForResult(i, Isense.PROFILE_CODE);
		setResult(RESULT_OK, i);		
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
        }
        return false;
    }
    
    
}

