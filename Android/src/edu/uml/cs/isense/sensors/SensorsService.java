package edu.uml.cs.isense.sensors;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.IsenseSensor;
import edu.uml.cs.isense.pincushion.BluetoothService;
import edu.uml.cs.isense.pincushion.pinpointInterface;

public class SensorsService extends Service {
	private Timer recordTimer;
	private Timer timeTimer;
	private Timer updateTimer;
	private Timer pinPointUpdater;
	private int INTERVAL;
	
	private LocationManager mLocationManager;
	private LocationUpdateListener locationListener;
	
	private SensorManager mSensorManager;
	private SensorListener sensorListener;
	
	private HashMap<Integer, Integer> indexMap;
	
	private JSONArray data;
	
	private int sid;
	private int eid;
	private boolean streaming = false;
	
	private RestAPI rapi;
	
	private Handler mHandler;
			
	private boolean timeTimerRunning = false;
	private boolean updateTimerRunning = false;
	private boolean pinPointTimerRunning = false;
	
	private boolean isSetup = false;
		
    private static pinpointInterface mPinpoint = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		if (recordTimer != null) {
			recordTimer.cancel();
		}
		if (updateTimer != null) {
			updateTimer.cancel();
		}
		if (timeTimer != null) {
			timeTimer.cancel();
		}
		if (pinPointUpdater != null) {
			pinPointUpdater.cancel();
		}
		
