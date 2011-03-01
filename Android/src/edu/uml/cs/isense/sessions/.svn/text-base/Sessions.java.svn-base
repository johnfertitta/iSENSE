package edu.uml.cs.isense.sessions;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import edu.uml.cs.isense.Isense;
import edu.uml.cs.isense.LoginActivity;
import edu.uml.cs.isense.R;

import edu.uml.cs.isense.ImageManager;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.objects.Session;
import edu.uml.cs.isense.sensors.Sensors;
import edu.uml.cs.isense.visualizations.ChartHandler;
import edu.uml.cs.isense.visualizations.MapVis;
import edu.uml.cs.isense.visualizations.Visualizations;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

public class Sessions extends TabActivity {	
	private ArrayList<Session> m_sessions = null;
	private ArrayList<String> m_urls = null;
	private ProgressDialog m_ProgressDialog = null;
	private ImageManager im = null;
	private int expID;
	private String name;
	private RestAPI rapi;
	private Context mContext;
	private Experiment mExperiment;
	public ArrayList<ExperimentField> fList;
	private MediaGallery mg;
	private SessionList sl;
	private Uri imageUri; 
	private String img_name = "";
	private String img_desc = "";
	
	static final private int MENU_ITEM_LOGIN = 1;
	static final private int MENU_ITEM_LOGOUT = 2;
	
