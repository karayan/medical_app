package gr.forth.ics.urbanNet.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class ImagesMapOverlay extends Overlay{
	
	private MapView mapView;
	private Projection projection;
	
    private static final int latitude = 35304854;
    private static final int longitude = 25072112;
    private GeoPoint icsMapCenter;

    private Bitmap icsMap;
    private Paint icsMapPaint;
    private Paint textPaint;
    private Paint lastMapPaint;
    
    private GeoPoint lastTap;
    
	public ImagesMapOverlay(MapView mapView){

		this.mapView = mapView;
		this.projection= mapView.getProjection();
		
	// icsMap=BitmapFactory.decodeResource(mapView.getResources(), R.drawable.aics);
		
		icsMapPaint = new Paint();
		icsMapPaint.setARGB(255, 235, 0, 235);
		icsMapPaint.setAntiAlias(true);
		
		textPaint = new Paint();
		textPaint.setARGB(255, 0, 0, 0);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(30);
		
		lastMapPaint = new Paint();
		lastMapPaint.setARGB(255, 0, 255, 0);
		lastMapPaint.setAntiAlias(true);
		lastMapPaint.setTextSize(20);
		
		icsMapCenter = new GeoPoint(latitude, longitude);
		
		lastTap = null;
		
		MapController map = mapView.getController();
		map.setZoom(12);
		map.animateTo(icsMapCenter);
		
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		lastTap = p;
		return true;
	}	

	@Override
	public void draw(Canvas canvas, MapView mapv, boolean shadow){
		// at map zoom 21 best project
		Point icsMapCenterPixels = new Point();
		mapv.getProjection().toPixels(icsMapCenter, icsMapCenterPixels);

		if( (icsMapCenterPixels.y<0 || icsMapCenterPixels.y>mapv.getMeasuredHeight()) ||
				icsMapCenterPixels.x<0 || icsMapCenterPixels.x>mapv.getMeasuredWidth()){
			
			return;
		}
		
		if(mapv.getZoomLevel()<21){
			canvas.drawCircle(icsMapCenterPixels.x, icsMapCenterPixels.y, 5, textPaint);
			canvas.drawText("ics map", icsMapCenterPixels.x, icsMapCenterPixels.y, textPaint);
			return;
		}

		Matrix matrix = new Matrix();
		float scale = (float) 0.02;
		float xc = (float) ( mapv.getZoomLevel() * scale - 0.03);
		float yc = (float) ( mapv.getZoomLevel() * scale + 0.09);
		matrix.postScale( xc, yc );
		matrix.postRotate(235);
		matrix.postTranslate(icsMapCenterPixels.x, icsMapCenterPixels.y);

	// canvas.drawBitmap(icsMap, matrix, icsMapPaint);

		if(lastTap!=null){
			Point lastTapPixels = new Point();
			mapv.getProjection().toPixels(lastTap, lastTapPixels);
			canvas.drawCircle(lastTapPixels.x, lastTapPixels.y, 5, lastMapPaint);
			canvas.drawText(lastTap.getLatitudeE6()+" "+lastTap.getLongitudeE6(), lastTapPixels.x, lastTapPixels.y, lastMapPaint);
		}
    }
	
}
