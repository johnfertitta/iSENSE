package edu.uml.cs.isense.sensors;

import java.util.HashMap;

import org.json.JSONArray;

import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Handler;

public interface ISensorsService {
	public void setup(LocationManager lm, SensorManager sm, Handler handler, int rate);
	
	public void start(int rate, JSONArray header, HashMap<Integer, Integer> map, int id, int expid, boolean s);

	public void start(int rate, JSONArray header);
	
	public String stop();
	
	public boolean enableSensor(int id);
	
	public void disableSensor(int id);
	
	public void updateRate(int rate);
}
