package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.database.Feedback.WATER;
import gr.forth.ics.urbanNet.database.SceintFeedback;
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
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * SetPlanActivity helps user to calculate the usage of MBs for a month.
 * @author Nikos Rapousis
 */
public class ScientFeedbackActivity extends ExpandableListActivity {
    private static final int CAMERA_REQUEST = 1888;
    private Button sendScore;
    private ImageView photo;
    private ImageButton capture;
    private float score;
    private WATER type_water = WATER.TAP;
    private RadioGroup radioGroup;
    private ArrayList<String> groupNames;
    private ArrayList<String> listComplaints = new ArrayList<String>();
    private ArrayList<ArrayList<FeedbackItem>> feedbackItems;
    private FeedbackItemAdapter expListAdapter;
    private SceintFeedback sFeedback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	EasyTracker.getInstance(this).activityStart(this);
	setContentView(R.layout.scient_feedback_view);
	setTitle("Scientist Feedback");
	sFeedback = new SceintFeedback();
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	sendScore = (Button) findViewById(R.id.button);
	sendScore.setBackgroundColor(UrbanNetApp.defaultColor);
	if (isOnline() == false) {
	    sendScore.setText(R.string.save_complaints);
	}
	radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
	groupNames = new ArrayList<String>();
	groupNames.add("Chemical");
	groupNames.add("Biological");
	groupNames.add("Color");
	groupNames.add("Pressure");
	groupNames.add("Appearance");

	feedbackItems = new ArrayList<ArrayList<FeedbackItem>>();
	ArrayList<FeedbackItem> feedbackItem5 = new ArrayList<FeedbackItem>();

