package edu.uml.cs.isense.visualizations;

import edu.uml.cs.isense.R;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DataTable extends Activity {
	private String dataString;
	private String[] dataArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.datatable);
		// Get the TableLayout
        TableLayout tl = (TableLayout) findViewById(R.id.datatable);

        Bundle extras = getIntent().getExtras();
		dataString = extras.getString("edu.uml.cs.isense.visualizations.data");
        		
		dataArray = dataString.split("\n");
		
        // Go through each item in the array
        for (int i = 0; i < dataArray.length; i++)
        {
            // Create a TableRow and give it an ID
            TableRow tr = new TableRow(this);
            
            tr.setLayoutParams(new LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));   
            
            tr.setBackgroundColor(Color.WHITE);
            
            // Create a TextView to house the name of the province
            TextView text = new TextView(this);
            text.setText(dataArray[i]);
            text.setTextColor(Color.BLACK);
            text.setBackgroundColor(Color.WHITE);
            tr.addView(text);

            // Add the TableRow to the TableLayout
            tl.addView(tr, new TableLayout.LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }
	}
	
}
