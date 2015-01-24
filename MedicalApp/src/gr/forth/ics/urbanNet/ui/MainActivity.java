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
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.location.LocationServiceConnection;
import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.network.NetworkServiceConnectable;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
//import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

//implement the OnClickListener interface
public class MainActivity extends Activity {
	Button buttonLogin;
	Button buttonSignup;
	EditText mEdit;
	
	NetworkServiceConnection networkServiceConnection;
	LocationServiceConnection locationServiceConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
//		networkServiceConnection = new NetworkServiceConnection(this);
//		bindService(new Intent(this, NetworkService.class), super.networkServiceConnection, Context.BIND_AUTO_CREATE);
//
//		// Need the location and network service
//		Intent intent = new Intent(this, LocationService.class);
//		bindService(intent, super.locationServiceConnection, Context.BIND_AUTO_CREATE);
		
		addListenerOnButton();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	
	public void addListenerOnButton() {

		final Context context = this;

		buttonLogin = (Button) findViewById(R.id.button_login);
		buttonSignup = (Button) findViewById(R.id.button_signup);
		
		buttonLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					Intent intent = new Intent(context, LoginActivity.class);
					startActivity(intent);
			}
		});
		
		buttonSignup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, UserDetails.class);
				startActivity(intent);
			}
		});
	}
}
