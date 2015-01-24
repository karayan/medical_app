package gr.forth.ics.urbanNet.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PointOverlay extends Overlay {
	private final static int VERTEX_RADIUS = 10;
	private Paint vertexPaint;
	private GeoPoint geoPoint;
	private MapView mapView;
	private Projection projection;

	public PointOverlay(MapView mapView) {
		this.vertexPaint= new Paint();
		this.vertexPaint.setARGB(100, 235, 0, 235);
		this.vertexPaint.setAntiAlias(true);
		this.mapView= mapView;
		this.projection= mapView.getProjection();
	}
	public PointOverlay(MapView mapView, GeoPoint geoPoint) {
		this(mapView);
		this.geoPoint=geoPoint;
	}
	
	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public void setGeoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}

	@Override
	public void draw(Canvas canvas, MapView mapv, boolean shadow){
        super.draw(canvas, mapv, shadow);        
        Point point = new Point();
        projection.toPixels(geoPoint, point);
        canvas.drawCircle(point.x, point.y, VERTEX_RADIUS, vertexPaint);
	}

}
