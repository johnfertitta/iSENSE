package edu.uml.cs.isense.visualizations;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uml.cs.isense.Isense;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.SessionData;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;

public class Visualizations extends Activity {
	private String sessions;
	private RestAPI rapi;
	private ChartHandler handler;
	private ArrayList<SessionData> mSessionData;
	private WebView wv;
	
    private final static int MENU_ITEM_SELECT_X = 1;
    private final static int MENU_ITEM_SELECT_Y = 2;
    private final static int MENU_ITEM_OPTIONS = 3;
    private final static int MENU_ITEM_SHARE = 4;
    private final static int MENU_ITEM_MAP = 5;
    private final static int MENU_ITEM_DATA_TABLE = 6;
	private ProgressDialog mProgressDialog;
	private Bundle extras;
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final ArrayList<SessionData> sessionData = mSessionData;
	    final JSONArray graphData = handler.getData();
	    final JSONObject graphOptions = handler.getOptions();
	    final String ses = sessions;
	    final int xindex = handler.getDataFieldXIndex();
	    final ArrayList<Integer> yindex = handler.getDataFieldYIndex();
	    final JSONArray graphFields = handler.getFields();

	    final Object[] objects = new Object[7];
	    objects[0] = (Object) sessionData;
	    objects[1] = (Object) graphData;
	    objects[2] = (Object) graphOptions;
	    objects[3] = (Object) ses;
	    objects[4] = (Object) xindex;
	    objects[5] = (Object) yindex;
	    objects[6] = (Object) graphFields;
	    return objects;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rapi = RestAPI.getInstance();
		
		setContentView(R.layout.visualizations);        
        wv = (WebView) findViewById(R.id.visualizationsWebView);
                
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLightTouchEnabled(true);
        webSettings.setRenderPriority(RenderPriority.HIGH);
		
    	extras = getIntent().getExtras();
		
		final Object data = getLastNonConfigurationInstance();
		final Object[] dataList = (Object[]) data;
		
