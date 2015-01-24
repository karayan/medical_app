package gr.forth.ics.urbanNet.map;


import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Overlay.Snappable;
import com.google.android.maps.Projection;


/**
 * @author katsarakis
 *
 */
public class LineOverlay extends Overlay implements Snappable{
	private final static int FIRST_VERTEX_RADIUS = 15;
	private final static int VERTEX_RADIUS = 10;	
	private Paint pathPaint, vertexPaint;
	protected Projection projection;	
	protected MapView mapView;	
	protected ArrayList<GeoPoint> geoPoints;
	//protected Path path;
	protected boolean allowTapEvents;
	
	public UndoRedo undoRedo;
	protected int selectedPoint;
	
	public LineOverlay(MapView mapView) {
		
		this.pathPaint= new Paint();
		pathPaint.setDither(true);
		this.pathPaint.setStrokeWidth(2);
		this.pathPaint.setStyle(Paint.Style.STROKE);
		this.pathPaint.setARGB(100, 255, 153, 51);
		this.pathPaint.setAntiAlias(true);		
		//this.pathPaint.setStrokeJoin(Paint.Join.ROUND);
		//this.pathPaint.setStrokeCap(Paint.Cap.ROUND);		
		this.vertexPaint= new Paint();
		this.vertexPaint.setARGB(100, 235, 0, 235);
		this.vertexPaint.setAntiAlias(true);
		this.projection= mapView.getProjection();
		this.mapView= mapView;
		this.allowTapEvents= true;
		this.geoPoints = new ArrayList<GeoPoint>();
		
		undoRedo = new UndoRedo(this.geoPoints);
		selectedPoint = -1;
	}
	
	public int getNVertices() {		
		return geoPoints.size();		
	}

	@Override
	public void draw(Canvas canvas, MapView mapv, boolean shadow){
        super.draw(canvas, mapv, shadow);        
        Point point = new Point();
        //path= new Path();
        int i=0;
        Point tempPoint = null;
        pathPaint.setARGB(100, 0, 255, 235);
        vertexPaint.setARGB(100, 235, 0, 235);
        Log.d("Gpoints"," size"+geoPoints.size());
		for (GeoPoint gPoint : geoPoints) {
			
			projection.toPixels(gPoint, point);
			
			if (tempPoint!=null) {
				//pathPaint.setARGB(150, (i-1)*(240/(geoPoints.size()-1)), ((geoPoints.size()-1)-(i-1))*(240/(geoPoints.size()-1)), ((geoPoints.size()-1)-(i-1))*(240/(geoPoints.size()-1)));
				pathPaint.setARGB(150, 255, ((geoPoints.size())-(i-1))*(220/(geoPoints.size())), ((geoPoints.size())-(i-1))*(220/(geoPoints.size())) );
				canvas.drawLine(tempPoint.x, tempPoint.y, point.x, point.y, pathPaint);
			}else{
				
				tempPoint = new Point();
			}
			tempPoint.x = point.x;
			tempPoint.y = point.y;
			
			if(selectedPoint==i++){
				canvas.drawCircle(point.x, point.y, 20, vertexPaint);
			}else{
				canvas.drawCircle(point.x, point.y, VERTEX_RADIUS, vertexPaint);
			}
			
			if(geoPoints.size()==i){
				vertexPaint.setARGB(200, 255, 0, 0);
				canvas.drawCircle(point.x, point.y, 13, vertexPaint);
			}
		}
		
    }

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {

		int currSelectedPoint = pointExist(p);
	    //Log.d(""+p,"here");
		if(!allowTapEvents){
			return false;
		}
		
		if(!isValidPoint(selectedPoint) && isValidPoint(currSelectedPoint)){
			 Log.d("Line Overlay","re-point");
			selectedPoint=currSelectedPoint;
			return super.onTap(p, mapView);
		}
		
		if(isValidPoint(selectedPoint)){
			Log.d("Line Overlay","re-potision");
			if(selectedPoint<0||selectedPoint>=geoPoints.size()){
				selectedPoint=-1;
				return super.onTap(p, mapView);
			}
			GeoPoint from = geoPoints.get(selectedPoint);
			geoPoints.set(selectedPoint, p);
			undoRedo.updateMovePoint(from, p, selectedPoint);
			selectedPoint=-1;
			return super.onTap(p, mapView);
		}
		
		if (!isValidPoint(selectedPoint)) {
			Log.d("Line Overlay","new-point");
			geoPoints.add(p);
			undoRedo.updateAddPoint(p, geoPoints.size()-1);
			return super.onTap(p, mapView);
		}
		return false;
		
	}
	
