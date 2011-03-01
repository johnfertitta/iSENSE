package edu.uml.cs.isense.experiments;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.ImageManager;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Experiment;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExperimentAdapter extends ArrayAdapter<Experiment> {
	private final int maxDimension = 50;
    public ArrayList<Experiment> items;
    private Context mContext;
    private int resourceID;
    private int loadingRow;
    private ImageManager im;
    public int itemsLoaded;
    public boolean allItemsLoaded;
    private Boolean loading;
    private UIUpdateTask updateTask;
    private Handler uiHandler = new Handler();
    private RestAPI rapi;
	public static final int pageSize = 10;
	public int page = 0;
	public String action = "browse";
	public String query = "";

    public ExperimentAdapter(Context context, int textViewResourceId, int loadingRow, ArrayList<Experiment> items, ImageManager i) {
            super(context, textViewResourceId, items);
            this.items = items;
            mContext = context;
            resourceID = textViewResourceId;
            this.loadingRow = loadingRow;
            im = i;
            itemsLoaded = 0;
            allItemsLoaded = false;
            loading = false;
            updateTask = new UIUpdateTask();
            rapi = RestAPI.getInstance();
    }
    
    public int getCount() {
    	int count = itemsLoaded;
    	if (!allItemsLoaded) ++count;
    	return count;
    }
    
    public Experiment getItem(int position) {
    	Experiment result;
    	synchronized(items) {
    		result = items.get(position);
    	}
    	return result;
    }
    
    public long getItemId(int position) {
    	return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {	
    	boolean isLastRow = position >= itemsLoaded;
    	    	
    	int rowResID = isLastRow ? loadingRow : resourceID;

        LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(rowResID, null);

    	if (!isLastRow && items.size() != 0) {
    		Experiment e = items.get(position);
            if (e != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if(tt != null) {
                    tt.setText(e.name);                            
                }
                if(bt != null) {
                	if (e.firstname == "" && e.lastname == "") {
                		bt.setVisibility(View.GONE);
                	}
                	else {
                		bt.setText("Created By: " + e.firstname + " " + e.lastname);
                	}
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
    	} else {
    		if (!allItemsLoaded) {
    			page++;
    			synchronized( loading ) {
    				if( !loading.booleanValue() ) {
    					loading = Boolean.TRUE;
    					Thread t = new LoadingThread();
    					t.start();
    				}
    			}
    		} else {
    			uiHandler.post( updateTask );
    		}
    	}
        return v;
    }
    
    class LoadingThread extends Thread {
        public void run() {
            ArrayList<Experiment> new_items = rapi.getExperiments(page, pageSize, action, query);
            
            if (new_items.size() == 0) {
            	allItemsLoaded = true;
            } else {
            	synchronized(items) {
            		items.addAll(new_items);
            	}
            	itemsLoaded += new_items.size();
            }

            synchronized( loading ) {
                loading = Boolean.FALSE;
            }
            uiHandler.post( updateTask );
        }
    }
    
    class UIUpdateTask implements Runnable {
        public void run() {
            notifyDataSetChanged();
        }
    }
    
}