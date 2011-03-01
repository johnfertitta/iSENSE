package edu.uml.cs.isense.profile;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.objects.Session;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ProfileSessionAdapter extends ArrayAdapter<Session> {
    public ArrayList<Session> items;
    private Context mContext;
    private int resourceID;
    
    public ProfileSessionAdapter(Context context, int textViewResourceId, ArrayList<Session> items) {
    		super(context, textViewResourceId, items);
            this.items = items;
            mContext = context;
            resourceID = textViewResourceId;
    }

    public Session getItem(int position) {
    	return items.get(position);
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
                    TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                    if(tt != null) {
                        tt.setText(s.name);                       
                    }
                    if(bt != null) {
                        bt.setText(s.timecreated);
                    }
            }
            
            return v;
    }
}