	try {
	    feedbackItem5.add(new FeedbackItem("Turbidity (ml)", false, TYPE.textFieldNumber, 0, 12, "Turbidity"));
	    feedbackItem5.add(new FeedbackItem("Temperature (\u2103)", false, TYPE.textFieldNumber, -10, 50, "Temperature"));
	    feedbackItem5.add(new FeedbackItem("Br (ml)", false, TYPE.textFieldNumber, "Br"));
	    feedbackItem5.add(new FeedbackItem("Ph (ml)", false, TYPE.textFieldNumber, 0.0, 14.0, "Ph"));
	    feedbackItem5.add(new FeedbackItem("Cl (ml)", false, TYPE.textFieldNumber, "Cl"));
	    feedbackItem5.add(new FeedbackItem("Na (ml)", false, TYPE.textFieldNumber, "Na"));
	    feedbackItem5.add(new FeedbackItem("K (ml)", false, TYPE.textFieldNumber, "K"));
	    feedbackItem5.add(new FeedbackItem("Mg2 (ml)", false, TYPE.textFieldNumber, "Mg2"));
	    feedbackItem5.add(new FeedbackItem("No3 (ml)", false, TYPE.textFieldNumber, "No3"));
	    feedbackItem5.add(new FeedbackItem("A (ml)", false, TYPE.textFieldNumber, "A"));
	    feedbackItem5.add(new FeedbackItem("DO (ml)", false, TYPE.textFieldNumber, "DO"));
	    feedbackItem5.add(new FeedbackItem("COD (ml)", false, TYPE.textFieldNumber, "COD"));
	    feedbackItem5.add(new FeedbackItem("BOD (ml)", false, TYPE.textFieldNumber, "BOD"));
	    feedbackItem5.add(new FeedbackItem("Acidity (ml)", false, TYPE.textFieldNumber, "Acidity"));
	    feedbackItem5.add(new FeedbackItem("P (ml)", false, TYPE.textFieldNumber, "P"));
	    feedbackItem5.add(new FeedbackItem("N (ml)", false, TYPE.textFieldNumber, "N"));
	    feedbackItem5.add(new FeedbackItem("H (ml)", false, TYPE.textFieldNumber, "H"));
	    feedbackItem5.add(new FeedbackItem("C (ml)", false, TYPE.textFieldNumber, "C"));
	    feedbackItem5.add(new FeedbackItem("Ca (ml)", false, TYPE.textFieldNumber, "Ca"));
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	feedbackItems.add(feedbackItem5);

	ArrayList<FeedbackItem> feedbackItem4 = new ArrayList<FeedbackItem>();
	feedbackItem4.add(new FeedbackItem("Ephemeroptera", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Plecoptera", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Mollusca", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Trichoptera", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Escherichia coli", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Coliform bacteria", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Cryptosporidium", false, TYPE.checkBox));
	feedbackItem4.add(new FeedbackItem("Giardia lamblia", false, TYPE.checkBox));
	feedbackItems.add(feedbackItem4);

	ArrayList<FeedbackItem> feedbackItem1 = new ArrayList<FeedbackItem>();
	feedbackItem1.add(new FeedbackItem("Red", false, TYPE.radio));
	feedbackItem1.add(new FeedbackItem("Green", false, TYPE.radio));
	feedbackItem1.add(new FeedbackItem("Black", false, TYPE.radio));
	feedbackItems.add(feedbackItem1);

	ArrayList<FeedbackItem> feedbackItem3 = new ArrayList<FeedbackItem>();
	feedbackItem3.add(new FeedbackItem("Low pressure", false, TYPE.radio));
	feedbackItem3.add(new FeedbackItem("No pressure", false, TYPE.radio));
	feedbackItem3.add(new FeedbackItem("High pressure", false, TYPE.radio));
	feedbackItems.add(feedbackItem3);
	ArrayList<FeedbackItem> feedbackItem2 = new ArrayList<FeedbackItem>();
	feedbackItem2.add(new FeedbackItem("Floating particles", false, TYPE.checkBox));
	feedbackItem2.add(new FeedbackItem("Sand", false, TYPE.checkBox));
	feedbackItem2.add(new FeedbackItem("Milky", false, TYPE.checkBox));
	feedbackItem2.add(new FeedbackItem("Rusty", false, TYPE.checkBox));
	feedbackItems.add(feedbackItem2);
	expListAdapter = new FeedbackItemAdapter(getApplicationContext(), groupNames, feedbackItems, sFeedback);
	setListAdapter(expListAdapter);

	sendScore.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {

		if (LocationService.lastKnownLocation == null) {
		    Toast.makeText(getApplicationContext(), "Enable WiFi or GPS localazition", Toast.LENGTH_LONG).show();
		}
		else {
		    sFeedback.setTimestamp(new Date().getTime());
		    sFeedback.setLocation(LocationService.lastKnownLocation);
		    sFeedback.setWaterType(type_water.ordinal());
		    Intent intent = new Intent(getApplicationContext(), MapViewActivity.class);
		    Bundle b = new Bundle();
		    b.putSerializable("sfeedback", sFeedback);
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

	radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

	    @Override
	    public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.tap_water) {
		    type_water = WATER.TAP;
		    if (groupNames.get(0).compareTo("Brand") == 0) {
			groupNames.remove(0);
			feedbackItems.remove(0);
			sFeedback.setWatername(" ");
			expListAdapter = new FeedbackItemAdapter(getApplicationContext(), groupNames, feedbackItems, sFeedback);
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
			expListAdapter = new FeedbackItemAdapter(getApplicationContext(), groupNames, feedbackItems, sFeedback);
			setListAdapter(expListAdapter);
		    }
		    catch (Exception e) {
			e.printStackTrace();
		    }
		}
		else if (checkedId == R.id.spring_water) {
		    type_water = WATER.NATURAL;
		    if (groupNames.get(0).compareTo("Brand") == 0) {
			groupNames.remove(0);
			feedbackItems.remove(0);
			sFeedback.setWatername(" ");
			expListAdapter = new FeedbackItemAdapter(getApplicationContext(), groupNames, feedbackItems, sFeedback);
			setListAdapter(expListAdapter);
		    }
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
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth() / 2; // deprecated
		Bitmap data = (Bitmap) intent.getExtras().get("data");
		photo.setVisibility(View.VISIBLE);
		// photo.setImageBitmap(capture);
		photo.setImageBitmap(Bitmap.createScaledBitmap(data, width, width / 2, false));
		capture.setVisibility(View.GONE);
	    }
	}
	catch (NullPointerException e) {

	}
    }

    @Override
    public void onBackPressed() {
	Intent intent = new Intent(getApplicationContext(), MapViewActivity.class);
	startActivity(intent);
	finish();

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
