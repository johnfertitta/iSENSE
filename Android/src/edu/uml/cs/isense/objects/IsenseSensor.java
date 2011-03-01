package edu.uml.cs.isense.objects;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.uml.cs.isense.sensors.Sensors;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class IsenseSensor {
	private CheckBox mCheckBox;
	private TextView mTextView;
	private ArrayList<Object> mValues;
	private ArrayList<String> mHeader;
	private Context mContext;
	private Sensor mSensor;
	private static RelativeLayout mLayout;
	private int mSensorType;
	private String mSensorName;
	
	public IsenseSensor(Context c, int type) {
		mContext = c;
		mSensor = null;
		mSensorType = type;
		
		mHeader = new ArrayList<String>();
		
		if (type == Sensors.TYPE_TIME) {
			mHeader.add("Time");
			mSensorName = "Time";
		} else if (type == Sensors.TYPE_LOCATION) {
			mHeader.add("Latitude");
			mHeader.add("Longitude");
			mHeader.add("Altitude");
			mSensorName = "Location";
		}
		
		mValues = new ArrayList<Object>();
		
		mLayout = new RelativeLayout(mContext);
		mLayout.setBackgroundColor(Color.WHITE);
		
		mTextView = new TextView(mContext);
		mTextView.setId(mSensorType * 100);
		mTextView.setTextColor(Color.BLACK);
		
		mCheckBox = new CheckBox(mContext);
		mCheckBox.setId(mSensorType);
		mCheckBox.setTextColor(Color.BLACK);
		
		 
		mLayout.addView(mCheckBox);
		
		RelativeLayout.LayoutParams temp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		temp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		temp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mLayout.addView(mTextView, temp);
		
		mCheckBox.setText(mSensorName);
	}
	
	public IsenseSensor(Context c, Sensor s) {
		mContext = c;
		mSensor = s;
				
		mHeader = new ArrayList<String>();
		mSensorType = mSensor.getType();
		
		if (mSensorType == Sensor.TYPE_ACCELEROMETER || mSensorType == Sensor.TYPE_MAGNETIC_FIELD) {
			String[] raw = mSensor.getName().split(" ");
			String temp = "";
			for (int i = 2; i < raw.length; i++) {
				temp += raw[i] + " ";
			}
			temp = temp.replace("sensor", "");
			temp = temp.trim();
			mSensorName = temp;
			mHeader.add(temp + " X");
			mHeader.add(temp + " Y");
			mHeader.add(temp + " Z");
			mHeader.add(temp);
		} else {
			String[] raw = mSensor.getName().split(" ");
			String temp = "";
			for (int i = 1; i < raw.length; i++) {
				temp += raw[i] + " ";
			}
			temp = temp.replace("sensor", "");
			temp = temp.trim();
			mSensorName = temp;
			mHeader.add(temp);
		}
		
		mValues = new ArrayList<Object>();
		
		mLayout = new RelativeLayout(mContext);
		mLayout.setBackgroundColor(Color.WHITE);
		
		mTextView = new TextView(mContext);
		mTextView.setId(mSensorType * 100); //I seriously have to do this because Android doesn't know how to gracefully handle view elements with the same ID
		mTextView.setTextColor(Color.BLACK);
		
		mCheckBox = new CheckBox(mContext);
		mCheckBox.setId(mSensorType);
		mCheckBox.setTextColor(Color.BLACK);
		 
		mLayout.addView(mCheckBox);
		
		RelativeLayout.LayoutParams temp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		temp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		temp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mLayout.addView(mTextView, temp);
		
		mCheckBox.setText(mSensorName);
	}
	
	public void setListener(OnCheckedChangeListener l) {
		mCheckBox.setOnCheckedChangeListener(l);
	}
	
	public Boolean toggleEnabled() {
		mCheckBox.setEnabled(!mCheckBox.isEnabled());
		
		return mCheckBox.isEnabled();
	}
	
	public Boolean setEnabled(Boolean value) {
		mCheckBox.setEnabled(value);
		
		return mCheckBox.isEnabled();
	}
	
	public Boolean toggleChecked() {
		mCheckBox.toggle();
		
		return mCheckBox.isChecked();
	}
	
	public Boolean setChecked(Boolean value) {
		mCheckBox.setChecked(value);
		
		return mCheckBox.isChecked();
	}
	
	public Boolean getChecked() {
		return mCheckBox.isChecked();
	}
	
	public Boolean setValue(ArrayList<Object> newValues) {
		mValues.clear();
				
		return mValues.addAll(newValues);
	}
	
	public ArrayList<Object> getValues() {
		return mValues;
	}
	
	public String getValueString() {
		return mValues.toString().replaceAll("[\\[\\]]", "");
	}
	
	public void updateView() {
		if (!mCheckBox.isChecked()) {
			mTextView.setText("");
			return;
		}

		if (mSensorType == Sensors.TYPE_TIME) {
			Long time = (Long) mValues.get(0);
			Date d = new Date(time);
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			mTextView.setText(df.format(d));
		} else {
			mTextView.setText(mValues.toString().replaceAll("[\\[\\]]", ""));
		}
	}
	
	public RelativeLayout getLayout() {
		return mLayout;
	}
	
	public String getHeaderString() {
		return mHeader.toString().replaceAll("[\\[\\]]", "");
	}
	
	public ArrayList<String> getHeader() {
		return mHeader;
	}
}
