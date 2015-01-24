package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.ui.SimpleGestureFilter.SimpleGestureListener;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class IntroActivity extends Activity implements SimpleGestureListener {
    private SimpleGestureFilter detector;
    private TextView body;
    private TextView header;
    private int num_text = 0;
    private int[] text = { R.string.info_1, R.string.info_2, R.string.info_3, R.string.info_4, R.string.info_5, R.string.info_6, R.string.info_7, R.string.info_8, R.string.info_9 };
    private Button button;
    private ImageButton next, prev;
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	EasyTracker.getInstance(this).activityStart(this);
	setContentView(R.layout.intro_view);
	this.setTitle("Interesting information");
	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	detector = new SimpleGestureFilter(this, this);
	body = (TextView) findViewById(R.id.body);
	header = (TextView) findViewById(R.id.header);
	header.setText(R.string.did_you_know + " that:");
	body.setText(text[num_text]);
	button = (Button) findViewById(R.id.button);
	button.setVisibility(View.GONE);
	button.setText(R.string.just_do);
	button.setBackgroundColor(UrbanNetApp.defaultColor);
	next = (ImageButton) findViewById(R.id.next);
	prev = (ImageButton) findViewById(R.id.prev);
	// prev.setVisibility(View.GONE);
	// next.setVisibility(View.GONE);
	button.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		finish();

	    }
	});
	prev.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		previousTouch();

	    }
	});
	next.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		nextTouch();

	    }
	});
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
	    overridePendingTransition(R.anim.come_from_left, R.anim.go_to_right);
	    previousTouch();
	    break;
	case SimpleGestureFilter.SWIPE_LEFT:
	    nextTouch();
	    overridePendingTransition(R.anim.come_from_right, R.anim.go_to_left);
	    break;
	}
    }

    private void previousTouch() {
	num_text--;
	    if (num_text < 0) {
		num_text = 0;
	    }
	    body.setText(text[num_text]);
    }

    private void nextTouch() {
	num_text++;
	    if (num_text == text.length) {
		button.setVisibility(View.VISIBLE);
		num_text = text.length - 1;
	    }
	    else {
		body.setText(text[num_text]);
	    }
    }
    @Override
    public void onDoubleTap() {

    }

}
