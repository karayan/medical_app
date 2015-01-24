/**
 * 
 */
package gr.forth.ics.urbanNet.map;

import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * @author katsarakis
 *
 */
public class CustomPolygonOverlay extends PolygonOverlay {
	
	public CustomPolygonOverlay(MapView mapView) {
		super(mapView);
		
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		
		int currSelectedPoint = pointExist(p);
		
		if(!allowTapEvents){
			return false;
		}
		
		if(!isValidPoint(selectedPoint) && isValidPoint(currSelectedPoint)){
			selectedPoint=currSelectedPoint;
			return super.onTap(p, mapView);
		}
		
		if(isValidPoint(selectedPoint)){
			GeoPoint from = geoPoints.get(selectedPoint);
			geoPoints.set(selectedPoint, p);
			undoRedo.updateMovePoint(from, p, selectedPoint);
			selectedPoint=-1;
			return super.onTap(p, mapView);
		}
		
		if (!isValidPoint(selectedPoint)) {
			
			isDefault= false;
			geoPoints.add(p);
			nVertices++;
			isDrawing = true;
			super.findCenter();
			if (geoPoints.size() > 2) {
				isValid= true;
			}
			undoRedo.updateAddPoint(p, geoPoints.size()-1);
			return super.onTap(p, mapView);
		}
		
		return false;

	}
	
	public boolean isValidPoint(int point){
		return point>=0 && point<geoPoints.size();
	}

	private int pointExist(GeoPoint my){
		
		Point point = new Point();
		Point currPoint = new Point();
		projection.toPixels(my, currPoint);
		
		for(int i=0; i<geoPoints.size(); i++ ){
			projection.toPixels(geoPoints.get(i), point);	
			//Log.d("all "+i, point.x+" - "+point.y + " "+ (Math.pow(currPoint.x-point.x, 2) + Math.pow(currPoint.y-point.y,2) ));
			if( ( Math.pow(currPoint.x-point.x, 2) + Math.pow(currPoint.y-point.y,2) ) <= 500 ){
				return i;
			}
		}
		return -1;
	}
	
}
