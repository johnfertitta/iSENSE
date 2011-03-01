package edu.uml.cs.isense.visualizations;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uml.cs.isense.objects.SessionData;

import android.app.Activity;
import android.app.ProgressDialog;
import android.webkit.WebView;

public class ChartHandler {
	private WebView mAppView;
	private String mGraphTitle = "";
	private int mSessionCount = 0;
	private int mDataFieldXIndex = 0;
	private JSONArray mGraphData = null;
	private JSONObject mGraphOptions = null;
	private JSONArray mGraphFields = null;
	private ArrayList<Integer> mDataFieldYIndex;
	private ArrayList<SessionData> mSessionData;
	private ProgressDialog mProgressDialog;
	private Activity mActivity;
	
	private static final String CHART_FILE = "file:///android_asset/flot/html/basechart.html";
	private static final String EMPTY_FILE = "file:///android_asset/flot/html/empty.html";
	private static final String JAVASCRIPT_PROPERTYNAME = "graphdata";
	private static final String DATE_FORMAT_STRING = "MM/dd/yyyy HH:mm:ss";
	
	public static class FIELD_TYPE {
	  public static final String STRING = "-1";
	  public static final String TIME = "7";
	  public static final String GEOSPACIAL = "19";
	  public static final String GEO_LAT = "57";
	  public static final String GEO_LON = "58";
	  public static final String NONE = "0";
	}

	public ChartHandler(WebView appView, ArrayList<SessionData> data, Activity a) {
		this.mAppView = appView;
		mSessionData = data;
		mGraphOptions = new JSONObject();
		mGraphData = new JSONArray();
		mGraphFields = mSessionData.get(0).FieldsJSON;
		mSessionCount = mSessionData.size();
		mActivity = a;
	}
        
    public String getGraphTitle() {
		return mGraphTitle;
	}  

    public void setGraphTitle(String title) {
    	mGraphTitle = title;
    }
    
    /**
    * This gets called by the javascript file after the graph is done plotting
    */
    public void finishGraph() {
    	if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }
    
    public synchronized void setGraphData() {
    	if (hasData()) {
    		loadGraph();
    		return;
    	}
		mSessionCount = mSessionData.size();
			
		//to ensure that setting graph data will result in something usable, we take the first field that isn't time or geo and plot it against the datapoint number
		int length = mSessionData.get(0).FieldsJSON.length();
			
		mDataFieldXIndex = -1;
		mDataFieldYIndex = new ArrayList<Integer>();
		
		for (int i = 0; i < length; i++) {
			if(isTypeTime(i) || isTypeLat(i) || isTypeLon(i) || isTypeGeospacial(i) || isTypeString(i)) continue;
			mDataFieldYIndex.add(i);
		}
			
		for (int i = 0; i < length; i++) {
			if (isTypeTime(i)) { 
				mDataFieldXIndex = i;
				break;
			}
		}
							
		createGraphData(mDataFieldXIndex, mDataFieldYIndex);
		ResetOptions();
    }
    
