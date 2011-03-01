package edu.uml.cs.isense.sensors;

import edu.uml.cs.isense.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class DataListActivity extends Activity {
    private DataDbAdapter mDbHelper;

    public static String DATA_ID = "DATA_ID";
    private final static int DELETE_ID = 0;

    private ListView newDataListView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.datalist);

        setResult(Activity.RESULT_CANCELED);

        newDataListView = (ListView) findViewById(R.id.data);
        newDataListView.setOnItemClickListener(mDataClickListener);
        newDataListView.setLongClickable(true);
        registerForContextMenu(newDataListView);
        
        mDbHelper = new DataDbAdapter(this);
        mDbHelper.open();
        fillData();
    }

    @Override
    protected void onDestroy() {
    	mDbHelper.close();
        super.onDestroy();
    }

    private void fillData() {
		Cursor c = mDbHelper.fetchAllData();
        startManagingCursor(c);

        String[] from = new String[] { DataDbAdapter.KEY_TITLE };
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.dataname};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter data = new SimpleCursorAdapter(this, R.layout.dataname, c, from, to);
        newDataListView.setAdapter(data);
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    		super.onCreateContextMenu(menu, v, menuInfo);
    		menu.add(0, DELETE_ID, 0,  "Delete");
    }

    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch (item.getItemId()) {
    		case DELETE_ID:
    			mDbHelper.deleteData(info.id);
    			fillData();
    			return true;
    		default:
    			return super.onContextItemSelected(item);
    	}
    }
    
    private OnItemClickListener mDataClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long id) {            
            mDbHelper.close();
        	Intent intent = new Intent();
            intent.putExtra(DATA_ID, id);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

}