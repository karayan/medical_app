/*
 * Copyright (C) 2011 www.itcsolutions.eu
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1, or (at your
 * option) any later version.
 *
 * This file is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 *
 */

/**
 *
 * @author Catalin - www.itcsolutions.eu
 * @version 2011
 *
 */
package gr.forth.ics.urbanNet.ui;


//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SecondActivity extends ActionBarActivity 
{
	Button localization;
	Button questionnaire;
	Button Info;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);    
        this.setContentView(R.layout.second);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addListenerOnButton(); 
    }
    public void addListenerOnButton() 
    {
    	final Context context = this;
    	localization = (Button) findViewById(R.id.buttonLocalization);
    	questionnaire = (Button) findViewById(R.id.buttonQuestionnaire);
    	Info = (Button) findViewById(R.id.buttonInfo);
    	
    	localization.setOnClickListener(new OnClickListener()
    	{
    		@Override
			public void onClick(View arg0) 
				{
				//Log.v("EditText", mEdit.getText().toString());
				Intent intentlocal = new Intent(context, LocalizationActivity.class);
//    			Intent intentlocal = new Intent(context, MapViewActivity.class);
                startActivity(intentlocal);
				}
    	});
    	questionnaire.setOnClickListener(new OnClickListener()
    	{
    		@Override
			public void onClick(View arg0) 
				{
				//Log.v("EditText", mEdit.getText().toString());
				Intent intentquest = new Intent(context, QuestActivity.class);
                startActivity(intentquest);
				}
    	});
    	Info.setOnClickListener(new OnClickListener()
    	{
    		@Override
			public void onClick(View arg0) 
				{
				//Log.v("EditText", mEdit.getText().toString());
				Intent intentInfo = new Intent(context, InfoActivity.class);
                startActivity(intentInfo);
				}
    	});
    	
    }
}
