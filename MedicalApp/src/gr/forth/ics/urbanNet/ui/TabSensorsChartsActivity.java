package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.preferences.MainPreferenceActivity;
import gr.forth.ics.urbanNet.ui.SimpleGestureFilter.SimpleGestureListener;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TabHost;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * @author Nikos Rapousis The UtrafficListTabActivity contain the TrafficDemandActivity,AppConsmuptionActivity and SetPlanActivity
 */
public class TabSensorsChartsActivity extends TabActivity implements SimpleGestureListener {
    public enum CONN_STATE {
	CONNECTED, LOST_CONNECTION, NOT_CONNECTED, DISCONNECTED
    }
    private SimpleGestureFilter detector;
    private TabHost tabHost;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	EasyTracker.getInstance(this).activityStart(this);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	setContentView(R.layout.u_sensors_charts_view);
	setTitle("Traffic Demand Charts");
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	Resources res = getResources(); // Resource object to get Drawables
	tabHost = getTabHost(); // The activity TabHost
	TabHost.TabSpec spec; // Reusable TabSpec for each tab
	Intent intent; // Reusable Intent for each tab

	/* Adding the TrafficHistoryChartsActivity Tab */

	intent = new Intent().setClass(this, LineChartsActivity.class);
	spec = tabHost.newTabSpec("LINE").setIndicator("Line chart", res.getDrawable(R.drawable.line_chart)).setContent(intent);
	tabHost.addTab(spec);

	/* Adding the TrafficDemandPieActivity Tab */
	intent = new Intent().setClass(this, BarChartsActivity.class);
	spec = tabHost.newTabSpec("BAR").setIndicator("Bar chart", res.getDrawable(R.drawable.bar_chart)).setContent(intent);
	tabHost.addTab(spec);

	// Setting the current Tab
	int tabNumber = 0;
	tabHost.setCurrentTab(tabNumber);
	detector = new SimpleGestureFilter(this, this);
    }

    @Override
    protected void onResume() {
	super.onResume();
    }

    /**
     * This function is called when a MotionEvent is detected. It passes the MotionEvent to the SimpleGestureFilter detector.onTouchEvent() method, in order to recognize if a Swipe
     * or DoubleTap event was performed.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
	this.detector.onTouchEvent(me);
	return super.dispatchTouchEvent(me);
    }

    /**
     * This function is called when a Swipe Gesture is detected.
     */
    @Override
    public void onSwipe(int direction) {
	switch (direction) {
	case SimpleGestureFilter.SWIPE_RIGHT:
	    // tabHost.setCurrentTab(0);
	    // overridePendingTransition(R.anim.come_from_left, R.anim.go_to_right);
	    break;
	case SimpleGestureFilter.SWIPE_LEFT:
	    // tabHost.setCurrentTab(1);
	    // overridePendingTransition(R.anim.come_from_right, R.anim.go_to_left);
	    break;
	}
    }

    /**
     * This function is called when a DoubleTap event is detected.
     */
    @Override
    public void onDoubleTap() {
	// Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
    }

    /**
     * Adds the menu. Menu is the bottom menu which becomes visible by pressing the menu-button.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.main_menu, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Intent intent = new Intent();
	switch (item.getItemId()) {
	case R.id.options:
	    intent.setClass(this, MainPreferenceActivity.class);
	    startActivity(intent);
	    break;
	case R.id.close:
	    finish();
	    break;
	}
	return false;
    }

    @Override
    public void onStop() {
	super.onStop();
	EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }
}
