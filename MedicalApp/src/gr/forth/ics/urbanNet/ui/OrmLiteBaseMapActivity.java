package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.location.LocationServiceConnectable;
import gr.forth.ics.urbanNet.location.LocationServiceConnection;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.map.PngOverlay;
import gr.forth.ics.urbanNet.network.NetworkServiceConnectable;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;
import gr.forth.ics.urbanNet.preferences.MainPreferenceActivity;
import gr.forth.ics.urbanNet.ui.SimpleGestureFilter.SimpleGestureListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

/**
 * <ul>
 * <li>Extends the MapActivity class, adding the public getHelper() method, allowing it to query the local database or save objects to it, using the OrmLite library.</li>
 * <li>Implements the SimpleGestureListener interface adding the feature to respond to swipe gestures.</li>
 * <li>Has connections with the <code>LocationService</code> and <code>
 * NetworkService</code>, allowing it to access the device's current location and connect with the urbanNet server.</li>
 * <li>Overrides the <code>protected boolean isRouteDisplayed()</code> method, required by the MapActivity</li>
 * </ul>
 * @author katsarakis
 */
public abstract class OrmLiteBaseMapActivity<H extends OrmLiteSqliteOpenHelper> extends MapActivity implements SimpleGestureListener, LocationServiceConnectable, NetworkServiceConnectable {
    private static final String MAP_PREFERENCE_FILENAME = "mapPrefs";
    private H helper; // A reference to the helper object.
    protected static MapView map;
    private LocationManager locationManager;
    protected CheckBox lockCheckbox, currentPositionCheckBox, satelliteCheckBox;
    protected boolean isLocked = true;
    protected boolean isTouched = false;
    MyLocationOverlay mylocationOverlay;
    PngOverlay floorPlanOverlay;
    private SimpleGestureFilter detector; // A field to reference an instance of
					  // SimpleGestureFilter
    protected SharedPreferences mapPrefs;
    private ImageButton zoomInButton;
    private ImageButton zoomOutButton;

