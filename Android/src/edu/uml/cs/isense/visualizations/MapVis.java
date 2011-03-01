package edu.uml.cs.isense.visualizations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.uml.cs.isense.Isense;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.SessionData;
import edu.uml.cs.isense.visualizations.ChartHandler.FIELD_TYPE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;

public class MapVis extends MapActivity {
	private ArrayList<SessionData> mSessionData;
	private String sessions;
	private RestAPI rapi;
	private int mSessionCount = 0;
	private JSONArray mGraphFields = null;
	private int mLatIndex = -1;
	private int mLonIndex = -1;
	private List<Overlay> mapOverlays;
	private MyItemizedOverlay[] mItemizedoverlays;
	private ProgressDialog mProgressDialog;
	private Drawable[] drawables;
	private Context mContext;
	private MapView mapView;
	private int pointDensity = 10;
	private MapController mapController;
	
	private final static int MENU_ITEM_POINT_DENSITY = 1;
	private final static int MENU_ITEM_DATA_PLOT = 2;
	private final static int MENU_ITEM_DATA_TABLE = 3;
	private final static int MENU_ITEM_MAP_MODE = 4;
	
	private Boolean noplot = false;
	
	private Bundle extras;
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final int density = pointDensity;
	    final List<Overlay> overlays = mapOverlays;
	    final Object[] objects = new Object[2];
	    
	    objects[0] = density;
	    objects[1] = overlays;
	    
