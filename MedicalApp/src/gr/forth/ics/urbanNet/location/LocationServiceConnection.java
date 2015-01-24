package gr.forth.ics.urbanNet.location;

import gr.forth.ics.urbanNet.location.LocationService.LocationBinder;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * @author katsarakis
 *
 */
public class LocationServiceConnection implements ServiceConnection {
	private LocationServiceConnectable parent;
	private boolean bound;
	private LocationService locationService;
	
	public LocationServiceConnection(LocationServiceConnectable parent) {
		this.parent= parent;
		this.bound= false;
	}
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		// We've bound to LocalService, cast the IBinder and get LocalService instance
		LocationBinder binder = (LocationBinder) service;
		locationService = binder.getService();
		bound = true;
		parent.onLocationServiceConnected();
		//Location loc = mService.getCurrentLocation();
		//polygonOverlay.setCenter(loc);
		//changeMap(loc);
	}
	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		bound = false;
		parent.onLocationServiceDisonnected();
	}
	public LocationService getService() {
		return locationService;
	}
	public boolean isBound() {
		return bound;
	}
}
