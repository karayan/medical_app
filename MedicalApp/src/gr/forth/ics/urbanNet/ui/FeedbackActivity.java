package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.database.Feedback;
import gr.forth.ics.urbanNet.database.Feedback.WATER;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.preferences.MainPreferenceActivity;
import gr.forth.ics.urbanNet.ui.FeedbackItem.TYPE;

import java.util.ArrayList;
import java.util.Date;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackActivity extends ExpandableListActivity {

    private FeedbackItemAdapter expListAdapter;
    private static final int CAMERA_REQUEST = 1888;
    private Bitmap imBitmap = null;
    private ArrayList<String> listComplaints = new ArrayList<String>();
    private Button sendScore;
    private ImageView photo;
    private ImageButton capture;
    private RatingBar rate;
    private float score;
    private TextView textScore;
    private ArrayList<ArrayList<FeedbackItem>> feedbackItems;
    private boolean state_photo = true;
    private RadioGroup radioGroup;
    private ArrayList<String> groupNames;
    private WATER type_water = WATER.TAP;
    private Feedback feedback;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	setTitle("Customer Feedback");
	setContentView(R.layout.feedback_view);
	feedback = new Feedback();
	sendScore = (Button) findViewById(R.id.button);
	sendScore.setBackgroundColor(UrbanNetApp.defaultColor);
	if (isOnline() == false) {
	    sendScore.setText(R.string.save_complaints);
	}
	photo = (ImageView) findViewById(R.id.imageView1);
	textScore = (TextView) findViewById(R.id.textView1);
	photo.setVisibility(View.GONE);
	photo.setClickable(true);
	capture = (ImageButton) findViewById(R.id.capture);
	rate = (RatingBar) findViewById(R.id.ratingBar);
	score = rate.getRating();
	radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
	groupNames = new ArrayList<String>();
	groupNames.add("Color");
	groupNames.add("Taste");
	groupNames.add("Odor");
	groupNames.add("Pressure");
	groupNames.add("Appearance");

	feedbackItems = new ArrayList<ArrayList<FeedbackItem>>();
	ArrayList<FeedbackItem> feedbackItem1 = new ArrayList<FeedbackItem>();
	feedbackItem1.add(new FeedbackItem("Red", false, TYPE.radio));
	feedbackItem1.add(new FeedbackItem("Green", false, TYPE.radio));
	feedbackItem1.add(new FeedbackItem("Black", false, TYPE.radio));
	feedbackItems.add(feedbackItem1);

	ArrayList<FeedbackItem> feedbackItem2 = new ArrayList<FeedbackItem>();
	feedbackItem2.add(new FeedbackItem("Bitter", false, TYPE.checkBox));
	feedbackItem2.add(new FeedbackItem("Salty", false, TYPE.checkBox));
	feedbackItem2.add(new FeedbackItem("Sweet", false, TYPE.checkBox));
	feedbackItems.add(feedbackItem2);

	feedbackItem2 = new ArrayList<FeedbackItem>();
	feedbackItem2.add(new FeedbackItem("Chlorine", false, TYPE.checkBox));
	feedbackItem2.add(new FeedbackItem("Chemical", false, TYPE.checkBox));
	feedbackItem2.add(new FeedbackItem("Sewer", false, TYPE.checkBox));
	feedbackItem2.add(new FeedbackItem("Gasoline", false, TYPE.checkBox));
	feedbackItems.add(feedbackItem2);

	ArrayList<FeedbackItem> feedbackItem3 = new ArrayList<FeedbackItem>();
	feedbackItem3.add(new FeedbackItem("Low pressure", false, TYPE.radio));
	feedbackItem3.add(new FeedbackItem("No pressure", false, TYPE.radio));
	feedbackItem3.add(new FeedbackItem("High pressure", false, TYPE.radio));
	feedbackItems.add(feedbackItem3);

	ArrayList<FeedbackItem> feedbackItem4 = new ArrayList<FeedbackItem>();
	feedbackItem4.add(new FeedbackItem("Floating particles", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Sand", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Milky", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Rusty", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Animal", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Plant", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Stain", false, TYPE.checkBox));
	feedbackItems.add(feedbackItem4);

	expListAdapter = new FeedbackItemAdapter(getApplicationContext(), groupNames, feedbackItems, feedback);
	setListAdapter(expListAdapter);
	radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

	    @Override
	    public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.tap_water) {
		    type_water = WATER.TAP;
		    if (groupNames.get(0).compareTo("Brand") == 0) {
			groupNames.remove(0);
			feedbackItems.remove(0);
			feedback.setWatername(" ");
			expListAdapter = new FeedbackItemAdapter(getApplicationContext(), groupNames, feedbackItems, feedback);
			setListAdapter(expListAdapter);
		    }
		}
		else if (checkedId == R.id.bottle_water) {
		    type_water = WATER.BOTTLE;
		    groupNames.add(0, "Brand");
		    ArrayList<FeedbackItem> feedbackItem0 = new ArrayList<FeedbackItem>();
		    try {
			feedbackItem0.add(new FeedbackItem("Selinari", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Zaros", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Rouvas", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Lutos", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Sitiako", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Zagori", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Krini", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Vikos", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Samaria", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Avra", false, TYPE.radioText, "Watername"));
			feedbackItem0.add(new FeedbackItem("Other", false, TYPE.textFieldString, "Watername"));
			feedbackItems.add(0, feedbackItem0);
			expListAdapter = new FeedbackItemAdapter(getApplicationContext(), groupNames, feedbackItems, feedback);
			setListAdapter(expListAdapter);
		    }
		    catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		else if (checkedId == R.id.spring_water) {
		    type_water = WATER.NATURAL;
		    if (groupNames.get(0).compareTo("Brand") == 0) {
			groupNames.remove(0);
			feedbackItems.remove(0);
			feedback.setWatername(" ");
			expListAdapter = new FeedbackItemAdapter(getApplicationContext(), groupNames, feedbackItems, feedback);
			setListAdapter(expListAdapter);
		    }
		}
	    }
	});
	rate.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
	    @Override
	    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		if (rate.getRating() > 0) {
		    score = rate.getRating();
		}
		Log.d(this.getClass().getName(), "The rate is: " + rating);
		if (rating >= 0.0 && rating <= 1.0) {
		    textScore.setText("BAD");
		}
		else if (rating > 1.0 && rating <= 2.0) {
		    textScore.setText("POOR");
		}
		else if (rating > 2.0 && rating <= 3.0) {
		    textScore.setText("FAIR");
		}
		else if (rating > 3.0 && rating <= 4.0) {
		    textScore.setText("GOOD");
		}
		else if (rating > 4.0 && rating <= 5.0) {
		    textScore.setText("EXCELLENT");
		}
	    }
	});

	capture.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		takephoto();
	    }
	});

	sendScore.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (LocationService.lastKnownLocation == null) {
		    Toast.makeText(getApplicationContext(), "Enable WiFi or GPS localazition", Toast.LENGTH_LONG).show();
		}
		else {
		    Intent intent = new Intent(getApplicationContext(), MapViewActivity.class);
		    Bundle b = new Bundle();
		    feedback.setTimestamp(new Date().getTime());
		    feedback.setLocation(LocationService.lastKnownLocation);
		    feedback.setImage(imBitmap);
		    feedback.setScore(score);
		    feedback.setWaterType(type_water.ordinal());
		    b.putSerializable("feedback", feedback);
		    intent.putExtras(b);
		    startActivity(intent);
		    if (!isOnline()) {
			Toast.makeText(getApplicationContext(), "Feedback saved locally.\n Enable Internet to upload", Toast.LENGTH_SHORT).show();
		    }
		    else {
			Toast.makeText(getApplicationContext(), "Feedback send to server", Toast.LENGTH_SHORT).show();
		    }
		    finish();
		}
	    }
	});

	photo.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {

		if (state_photo) {
		    Display display = getWindowManager().getDefaultDisplay();
		    int width = display.getWidth(); // deprecated
		    photo.setImageBitmap(Bitmap.createScaledBitmap(imBitmap, width, width / 2, false));
		    state_photo = false;
		}
		else {
		    photo.setImageBitmap(imBitmap);
		    state_photo = true;
		}
	    }
	});
    }

    private void takephoto() {
	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	try {
	    if (intent.getExtras().get("data") != null && requestCode == CAMERA_REQUEST) {

		imBitmap = (Bitmap) intent.getExtras().get("data");
		photo.setVisibility(View.VISIBLE);
		photo.setImageBitmap(imBitmap);
		capture.setVisibility(View.GONE);
		state_photo = true;
	    }
	}
	catch (NullPointerException e) {
	    e.printStackTrace();
	}
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
    public void onBackPressed() {
	if (state_photo == false) {
	    photo.setImageBitmap(imBitmap);
	    state_photo = true;
	}
	else {
	    Intent intent = new Intent(getApplicationContext(), MapViewActivity.class);
	    startActivity(intent);
	    finish();
	}
    }

    public Boolean isOnline() {
	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo ni = cm.getActiveNetworkInfo();
	if (ni == null) {
	    // There are no active networks.
	    return false;
	}
	else return true;
    }
}
