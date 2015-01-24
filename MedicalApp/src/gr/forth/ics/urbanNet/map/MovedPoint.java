package gr.forth.ics.urbanNet.map;


import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

/**
 * undo/redo functionality about moving a point from one place to another.
 * From is where was the point
 * to where we move it
 * index is where it is located toi the geopoint array
 * 
 * @author chrysohous
 *
 */

public class MovedPoint extends MoveAction {

	private GeoPoint from;
	private GeoPoint to;
	private int index;
	
	public MovedPoint(GeoPoint from, GeoPoint to, int index){
		super();
		
		this.from = from;
		this.to = to;
		this.index = index;
	}
	

	@Override
	public void actionUndo(ArrayList<GeoPoint> geoPoints) {
		geoPoints.set(index, from);
		
	}

	@Override
	public void actionRedo(ArrayList<GeoPoint> geoPoints) {
		geoPoints.set(index, to);
	}


	@Override
	public MoveAction clone() {
		GeoPoint from = new GeoPoint(this.from.getLatitudeE6(), this.from.getLongitudeE6()); 
		GeoPoint to = new GeoPoint(this.to.getLatitudeE6(), this.to.getLongitudeE6()); 
		return new MovedPoint(from, to, index);
	}

}
