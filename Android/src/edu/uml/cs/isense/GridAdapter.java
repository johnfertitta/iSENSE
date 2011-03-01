package edu.uml.cs.isense;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.comm.RestAPI;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private RestAPI rapi;

    public GridAdapter(Context c) {
        mContext = c;
        rapi = RestAPI.getInstance();
    }

    public int getCount() {
    	if (!rapi.isLoggedIn()) return mThumbIds.length - 1;
    	
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	ImageView imageView;
        
        if (convertView == null) {
        	imageView = new ImageView(mContext);
        	imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
        	imageView.setScaleType(ImageView.ScaleType.CENTER);
        	imageView.setPadding(8, 8, 8, 8);
        } else {
        	imageView = (ImageView) convertView;
        }
        
        imageView.setImageResource(mThumbIds[position]);
        
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.experiments, R.drawable.record_data, 
            R.drawable.people, R.drawable.profile
    };
}
