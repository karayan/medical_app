/**
 * 
 */
package gr.forth.ics.urbanNet.map;

import gr.forth.ics.urbanNet.utilities.Polygon;

import java.util.ArrayList;

import android.graphics.Point;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * @author katsarakis
 */
public class RegularPolygonOverlay extends PolygonOverlay {
    /*private ArrayList<GeoPoint> polygon = new ArrayList<GeoPoint>(); private Path path = new Path(); private Paint polygonPaint; private boolean isDrawing = false, isPolygonValid
     * = false, isDefault = true; private MapView mapViewFromActivity; private Float accuracy = null, pxAccuracy; private GeoPoint center = null; private Point pxCenter; private
     * final static int FIRST_CIRCLE_RADIOUS = 15; private final static int CIRCLE_RADIOUS = 10; private Paint circlePaint; private Projection projection; private int vertices;
     * private boolean allowTapEvents; public PolygonOverlay(MapView mapView) { polygonPaint = new Paint(); polygonPaint.setStrokeWidth(2);
     * polygonPaint.setStyle(Paint.Style.FILL_AND_STROKE); polygonPaint.setARGB(100, 255, 153, 51); polygonPaint.setAntiAlias(true); mapViewFromActivity = mapView; circlePaint =
     * new Paint(); circlePaint.setARGB(100, 235, 0, 235); circlePaint.setAntiAlias(true); projection = mapView.getProjection(); pxCenter = new Point(); vertices = 4;
     * allowTapEvents=true; } */
    // private GeoPoint center; // position in earth (geography coordinates)
    // private Point pixelCenter; // position in screen (pixel coordinates)
    private float radius; // in meters
    // private float pixelRadius; //in pixels
    private boolean allowTapEvents;

    public RegularPolygonOverlay(MapView mapView, GeoPoint centerGeoPoint, float radius, int nVertices) {
	super(mapView);
	this.center = centerGeoPoint;
	this.radius = radius;
	super.nVertices = nVertices;
	this.allowTapEvents = true;
	normalizeShape();
    }

    public RegularPolygonOverlay(MapView mapView, Location loc, int nVertices) {
	super(mapView);
	super.center = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
	this.radius = Math.max(50, loc.getAccuracy());
	super.nVertices = nVertices;
	this.allowTapEvents = true;
	normalizeShape();
    }

    public void moveToCurrentLocation(Location loc) {
	Log.d(this.getClass().getName(), "got it: " + loc.toString());
	this.radius = loc.getAccuracy();
	super.center = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
	// projection.toPixels(this.center, pixelCenter);
	// pixelRadius = projection.metersToEquatorPixels(radius);
    }

    public void moveToGeoPoint(GeoPoint geoPoint) {
	super.center = geoPoint;
	normalizeShape();
    }

    public Point getPixelCenter() {
	Point pixelCenter = null;
	return projection.toPixels(super.center, pixelCenter);
    }

    public float getPixelRadius() {
	return projection.metersToEquatorPixels(radius);
    }

    public void setNVertices(int n) {
	super.nVertices = n;
	normalizeShape();
    }

    @Override
    public int getNVertices() {
	return super.nVertices;
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
	if (super.allowTapEvents) {
	    moveToGeoPoint(p);
	    /*isDefault= false; geoPoints.add(p); nVertices++; isDrawing = true; if (geoPoints.size() > 2) { isValid= true; } */
	    return true;
	    // return super.onTap(p, mapView);
	}
	else {
	    return false;
	}
    }

    public void normalizeShape() {
	geoPoints.clear();
	projection.toPixels(super.center, getPixelCenter());
	// pixelRadius = projection.metersToEquatorPixels(radius);
	ArrayList<Point> polygonList = Polygon.getPolygon(getPixelCenter(), 10 * getPixelRadius(), nVertices, 0);
	for (Point p : polygonList) {
	    geoPoints.add(projection.fromPixels(p.x, p.y));
	}
    }

}