    private Runnable createGraphDataThread = new Runnable() {
		@Override
		public void run() {
			int fieldLength = mDataFieldYIndex.size();
			
	    	mGraphData = new JSONArray();
		    for (int i = 0; i < mSessionCount; i++) {
		    	SessionData currentSession = mSessionData.get(i);
				for (int j = 0; j < fieldLength; j++) {
					JSONObject field = new JSONObject();
					JSONArray dataPoints = new JSONArray();
					ArrayList<String> currentSet = currentSession.fieldData.get(mDataFieldYIndex.get(j));
					
					int lenght = currentSet.size();
					
					for (int z = 0; z < lenght; z++) {
						JSONArray singlePoint = new JSONArray();
						String yValue = currentSet.get(z);
						
						if (mDataFieldXIndex == -1) {
							singlePoint.put(z);
						} else {
							String xValue = currentSession.fieldData.get(mDataFieldXIndex).get(z);
							if (isTypeTime(mDataFieldXIndex)) xValue = formatTimeString(xValue);
							singlePoint.put(xValue);
						}
						singlePoint.put(yValue);
						dataPoints.put(singlePoint);
					}
					try {
						field.put("data", dataPoints);
						field.put("label", getSessionName(i) + " - " + getFieldName(mDataFieldYIndex.get(j)));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

					mGraphData.put(field);
				}
		    }
			setXAxisMode();	
	    	mActivity.runOnUiThread(loadGraphRunnable);
		}
    };
    
    public void createGraphData(int xAxisIndex, int yAxisIndex) {
    	ArrayList<Integer> temp = new ArrayList<Integer>();
    	temp.add(yAxisIndex);
    	createGraphData(xAxisIndex, temp);
    }
    
    public void createGraphData(int xAxisIndex, ArrayList<Integer> yAxisIndex) {
		mDataFieldYIndex = yAxisIndex;
		mDataFieldXIndex = xAxisIndex;
		
		Thread thread =  new Thread(null, createGraphDataThread, "MagentoBackground");
        thread.start();
        mProgressDialog = ProgressDialog.show(mActivity, "Please wait...", "Building chart ...", true, true);
    }
    
    private String formatTimeString(String time) {
    	DateFormat df = new SimpleDateFormat(DATE_FORMAT_STRING);
    	Date parsed;
    	long unixts = 0;
    	
    	try {
    		unixts = Long.parseLong(time);
    		
    		//Need to see if it is in millis or seconds
    		Calendar cal = Calendar.getInstance();
    	    Long currentMillis = df.parse(df.format(cal.getTime())).getTime();
    	    if (unixts * 1000 <= currentMillis) {
    	    	unixts *= 1000;
    	    }
    	} catch (NumberFormatException e) {
    		try {
				parsed = df.parse(time);
				unixts = parsed.getTime();
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	} catch ( ParseException e2) {
    		
    	}

    	return unixts + "";
    }
    
    public ArrayList<String> getFieldNames() { 
    	int length = mGraphFields.length();
    	ArrayList<String> ret = new ArrayList<String>();
		for (int i = 0; i < length; i++) {
			try {
				ret.add(mGraphFields.getJSONObject(i).getString("field_name") + " (" +  mGraphFields.getJSONObject(i).getString("unit_abbreviation") + ")");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret;
    }
    
    public String getFieldName(int index) {
    	ArrayList<String> fields = getFieldNames();
    	return fields.get(index);
    }
    
    public String getSessionName(int index) {
    	try {
			return mSessionData.get(index).MetaDataJSON.getJSONObject(0).getString("name");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
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
	
	public boolean isTypeTime(int index) {
		return isType(index, FIELD_TYPE.TIME);
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
	
	public boolean isTypeString(int index) {
		return isType(index, FIELD_TYPE.STRING);
	}
	
	public JSONArray getFields() {
		return mGraphFields;
	}
	
	public void setFields(JSONArray fields) {
		mGraphFields = fields;
	}
    
	public Boolean hasData() {
		if (mGraphData.length() == 0 || mGraphData == null) {
			return false;
		}
		return true;
	}
	
    public JSONArray getData() {
    	return mGraphData;
    }
    
    public void setData(JSONArray data) {
    	mGraphData = data;
    }
    
    public synchronized void setXAxisMode() {
    	mGraphOptions.remove("xaxis");
    	
		JSONObject xAxis = new JSONObject();

		try {
		    if (mDataFieldXIndex != -1 && isTypeTime(mDataFieldXIndex)) {
		    	xAxis.put("mode", "time");
			} else {
				xAxis.put("mode", null);
			}
			mGraphOptions.put("xaxis", xAxis);

	    } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
		
    public JSONObject makeJSON(String opt, Object value) {
    	JSONObject ret = new JSONObject();
    	
    	try {
			ret.put(opt, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return ret;
    }
    
    public synchronized void addOption(String opt, JSONObject value) {
    	mGraphOptions.remove(opt);
    	try {
			mGraphOptions.put(opt, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public synchronized void removeOption(String opt) {
    	mGraphOptions.remove(opt);
    }
    
    public synchronized void ResetOptions() {
    	try {
    		mGraphOptions = new JSONObject();
			mGraphOptions.put("points", getShow(true));
			mGraphOptions.put("legend", getShow(true));
			
			JSONObject xAxis;

			if (mDataFieldXIndex != -1 && isTypeTime(mDataFieldXIndex)) {
				xAxis = makeJSON("mode", "time");
				mGraphOptions.put("lines", getShow(true));
			} else {
				xAxis = makeJSON("mode", null);
				mGraphOptions.put("lines", getShow(false));
			}
			
			mGraphOptions.put("xaxis", xAxis);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public synchronized JSONObject getOptions() {
    	return mGraphOptions;
    }
    
    public void setOptions(JSONObject opts) {
    	mGraphOptions = opts;
    }
    
    private Runnable loadGraphRunnable = new Runnable() {
    	@Override
		public void run() {
    		if (!hasData()) {
    			mAppView.loadUrl(EMPTY_FILE);
    			finishGraph();
    			return;
    		}
    		
			int width = mAppView.getWidth() - 5;
	    	int height = mAppView.getHeight() - 5;
	    	
	    	mAppView.loadUrl("javascript:SetGraph(\"" + width + "px\", \"" + height + "px\")");
	    	mAppView.loadUrl("javascript:GotGraph(" + mGraphData.toString() + ", " + mGraphOptions.toString() + ")");
		}
    };
    
    public void loadGraph() {
    	mActivity.runOnUiThread(loadGraphRunnable);
    }
    
    public synchronized void loadChartPage() {
    	mAppView.addJavascriptInterface(this, JAVASCRIPT_PROPERTYNAME);
    	mAppView.loadUrl(CHART_FILE);
    }
    
    protected JSONObject getShow(boolean val) {
    	JSONObject ret = new JSONObject();
    	try {
    		ret.put("show", val);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return ret;
    }
    
    public void setDataFieldXIndex(int index) {
    	mDataFieldXIndex = index;
    }
    
    public int getDataFieldXIndex() {
    	return mDataFieldXIndex;
    }
    
    public boolean isFieldDisplayed(int index) {
    	int length = mDataFieldYIndex.size();
    	for (int i = 0; i < length; i++) {
    		if (mDataFieldYIndex.get(i) == index) return true;
    	}
    	return false;
    }
    
    public void setDataFieldYIndex(ArrayList<Integer> index) {
    	mDataFieldYIndex = index;
    }
    
    public ArrayList<Integer> getDataFieldYIndex() {
    	return mDataFieldYIndex;
    }
   
}