        if (data != null) {
            // The activity was destroyed/created automatically
            sessions = (String) dataList[3];
            mSessionData = (ArrayList<SessionData>) dataList[0];
            
            handler = new ChartHandler(wv, mSessionData, this);
            
        	handler.setData((JSONArray) dataList[1]);
        	handler.setOptions((JSONObject) dataList[2]);
        	handler.setFields((JSONArray) dataList[6]);
        	handler.setDataFieldXIndex((Integer)dataList[4]);
        	handler.setDataFieldYIndex((ArrayList<Integer>) dataList[5]);
            handler.loadChartPage();
        } else {
        	sessions = extras.getString("edu.uml.cs.isense.visualizations.session_list");
        	retrieveSessionData();
        }

    }
	
	public void retrieveSessionData() {
		if (sessions.compareTo("-1") != 0) {
			Runnable download = new Runnable() {
				@Override
				public void run() {
		    		mSessionData = rapi.sessiondata(sessions);
		    		runOnUiThread(startChart);
				}
			};
			Thread thread =  new Thread(null, download, "MagentoBackground");
	        thread.start();
	        mProgressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving Data ...", true, true);
    	} else {
    		try {
				JSONArray array = new JSONArray((String)extras.get("edu.uml.cs.isense.visualizations.RawJSON"));
				mSessionData = new ArrayList<SessionData>();
				SessionData ses = new SessionData();
				ses.RawJSON = null;
				
				ses.FieldsJSON = array.getJSONObject(0).getJSONArray("fields");
				ses.MetaDataJSON = array.getJSONObject(1).getJSONArray("meta");
				ses.DataJSON = array.getJSONObject(2).getJSONArray("data");
				
				int fieldCount = ses.FieldsJSON.length();

				ses.fieldData = new ArrayList<ArrayList<String>>();
				
				for (int j = 0; j < fieldCount; j++) {
					ArrayList<String> tempList = new ArrayList<String>();
					int dataLength = ses.DataJSON.length();
					for (int z = 0; z < dataLength; z++) {
						tempList.add(ses.DataJSON.getJSONArray(z).getString(j));
					}
					ses.fieldData.add(tempList);
				}
				mSessionData.add(ses);
				handler = new ChartHandler(wv, mSessionData, Visualizations.this);
				handler.loadChartPage();			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
	
	private Runnable startChart = new Runnable() {
		@Override
		public void run() {
			handler = new ChartHandler(wv, mSessionData, Visualizations.this);
			handler.loadChartPage();
    		if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
		}
	};
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private boolean hasGPS() {
		int length = handler.getFields().length();
			
		for (int i = 0; i < length; i++) {
			if (handler.isTypeLat(i) || handler.isTypeLon(i) || handler.isTypeGeospacial(i)) return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		
		menu.add(Menu.NONE, MENU_ITEM_SELECT_X, Menu.NONE, R.string.XAxisSelect);
		menu.add(Menu.NONE, MENU_ITEM_SELECT_Y, Menu.NONE, R.string.YAxisSelect);
		menu.add(Menu.NONE, MENU_ITEM_OPTIONS, Menu.NONE, R.string.GraphOptions);
		menu.add(Menu.NONE, MENU_ITEM_SHARE, Menu.NONE, R.string.GraphShare);
		menu.findItem(MENU_ITEM_SHARE).setIcon(android.R.drawable.ic_menu_share);
		
		if (hasGPS()) {
			menu.add(Menu.NONE, MENU_ITEM_MAP, Menu.NONE, R.string.GraphMap);
			menu.findItem(MENU_ITEM_MAP).setIcon(android.R.drawable.ic_menu_mapmode);
		}
		
		if (handler.hasData()) menu.add(Menu.NONE, MENU_ITEM_DATA_TABLE, Menu.NONE, R.string.DataTable);

        return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case MENU_ITEM_SELECT_X:
    			showDialog(Isense.DIALOG_X_AXIS);
        		break;
        	case MENU_ITEM_SELECT_Y:
    			showDialog(Isense.DIALOG_Y_AXIS);
        		break;
        	case MENU_ITEM_OPTIONS:
        		showDialog(Isense.DIALOG_GRAPH_OPTIONS);
        		break;
        	case MENU_ITEM_SHARE:
				Picture pic = wv.capturePicture();
				
				Bitmap image = Bitmap.createBitmap(wv.getWidth() - 5, wv.getHeight() - 5, Bitmap.Config.ARGB_8888);
				
				pic.draw(new Canvas(image));
				
				Uri uri = null;
				
				uri = Uri.parse(Images.Media.insertImage(getContentResolver(), image, getIntent().getStringExtra(Intent.EXTRA_TITLE), null));
	        
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("image/jpeg");
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this data set from the iSENSE Android App!");
				sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(sendIntent, "Share using:"));
				break;
        	case MENU_ITEM_MAP:
        		Intent i = new Intent(getBaseContext(), MapVis.class);
				i.putExtras(getIntent());
				if (!handler.hasData()) i.putExtra("edu.uml.cs.isense.visualizations.noplot", true);
	    	  	startActivity(i);
	    	  	break;
        	case MENU_ITEM_DATA_TABLE:
        		Intent d = new Intent(getBaseContext(), DataTable.class);
        		String dataString = "";
        		int size = mSessionData.size();
        		
        		for (int j = 0; j < size; j++) {
        			SessionData current = mSessionData.get(j);
        			int length = current.DataJSON.length();
        			
        			for (int k = 1; k < length; k++) {
        				try {
							JSONArray temp = current.DataJSON.getJSONArray(k);
							
							dataString += temp.toString().replace("[", "").replace("]", "").replace("\"", "") + "\n";
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        				
        			}
        			
        		}
        		
        		String header = "";
        		try {
        			header = mSessionData.get(0).DataJSON.getJSONArray(0).toString().replace("[", "").replace("]", "").replace("\"", "") + "\n";
        		} catch (JSONException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		
        		d.putExtra("edu.uml.cs.isense.visualizations.data", header + dataString);
        		startActivity(d);
        		break;
        }
        return false;
    }
    
    protected Dialog onCreateDialog(final int id) {
	    Dialog dialog;

	    switch(id) {
	    	case Isense.DIALOG_X_AXIS:
	    		dialog = getXAxisPrompt();
	    		break;
	    	case Isense.DIALOG_Y_AXIS:
	    		dialog = getYAxisPrompt();
	    		break;
	    	case Isense.DIALOG_GRAPH_OPTIONS:
	    		dialog = getOptionsPrompt();
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
    
    private Dialog getOptionsPrompt() {
    	final String[] items = new String[2];
    	final boolean[] checked=  new boolean[2];
    	
    	items[0] = "Lines";
    	items[1] = "Legend";
    	
    	JSONObject opts = handler.getOptions();
    	
    	try {
			checked[0] = opts.getJSONObject("lines").getBoolean("show");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			checked[0] = false;
		}
		
		try {
			checked[1] = opts.getJSONObject("legend").getBoolean("show");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			checked[1] = false;
		}

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select the options");
    	builder.setMultiChoiceItems(items, checked, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checked[which] = isChecked;
				if (isChecked) {
					handler.addOption(items[which].toLowerCase(), handler.getShow(true));
				} else {
					handler.addOption(items[which].toLowerCase(), handler.getShow(false));
				}
				handler.loadGraph();
			}
    	});
    	AlertDialog alert = builder.create();
    	return alert;
    }
    
    private Dialog getXAxisPrompt() {
    	ArrayList<String> rawItems = handler.getFieldNames();
    	ArrayList<String> itemsList = new ArrayList<String>();
    	int length = rawItems.size();
    	
    	for (int i = 0; i < length; i++) {
    		if (handler.isTypeGeospacial(i) || handler.isTypeLat(i) || handler.isTypeLon(i)) continue;
    		itemsList.add(rawItems.get(i));
    	}
    	
    	itemsList.add("Data Point Number");
    	
    	final int[] ids = new int[itemsList.size()];
    	int j = 0;
    	for (int i = 0; i < length; i++) {
    		if (handler.isTypeGeospacial(i) || handler.isTypeLat(i) || handler.isTypeLon(i)) continue;
    		ids[j++] = i;
    	}
       
    	ids[ids.length - 1] = -1;
    	
    	length = itemsList.size();
    	String[] items = new String[length];
    	for (int i = 0; i < length; i++) {
    		items[i] = itemsList.get(i);
    	}
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a field");
		builder.setCancelable(true);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
		    	handler.createGraphData(ids[id], handler.getDataFieldYIndex());
		    }
		});
		AlertDialog alert = builder.create();	
		
		return alert;
	}
    
    private Dialog getYAxisPrompt() {
    	ArrayList<String> rawItems = handler.getFieldNames();
    	ArrayList<String> itemsList = new ArrayList<String>();
    	int length = rawItems.size();

    	for (int i = 0; i < length; i++) {
    		if (handler.isTypeGeospacial(i) || handler.isTypeLat(i) || handler.isTypeLon(i) || handler.isTypeTime(i)) continue;
    		itemsList.add(rawItems.get(i));
    	}
    	
    	final int[] ids = new int[itemsList.size()];
    	int j = 0;
    	for (int i = 0; i < length; i++) {
    		if (handler.isTypeGeospacial(i) || handler.isTypeLat(i) || handler.isTypeLon(i) || handler.isTypeTime(i)) continue;
    		ids[j++] = i;
    	}
    	
    	length = itemsList.size();
    	String[] items = new String[length];
    	final boolean checked[] = new boolean[length];
    	for (int i = 0; i < length; i++) {
    		items[i] = itemsList.get(i);
    		
    		if (handler.isFieldDisplayed(ids[i])) checked[i] = true;
    		else checked[i] = false;
    	}
    	
    
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Pick the fields to display");
    	builder.setMultiChoiceItems(items, checked, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checked[which] = isChecked;
			}
    	});
    	builder.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				ArrayList<Integer> items = new ArrayList<Integer>();
				for (int i = 0; i < checked.length; i++) {
					if (checked[i]) items.add(ids[i]);
				}
				handler.createGraphData(handler.getDataFieldXIndex(), items);
			}
    	});

    	AlertDialog alert = builder.create();
		
		return alert;
    }

}