		mSensorManager.unregisterListener(sensorListener);
		mLocationManager.removeUpdates(locationListener);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new SensorServiceBinder();
	}
	
	public class SensorServiceBinder extends Binder implements ISensorsService {
		public void setPinpoint(pinpointInterface ppi) {
			mPinpoint = ppi;
		}
		
		public void setup(LocationManager lm, SensorManager sm, Handler handler, int rate) {
			if (isSetup) return;
			
			rapi = RestAPI.getInstance();

			INTERVAL = rate;
			
			mLocationManager = lm;
			mSensorManager = sm;

			locationListener = new LocationUpdateListener();
			sensorListener = new SensorListener();

			mHandler = handler;
			isSetup = true;
		}

		@Override
		public void start(int rate, JSONArray header) {
			start(rate, header, null, -1, -1, false);
		}
		
		public void start(int rate, JSONArray header, HashMap<Integer, Integer> map, int id, int expid, boolean s) {
	        INTERVAL = rate;
	        data = new JSONArray();
	        data.put(header);
	        streaming = s;
	        indexMap = map;
	        sid = id;
	        eid = expid;

			startService();
	    }
		
		public void updateRate(int rate) {
			INTERVAL = rate;
			
			if (timeTimerRunning) {
				if (timeTimer != null) {
					timeTimer.cancel();
					timeTimerRunning = false;
					startTime();
					timeTimerRunning = true;
				}
			}
			
			if (updateTimerRunning) {
				if (updateTimer != null) {
					updateTimer.cancel();
					updateTimerRunning = false;
					startUpdater();
					updateTimerRunning = true;
				}
			}
		}
		
		public String stop() {
			stopService();
			return data.toString();
		}
		
		public boolean enableSensor(int id) {
			if (!updateTimerRunning) {
				startUpdater();
				updateTimerRunning = true;
			}
			
			if (id == Sensors.TYPE_TIME) {
				if (!timeTimerRunning) 	{
					startTime();
					timeTimerRunning = true;
				}
			} else if (id == Sensors.TYPE_LOCATION) {
				if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
					mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, locationListener);
					return true;
				} else {
					return false;
				}
			} else if (id == Sensors.TYPE_PINPOINT) {
				startPinPoint();
				pinPointTimerRunning = true;
			} else {
				return mSensorManager.registerListener(sensorListener, mSensorManager.getDefaultSensor(id), SensorManager.SENSOR_DELAY_NORMAL);
			}
			
			return true;
		}

		public void disableSensor(int id) {
			if (id == Sensors.TYPE_TIME) {
				if (timeTimerRunning) {
					if (timeTimer != null) {
						timeTimer.cancel();
						timeTimerRunning = false;
					}
				}
			} else if (id == Sensors.TYPE_LOCATION) {
				mLocationManager.removeUpdates(locationListener);
			} else if (id == Sensors.TYPE_PINPOINT) {
				if (pinPointTimerRunning) {
					if (pinPointUpdater != null) {
						pinPointUpdater.cancel();
						pinPointTimerRunning = false;
					}
				}
				mPinpoint = null;
			} else {
				mSensorManager.unregisterListener(sensorListener, mSensorManager.getDefaultSensor(id));
			}
			
			if (updateTimerRunning) {
				Collection<IsenseSensor> collection = Sensors.mSensorMap.values();

				for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
		    		IsenseSensor s = it.next();
		    		if (s.getChecked()) return;
				}
				
				if (updateTimer != null) {
					updateTimer.cancel();
					updateTimerRunning = false;
				}
			}
			
		}
	}

	private void startPinPoint() {
		pinPointUpdater = new Timer();
		pinPointUpdater.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				IsenseSensor i = Sensors.mSensorMap.get(Sensors.TYPE_PINPOINT);
				try {
					i.setValue(mPinpoint.requestDataStream());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 0, INTERVAL);
	}
	
	private void startService() {
		recordTimer = new Timer();
		recordTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				appendData();
			}
		}, 0, INTERVAL);
	}
	
	private void stopService() {
		if (recordTimer != null) {
			recordTimer.cancel();
		}
	}
	
	private void startUpdater() {
		updateTimer = new Timer();
		updateTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				mHandler.sendMessage(mHandler.obtainMessage(Sensors.DATA_UPDATE));
			}
		}, 0, INTERVAL);
	}
	
	private void startTime() {
		timeTimer = new Timer();
		timeTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				ArrayList<Object> temp = new ArrayList<Object>();
				temp.add(System.currentTimeMillis());
				Sensors.mSensorMap.get(Sensors.TYPE_TIME).setValue(temp);
			}
		}, 0, INTERVAL);
	}
	
	public void appendData() {
		JSONArray temp = new JSONArray();
	
		Collection<IsenseSensor> collection = Sensors.mSensorMap.values();
    		
    	for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
    		IsenseSensor s = it.next();
    		if (s.getChecked()) {
    			for (Iterator<Object> i = s.getValues().iterator(); i.hasNext();) {
    				temp.put(i.next());
    			}
    		}
    	}

		data.put(temp);

		if (streaming) {
			//we are streaming!
			final JSONArray streamData = new JSONArray();

        	for (Iterator<IsenseSensor> it = collection.iterator(); it.hasNext(); ) {
        		IsenseSensor s = it.next();
        		for (Iterator<Object> i = s.getValues().iterator(); i.hasNext();) {
        			streamData.put(i.next());
        		}
        	}

			Runnable upload = new Runnable() {
				@Override
				public void run() {
					JSONArray newData = rebuildDataSet(streamData);
					rapi.updateSessionData(sid, eid + "", newData);
				}
			};
			Thread thread = new Thread(null, upload, "MagentoBackground");
			thread.start();
		}
	}

	private JSONArray rebuildDataSet(JSONArray data) {
		int length = indexMap.size();
		
		JSONArray newData = new JSONArray();
		
		for (int i = 0; i < length; i++) {
			int index = indexMap.get(i);
			
			try {
				JSONArray row = newData.getJSONArray(0);
				row.put(data.get(index));
				newData.put(0, row);
			} catch (JSONException e) {
				JSONArray row = new JSONArray();
				try {
					row.put(data.get(index));
				} catch (JSONException e1) {
					e1.printStackTrace();
					continue;
				}
				newData.put(row);
			}
		}
		
		JSONArray ret;
		try {
			ret = newData.getJSONArray(0);
			return ret;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private class SensorListener implements SensorEventListener {	
		private float[] isenseFormat(float[] raw) {
			float[] vals = raw;
			
			DecimalFormat format = new DecimalFormat("0.00");
			
			for (int i = 0; i < vals.length; i++) {
				vals[i] = Float.parseFloat(format.format(vals[i]));
			}
			
			return vals;
		}
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			ArrayList<Object> valList = new ArrayList<Object>();
			float[] vals = isenseFormat(event.values);
			int type = event.sensor.getType();
			IsenseSensor s = Sensors.mSensorMap.get(type);
			DecimalFormat format = new DecimalFormat("0.00");
			switch(type) {
				case Sensor.TYPE_ACCELEROMETER:
				case Sensor.TYPE_MAGNETIC_FIELD:
					valList.add(vals[0]);
					valList.add(vals[1]);
					valList.add(vals[2]);
					valList.add(Float.parseFloat(format.format(Math.sqrt(Math.pow(vals[0], 2) + Math.pow(vals[1], 2) + Math.pow(vals[2], 2)))));
					break;
				case Sensor.TYPE_ORIENTATION:
				case Sensor.TYPE_TEMPERATURE:
				case Sensor.TYPE_GYROSCOPE:
				case Sensor.TYPE_LIGHT:
				case Sensor.TYPE_PRESSURE:
				case Sensor.TYPE_PROXIMITY:
				default:
					valList.add(vals[0]);
					break;	
			}
			s.setValue(valList);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}
	}

	private class LocationUpdateListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			if (loc != null) {
				IsenseSensor s = Sensors.mSensorMap.get(Sensors.TYPE_LOCATION);
				ArrayList<Object> vals = new ArrayList<Object>();
				vals.add(loc.getLatitude());
				vals.add(loc.getLongitude());
				vals.add(loc.getAltitude());
				s.setValue(vals);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		// onProviderEnabled
		// if the GPS wasn't enabled but now is, subscribe to it. We keep the
		// subscription to the network until the GPS has a lock.
		@Override
		public void onProviderEnabled(String provider) {
			if (provider == LocationManager.GPS_PROVIDER) {
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, locationListener);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
	}
	
}