	public boolean isValidPoint(int point){
		return point>=0 && point<geoPoints.size();
	}
	
	/**
	 * The current LineOverlay object contains an ArrayList of geoPoints that form a line. This method returns a
	 * new ArrayList of geoPoints, which form a polygon "following" the line. This method is useful for finding
	 * the area of a road, river or other line. 
	 * 
	 * @param pathWidth The width in meters that the polygon will have.
	 * @return ArrayList of geoPoints that form the polygon
	 */
	public ArrayList<GeoPoint> getPolygonGeoPoints(float pathWidth) {		
		/* 
		 * Each couple of adjacent vertices of the path declares a vector. (e.g., path:ABC, vector1:AB vector2:BC)
		 * 
		 * The first and the last vertices participate only to one vector and this will be considered the path's
		 * azimuth at that vertex. (e.g., vertexA:vactor1, vertexC:vector2, azimuth@vertexA=vector1.azimuth,
		 * azimuth@vertexC=vector2.azimuth)
		 * 
		 * Every intermediate vertex participates in two vectors. It is the end point of the first and the
		 * start point of the second. For the intermediate vertices, the azimuth of the sum of these vectors  will be
		 * considered the path's azimuth at that vertex. (e.g., vertexB:{vector1,vector2}
		 * azimuth@vertexB=(vector1+vector2).azimuth)
		 * 
		 * For the first vertex of the path, a point will created "back" of it, formally at an offset
		 * (radius, azimuth) = (pathWidth, +pi).
		 * 
		 * For each vertex of the path (including the first and the last) two points will be created, one at the
		 * "right" and one the "left" of it. Formally, the "right" will be at an offset of
		 * (radius, azimuth) = (pathWidth, -pi/2) and the "left" will be at an offset of
		 * (radius, azimuth) = (pathWidth, +pi/2)
		 * 
		 * For the last vertex of the path, a point will created in "front" of it, formally at an offset
		 * (radius, azimuth) = (pathWidth, 0).
		 * 
		 * At the end of Part B, all points will have been created and stored with the right order in the
		 * ArrayList<Point> points.
		 */
		Point pointA;
		Point pointB;		
		Point firstPoint= new Point();
		ArrayList<Point> firstHalfOfPoints= new ArrayList<Point>();
		Point middlePoint= new Point();
		ArrayList<Point> secondHalfOfPoints= new ArrayList<Point>();
		int dx, dy;
		double meanAzimuth;				
		for (int i=0; i<= geoPoints.size()-1; i++) {
			pointA = new Point();
			pointB = new Point();
			if (i==0) /*first vertex of Path*/ {
				projection.toPixels(geoPoints.get(i), pointA);
				projection.toPixels(geoPoints.get(i+1), pointB);
				// find the mean azimuth of the path at each of its vertices
				meanAzimuth=Math.atan2((pointB.y-pointA.y), (pointB.x-pointA.x));
				//calculate the position at the point "back" of the first vertex of the path
				projection.toPixels(geoPoints.get(0), firstPoint);
				dx= (int) Math.round(projection.metersToEquatorPixels(pathWidth)*Math.cos(meanAzimuth+(Math.PI)));
				dy= (int) Math.round(projection.metersToEquatorPixels(pathWidth)*Math.sin(meanAzimuth+(Math.PI)));
				firstPoint.offset(dx, dy);				
			} else if (i > geoPoints.size()-2) /* last vertex of Path */{
				projection.toPixels(geoPoints.get(i-1), pointA);
				projection.toPixels(geoPoints.get(i), pointB);
				// find the mean azimuth of the path at each of its vertices
				meanAzimuth=Math.atan2((pointB.y-pointA.y), (pointB.x-pointA.x));
				//calculate the position at the point in "front" of the last vertex of the path		
				projection.toPixels(geoPoints.get(geoPoints.size()-1), middlePoint);
				dx= (int) Math.round(projection.metersToEquatorPixels(pathWidth)*Math.cos(meanAzimuth));
				dy= (int) Math.round(projection.metersToEquatorPixels(pathWidth)*Math.sin(meanAzimuth));
				middlePoint.offset(dx, dy);
			} else /* intermediate vertices of Path */ {
				projection.toPixels(geoPoints.get(i-1), pointA);
				projection.toPixels(geoPoints.get(i+1), pointB);
				// find the mean azimuth of the path at each of its vertices
				meanAzimuth=Math.atan2((pointB.y-pointA.y), (pointB.x-pointA.x));
			}			
			//Log.d(this.getClass().getName(), "Mean Azimuth= "+meanAzimuth);
			// point with offset +pi/2
			pointA = new Point();
			projection.toPixels(geoPoints.get(i), pointA);
			dx= (int) Math.round(projection.metersToEquatorPixels(pathWidth)*Math.cos(meanAzimuth+(Math.PI/2)));
			dy= (int) Math.round(projection.metersToEquatorPixels(pathWidth)*Math.sin(meanAzimuth+(Math.PI/2)));
			pointA.offset(dx, dy);
			firstHalfOfPoints.add(pointA);// add +pi/2 degrees point at the end of the firstHalfOfPoints
			// point with offset -pi/2
			pointB = new Point();
			projection.toPixels(geoPoints.get(i), pointB);
			dx= (int) Math.round(projection.metersToEquatorPixels(pathWidth)*Math.cos(meanAzimuth-(Math.PI/2)));
			dy= (int) Math.round(projection.metersToEquatorPixels(pathWidth)*Math.sin(meanAzimuth-(Math.PI/2)));
			pointB.offset(dx, dy);
			secondHalfOfPoints.add(0, pointB); // add -pi/2 degrees point at the beginning of the secondHalfOfPoints
			//Log.d(this.getClass().getName(), "Point("+i+",+90)=["+pointA.x+","+pointA.y+"]");
			//Log.d(this.getClass().getName(), "Point("+i+",-90)=["+pointB.x+","+pointB.y+"]");
		}		
		/*
		 * Merge firstPoint, firstHalfOfPoints, middlePoint, secondHalfOfPoints into an ArrayList of Points, with the
		 * right order.
		 */
		ArrayList<Point> points= new ArrayList<Point>();
		points.add(firstPoint);
		points.addAll(firstHalfOfPoints);
		points.add(middlePoint);
		points.addAll(secondHalfOfPoints);
		//Log.d(this.getClass().getName(), "points.size()= "+points.size());		
		/* 
		 * The ArrayList<Point> points forms a polygon, but the points are expressed in Cartesian coordinates of
		 * on-screen pixels. Considering the part of the map that is displayed, the current zoom level of the map
		 * and the projection system, the list of points will be converted in a list of GeoPoints, which are
		 * expressed in polar coordinates at the surface of the earth.
		 */
		ArrayList<GeoPoint> geoPoints= new ArrayList<GeoPoint>();
		for (Point point : points) {
			//Log.d(this.getClass().getName(), "point=["+point.x+","+point.y+"]");
			geoPoints.add(projection.fromPixels(point.x, point.y));
		}
		return geoPoints;		
	}
	
