package gr.forth.ics.urbanNet.map;

import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapOverlay extends Overlay {

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
	    
		Log.d(this.getClass().getName(), p.getLatitudeE6() + "," + p.getLongitudeE6());
		
	    return true;        
	
	}
	
}
