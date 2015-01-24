package gr.forth.ics.urbanNet.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.MapView;

public class CustomMapView extends MapView {

	private boolean isMovable = false;

	public CustomMapView(Context context, String apiKey) {
		super(context, apiKey);

	}

	public CustomMapView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public CustomMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(isMovable)
			return super.onTouchEvent(event);
	
		return false;
		
	}

	public void setMovable(boolean movable) {

		isMovable = movable;

	};

}