	/**
	 * Creates a PolygonOverlay that follows the line of this LineOverlay object, having a specified width. This
	 * method is useful for finding the area of a road, river or other line.
	 * 
	 * @param pathWidth The width in meters that the polygon will have.
	 * @return the polygon
	 */
	public CustomPolygonOverlay getPolygonOverlay(float pathWidth) {
		CustomPolygonOverlay polygonOverlay= new CustomPolygonOverlay(mapView);
		polygonOverlay.setGeoPoints(getPolygonGeoPoints(pathWidth));
		return polygonOverlay;
	}
	
	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, MapView arg3) {
		return false;
	}
	
	public void allowTaps() {
		allowTapEvents=true;
	}
	public void forbidTaps() {
		allowTapEvents=false;
	}
	
	/**
	 * Calculates the bounding box of the line and returns its center.
	 * @return The center of the box.
	 */
	public GeoPoint getCenter() {
		GeoPoint[] geoPointSpan= getSpan();
		return new GeoPoint( (geoPointSpan[0].getLatitudeE6() + geoPointSpan[1].getLatitudeE6())/2, (geoPointSpan[0].getLongitudeE6() + geoPointSpan[1].getLongitudeE6())/2 );
	}
	
	/**
	 * Calculates the bounding box of the line and returns the NW-est and the SE-est geoPoints of the box.
	 * @return Array of 2 GeoPoints. The first is the NW-est geopoint and the second is the SE-est one.
	 */
	public GeoPoint[] getSpan() {
		// set min and max for two points
		int nwLat = -90 * 1000000;
		int nwLng = 180 * 1000000;
		int seLat = 90 * 1000000;
		int seLng = -180 * 1000000;
		// find bounding lats and lngs
		for (GeoPoint point : geoPoints) {
			nwLat = Math.max(nwLat, point.getLatitudeE6());
			nwLng = Math.min(nwLng, point.getLongitudeE6());
			seLat = Math.min(seLat, point.getLatitudeE6());
			seLng = Math.max(seLng, point.getLongitudeE6());
		}
		return new GeoPoint[] {new GeoPoint(nwLat, nwLng), new GeoPoint(seLat, seLng)};
	}
	
	/**
	 * Method that returns an ArrayList with GeoPoints, with the order that user
	 * taped them
	 * 
	 * @return ArrayList with GeoPoints
	 */
	public ArrayList<GeoPoint> getGeoPoints() {
		return geoPoints;
	}
	
	/**
	 * Sets the ArrayList of GeoPoints
	 * 
	 * @param polygon
	 */
	public void setGeoPoints(ArrayList<GeoPoint> geoPoints) {
		this.geoPoints= geoPoints;
	}
	
	/**
	 * Clears the already drawn shape
	 */
	public void clearGeoPoints() {
		//this.path.rewind();
		this.geoPoints.clear();
		this.mapView.postInvalidate();
	}
	
	/**
	 * Method that sets the current mapView on which this overlay belongs.
	 * 
	 * @param mapView the MapView of the current overlay
	 */
	public void setCurrentMapView(MapView mapView) {
		this.mapView = mapView;
	}
	
	private OnClickListener mCorkyListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	      // do something when the button is clicked
	    }
	};
	
	protected int pointExist(GeoPoint my){
		
		Point point = new Point();
		Point currPoint = new Point();
		projection.toPixels(my, currPoint);
		
		for(int i=0; i<geoPoints.size(); i++ ){
			projection.toPixels(geoPoints.get(i), point);	
			if( ( Math.pow(currPoint.x-point.x, 2) + Math.pow(currPoint.y-point.y,2) ) <= 500 ){
				return i;
			}
		}
		return -1;
	}
	
	public boolean undo(){
		return undoRedo.undo();
	}
	
	public boolean redo(){
		return undoRedo.redo();
	}
	
	@Override
	public LineOverlay clone(){
		LineOverlay tmp = new LineOverlay(mapView);
		
		for(GeoPoint geopoint: geoPoints){
			GeoPoint geoClone = new GeoPoint(geopoint.getLatitudeE6(), geopoint.getLongitudeE6());
			tmp.geoPoints.add(geoClone);
		}

		tmp.undoRedo = undoRedo.clone(tmp.geoPoints);
		
		return tmp;
	}

}
