package gr.forth.ics.urbanNet.map;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

/**
 * abstract level functionality for undo/redo operation for points.
 * Is written to support many kind of shaps that are stored as a geopoint
 * array, like polygone, path line. 
 * 
 * @author chrysohous
 *
 */

public abstract class MoveAction {

	
	public MoveAction(){
		
	}
	
	public abstract void actionUndo(ArrayList<GeoPoint> geoPoints);
	public abstract void actionRedo(ArrayList<GeoPoint> geoPoints);
	
	@Override
	public abstract MoveAction clone();
}
