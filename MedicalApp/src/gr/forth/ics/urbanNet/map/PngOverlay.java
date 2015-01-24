package gr.forth.ics.urbanNet.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * @author katsarakis
 *
 */
public class PngOverlay extends Overlay {	
	//private static final GeoPoint TOP_LEFT_FORTH_GP= getGeoPointFromDoubles(35.305385,25.071908);
	private static final GeoPoint TOP_LEFT_FORTH_GP= getGeoPointFromDoubles(35.305416,25.071868);
	
	private static final GeoPoint BOTTOM_RIGHT_FORTH_GP= getGeoPointFromDoubles(35.304809,25.072579);
	//private final static int VERTEX_RADIUS = 10;
	
	private MapView mapView;
	private Projection projection;
	private GeoPoint topLeftGeoPoint, bottomRightGeoPoint;
	private Bitmap original;
	
	//private Paint vertexPaint;
	private Paint bitmapPaint;

	/**
	 * @param topGeoPoint 
	 * @param bottomGeoPoint 
	 * 
	 */
	public PngOverlay(MapView mapView, Bitmap original, GeoPoint topLeftGeoPoint, GeoPoint bottomRightGeoPoint) {
		this.mapView= mapView;
		this.projection= mapView.getProjection();
		this.original = Bitmap.createScaledBitmap(original, original.getWidth(), original.getHeight(), true);
		this.topLeftGeoPoint = topLeftGeoPoint;
	    this.bottomRightGeoPoint = bottomRightGeoPoint;
	    
	    //this.vertexPaint= new Paint();
		//this.vertexPaint.setARGB(100, 235, 0, 235);
		//this.vertexPaint.setAntiAlias(true);
		this.bitmapPaint= new Paint();
		this.bitmapPaint.setFilterBitmap(true);
		this.bitmapPaint.setAntiAlias(true);
	}
	public PngOverlay(MapView mapView, Bitmap original) {
		this(mapView, original, TOP_LEFT_FORTH_GP, BOTTOM_RIGHT_FORTH_GP);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
		Point topLeftPoint = new Point();
		//Point topRightPoint = new Point();
		Point bottomRightPoint = new Point();
		//Point bottomLeftPoint = new Point();
		projection.toPixels(topLeftGeoPoint, topLeftPoint);
		//projection.toPixels(new GeoPoint(topLeftGeoPoint.getLatitudeE6(), bottomRightGeoPoint.getLongitudeE6()), topRightPoint);
		projection.toPixels(bottomRightGeoPoint, bottomRightPoint);
		//projection.toPixels(new GeoPoint(bottomRightGeoPoint.getLatitudeE6(), topLeftGeoPoint.getLongitudeE6()), bottomLeftPoint);
		
		//canvas.drawCircle(topLeftPoint.x, topLeftPoint.y, VERTEX_RADIUS, vertexPaint);
		//canvas.drawCircle( bottomRightPoint.x,  bottomRightPoint.y, VERTEX_RADIUS, vertexPaint);
		
		//canvas.drawLine(topLeftPoint.x, topLeftPoint.y, topRightPoint.x, topRightPoint.y, vertexPaint);
		//canvas.drawLine(topRightPoint.x, topRightPoint.y, bottomRightPoint.x, bottomRightPoint.y, vertexPaint);
		//canvas.drawLine(bottomRightPoint.x, bottomRightPoint.y, bottomLeftPoint.x, bottomLeftPoint.y, vertexPaint);
		//canvas.drawLine(bottomLeftPoint.x, bottomLeftPoint.y, topLeftPoint.x, topLeftPoint.y, vertexPaint);
		
		canvas.drawBitmap(original, null, new Rect(topLeftPoint.x, topLeftPoint.y, bottomRightPoint.x, bottomRightPoint.y), bitmapPaint);
	}
	
	private static GeoPoint getGeoPointFromDoubles(double lat, double lng) {
		return new GeoPoint(((int) Math.round(lat*1E6)), ((int) Math.round(lng*1E6)));
	}

}
