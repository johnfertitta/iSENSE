package edu.uml.cs.isense.profile;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.ImageManager;
import edu.uml.cs.isense.objects.Experiment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileExperimentAdapter extends ArrayAdapter<Experiment> {
	private final int maxDimension = 50;
    private ArrayList<Experiment> items;
    private Context mContext;
    private int resourceID;
    private ImageManager im;
    
    public ProfileExperimentAdapter(Context context, int textViewResourceId, ArrayList<Experiment> items, ImageManager i) {
            super(context, textViewResourceId, items);
            this.items = items;
            mContext = context;
            resourceID = textViewResourceId;
            im = i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
            Experiment e = items.get(position);
            View v = convertView;
        	if (v == null) {
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(resourceID, null);
            }
            if (e != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if(tt != null) {
                    tt.setText(e.name);                            
                }
                if(bt != null) {
                	bt.setText(e.timecreated);
                }
                
                ImageView imgView = (ImageView) v.findViewById(R.id.icon);
            	
                imgView.setAdjustViewBounds(true);
                imgView.setMaxHeight(maxDimension);
                imgView.setMaxWidth(maxDimension);
                
                if (e.provider_url != null && e.provider_url != "" && e.provider_url != "null") {    
                	imgView.setVisibility(View.VISIBLE);
                	imgView.setImageResource(R.drawable.clear);
                    im.fetchBitmapOnThread(e.provider_url, imgView);
                } else {
                	imgView.setVisibility(View.GONE);
                }
            }
            
            return v;
    }
    
}