package edu.uml.cs.isense.sensors;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class FieldMatch extends Activity {
	private JSONArray header;
	private JSONArray remoteHeader;
	
	private static TextView[] textViews;
	private Spinner[] spinners;
			
	private HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		try {
			header = new JSONArray(extras.getString("edu.uml.cs.isense.fieldmatch.header"));
			remoteHeader = new JSONArray(extras.getString("edu.uml.cs.isense.fieldmatch.remoteheader"));
			int length = extras.getInt("edu.uml.cs.isense.fieldmatch.length");
			for (int i = 0; i < length; i++) {
				if (extras.get("edu.uml.cs.isense.fieldmatch." + i) != null) {
					indexMap.put(i, extras.getInt("edu.uml.cs.isense.fieldmatch." + i));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int fieldCount = header.length();
		int remoteFieldCount = remoteHeader.length();

		textViews = new TextView[remoteFieldCount];
		spinners = new Spinner[remoteFieldCount];
		
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	    for (int i = 0; i < fieldCount; i++) {
	    	try {
				adapter.add(header.getString(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT );
		LinearLayout l = new LinearLayout(this);
		l.setOrientation(LinearLayout.VERTICAL);
		
		ScrollView sv = new ScrollView(this);
		sv.setBackgroundColor(Color.WHITE);
		LinearLayout.LayoutParams inlp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout inl = new LinearLayout(this);
		inl.setOrientation(LinearLayout.VERTICAL);
		inl.setBackgroundColor(Color.WHITE);
		
		for (int i = 0; i < remoteFieldCount; i++) {
			RelativeLayout temp = new RelativeLayout(this);
			temp.setBackgroundColor(Color.WHITE);
					
			textViews[i] = new TextView(this);
			textViews[i].setId(i);
			spinners[i] = new Spinner(this);
			if (indexMap.get(i) != null) {
				textViews[i].setVisibility(View.GONE);
				spinners[i].setVisibility(View.GONE);
				continue;
			}

			try {
				textViews[i].setVisibility(View.VISIBLE);
				textViews[i].setText(remoteHeader.getString(i) + " : ");
				textViews[i].setTextColor(Color.BLACK);
				spinners[i].setVisibility(View.VISIBLE);
				spinners[i].setPrompt("Select a match for " + remoteHeader.getString(i));
				spinners[i].setAdapter(adapter);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			temp.addView(textViews[i]);
			RelativeLayout.LayoutParams tempp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tempp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			temp.addView(spinners[i], tempp);
			
			inl.addView(temp);
		}
		
		Button ok = new Button(this);
		ok.setText("Done");
		inl.addView(ok, inlp);
		sv.addView(inl, lp);
		l.addView(sv, lp);
		
		setContentView(l);
		
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int length = remoteHeader.length();
			
				Intent intent = new Intent();
	            
	            for (int i = 0; i < length; i++) {
	            	if (spinners[i].getVisibility() == View.GONE) {
		            	intent.putExtra("edu.uml.cs.isense.fieldmatch." + i, indexMap.get(i));
	            	} else {
	            		intent.putExtra("edu.uml.cs.isense.fieldmatch." + i, spinners[i].getSelectedItemPosition());
	            	}
	            }
	            
	            intent.putExtra("edu.uml.cs.isense.fieldmatch.length", length);

	            setResult(Activity.RESULT_OK, intent);
	            finish();
			}
		});
		
	}

}