	protected Dialog onCreateDialog(final int id) {
	    Dialog dialog;
    	LoginActivity la = new LoginActivity(mContext);
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
				    	  	  final Intent i = new Intent(getBaseContext(), Sensors.class);
							  i.putExtra("edu.uml.cs.isense.sensors.exp_id", expID);
				    	  	  startActivityForResult(i, Isense.DATA_COLLECT_CODE);
				    		  break;
				    	  	case LoginActivity.LOGIN_CANCELED:
				    		  Log.d("sessions", "user canceled");
				    		  break;
				    	  	case LoginActivity.LOGIN_FAILED:
				    		  Log.d("sessions", "failed to log in");
				    		  break;
				    	  }
				      }
				}, "You need to be logged in to do that.");
				break;
		    case Isense.DIALOG_GET_IMG_FOR_OLD:
		    	dialog = getImageInfoDialog(new Handler() {
		    		public void handleMessage(Message msg) {
		    			switch (msg.what){
			    			case RESULT_OK:
			    				Intent intent = new Intent();
			                    intent.setType("image/*");
			                    intent.setAction(Intent.ACTION_GET_CONTENT);
			                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), Isense.PICTURE_BROWSE_REQUESTED);
			    				break;
			    			case RESULT_CANCELED:
			    				break;
		    			}
		    		}
		    	});
		    	break;
		    case Isense.DIALOG_GET_IMG_FOR_NEW:
		    	dialog = getImageInfoDialog(new Handler() {
		    		public void handleMessage(Message msg) {
		    			switch (msg.what){
			    			case RESULT_OK:
			    				//create parameters for Intent with filename
			    				ContentValues values = new ContentValues();
			    				//values.put(MediaStore.Images.Media.TITLE, img_name);
			    				//values.put(MediaStore.Images.Media.DESCRIPTION,img_desc);
			    				//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
			    				imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			    				//create new Intent
			    				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			    				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			    				startActivityForResult(intent, Isense.CAMERA_PIC_REQUESTED);
			    				break;
			    			case RESULT_CANCELED:
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
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final ArrayList<Session> sessionsList = m_sessions;
	    final ArrayList<String> urlList = m_urls;
	    final Experiment exp = mExperiment;
	    final Object[] objects = new Object[3];
	    objects[0] = (Object) sessionsList;
	    objects[1] = (Object) urlList;
	    objects[2] = (Object) exp;
	    return objects;
	    
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Isense.DATA_COLLECT_CODE) {
			if (resultCode == RESULT_OK) {
				SessionList sl = new SessionList();
				sl.loadSessionList();
			}
		} else if (requestCode == Isense.CAMERA_PIC_REQUESTED) {
			if(resultCode == RESULT_OK) {				
	            File f = convertImageUriToFile(imageUri, this);

	            rapi.uploadPicture(f, expID, img_name, img_desc);
	            mg.getMedia();
			}
		} else if (requestCode == Isense.PICTURE_BROWSE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				Uri selectedImageUri = data.getData();
	            File f = convertImageUriToFile(selectedImageUri, this);

	            rapi.uploadPicture(f, expID, img_name, img_desc);
	            mg.getMedia();
			}
		}
	}
	
	public static File convertImageUriToFile (Uri imageUri, Activity activity)  {
		Cursor cursor = null;
		try {
		    String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
		    cursor = activity.managedQuery( imageUri,
		            proj, // Which columns to return
		            null,       // WHERE clause; which rows to return (all rows)
		            null,       // WHERE clause selection arguments (none)
		            null); // Order-by clause (ascending by name)
		    int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
		    if (cursor.moveToFirst()) {
		        String orientation =  cursor.getString(orientation_ColumnIndex);
		        return new File(cursor.getString(file_ColumnIndex));
		    }
		    return null;
		} finally {
		    if (cursor != null) {
		        cursor.close();
		    }
		}
	}
	
	public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }

	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mContext = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sessions);
		
		mg = new MediaGallery();
		sl = new SessionList();
		
		im = ImageManager.getInstance();
		rapi = RestAPI.getInstance();
		
		Bundle extras = getIntent().getExtras();
		expID = extras.getInt("edu.uml.cs.isense.experiments.exp_id");
		name = extras.getString("edu.uml.cs.isense.experiments.name");
		
		setTitle("iSENSE - " + name);

		final Object data = getLastNonConfigurationInstance();
		final Object[] dataList = (Object[]) data;
		
        if (data != null) {
            // The activity was destroyed/created automatically
            m_sessions = (ArrayList<Session>) dataList[0];
            m_urls = (ArrayList<String>) dataList[1];
            mExperiment = (Experiment) dataList[2];
        }
		
		
		TabHost mTabHost = getTabHost();
		mTabHost.addTab(mTabHost.newTabSpec("experiment").setIndicator(
				name).setContent(R.id.metalayout));
		mTabHost.addTab(mTabHost.newTabSpec("sessionlist").setIndicator(
				"Sessions").setContent(R.id.sessions));
		mTabHost.addTab(mTabHost.newTabSpec("media").setIndicator(
				"Media").setContent(R.id.gallery));

		mTabHost.setCurrentTab(0);
		
		
		TabListener tl = new TabListener();

		mTabHost.setOnTabChangedListener(tl);
		
		
		if (data == null) {
			Runnable viewExperimentMeta = new Runnable(){
	            @Override
	            public void run() {
	                mExperiment = rapi.getExperiment(expID);
	                runOnUiThread(buildExperimentTab);
	            }
	        };
			
			Thread thread =  new Thread(null, viewExperimentMeta, "MagentoBackground");
	        thread.start();
	        m_ProgressDialog = ProgressDialog.show(Sessions.this, "Please wait...", "Retrieving data ...", true, true);
		} else {
			runOnUiThread(buildExperimentTab);
		}
	}

	class TabListener implements OnTabChangeListener {
		public void onTabChanged(String tabID) {
			if (tabID == "sessionlist") {
				sl.loadSessionList();
			} else if (tabID == "media") {
				mg.loadGallery();
			}
		}
	}
	
	private Runnable buildExperimentTab = new Runnable() {
		public void run() {
			Experiment e = mExperiment;
			
			if (e == null) {
				return;
			}
			
			TextView tv = (TextView) findViewById(R.id.description);
			tv.setText(e.description);
			
			tv = (TextView) findViewById(R.id.creator);
			tv.setText(e.firstname + " " + e.lastname);
			
			tv = (TextView) findViewById(R.id.created);
			tv.setText(e.timecreated);
			
			tv = (TextView) findViewById(R.id.lastupdated);
			tv.setText(e.timemodified);
			
			tv = (TextView) findViewById(R.id.tags);
			tv.setText(rapi.getExperimentTags(expID));
			
			fList = rapi.getExperimentFields(expID);
			int length = fList.size();
			String fields = "";
			for (int i = 0; i < length; i++) {
				ExperimentField f = fList.get(i);
				fields += f.field_name + " " + "(" + f.unit_abbreviation + ")" + ", ";
			}
			if (fields != "") fields = fields.substring(0, fields.lastIndexOf(","));
		
			tv = (TextView) findViewById(R.id.fields);
			tv.setText(fields);
			
            if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) m_ProgressDialog.dismiss();
		}
	};
	
	private Dialog getImageInfoDialog(Handler h) {
		final View v;
		LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.imginfo, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        
        final EditText img_name_input = (EditText) v.findViewById(R.id.imgNameInput);
		final EditText img_desc_input = (EditText) v.findViewById(R.id.imgDescInput);
        
		final Message success = Message.obtain();
		success.setTarget(h);
		success.what = RESULT_OK;
		
		final Message rejectMsg = Message.obtain();
		rejectMsg.setTarget(h);
		rejectMsg.what = RESULT_CANCELED;
		
        builder.setView(v);
        
        builder.setMessage("Enter image info")
        	   .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        		   public void onClick(DialogInterface dialog, int id) {
        			   img_name = img_name_input.getText().toString() ;
        			   img_desc = img_desc_input.getText().toString();
        			   
        			   success.sendToTarget();
    				   dialog.dismiss();
        		   }   
        	   })
        	   .setCancelable(true)
        	   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
       			@Override
    			public void onClick(DialogInterface dialog, int id) {
    				rejectMsg.sendToTarget();
     			    dialog.dismiss();
    			}
        	   })
        	   .setOnCancelListener(new OnCancelListener() {
        		   public void onCancel(DialogInterface dialog) {
        			   rejectMsg.sendToTarget();
        			   dialog.dismiss();
        		   }
        	   });
       
        return builder.create();
    
	}
	
	class MediaGallery {
		private SessionsMediaAdapter m_adapter = null;
		private Runnable viewSessions;
		private Gallery g = null;
		private TextView noMedia;
		
		public void loadGallery() {	
			findViewById(R.id.ButtonUploadPicture).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showDialog(Isense.DIALOG_GET_IMG_FOR_OLD);
				}
				
			});
			
			findViewById(R.id.ButtonTakePicture).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showDialog(Isense.DIALOG_GET_IMG_FOR_NEW);
				}
				
			});
			
			
			if (m_adapter == null) {
				m_adapter = new SessionsMediaAdapter(getBaseContext(), R.id.gallery1, new ArrayList<String>(), im);
			}
			
			noMedia = (TextView) findViewById(R.id.galleryText);
			
			g = (Gallery) findViewById(R.id.gallery1);
			g.setAdapter(m_adapter);
			g.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					ImageView imageView = (ImageView) findViewById(R.id.image1);
					
	                im.fetchBitmapOnThread(m_urls.get(position), imageView);		
	                
	           	}
			});
			
			g.setAlwaysDrawnWithCacheEnabled(true);
			g.setDrawingCacheEnabled(true);
		    
		    if (m_urls == null) {
				viewSessions = new Runnable() {
					@Override
					public void run() {
						getMedia();
					}
				};
				Thread thread = new Thread(null, viewSessions, "MagentoBackground");
				thread.start();
				m_ProgressDialog = ProgressDialog.show(Sessions.this, "Please wait...", "Retrieving data ...", true, true);
			} else {
				runOnUiThread(returnRes);
			}
		}
		
		private Runnable returnRes = new Runnable() {

			@Override
			public void run() {
				if (m_urls != null && m_urls.size() > 0) {
					if (m_adapter != null || m_adapter.isEmpty() == false) m_adapter.clear();
					m_adapter.notifyDataSetChanged();
					for (int i = 0; i < m_urls.size(); i++)
						m_adapter.add(m_urls.get(i));
				} else {
					noMedia.setVisibility(View.VISIBLE);
				}
				if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) m_ProgressDialog.dismiss();
				m_adapter.notifyDataSetChanged();
			}
		};
		
		public void getMedia() {
			try {
				m_urls = rapi.getExperimentImages(expID);
				
				if (returnRes != null) {
					runOnUiThread(returnRes);
				}
			} catch (Exception e) {
				Log.e("BACKGROUND_PROC", e.getMessage());
			}
		}
	}
	
	class SessionList {
		private SessionAdapter s_adapter = null;
		private ListView lv;
		private Runnable viewSessions;

		private Boolean isGPS(int id) {
			if (ChartHandler.FIELD_TYPE.GEO_LAT.compareTo(id + "") != 0 
					|| ChartHandler.FIELD_TYPE.GEO_LON.compareTo(id + "") != 0 
					|| ChartHandler.FIELD_TYPE.GEOSPACIAL.compareTo(id + "") != 0) return true;
			return false;
		}
		
		public void loadSessionList() {
			lv = (ListView) findViewById(R.id.sessionlist);			
	
			findViewById(R.id.ButtonView).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							String sessions = "";
							Vector<Integer> selectedSessions = new Vector<Integer>();
							int childCount = m_sessions.size();
							
							for (int i = 0; i < childCount; i++) {
								Session s = m_sessions.get(i);
								if (s.checked) {
									selectedSessions.add(s.session_id);
									sessions += s.session_id + "+";
								}
							}
							
							if (sessions != "") {
								sessions = sessions.substring(0, sessions.lastIndexOf("+"));

								if (fList.size() == 2) {
									int id_1 = fList.get(0).type_id;
									int id_2 = fList.get(1).type_id;
									
									if (isGPS(id_1) && isGPS(id_2)) {
										final Intent i = new Intent(getBaseContext(), MapVis.class);
										i.putExtra("edu.uml.cs.isense.visualizations.session_list", sessions);
										i.putExtra("edu.uml.cs.isense.visualizations.noplot", true);
										startActivity(i);
									} else {
										final Intent i = new Intent(getBaseContext(), Visualizations.class);
										i.putExtra("edu.uml.cs.isense.visualizations.session_list", sessions);
										startActivity(i);
									}
								
								} else {
									final Intent i = new Intent(getBaseContext(), Visualizations.class);
									i.putExtra("edu.uml.cs.isense.visualizations.session_list", sessions);
									startActivity(i);
								}
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
								builder.setTitle("Please select a session to view first.");
								builder.setCancelable(true);
								builder.setPositiveButton("Ok", null);
								AlertDialog alert = builder.create();	
								alert.show();
							}
							
						}
					});

			findViewById(R.id.ButtonAll).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							setAll(true);							
						}
					});

			findViewById(R.id.ButtonNone).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							setAll(false);
						}
					});
			
			findViewById(R.id.ButtonContribute).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View arg0) {	
							if (!rapi.isLoggedIn()) {
								showDialog(Isense.DIALOG_LOGIN_ID_WITH_MSG);
							} else {
								final Intent i = new Intent(getBaseContext(), Sensors.class);
								i.putExtra("edu.uml.cs.isense.sensors.exp_id", expID);
								startActivityForResult(i, Isense.DATA_COLLECT_CODE);
							}
						}
					});
			
			if (s_adapter == null) {
				this.s_adapter = new SessionAdapter(getBaseContext(), R.layout.sessionrow, new ArrayList<Session>());
			}

			lv.setAdapter(this.s_adapter);
			
			lv.requestFocus();
	
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> av, View v,int position, long id) {
					CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.middletext);
					ctv.toggle();
					s_adapter.toggle(position);
				}

			});
			
			
			if (m_sessions == null) {
				m_sessions = new ArrayList<Session>();
			
				viewSessions = new Runnable() {
					@Override
					public void run() {
						getSessions();
					}
				};
				Thread thread = new Thread(null, viewSessions, "MagentoBackground");
				thread.start();
				m_ProgressDialog = ProgressDialog.show(Sessions.this, "Please wait...", "Retrieving data ...", true, true);
			} else {
				runOnUiThread(returnRes);
			}
		}
		
		public void setAll(boolean value) {
			int length = m_sessions.size();
			
			for (int i = 0; i < length; i++) {
				s_adapter.set(i, value);
			}
			
			
			int childCount = lv.getChildCount();
			CheckedTextView ctv = null;
			
			for (int i = 0; i < childCount; i++) {
				ctv = (CheckedTextView) lv.getChildAt(i).findViewById(R.id.middletext);
				ctv.setChecked(value);
				s_adapter.set(i, value);
			}
		}
		
		private Runnable returnRes = new Runnable() {

			@Override
			public void run() {
				if (m_sessions != null && m_sessions.size() > 0) {
					s_adapter.notifyDataSetChanged();
					for (int i = 0; i < m_sessions.size(); i++)
						s_adapter.add(m_sessions.get(i));
				}
				if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) m_ProgressDialog.dismiss();
				s_adapter.notifyDataSetChanged();
			}
		};
		
		private void getSessions() {
			try {
				m_sessions = rapi.getSessions(expID);
				
				if (returnRes != null) {
					runOnUiThread(returnRes);
				}
			} catch (Exception e) {
				Log.e("BACKGROUND_PROC", e.getMessage());
			}
	}
  }
}
