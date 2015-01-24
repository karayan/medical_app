package gr.forth.ics.urbanNet.ui;


//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;


public class UserDetails extends ActionBarActivity {
	Button Save;
	final Calendar myCalendar = Calendar.getInstance();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.userdetails);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        addListenerOnButton();
        final EditText EditTextBirth = (EditText) findViewById(R.id.EditTextBirth);
        
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

	        @Override
	        public void onDateSet(DatePicker view, int year, int monthOfYear,
	                int dayOfMonth) {
	            // TODO Auto-generated method stub
	            myCalendar.set(Calendar.YEAR, year);
	            myCalendar.set(Calendar.MONTH, monthOfYear);
	            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	            
	            if (EditTextBirth.hasFocus()){
	            	System.out.println("focus on dateFrom");
	            	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
	                imm.hideSoftInputFromWindow(EditTextBirth.getWindowToken(), 0);
	            	updateLabel(EditTextBirth);
	            }
	            
	        }

		

	    };
	    
	    EditTextBirth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(UserDetails.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        
	  //------------------------
	    // Array of choices
	    String education[] = {"Education","Middle / Secondary School","High School","Professional Lyceum","Private education", "Vocational education and training"," Technical Professional/Vocational School / TEE","Technical Professional/Vocational Lyceum, TEL","Technical Professional/Vocational School, TES","Unified Multidisciplinary Lyceum, EPL","Higher Educational Institutes"};

	    // Selection of the spinner
	    Spinner spinner = (Spinner) findViewById(R.id.SpinnerEducation);

	    // Application of the Array to the Spinner
	    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, education);
	    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
	    spinner.setAdapter(spinnerArrayAdapter);
        
	    //----occupation------
	    String occupation[] = {"Occupation","Amateur","High School","Business analyst","Business architect", "Chef"," Civil estimator",
	    		"Development chef","Food critic","Lawyer","Lecturer","Engineer","Officer","Physician","Principal teacher",
	    		"Professional","Signwriter","Statistician","Unemployed"};

	    // Selection of the spinner
	    Spinner spinnerOc = (Spinner) findViewById(R.id.SpinnerOcucupation);

	    // Application of the Array to the Spinner
	    ArrayAdapter<String> spinnerArrayAdapterOc = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, occupation);
	    spinnerArrayAdapterOc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
	    spinnerOc.setAdapter(spinnerArrayAdapterOc);
	    //------Nationality--------
	    String nationality[] = {"Nationality","Albanian","American","Australian","Austrian", "British","Bulgarian",
	    		"Chinese","English","German","Greek","Japanese","Korean","Mexican",
	    		"Portuguese","Slovak","South African","Swede"};

	    // Selection of the spinner
	    Spinner spinnerNa = (Spinner) findViewById(R.id.SpinnerNationality);

	    // Application of the Array to the Spinner
	    ArrayAdapter<String> spinnerArrayAdapterNa = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, nationality);
	    spinnerArrayAdapterNa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
	    spinnerNa.setAdapter(spinnerArrayAdapterNa);
	    
	    
	    
    }
    public void addListenerOnButton() 
    {
    	final Context context = this;
    	
    
    	Save = (Button) findViewById(R.id.btnSave);
    	
    	Save.setOnClickListener(new OnClickListener()
    	{
    		@Override
			public void onClick(View arg0) 
				{
				//Log.v("EditText", mEdit.getText().toString());
				Intent intentS = new Intent(context, SecondActivity.class);
                startActivity(intentS);
				}
    	});
    }
    private void updateLabel(EditText edittext) {
		  String myFormat = "yyy/MM/dd"; //In which you need put here  "MM/dd/yy"
		  SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
		  edittext.setText(sdf.format(myCalendar.getTime()));
	  }
    
}
