package gr.forth.ics.urbanNet.map;


import java.util.ArrayList;

import android.util.Log;

import com.google.android.maps.GeoPoint;

/**
 * undo/redo functionality about adding a new point to the shape.
 * It must remember where is the new point and in which index in the geopoint array.
 * 
 * @author chrysohous
 *
 */

public class AddPoint extends MoveAction{

	private GeoPoint at;
	private int index;
	
	public AddPoint(GeoPoint at, int index){
		super();
		
		this.at = at;
		this.index = index;
	}

	@Override
	
	public void actionUndo(ArrayList<GeoPoint> geoPoints) {
		Log.d( "tag", geoPoints.size()+" "+index);
		geoPoints.remove(index);
	}

	@Override
	public void actionRedo(ArrayList<GeoPoint> geoPoints) {
		geoPoints.add(at);
		
	}

	@Override
	public MoveAction clone() {
		GeoPoint tmp = new GeoPoint(at.getLatitudeE6(), at.getLongitudeE6()); 
		
		return new AddPoint(tmp, index);
	}
	
}
