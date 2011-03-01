package edu.uml.cs.isense.theodolite;

import java.util.Timer;
import java.util.TimerTask;

import edu.uml.cs.isense.inclinometer.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Theodolite extends Activity implements SensorEventListener {
	private SensorManager mSensorManager;
	private TextView angleTv;
	private AlertDialog calibrate;
	private TextView altitudeTv;
	private static TextView flightTimeTv;
	private EditText distanceInput;
	private float distance = 0;
	private float angle = 0;
	private Button startStop;
	private boolean running = true;
	private Vibrator vibrator;
	private static float time = 0;
	private Timer timeTimer;
	private static int INTERVAL = 100;
    private static MediaPlayer mMediaPlayer;  
    private static boolean muted = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
        setContentView(R.layout.main);

        angleTv = (TextView) findViewById(R.id.currentAngle);
        altitudeTv = (TextView) findViewById(R.id.currentAltitude);
        distanceInput = (EditText) findViewById(R.id.distance);
        flightTimeTv = (TextView) findViewById(R.id.flightTime);
                	
        distanceInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				try {
					distance = Float.parseFloat(s.toString());
				} catch (NumberFormatException e) {
					distance = 0;
				}
				altitudeTv.setText(distance * Math.tan(Math.toRadians(angle)) + "");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
        	
        });
        
        mMediaPlayer = MediaPlayer.create(this, R.raw.beep);  

        running = false;
        
        startStop = (Button) findViewById(R.id.startStop);
        startStop.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				vibrator.vibrate(300);
				return false;
			}
        	
        });
        
        startStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (running) {
			        mSensorManager.unregisterListener(Theodolite.this);
			        running = false;
			        startStop.setText("Touch to start");
			        stopTime();
				} else {
			    	mSensorManager.registerListener(Theodolite.this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
			    	running = true;
			    	startStop.setText("Touch to stop");
			    	time = 0;
			    	startTime();
				}
			}
        	
        });
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("The compass needs to be recalibrated, please do it now by moving the phone in a figure 8").setCancelable(false);
	    
	    calibrate = builder.create();
        
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	}

	// The Handler that gets information back from the BluetoothChatService
    private static final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	case 1:
            		time += INTERVAL;
            		
            		if (time % 1000 == 0) {
            			playAudio();
            		}
            		
            		flightTimeTv.setText(time / 1000 + " seconds");
            		break;
            }
        }
    };
    
    private static void playAudio() {  
        try {  
        	if (!muted) {
        		mMediaPlayer.setLooping(false);  
        		mMediaPlayer.start();
        	}
        } catch (Exception e) {  
        }  
    }  
	
	private void startTime() {
		timeTimer = new Timer();
		timeTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {				
				mHandler.sendMessage(mHandler.obtainMessage(1, 1));
			}
		}, 0, INTERVAL);
	}
	
	private void stopTime() {
		timeTimer.cancel();
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		if (muted) {
	        menu.add(Menu.NONE, 1, Menu.NONE, "Unmute");
        } else {
	        menu.add(Menu.NONE, 1, Menu.NONE, "Mute");
        }

        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case 1:
        		muted = !muted;
        		break;
        }
        return false;
    }
	
	
    @Override
    protected void onResume() {
    	super.onResume();
    	//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		if (accuracy == 3) {
			calibrate.dismiss();
		}
		
		if (accuracy < 3) {
			calibrate.show();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		angle = Math.abs(event.values[1]);
		angleTv.setText(angle + " degrees");
		altitudeTv.setText(distance * Math.tan(Math.toRadians(angle)) + "");
	}
    
}