	    return objects;
	}

	protected Dialog onCreateDialog(final int id) {
	    Dialog dialog;

	    switch(id) {
	    	case Isense.DIALOG_GRAPH_OPTIONS:
	    		dialog = getDensityPrompt();
	    		break;
	    	case Isense.DIALOG_MAP_MODE:
	        	String[] items = new String[2];
	        	items[0] = "Map";
	        	items[1] = "Satellite";
	        	
	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    		builder.setTitle("Map mode");
	    		builder.setCancelable(true);
	    		builder.setItems(items, new DialogInterface.OnClickListener() {
	    		    public void onClick(DialogInterface dialog, int id) {
	    		    	if (id == 1)
	    		    		mapView.setSatellite(true);
	    		    	else
	    		    		mapView.setSatellite(false);
	    		    }
	    		});
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapvis);
	    
	    rapi = RestAPI.getInstance();
	    
		extras = getIntent().getExtras();
		sessions = extras.getString("edu.uml.cs.isense.visualizations.session_list");
		noplot = extras.getBoolean("edu.uml.cs.isense.visualizations.noplot", false);
		
	    mapView = (MapView) findViewById(R.id.mapview);
		mapController = mapView.getController();
	    mapView.setBuiltInZoomControls(true);
	    
	    final Object data = getLastNonConfigurationInstance();
		final Object[] dataList = (Object[]) data;
		
		mapOverlays = mapView.getOverlays();
		
		if (data != null) {
			pointDensity = (Integer) dataList[0];
			mapOverlays.addAll((List<Overlay>) dataList[1]);
		}
		
	    drawables = new Drawable[10];
	    
	    drawables[0] = this.getResources().getDrawable(R.drawable.blue_marker);
	    drawables[1] = this.getResources().getDrawable(R.drawable.red_marker);
	    drawables[2] = this.getResources().getDrawable(R.drawable.orange_marker);
	    drawables[3] = this.getResources().getDrawable(R.drawable.green_marker);
	    drawables[4] = this.getResources().getDrawable(R.drawable.brown_marker);
	    drawables[5] = this.getResources().getDrawable(R.drawable.purple_marker);
	    drawables[6] = this.getResources().getDrawable(R.drawable.pink_marker);
	    drawables[7] = this.getResources().getDrawable(R.drawable.yellow_marker);
	    drawables[8] = this.getResources().getDrawable(R.drawable.darkgreen_marker);
	    drawables[9] = this.getResources().getDrawable(R.drawable.paleblue_marker);
	             
	    mContext = this;

		if (data == null) {
		    retrieveSessionData();
		}
	}
	
	public void retrieveSessionData() {
		if (sessions.compareTo("-1") != 0) {
			Runnable getData = new Runnable() {
		    	@Override
		    	public void run() {
		    		mSessionData = rapi.sessiondata(sessions);
				
		    		mGraphFields = mSessionData.get(0).FieldsJSON;
		    		mSessionCount = mSessionData.size();
			    
		    		mItemizedoverlays = new MyItemizedOverlay[mSessionCount];

		    		int length = mGraphFields.length();
			    
		    		for (int i = 0; i < length; i++) {
		    			if (isUnitLon(i) || isTypeLon(i)) mLonIndex = i;
		    			if (isUnitLat(i) || isTypeLat(i)) mLatIndex = i;
		    			if (mLatIndex != -1 && mLonIndex != -1) break;
		    		}
		    		
				    runOnUiThread(createGraphDataThread);
		    	}
		    };
	    	Thread thread =  new Thread(null, getData, "MagentoBackground");
	    	thread.start();
	    	mProgressDialog = ProgressDialog.show(this, "Please wait...", "Building Map...", true, true);
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
	    		mGraphFields = mSessionData.get(0).FieldsJSON;					
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			createGraphData();
	    }
		
	}

	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		
		menu.add(Menu.NONE, MENU_ITEM_POINT_DENSITY, Menu.NONE, R.string.PointDensity);
		//menu.add(Menu.NONE, MENU_ITEM_MEASURED_FIELD, Menu.NONE, R.string.MeasuredField);
		menu.add(Menu.NONE, MENU_ITEM_MAP_MODE, Menu.NONE, R.string.MapMode);
		menu.findItem(MENU_ITEM_MAP_MODE).setIcon(android.R.drawable.ic_menu_mapmode);
		if (!noplot) menu.add(Menu.NONE, MENU_ITEM_DATA_PLOT, Menu.NONE, R.string.GraphPlot);
		
		menu.add(Menu.NONE, MENU_ITEM_DATA_TABLE, Menu.NONE, R.string.DataTable);
		
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case MENU_ITEM_POINT_DENSITY:
        		showDialog(Isense.DIALOG_GRAPH_OPTIONS);
    			break;
        	case MENU_ITEM_MAP_MODE:
        		showDialog(Isense.DIALOG_MAP_MODE);
        		break;
        	case MENU_ITEM_DATA_PLOT:
        		finish();
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
    
    private Dialog getDensityPrompt() {
    	final String[] items = new String[4];
    	int checked = 0;
    	final int[] values = new int[4];
    	
    	items[0] = "10%";
    	items[1] = "25%";
    	items[2] = "50%";
    	items[3] = "100%";
    	values[0] = 10;
    	values[1] = 4;
    	values[2] = 2;
    	values[3] = 1;
    	
    	switch (pointDensity) {
    		case 10:
    			checked = 0;
    			break;
    		case 4:
    			checked = 1;
    			break;
    		case 2:
    			checked = 2;
    			break;
    		case 1:
    			checked = 3;
    			break;
    	}

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select the percentage of points to display");
    	builder.setSingleChoiceItems(items, checked, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pointDensity = values[which];
				createGraphData();
				dialog.dismiss();
			}
    	});
    	AlertDialog alert = builder.create();
    	return alert;
    }
	
	private Runnable createGraphDataThread = new Runnable() {
		@Override
		public void run() {			
			int fieldCount = mGraphFields.length();
			mapOverlays.clear();
			Double avgLat = 0.0;
			Double avgLon = 0.0;
			Double pointCount = 0.0;
		    for (int i = 0; i < mSessionCount; i++) {
		    	try {
					mItemizedoverlays[i] = new MyItemizedOverlay(drawables[i % 10], mContext);
					SessionData currentSession = mSessionData.get(i);
					ArrayList<String> latList = currentSession.fieldData.get(mLatIndex);
					ArrayList<String> lonList = currentSession.fieldData.get(mLonIndex);
					ArrayList<ArrayList<String>> fields = currentSession.fieldData;
					
					int length = latList.size();

					if (length == 0) {
						continue;
					}

					String sessionName = getSessionName(i);
					
					int increment = length / (length/pointDensity);
					
					if (increment < 0) increment = 1;
							    	
					for (int z = 0; z < length; z += increment) {
						try {
							Double lat = Double.parseDouble(latList.get(z))*1E6;
							Double lon = Double.parseDouble(lonList.get(z))*1E6;
							
							avgLat += lat;
							avgLon += lon;
							pointCount++;
						
							String popupText = "";
							for (int j = 0; j < fieldCount; j++) {
								if (j == mLatIndex || j == mLonIndex) continue;
								try {
									JSONObject field = currentSession.FieldsJSON.getJSONObject(j);
									popupText += field.getString("field_name") + ": " + fields.get(j).get(z) + " " + field.getString("unit_abbreviation") + "\n";
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							if (popupText.compareTo("") != 0) popupText.subSequence(0, popupText.lastIndexOf("\n"));
						
							GeoPoint point = new GeoPoint(lat.intValue(), lon.intValue());
							OverlayItem overlayitem = new OverlayItem(point, sessionName + ", Datapoint #" + z + 1, popupText);

							mItemizedoverlays[i].addOverlay(overlayitem);
						} catch (NumberFormatException e) {
							continue;
						}
					}

					mItemizedoverlays[i].callPopulate();
					mapOverlays.add(mItemizedoverlays[i]);
				} catch (ArrayIndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					continue;
				}
		    }
		    try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
		    avgLat /= pointCount;
		    avgLon /= pointCount;
		    GeoPoint center = new GeoPoint(avgLat.intValue(), avgLon.intValue());
		    mapController.animateTo(center);
		    mapView.postInvalidate();

		}
    };
    
    public String getSessionName(int index) {
    	try {
			return mSessionData.get(index).MetaDataJSON.getJSONObject(0).getString("name");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
    }
    
    public void createGraphData() {
		Thread thread =  new Thread(null, createGraphDataThread, "MagentoBackground");
        thread.start();
        mProgressDialog = ProgressDialog.show(this, "Please wait...", "Building map ...", true, true);
    }
	
    public boolean isUnitLat(int index) {
    	try {
			if (mGraphFields.getJSONObject(index).getString("unit_id").compareTo("57") == 0) return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
    }
    
    public boolean isUnitLon(int index) {
    	try {
			if (mGraphFields.getJSONObject(index).getString("unit_id").compareTo("58") == 0) return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
    }
    
	public boolean isType(int index, String type) {
		try {
			if (mGraphFields.getJSONObject(index).getString("type_id").compareTo(type) == 0) return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isTypeGeospacial(int index) {
		return isType(index, FIELD_TYPE.GEOSPACIAL);
	}
	
	public boolean isTypeLat(int index) {
		return isType(index, FIELD_TYPE.GEO_LAT);
	}
	
	public boolean isTypeLon(int index) {
		return isType(index, FIELD_TYPE.GEO_LON);
	}
	
}
