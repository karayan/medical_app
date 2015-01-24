package gr.forth.ics.urbanNet.map;

import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * Class that will enable to get data from user interaction with the Google maps. For example we can get the true geo-coordinates from a user single tap, or set a marker on the
 * screen with a double touch and so on.
 * @author surligas
 * @author syntych
 */
public abstract class PolygonOverlay extends Overlay implements Overlay.Snappable {
    private final static int FIRST_VERTEX_RADIUS = 15;
    private final static int VERTEX_RADIUS = 10;
    private Paint polygonPaint, vertexPaint;
    protected Projection projection;
    protected MapView mapView;
    protected boolean isDrawing, isValid, isDefault;
    protected ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
    protected Path path = new Path();
    protected int nVertices;
    protected boolean allowTapEvents;
    protected GeoPoint center;

    public UndoRedo undoRedo;
    protected int selectedPoint;

    public PolygonOverlay(MapView mapView) {
	this.isDrawing = false;
	this.isValid = false;
	this.isDefault = true;
	this.polygonPaint = new Paint();
	this.polygonPaint.setStrokeWidth(2);
	this.polygonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	this.polygonPaint.setARGB(100, 255, 153, 51);
	this.polygonPaint.setAntiAlias(true);
	this.vertexPaint = new Paint();
	this.vertexPaint.setARGB(100, 235, 0, 235);
	this.vertexPaint.setAntiAlias(true);
	this.projection = mapView.getProjection();
	this.mapView = mapView;
	this.nVertices = 0;
	this.allowTapEvents = true;

	selectedPoint = -1;
	undoRedo = new UndoRedo(geoPoints);
    }

    public int getNVertices() {
	return nVertices;
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
	if (shadow) {
	    if (isDrawing == false) {
		// return;
	    }
	    if (isDefault) {
		// normalizeShape();
	    }
	    boolean first = true;
	    /* Clear the old path at first */
	    path.rewind();

	    Point point = new Point();
	    int i = 0;
	    for (GeoPoint gPoint : geoPoints) {
		projection.toPixels(gPoint, point);
		if (first) {
		    path.moveTo(point.x, point.y);
		    first = false;
		}
		else path.lineTo(point.x, point.y);

		if (selectedPoint == i++) {
		    canvas.drawCircle(point.x, point.y, 20, vertexPaint);
		}
		else {
		    canvas.drawCircle(point.x, point.y, VERTEX_RADIUS, vertexPaint);
		}
	    }
	    /* If indeed is a polygon just close the perimeter */
	    if (geoPoints.size() > 2) {
		path.close();
	    }
	    canvas.drawPath(path, polygonPaint);
	    super.draw(canvas, mapView, shadow);
	}
    }

    @Override
    public boolean onSnapToItem(int arg0, int arg1, Point arg2, MapView arg3) {
	return false;
    }

    public void allowTaps() {
	allowTapEvents = true;
    }

    public void forbidTaps() {
	allowTapEvents = false;
    }

    /**
     * Method that checks if currently a polygon it is drawn on the map overlay
     * @return true if there is currently an active polygon, false otherwise
     */
    public boolean isDrawingPolygon() {
	return isDrawing;
    }

    /**
     * Method that returns an ArrayList with GeoPoints, with the order that user taped them
     * @return ArrayList with GeoPoints
     */
    public ArrayList<GeoPoint> getGeoPoints() {
	return geoPoints;
    }

    /**
     * Sets the ArrayList of GeoPoints
     * @param polygon
     */
    public void setGeoPoints(ArrayList<GeoPoint> geoPoints) {
	this.geoPoints = geoPoints;
	findCenter();
    }

    /**
     * Clears the already drawn shape
     */
    public void clearGeoPoints() {
	this.path.rewind();
	this.geoPoints.clear();
	this.isValid = false;
	this.isDrawing = false;
	this.mapView.postInvalidate();
    }

    /**
     * Method that returns if the drawn shape is a polygon or not.
     * @return true if it is polygon, false otherwise
     */
    public boolean isValid() {
	return isValid;
    }

    /**
     * Method that sets the current mapView on which this overlay belongs.
     * @param mapView the MapView of the current overlay
     */
    public void setCurrentMapView(MapView mapView) {
	this.mapView = mapView;
    }

    /**
     * Calculates the bounding box of the polygon and returns the NW-est and the SE-est geoPoints of the box.
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
	return new GeoPoint[] { new GeoPoint(nwLat, nwLng), new GeoPoint(seLat, seLng) };
    }

    public void findCenter() {
	GeoPoint[] geoPointSpan = getSpan();
	this.center = new GeoPoint((geoPointSpan[0].getLatitudeE6() + geoPointSpan[1].getLatitudeE6()) / 2, (geoPointSpan[0].getLongitudeE6() + geoPointSpan[1].getLongitudeE6()) / 2);
    }

    public GeoPoint getCenter() {
	return this.center;
    }

    public void scale(float factor) {
	Point centerPoint = new Point();
	Point point = new Point();
	double azimuth;
	double radius;
	double radius2;

	projection.toPixels(this.center, centerPoint);
	for (int i = 0; i < geoPoints.size(); i++) {
	    point = new Point();
	    projection.toPixels(geoPoints.get(i), point);
	    azimuth = Math.atan2((point.y - centerPoint.y), (point.x - centerPoint.x));
	    radius = Math.sqrt(Math.pow(point.x - centerPoint.x, 2) + Math.pow(point.y - centerPoint.y, 2));
	    radius2 = factor * radius;
	    // Log.d(this.getClass().getName(), "dr= "+(radius2-radius));
	    // point.set((int) Math.round(radius*Math.cos(azimuth)), (int) Math.round(radius*Math.sin(azimuth)));
	    // point.set(centerPoint.x, centerPoint.y);
	    point.offset((int) Math.round((radius2 - radius) * Math.cos(azimuth)), (int) Math.round((radius2 - radius) * Math.sin(azimuth)));
	    geoPoints.set(i, projection.fromPixels(point.x, point.y));
	}

    }

    public boolean undo() {
	return undoRedo.undo();
    }

    public boolean redo() {
	return undoRedo.redo();
    }

    public void setDrawingPolygon(boolean isDrawing) {
	this.isDrawing = isDrawing;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "PolygonOverlay [isDrawing=" + isDrawing + ", isValid=" + isValid + ", isDefault=" + isDefault + ", geoPoints=" + geoPoints.size() + ", path=" + path + ", nVertices=" + nVertices + ", allowTapEvents=" + allowTapEvents + ", selectedPoint=" + selectedPoint + "]";
    }
}
