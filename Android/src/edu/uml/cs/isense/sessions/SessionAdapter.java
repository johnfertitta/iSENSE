package edu.uml.cs.isense.sessions;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.objects.Session;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class SessionAdapter extends ArrayAdapter<Session> {
    private ArrayList<Session> items;
    private Context mContext;
    private int resourceID;
    
    public SessionAdapter(Context context, int textViewResourceId, ArrayList<Session> items) {
    		super(context, textViewResourceId, items);
            this.items = items;
            mContext = context;
            resourceID = textViewResourceId;
    }

    public void toggle(int position) {
    	Session s = items.get(position);
    	s.checked = !s.checked;
    	items.remove(position);
    	items.add(position, s);
    }
    
    public void set(int position, boolean value) {
    	Session s = items.get(position);
    	s.checked = value;
    	items.remove(position);
    	items.add(position, s);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(resourceID, null);
            }
            Session s = items.get(position);
            if (s != null) {
                    TextView tt = (TextView) v.findViewById(R.id.toptext);
                    CheckedTextView mt = (CheckedTextView) v.findViewById(R.id.middletext);
                    TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                    if(tt != null) {
                        tt.setText(s.firstname + " " + s.lastname);                            
                    }
                    if(bt != null) {
                        bt.setText(s.timecreated);
                    }
                    if (mt != null) {
                    	mt.setText(s.name);
                    	mt.setChecked(s.checked);
                    }
            }
            
            return v;
    }
}