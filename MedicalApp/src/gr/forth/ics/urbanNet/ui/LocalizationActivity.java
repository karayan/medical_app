package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapFragment;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

public class LocalizationActivity extends ActionBarActivity {

	private GoogleMap googleMap;
	private int busstopicon, busicon;

	public static double dLat;
	public static double dLong;
	
	public static String BusID="";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_localization);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// GPS Services
		// create class object
		GPSservice gps = new GPSservice(LocalizationActivity.this);

        // check if GPS enabled     
        if(gps.canGetLocation()){ 
            dLat = gps.getLatitude();
            dLong = gps.getLongitude();
            Toast.makeText(getApplicationContext(), "GPS:Your Location is \n\nLat: " + dLat + "\nLong: " + dLong, Toast.LENGTH_LONG).show();    
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        // Stop GPS Services
		
		// Loading map
        initializeMap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.localization, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_map_hybrid){
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		}
		if (id == R.id.action_map_normal){
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		if (id == R.id.action_map_sattelite){
			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	//function to load map. If map is not created it will create it for you
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initializeMap() {
    	
    	
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        
        //busicon = R.drawable.yellow_point;
        //busstopicon = R.drawable.blue_point;
        
        
        if( dLat==0 && dLong==0 ){
        	CameraPosition cameraPosition = new CameraPosition.Builder()
        										.target( new LatLng(35.338953, 25.132335) )
                								.zoom(16)
                								.build();
        	
        	googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else{
        	CameraPosition cameraPosition = new CameraPosition.Builder()
			.target( new LatLng(dLat, dLong) )
			.zoom(16)
			.build();
        	
        	googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        
    	// latitude and longitude
    	double latitude = dLat; // = 35.304912;
    	double longitude = dLong; // = 25.083903;

    	// create marker
        //MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(" BUS ");
    	//MarkerOptions marker = new MarkerOptions().position(new LatLng(dLat, dLong)).title(" BUS ");
    	googleMap.addCircle(new CircleOptions()
    							.center(new LatLng(latitude, longitude))
    							.strokeColor(Color.BLUE)
    							.fillColor(Color.TRANSPARENT)
    							.strokeWidth((float) 10.0)
    							.radius(10)
    							);    	
    	//Toast.makeText(getApplicationContext(), "Your Location is \n\nLat: " + dLat + "\nLong: " + dLong, Toast.LENGTH_LONG).show();    
    	
    	/***** GoogleMap Settings *****/
    	
    	// Other supported types include: MAP_TYPE_NORMAL, MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
    	googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    	/*
    	googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    	googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    	googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    	googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
    	*/
    	
    	googleMap.setMyLocationEnabled(true); // false to disable
    	
    	googleMap.getUiSettings().setZoomGesturesEnabled(true); 
    	
    	googleMap.getUiSettings().setZoomControlsEnabled(true);
    	
    	googleMap.getUiSettings().setCompassEnabled(true);
    	
    	googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    	
    	googleMap.getUiSettings().setRotateGesturesEnabled(true);
    	
    	
    	//googleMap.setOnMapLongClickListener(OnMapLongClickListener);
    	//googleMap.setOnCameraChangeListener(OnCameraChangeListener);
    	
    }
	
	
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_localization,
					container, false);
			return rootView;
		}
	}
}