    LocationListener locationListener = new LocationListener() {
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onLocationChanged(Location location) {
	    map.getController().stopAnimation(false);
	    map.getController().animateTo(new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
	    locationManager.removeUpdates(locationListener);
	}
    };
    private int oldZoomLevel;

	@Override
    protected void onResume() {
	super.onResume();
	checkMapViewMode();
	}

    /**
     * onCreate
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	EasyTracker.getInstance(this).activityStart(this);
	setContentView(R.layout.map_view_view);
	mapPrefs = getSharedPreferences(UrbanNetApp.APP_PREFERENCE_FILENAME, MODE_PRIVATE);
	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	zoomInButton = (ImageButton) findViewById(R.id.mapZoomInButton);
	zoomOutButton = (ImageButton) findViewById(R.id.mapZoomOutButton);
	map = (MapView) findViewById(R.id.mapview2);
	map.setClickable(true);
	map.getController().setZoom(13);
	oldZoomLevel = 13;
	map.getOverlays().clear();
	map.invalidate();
	zoomInButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (map.getZoomLevel() >= 20) {
		    map.getController().setZoom(20);
		}
		else {
		    map.getController().zoomIn();
		}
	    }
	});

	zoomOutButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (map.getZoomLevel() <= 2) {
		    map.getController().setZoom(2);
		}
		else {
		    map.getController().zoomOut();
		}
	    }
	});

	/* toggle showing the current location */
	currentPositionCheckBox = (CheckBox) findViewById(R.id.currentPositionCheckBox);
	currentPositionCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
		    boolean isNetLocProviderOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		    boolean isGpsLocProviderOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		    if (!isNetLocProviderOn && !isGpsLocProviderOn) {
			Toast.makeText(OrmLiteBaseMapActivity.this, "No location provider enabled.", Toast.LENGTH_LONG).show();
		    }
		    mylocationOverlay = new MyLocationOverlay(OrmLiteBaseMapActivity.this, map);
		    mylocationOverlay.enableMyLocation();
		    map.getOverlays().add(mylocationOverlay);
		    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, locationListener);
		    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
		}
		else {
		    mylocationOverlay.disableMyLocation();
		    map.getOverlays().remove(mylocationOverlay);
		    mylocationOverlay = null;
		}
	    }
	});

	setMapToCurrentLocation();

	detector = new SimpleGestureFilter(this, this);
	checkMapViewMode();
	}

    /**
     * Adds the menu. Menu is the bottom menu which becomes visible by pressing the menu-button.
     */
    // @override kati ginetai me to extend
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.main_menu, menu);
	return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Intent intent = new Intent();
	switch (item.getItemId()) {
	case R.id.options:
	    intent.setClass(this, MainPreferenceActivity.class);
	    startActivity(intent);
	    break;
	case R.id.close:
	    finish();
	    break;
	}
	return false;
	}

    public void checkMapViewMode() {
	boolean isSatellite = mapPrefs.getString("mapViewPreference", "Roadmap").equals("Satellite");
	map.setSatellite(isSatellite);
    }

    public static void setMapView(boolean satellite) {
	map.setSatellite(satellite);
    }

    public boolean setMapToCurrentLocation() {
	Location location = null;
	location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	if (location == null) {
	    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    if (location != null) {
		map.getController().animateTo(new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
		return true;
	    }
	    return false;
	}
	else {
	    map.getController().animateTo(new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, locationListener);
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
	    return true;
		}
    }

    /**
     * Copy pasted from OrmLiteBaseActivity source code
     */
    @Override
    protected void onDestroy() {
	super.onDestroy();
	releaseHelper(helper);
	SharedPreferences.Editor mapPrefsEditor = mapPrefs.edit();
	mapPrefsEditor.commit();
    }

    /**
     * For accounting purposes, the (google) server needs to know whether or not you are currently displaying any kind of route information, such as a set of driving directions.
     */
    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

    /**
     * Copy pasted from OrmLiteBaseActivity source code
     * @return the helper object
     */
    public synchronized H getHelper() {
	if (helper == null) {
	    helper = getHelperInternal(this);
	}
	return helper;
    }

    /**
     * Copy pasted from OrmLiteBaseActivity source code
     * @return
     */
    public ConnectionSource getConnectionSource() {
	return getHelper().getConnectionSource();
	}

    /**
     * Copy pasted from OrmLiteBaseActivity source code
     * @param context
     * @return
     */
    protected H getHelperInternal(Context context) {
	@SuppressWarnings("unchecked")
	H newHelper = (H) OpenHelperManager.getHelper(context);
	return newHelper;
    }

    /**
     * Copy pasted from OrmLiteBaseActivity source code
     * @param helper
     */
    protected void releaseHelper(H helper) {
	if (helper != null) {
	    OpenHelperManager.release();
	    helper = null;
	}
    }

    /**
     * This function is called when a MotionEvent is detected. It passes the MotionEvent to the SimpleGestureFilter detector.onTouchEvent() method, in order to recognize if a Swipe
     * or DoubleTap event was performed.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
	isTouched = true;
	this.detector.onTouchEvent(me);
	return super.dispatchTouchEvent(me);
	}

    /**
     * This function is called when a Swipe Gesture is detected.
     */
    @Override
    public void onSwipe(int direction) {
	}

    /**
     * This function is called when a DoubleTap event is detected.
     */
    @Override
    public void onDoubleTap() {
    }

    /**
     * This function is called when a onTap event is detected.
     */
    public void onTap() {
	}

    /**
     * Connection with LocationService
     */
    protected LocationServiceConnection locationServiceConnection = new LocationServiceConnection(this);
    protected BroadcastReceiver locationChangedReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	}
    };
    /**
     * Connection with NetworkService
     */
    protected NetworkServiceConnection networkServiceConnection = new NetworkServiceConnection(this);

    protected ServiceConnection getNetworkServiceConnection() {
	return networkServiceConnection;
    }

    public static MapView getMap() {
	return map;
	}

	@Override
    public void onStop() {
	super.onStop();
	if (mylocationOverlay != null) {
	    currentPositionCheckBox.setChecked(false);
	}
	EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}
}
