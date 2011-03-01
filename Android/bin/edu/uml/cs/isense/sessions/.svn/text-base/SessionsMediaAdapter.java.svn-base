package edu.uml.cs.isense.sessions;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.ImageManager;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class SessionsMediaAdapter extends ArrayAdapter<String> {
	private ArrayList<String> mImageUrls;
    private Context mContext;
    private int itemBackground;
    private ImageManager im;

    public SessionsMediaAdapter(Context c, int textViewResourceId, ArrayList<String> items, ImageManager i) 
    {
    	super(c, textViewResourceId, items);
        mContext = c;
        mImageUrls = items;
        im = i;
        //---setting the style---
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.Gallery1);
        itemBackground = a.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 0);
        a.recycle();                    
    }

    //---returns the number of images---
    public int getCount() {
        return mImageUrls.size();
    }

    //---returns an ImageView view---
    public View getView(int position, View convertView, ViewGroup parent) {
    	ImageView imageView = (ImageView) convertView;
    	
    	if (imageView == null) {
    		imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new Gallery.LayoutParams(150, 120));
            imageView.setBackgroundResource(itemBackground);
    	}

        if (mImageUrls.get(position) != null) {
        	im.fetchBitmapOnThread(mImageUrls.get(position), imageView);
        }
                
        return imageView;
    }
	
}
