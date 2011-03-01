package edu.uml.cs.isense.people;

import java.util.ArrayList;

import edu.uml.cs.isense.R;

import edu.uml.cs.isense.ImageManager;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Person;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
//import android.widget.ImageView;
import android.widget.TextView;

public class PeopleAdapter extends ArrayAdapter<Person> {
	//private final int maxDimension = 50;
	public ArrayList<Person> items;
    private Context mContext;
    private int resourceID;
    //private ImageManager im;
    private int loadingRow;
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
    
	public PeopleAdapter(Context context, int textViewResourceId, int loadingRow, ArrayList<Person> items, ImageManager i) {
        super(context, textViewResourceId, items);
        this.items = items;
        mContext = context;
        resourceID = textViewResourceId;
        this.loadingRow = loadingRow;
        //im = i;
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
    
    public Person getItem(int position) {
    	Person result;
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
    		Person e = items.get(position);
    		
            if (e != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if(tt != null) {
                    tt.setText(e.firstname + " " + e.lastname);                            
                }

                if(bt != null) {
                    if (e.experiment_count != 0 && e.session_count != 0) {
                    	bt.setText(e.firstname + " created " + e.experiment_count + " experiments and contributed " + e.session_count + " sessions.");
                    } else if (e.experiment_count == 0 && e.session_count != 0) {
                    	bt.setText(e.firstname + " conributed " + e.session_count + " sessions.");
                    } else if (e.experiment_count != 0 && e.session_count == 0) {
                    	bt.setText(e.firstname + " created " + e.experiment_count + " experiments");
                    } else {
                    	bt.setText(e.firstname + " has not contributed to an experiment yet.");
                    }
                }
                /*
                ImageView imgView = (ImageView) v.findViewById(R.id.icon);
            	
                imgView.setAdjustViewBounds(true);
                imgView.setMaxHeight(maxDimension);
                imgView.setMaxWidth(maxDimension);
                
                if (e.url != null && e.url != "" && e.url != "null") {    
                	imgView.setVisibility(View.VISIBLE);
                	imgView.setImageResource(R.drawable.clear);
                    im.fetchBitmapOnThread(e.url, imgView);
                } else {
                	imgView.setVisibility(View.VISIBLE);
                	imgView.setImageResource(R.drawable.user);
                }*/
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
            ArrayList<Person> new_items = rapi.getPeople(page, pageSize, action, query);
            
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