package gr.forth.ics.urbanNet.ui;


//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.ActionBarActivity;

public class ErrActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.err);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
