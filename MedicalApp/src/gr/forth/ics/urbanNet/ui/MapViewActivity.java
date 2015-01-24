package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.database.Feedback;
import gr.forth.ics.urbanNet.database.Query;
import gr.forth.ics.urbanNet.database.SceintFeedback;
import gr.forth.ics.urbanNet.database.SearchedPolygon;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.map.CustomPolygonOverlay;
import gr.forth.ics.urbanNet.map.PolygonOverlay;
import gr.forth.ics.urbanNet.map.RegularPolygonOverlay;
import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.network.NetworkServiceConnectable;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;
import gr.forth.ics.urbanNet.network.Response;
import gr.forth.ics.urbanNet.network.ServerException;
import gr.forth.ics.urbanNet.preferences.MainPreferenceActivity;
import gr.forth.ics.urbanNet.utilities.UserID;

import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;

import org.acra.ACRA;
import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.maps.GeoPoint;

public class MapViewActivity extends OrmLiteBaseMapActivity<DatabaseHelper> implements NetworkServiceConnectable {

    private AutoCompleteTextView searchInput;
    private Button feedbackButton, searchAreaButton;
    // private ImageButton favoritesButton;
    // private ImageButton graphs;
    private ImageButton settingButton, sensorButton, undoButton, redoButton, clearProvidersListButton;
    private PolygonOverlay polygonOverlay;
    private QueryResponse providersListAdapter;
    private ListView waterScoreList;
    private LinearLayout viewLayout_providers, viewLayout_map;
    private RadioGroup toolsRadioGroup;
    private AlertDialog.Builder alertBuilder;
    private AlertDialog queryTypeAlert;
    private RadioButton hexagon, customShape;
    private static String responseString, queryToken;
    private boolean onAdvancedMode;
    private WifiManager wifiManager;
    private InputMethodManager imm;
    private InstallApp installApp;
    private Editor appPrefsEditor;
    private SharedPreferences appPrefs;
    private ArrayList<GeoPoint> te;
    private int checkedRadios;
    private String[] COUNTRIES;
    private Tracker umapTrackerEvent;
    private Map<String, String> params = new HashMap<String, String>();
    private Toast search_until, internet;
    private long untilTimestamp;

    /**
     * urbanNet server responds to a network recommendation query with a list of network names, types, and scores. Besides this information, it is very useful for the user to show
     * also if a network included in the recommendations list is currently available (i.e., the network interface is receiving beacons from this network). This class implements a
     * compact method to receive a single and updated list of scanResults, and use both this list, and the response of the urbanNet server to the query. Both of these data
     * structures are received asynchronously, so this class has to wait until both are available, before presenting the calculated result to the user.
     * @author michalis
     */
    private class QueryPresenter {
	private Query query;
	private boolean autoPresent = false;
	private Timer timer = new Timer();
	private ProgressDialog progressDialog;

	/** Basic Constror */
	public QueryPresenter() {
	    super();
	    autoPresent = true;
	    startTimer();
	}

	/***
	 * Constructor In case the query and its responseString is provided in the constructor ({@link MapViewActivity} is started from History or Favorites), this object will have
	 * to wait only for the scanResults, and will be ready to present the query as soon as they are available. So, this constructor sets the {@link QueryPresenter#autoPresent}
	 * to true.
	 * @param query The query to be presented to the user.
	 */
	public QueryPresenter(Query query) {
	    this();
	    this.query = query;
	    autoPresent = true;
	    progressDialog = ProgressDialog.show(MapViewActivity.this, "", "Calculate score");
	}

	/**
	 * In case this object was created when the user was executing a query, this method should be called as soon as the server response arrives, to provide a complete Query
	 * object, with non-null responseString to this object.
	 */
	public void setQuery(Query query) {
	    this.query = query;
	}

	/**
	 * If the scanResults have not arrived after a given time, probably the WiFi interface is turned-off. To address this issue, after a given time a counter is timed-out and
	 * triggers the presentation of the query, showing all WiFi recommendations as "NOT AVAILABLE".
	 */
	public void startTimer() {
	    timer.schedule((new TimerTask() {
		@Override
		public void run() {
		    if (query != null) presentQuery();
		}
	    }), 1600);
	}

	/**
	 * Presents the query response to the user, using the UI. The appropriate manipulations on the View objects are done here.
	 */
	void presentQuery() {
	    runOnUiThread(new Runnable() {
		@Override
		public void run() {
		    if (progressDialog != null) progressDialog.cancel();
		    if (polygonOverlay != null) {
			map.getOverlays().remove(polygonOverlay);
		    }
		    polygonOverlay = new CustomPolygonOverlay(map);

		    if (query.getQueryPolygonName() != null) {
			// query by city/village name
			polygonOverlay.setGeoPoints(query.getQueryGeoPointsName());
		    }
		    else {
			// query by predefined/manual polygon
			polygonOverlay.setGeoPoints(query.getQueryGeoPoints());
		    }
		    polygonOverlay.forbidTaps();
		    polygonOverlay.setDrawingPolygon(true);
		    map.getOverlays().add(polygonOverlay);
		    providersListAdapter = new QueryResponse(MapViewActivity.this, getBaseContext(), waterScoreList, query.getResponseString(), isCurrLocInShownArea());
		    openWaterScoreList();
		    map.invalidate();
		    centerMapOnPolygonOverlay();
		    if (!onAdvancedMode) {
			clearOverlays();
		    }
		    if (searchInput.getTextSize() != 0) {
			searchInput.setCursorVisible(false);
			searchInput.setText("");
			searchInput.setHint("Query City");
			searchInput.invalidate();
			searchInput.refreshDrawableState();
		    }
		    MapViewActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	    });
	}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	if (keyCode == KeyEvent.KEYCODE_BACK) {
	    if (waterScoreList.isShown()) {
		closeProvidersList();
	    }
	    else if (polygonOverlay != null && polygonOverlay.isDrawingPolygon() && polygonOverlay.getGeoPoints().size() > 0) {
		if (onAdvancedMode) {
		    toolsRadioGroup.clearCheck();
		    undoButton.setVisibility(View.GONE);
		    redoButton.setVisibility(View.GONE);
		}
		map.getOverlays().remove(polygonOverlay);
		polygonOverlay.clearGeoPoints();
		polygonOverlay = null;
		map.invalidate();

	    }
	    else {
		super.onBackPressed();
	    }
	}
	return false;
    }

    @Override
    protected void onResume() {
	super.onResume();
	checkAdvancedMode();
	if (providersListAdapter != null) {
	    waterScoreList.refreshDrawableState();
	    providersListAdapter.refreshView();
	    viewLayout_providers.refreshDrawableState();
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTitle("urbanNet");
	EasyTracker.getInstance(this).activityStart(this);
	startGoogleTracking();
	umapTrackerEvent = GoogleAnalytics.getInstance(this).getTracker("UA-49826422-1");
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	appPrefs = getSharedPreferences(UrbanNetApp.APP_PREFERENCE_FILENAME, MODE_PRIVATE);
	wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	new Toast(getApplicationContext());
	internet = Toast.makeText(getApplicationContext(), "No Internet connection", Toast.LENGTH_LONG);

	alertBuilder = new AlertDialog.Builder(this);
	alertBuilder.setTitle(getString(R.string.query_type_alert_title));

	/* View radioGroupView = View.inflate(this, R.layout.radio_group, null); RadioGroup radiogroup = (RadioGroup) radioGroupView.findViewById(R.id.radioGroup1);
	 * radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	 * @Override public void onCheckedChanged(RadioGroup arg0, int checkID) { Calendar calendar = Calendar.getInstance(); if (checkID == R.id.radio_12) {
	 * calendar.add(Calendar.HOUR, -12); Date newDate = calendar.getTime(); Toast.makeText(getApplicationContext(), "Search until: " + newDate.toGMTString(),
	 * Toast.LENGTH_LONG).show(); } else if (checkID == R.id.radio_week) { calendar.add(Calendar.DAY_OF_YEAR, -7); Date newDate = calendar.getTime();
	 * Toast.makeText(getApplicationContext(), "Search until: " + newDate.toGMTString(), Toast.LENGTH_LONG).show(); } else if (checkID == R.id.radio_month) {
	 * calendar.add(Calendar.MONTH, -1); Date newDate = calendar.getTime(); Toast.makeText(getApplicationContext(), "Search until: " + newDate.toGMTString(),
	 * Toast.LENGTH_LONG).show(); } } alertBuilder.setView(radioGroupView); }); */

	View spinnerView = View.inflate(this, R.layout.spinner, null);
	Spinner spinner = (Spinner) spinnerView.findViewById(R.id.spinner1);
	List<String> list = new ArrayList<String>();
	list.add("Last 12hour measurements");
	list.add("Last week measurements");
	list.add("Last month measurements");
	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	spinner.setAdapter(dataAdapter);
	spinner.setSelection(0);
	new Toast(getApplicationContext());
	search_until = Toast.makeText(getApplicationContext(), " ", Toast.LENGTH_LONG);
	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

	    @Override
	    public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
		Calendar calendar = Calendar.getInstance();
		Date newDate = new Date();
		if (position == 0) {
		    calendar.add(Calendar.HOUR, -12);
		    newDate = calendar.getTime();
		    search_until.setText("Search until: " + newDate.toLocaleString());
		}
		else if (position == 1) {
		    calendar.add(Calendar.DAY_OF_YEAR, -7);
		    newDate = calendar.getTime();
		    search_until.setText("Search until: " + newDate.toLocaleString());
		}
		else if (position == 2) {
		    calendar.add(Calendar.MONTH, -1);
		    newDate = calendar.getTime();
		    search_until.setText("Search until: " + newDate.toLocaleString());
		}
		untilTimestamp = newDate.getTime();
		search_until.show();
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> arg0) {

	    }

	});
	alertBuilder.setView(spinnerView);
	// display only the 3rd query
	CharSequence[] queryDescriptions = { Query.queryType[2], Query.queryType[3] };
	alertBuilder.setItems(queryDescriptions, new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int item) {
		if (networkServiceConnection.isBound() && isConnective()) {
		    // Because display only the 3rd query selected
		    // (item+2)
		    if (!searchInput.getText().toString().equals("")) {
			queryToken = searchInput.getText().toString();
			new LocationAsyncTask().execute(queryToken, Integer.toString(item + 2));
		    }
		    else {
			new QueryAsyncTask().execute(item + 2);
		    }
		}
		else {

		}
	    }
	});

	searchInput = (AutoCompleteTextView) findViewById(R.id.searchInput);
	searchInput.setText("");
	searchInput.setHint("Query City");
	updateSearchedAreas();

	queryTypeAlert = alertBuilder.create();
	networkServiceConnection = new NetworkServiceConnection(this);
	bindService(new Intent(this, NetworkService.class), super.networkServiceConnection, Context.BIND_AUTO_CREATE);

	// Need the location and network service
	Intent intent = new Intent(this, LocationService.class);
	bindService(intent, super.locationServiceConnection, Context.BIND_AUTO_CREATE);

	// find the interface objects to handle them
	viewLayout_map = (LinearLayout) findViewById(R.id.viewLayout_map);
	viewLayout_providers = (LinearLayout) findViewById(R.id.viewLayout_providers);

	searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
	searchInput.setCursorVisible(false);

	feedbackButton = (Button) findViewById(R.id.feedbackButton);
	// favoritesButton = (ImageButton) findViewById(R.id.favoritesButton);
	// graphs = (ImageButton) findViewById(R.id.graphs);
	settingButton = (ImageButton) findViewById(R.id.settingButton);
	sensorButton = (ImageButton) findViewById(R.id.sensorButton);
	undoButton = (ImageButton) findViewById(R.id.undo);
	redoButton = (ImageButton) findViewById(R.id.redo);
	searchAreaButton = (Button) findViewById(R.id.searchAreaButton);
	clearProvidersListButton = (ImageButton) findViewById(R.id.clearProvidersListButton);
	waterScoreList = (ListView) findViewById(R.id.providersList);
	toolsRadioGroup = (RadioGroup) findViewById(R.id.toolsRadioGroup);
	/* toggle showing the current location */

	toolsRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    @Override
	    public void onCheckedChanged(RadioGroup group, int checkedId) {
		// do nothing
	    }
	});
	hexagon = (RadioButton) toolsRadioGroup.findViewById(R.id.hexagonRadio);
	customShape = (RadioButton) toolsRadioGroup.findViewById(R.id.customShapeRadio);

	hexagon.setOnTouchListener(new OnTouchListener() {
	    @Override
	    public boolean onTouch(View v, MotionEvent event) {
		hexagon.setChecked(true);
		hexagon.setClickable(true);
		if (undoButton.getVisibility() == View.VISIBLE || redoButton.getVisibility() == View.VISIBLE) {
		    undoButton.setVisibility(View.GONE);
		    redoButton.setVisibility(View.GONE);
		}
		clearOverlays();
		/* Calculate the radius of the hexagon to show, so that it will be the 1/4 of the screen width. */
		/* Points */
		Point centerPoint = new Point();
		map.getProjection().toPixels(map.getMapCenter(), centerPoint);
		Point rightPoint = new Point(centerPoint.x + map.getWidth() / 30, centerPoint.y); // 30:
												  // random
												  // number
												  // :P
		/* GeoPoints */
		GeoPoint centerGeoPoint = map.getProjection().fromPixels(centerPoint.x, centerPoint.y);
		GeoPoint rightGeoPoint = map.getProjection().fromPixels(rightPoint.x, rightPoint.y);
		/* Locations */

		Location locCenter = new Location("");
		float radius = 0;
		locCenter.setLatitude(0d);
		locCenter.setLongitude((centerGeoPoint.getLongitudeE6()) / 1E6);
		Location locRight = new Location(locCenter);
		locRight.setLongitude((rightGeoPoint.getLongitudeE6()) / 1E6);
		/* regular polygon radius */
		radius = locCenter.distanceTo(locRight);
		polygonOverlay = new RegularPolygonOverlay(map, map.getMapCenter(), radius, 6);
		polygonOverlay.setDrawingPolygon(true);
		/* polygonOverlay.setCenter(locationServiceConnection.getService( ).getCurrentLocation()); polygonOverlay.setVertices(6); */
		map.getOverlays().add(polygonOverlay);
		map.invalidate();

		return false;
	    }
	});

	customShape.setOnTouchListener(new OnTouchListener() {
	    @Override
	    public boolean onTouch(View v, MotionEvent event) {
		if (polygonOverlay == null) {
		    Toast.makeText(getApplicationContext(), "Tap the screen to define polygon", Toast.LENGTH_LONG).show();
		}
		customShape.setChecked(true);
		customShape.setClickable(true);
		clearOverlays();
		polygonOverlay = new CustomPolygonOverlay(map);
		polygonOverlay.setDrawingPolygon(true);
		map.getOverlays().add(polygonOverlay);
		
		map.invalidate();
		return false;
	    }
	});

	searchInput.setOnTouchListener(new OnTouchListener() {
	    @Override
	    public boolean onTouch(View v, MotionEvent event) {
		updateSearchedAreas();
		searchInput.setHint("");
		searchInput.setCursorVisible(true);
		imm = (InputMethodManager) MapViewActivity.this.getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(searchInput, 0);
		return true;
	    }

	});

	// the search button of the edittext
	searchInput.setOnEditorActionListener(new OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		    if (searchInput.getText().toString().matches("[a-zA-Z]+") == false) {
			Toast.makeText(getApplicationContext(), "Write the name of the city", Toast.LENGTH_SHORT).show();
		    }
		    else {
			searchInput.setCursorVisible(false);
			queryTypeAlert.show();
		    }
		}
		return false;
	    }
	});

	// the search button
	feedbackButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent();
		String feedbackUser = appPrefs.getString("feedbackPreference", "Customer");
		if (feedbackUser.compareTo("Customer") == 0) {
		    intent.setClass(MapViewActivity.this, FeedbackActivity.class);
		}
		else {
		    intent.setClass(MapViewActivity.this, ScientFeedbackActivity.class);
		}
		startActivity(intent);
		finish();
	    }
	});

	/* favoritesButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { Intent intent = new Intent(); Bundle b = new Bundle();
	 * b.putInt("tabNumber", 1); intent.putExtras(b); intent.setClass(MapViewActivity.this, QueryListTabActivity.class); startActivity(intent); finish(); } });
	 * graphs.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { Intent intent = new Intent(); intent.setClass(MapViewActivity.this,
	 * HistogramActivity.class); startActivity(intent); } }); */

	settingButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent();
		intent.setClass(MapViewActivity.this, MainPreferenceActivity.class);
		startActivity(intent);
	    }
	});
	sensorButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent();
		intent.setClass(MapViewActivity.this, TabSensorsChartsActivity.class);
		startActivity(intent);
	    }
	});
	undoButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (polygonOverlay != null) polygonOverlay.undo();
		if (polygonOverlay.getGeoPoints().size() == 0) {
		    undoButton.setVisibility(View.GONE);
		    redoButton.setVisibility(View.GONE);
		    polygonOverlay.clearGeoPoints();
		}
		map.invalidate();
	    }
	});

	redoButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (polygonOverlay != null) polygonOverlay.redo();
		map.invalidate();
	    }
	});

	searchAreaButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		wifiManager.startScan();
		if (isConnective()) {
		    params.put(Fields.HIT_TYPE, com.google.analytics.tracking.android.HitTypes.EVENT);
		    params.put(Fields.EVENT_CATEGORY, "New Map Activity");
		    params.put(Fields.EVENT_LABEL, "Query");
		    params.put(Fields.CLIENT_ID, new UserID().getIdUser(getApplication()));
		    if (onAdvancedMode) {
			if (polygonOverlay != null && polygonOverlay.getGeoPoints().size() >= 3) {
			    queryTypeAlert.show();
			    params.put(Fields.EVENT_ACTION, "Query make on Advance Mode ");
			}
			else {
			    Toast.makeText(getApplicationContext(), "Polygon not defined", Toast.LENGTH_LONG).show();
			}
		    }
		    else {
			if (locationServiceConnection.isBound()) {
			    params.put(Fields.EVENT_ACTION, "Query make on Simple Mode ");
			    clearOverlays();

			    ArrayList<GeoPoint> geoPoints = getCurrentlyShownAreaGeoPoints();
			    polygonOverlay = new CustomPolygonOverlay(map);
			    polygonOverlay.setGeoPoints(geoPoints);
			    // uncomment if you want to see the search area in
			    // the map
			    // map.getOverlays().add(polygonOverlay);
			    // map.invalidate();

			    if (networkServiceConnection.isBound()) {
				queryTypeAlert.show();
				// isSearchAreaButton = true;
			    }
			}
		    }
		    umapTrackerEvent.send(params);
		    params.clear();
		}
		else {

		    internet.show();
		}
	    }
	});

	clearProvidersListButton.setOnTouchListener(new OnTouchListener() {
	    @Override
	    public boolean onTouch(View v, MotionEvent me) {
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
		    closeProvidersList();
		}
		return true;
	    }
	});

	map.setOnTouchListener(new OnTouchListener() {
	    @Override
	    public boolean onTouch(View v, MotionEvent me) {
		if (me.getAction() == MotionEvent.ACTION_DOWN && viewLayout_providers.getVisibility() == View.VISIBLE) {
		    closeProvidersList();
		}
		if (imm != null && imm.isActive()) {
		    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
		    searchInput.setCursorVisible(false);
		    searchInput.setText("");
		    searchInput.setHint("Query City");
		    searchInput.invalidate();
		    searchInput.refreshDrawableState();
		}
		if (polygonOverlay != null && polygonOverlay.getGeoPoints().size() == 0) {
		    redoButton.setVisibility(View.VISIBLE);
		    undoButton.setVisibility(View.VISIBLE);
		}
		return false;
	    }
	});

	checkAdvancedMode();
	Bundle b = getIntent().getExtras();
	/* When this Activity is started by a History/Favorites Query item, the Query object is serialized in stored in the Intent. The code section below tries to extract this
	 * query and show the result on screen. */
	try {
	    Query query = (Query) b.getSerializable("query");
	    Log.d(this.getClass().getName(), "Bundle with query found. " + query.toString());
	    QueryPresenter queryPresenter = new QueryPresenter(query);

	}
	catch (Exception e) {
	    Log.d(this.getClass().getName(), "Bundle with query not found.");
	}
	/* When this Activity is started by a Feedback the FeedbackTo object is serialized in stored in the Intent. */
	try {
	    Feedback feedback = (Feedback) b.getSerializable("feedback");
	    Log.d(this.getClass().getName(), "Bundle with feedback found. " + feedback.toString());
	    getHelper().getFeedbackDao().create(feedback);
	}
	catch (Exception e) {
	    Log.d(this.getClass().getName(), "Bundle with feedback not found.");
	}
	/* When this Activity is started by a Feedback the FeedbackTo object is serialized in stored in the Intent. */
	try {
	    SceintFeedback sfeedback = (SceintFeedback) b.getSerializable("sfeedback");
	    Log.d(this.getClass().getName(), "Bundle with sfeedback found. " + sfeedback.toString());
	    getHelper().getSceintFeedbackDao().create(sfeedback);
	}
	catch (Exception e) {
	    Log.d(this.getClass().getName(), "Bundle with sfeedback not found.");
	}
    }

    private void updateSearchedAreas() {
	try {
	    List<SearchedPolygon> Areas = getHelper().getSearchedPolygonDao().queryBuilder().query();
	    List<String> places = new ArrayList<String>();
	    for (int i = 0; i < Areas.size(); i++) {
		places.add(Areas.get(i).getPlace());

	    }
	    COUNTRIES = new String[places.size()];
	    places.toArray(COUNTRIES);
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.my_dropdown, COUNTRIES);
	    searchInput.setAdapter(adapter);
	    searchInput.setThreshold(1);
	}
	catch (SQLException e1) {
	    e1.printStackTrace();
	}
    }

    private void startGoogleTracking() {
	// Tracker t1 becomes the default tracker because it is initialized
	// first.
	Tracker t1 = GoogleAnalytics.getInstance(this).getTracker("UA-49826422-1");
	// Returns tracker t1.
	Tracker defaultTracker = GoogleAnalytics.getInstance(this).getDefaultTracker();
	// Hit sent to UA-XXXX-1.
	defaultTracker.send(MapBuilder.createAppView().set(Fields.SCREEN_NAME, "Home Screen").build());

    }

    private void checkUpdates() {
	Log.d(this.getClass().getName(), "UPDATE CHECK");
	appPrefsEditor = appPrefs.edit();
	if (appPrefs.getBoolean("update_umap", false)) {
	    String newversion = "";
	    try {
		if (networkServiceConnection.isBound()) {
		    newversion = networkServiceConnection.getService().getUpdateVersionCode();
		    installApp = new InstallApp();
		    installApp.setContext(getApplicationContext(), networkServiceConnection);

		    if (installApp.isnewVersion(newversion)) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("urbanNet new version");
			dialog.setMessage("New version of urbanNet is available. Update now ?");

			View checkBoxView = View.inflate(this, R.layout.checkbox, null);
			CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
			checkBox.setText("Don't show again.");
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			    @Override
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
				    appPrefsEditor.putBoolean("update_umap", false).commit();
				}
				else {
				    appPrefsEditor.putBoolean("update_umap", true).commit();
				}
			    }
			});

			dialog.setView(checkBoxView);
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog, int which) {
				installApp.execute("urbanNet.apk");
				dialog.dismiss();
			    }
			});
			dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			    }
			});
			dialog.show();
		    }

		}
	    }

	    catch (SocketException e) {
		e.printStackTrace();
	    }
	    catch (SSLException e) {
		e.printStackTrace();
	    }
	    catch (ClientProtocolException e) {
		e.printStackTrace();
	    }
	    catch (IOException e) {
		e.printStackTrace();
	    }
	    catch (ServerException e) {
		e.printStackTrace();
	    }
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

    public void checkAdvancedMode() {
	onAdvancedMode = mapPrefs.getBoolean("advancedMode", false);
	undoButton.setVisibility(View.GONE);
	redoButton.setVisibility(View.GONE);
	clearOverlays();
	if (onAdvancedMode) {
	    toolsRadioGroup.setVisibility(View.VISIBLE);
	    toolsRadioGroup.clearCheck();
	    checkedRadios = toolsRadioGroup.getCheckedRadioButtonId();
	}
	else toolsRadioGroup.setVisibility(View.GONE);
    }

    private void clearOverlays() {
	if (polygonOverlay != null) {
	    map.getOverlays().remove(polygonOverlay);
	    polygonOverlay = null;
	}
    }

    public static String getResponseString() {
	return responseString;
    }

    private void openWaterScoreList() {
	viewLayout_map.setVisibility(View.GONE);
	viewLayout_providers.setVisibility(View.VISIBLE);
    }

    public void closeProvidersList() {
	final int FADE_OUT_DURATION = 1000;
	Animation fadeOut = new AlphaAnimation(1, 0);
	fadeOut.setInterpolator(new DecelerateInterpolator());
	fadeOut.setDuration(FADE_OUT_DURATION);
	AnimationSet animation = new AnimationSet(false);
	animation.addAnimation(fadeOut);
	viewLayout_providers.setAnimation(animation);
	providersListAdapter.clearAdapter();
	viewLayout_providers.setVisibility(View.GONE);
	viewLayout_map.setVisibility(View.VISIBLE);
	if (onAdvancedMode) {
	    redoButton.setVisibility(View.GONE);
	    undoButton.setVisibility(View.GONE);
	}
	// map.getOverlays().remove(polygonOverlay);
    }

    @Override
    public void onLocationServiceConnected() {
    }

    @Override
    public void onLocationServiceDisonnected() {
    }

    @Override
    public void onNetworkServiceConnected() {
	checkUpdates();
    }

    @Override
    public void onNetworkServiceDisonnected() {
    }

    /**
     * Centers and zooms the map on the polygonOverlay
     */
    private void centerMapOnPolygonOverlay() {
	GeoPoint[] geoPointSpan = polygonOverlay.getSpan();
	Point[] pointSpan = { new Point(), new Point() };
	map.getProjection().toPixels(geoPointSpan[0], pointSpan[0]);
	map.getProjection().toPixels(geoPointSpan[1], pointSpan[1]);
	map.getController().zoomToSpan(Math.abs(geoPointSpan[0].getLatitudeE6() - geoPointSpan[1].getLatitudeE6()), Math.abs(geoPointSpan[0].getLongitudeE6() - geoPointSpan[1].getLongitudeE6()));
	map.getController().animateTo(new GeoPoint((geoPointSpan[0].getLatitudeE6() + geoPointSpan[1].getLatitudeE6()) / 2, (geoPointSpan[0].getLongitudeE6() + geoPointSpan[1].getLongitudeE6()) / 2));
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	unbindService(networkServiceConnection);
	unbindService(locationServiceConnection);
	internet.cancel();
	search_until.cancel();
	finish();
    }

    private boolean isConnective() {
	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	return (networkInfo != null);
    }

    private void searchAreaDisplay(ArrayList<GeoPoint> geopoints) {
	polygonOverlay = new CustomPolygonOverlay(map);
	polygonOverlay.setGeoPoints(geopoints);
	centerMapOnPolygonOverlay();
	if (onAdvancedMode) {
	    map.getOverlays().add(polygonOverlay);
	    polygonOverlay.setDrawingPolygon(true);
	}
	else clearOverlays();
    }

    /**
     * Returns an ArrayList with 4 GeoPoints denoting the 4 corners of the currently visible part of the map.
     * @return ArrayList with 4 GeoPoints: upLeft, upRight, downLeft, downRight
     */
    private ArrayList<GeoPoint> getCurrentlyShownAreaGeoPoints() {
	ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
	int halfWidth = (int) (map.getWidth() * 0.4);
	int halfHeight = (int) (map.getHeight() * 0.4);

	Point centerPoint = new Point();
	map.getProjection().toPixels(map.getMapCenter(), centerPoint);

	Point upLeft = new Point(centerPoint.x - halfWidth, centerPoint.y + halfHeight);
	Point upRight = new Point(centerPoint.x + halfWidth, centerPoint.y + halfHeight);
	Point DownLeft = new Point(centerPoint.x - halfWidth, centerPoint.y - halfHeight);
	Point DownRight = new Point(centerPoint.x + halfWidth, centerPoint.y - halfHeight);

	geoPoints.add(map.getProjection().fromPixels(DownLeft.x, DownLeft.y));
	geoPoints.add(map.getProjection().fromPixels(upLeft.x, upLeft.y));
	geoPoints.add(map.getProjection().fromPixels(upRight.x, upRight.y));
	geoPoints.add(map.getProjection().fromPixels(DownRight.x, DownRight.y));
	return geoPoints;
    }

    /**
     * Checks if the current (last known GPS- or network-provided) location falls in the currently shown region of map.
     * @return true, if the current location of at least one location provider falls in shown region of map, false otherwise.
     */
    private boolean isCurrLocInShownArea() {
	ArrayList<GeoPoint> box = getCurrentlyShownAreaGeoPoints();
	GeoPoint nwGp = box.get(1);
	GeoPoint seGp = box.get(3);

	double e = 0.0; // margin in decimal degrees

	Location nwLoc = new Location("");
	nwLoc.setLatitude(nwGp.getLatitudeE6() / 1000000.0);
	nwLoc.setLongitude(nwGp.getLongitudeE6() / 1000000.0);

	Location seLoc = new Location("");
	seLoc.setLatitude(seGp.getLatitudeE6() / 1000000.0);
	seLoc.setLongitude(seGp.getLongitudeE6() / 1000000.0);

	boolean gpsLongInRange = false, gpsLatInRange = false;
	Location gpsLoc = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
	if (gpsLoc != null) {
	    gpsLongInRange = nwLoc.getLongitude() <= gpsLoc.getLongitude() + e && gpsLoc.getLongitude() - e <= seLoc.getLongitude();
	    gpsLatInRange = nwLoc.getLatitude() <= gpsLoc.getLatitude() + e && gpsLoc.getLatitude() - e <= seLoc.getLatitude();
	}

	boolean netLongInRange = false, netLatInRange = false;
	Location netLoc = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	if (netLoc != null) {
	    netLongInRange = nwLoc.getLongitude() <= netLoc.getLongitude() + e && netLoc.getLongitude() - e <= seLoc.getLongitude();
	    netLatInRange = nwLoc.getLatitude() <= netLoc.getLatitude() + e && netLoc.getLatitude() - e <= seLoc.getLatitude();
	}

	return (gpsLongInRange && gpsLatInRange) || (netLongInRange && netLatInRange);
    }

    private boolean isAppInstalled(String packagename) {
	PackageManager pm = getApplicationContext().getPackageManager();
	try {
	    pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
	    return true;
	}
	catch (NameNotFoundException e) {
	    return false;
	}
    }

    @Override
    public void onDoubleTap() {
	if (!waterScoreList.isShown()) {
	    map.getController().zoomIn();
	}
    }

    @Override
    public void onTap() {
	int checked = toolsRadioGroup.getCheckedRadioButtonId();
	if (onAdvancedMode && checked != R.id.customShapeRadio && checked != R.id.hexagonRadio) {
	    Toast.makeText(getApplicationContext(), "Select a polygon on the left", Toast.LENGTH_SHORT).show();
	}
    }

    @Override
    public void onStop() {
	super.onStop();
	EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }

    /**
     * Performs a query to the urbanNet server asynchronously (in a new thread), to avoid freezing the GUI.
     */
    private class QueryAsyncTask extends AsyncTask<Integer, Void, Void> {
	private ProgressDialog progressDialog;
	private Query query;
	private int alertBuilderClickedItem;
	private long t1;
	// private ShowQueryBroadcastReceiver sqbr;
	private QueryPresenter queryPresenter;

	@Override
	protected void onPreExecute() {
	    // update the UI immediately after the task is executed
	    super.onPreExecute();
	    t1 = System.currentTimeMillis();
	    progressDialog = ProgressDialog.show(MapViewActivity.this, "", "Calculating scores");
	}

	@Override
	protected Void doInBackground(Integer... item) {
	    alertBuilderClickedItem = item[0];
	    /* Task 1: While waiting for the urbanNet server response, its nice to also get fresh scan results. */
	    queryPresenter = new QueryPresenter();
	    queryPresenter.autoPresent = false;
	    /* Task 2: Send the query to the urbanNet server. */
	    query = new Query(polygonOverlay.getGeoPoints(), alertBuilderClickedItem);
	    query.setUntilTimestamp(untilTimestamp);
	    Log.d(this.getClass().getName(), query.toString());
	    Response response = new Response();
	    // response.setValue("Zaros\t5\tBOTTLE\nNatural water\t4\tNATURAL\nTap Water\t3.5\tTAP");
	    // response.setType(0);
	    try {
		response = networkServiceConnection.getService().sendQuery(query);
		Log.d(this.getClass().getName(), response.toString());
		if (response.getValue().contains("none found") && onAdvancedMode) {
		    response.setValue("Search a larger region(polygon)");
		}
		else if (response.getValue().contains("none found") && onAdvancedMode == false) {
		    response.setValue("Search a larger region");
		}
		query.setResponseString(response);
		query.setServerDelay(response.getServerDelay());
		query.setNetworkDelay(response.getNetworkDelay());
		responseString = query.getResponseString();
	    }
	    catch (ServerException e2) {
		query.setResponseString(e2.getMessage());
		e2.printStackTrace();
	    }
	    catch (SSLProtocolException e) {
		query.setResponseString("SSL error. Client not authenticated.");
		ACRA.getErrorReporter().handleSilentException(e);
		e.printStackTrace();
	    }
	    catch (IOException e2) /* Network related Exception */{
		query.setResponseString("Network Error: " + e2.getMessage());
		e2.printStackTrace();
	    }
	    return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
	    super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Void result) {
	    progressDialog.cancel();
	    queryPresenter.setQuery(query);
	    queryPresenter.presentQuery();
	    // save the query in the local db
	    query.setAndroidDelay(System.currentTimeMillis() - t1 - query.getServerDelay() - query.getNetworkDelay());
	    try {
		getHelper().getQueryDao().create(query);
	    }
	    catch (SQLException e) {
		e.printStackTrace();
	    }
	}

	public int getQueryMode() {
	    return query.getQueryMode();
	}
    }

    /**
     * Used with the name of the city.
     * @author rapousis
     */
    private class LocationAsyncTask extends AsyncTask<String, Integer, ArrayList<GeoPoint>> {
	private ProgressDialog progressDialog;
	private Query query;
	private int alertBuilderClickedItem;
	private long t1;
	private QueryPresenter queryPresenter;
	private ArrayList<GeoPoint> geopoints = new ArrayList<GeoPoint>();

	@Override
	protected void onPreExecute() {
	    // update the UI immediately after the task is executed
	    super.onPreExecute();
	    t1 = System.currentTimeMillis();
	    progressDialog = ProgressDialog.show(MapViewActivity.this, "", "Calculating scores");
	}

	@Override
	protected ArrayList<GeoPoint> doInBackground(String... name) {
	    alertBuilderClickedItem = Integer.parseInt(name[1]);
	    queryPresenter = new QueryPresenter();
	    queryPresenter.autoPresent = false;
	    String city = name[0].substring(0, 1).toUpperCase() + name[0].substring(1).toLowerCase();
	    /* Task 2: Send the query to the urbanNet server. */
	    query = new Query(city, alertBuilderClickedItem);
	    Log.d(this.getClass().getName(), query.toString());
	    Response response;
	    try {
		response = networkServiceConnection.getService().sendQuery(query);
		Log.d(this.getClass().getName(), response.toString());
		if (response.getValue().contains("none found") && onAdvancedMode) {
		    response.setValue("Search a larger region(polygon)");
		}
		else if (response.getValue().contains("none found") && onAdvancedMode == false) {
		    response.setValue("Search a larger region");
		}
		query.setResponseString(response.getValue());
		query.setResponsePolygonString(response.getPolygonString());
		query.setServerDelay(response.getServerDelay());
		query.setNetworkDelay(response.getNetworkDelay());
		responseString = query.getResponseString();
	    }
	    catch (ServerException e2) {
		query.setResponseString(e2.getMessage());
		e2.printStackTrace();
	    }
	    catch (SSLProtocolException e) {
		query.setResponseString("SSL error. Client not authenticated.");
		ACRA.getErrorReporter().handleSilentException(e);
		e.printStackTrace();
	    }
	    catch (IOException e2) /* Network related Exception */{
		query.setResponseString("Network Error: " + e2.getMessage());
		e2.printStackTrace();
	    }
	    try {
		geopoints = query.getQueryGeoPointsName();
		List<SearchedPolygon> list = getHelper().getSearchedPolygonDao().queryBuilder().where().eq("place", city).query();
		if (list.isEmpty()) {
		    SearchedPolygon searchedPolygon = new SearchedPolygon(city, geopoints);
		    try {
			getHelper().getSearchedPolygonDao().create(searchedPolygon);
		    }
		    catch (SQLException e) {
			e.printStackTrace();
		    }
		}
		return geopoints;
	    }
	    catch (Exception e) {
		return null;
	    }
	}

	@Override
	protected void onPostExecute(ArrayList<GeoPoint> geopoints) {
	    progressDialog.cancel();
	    if (geopoints == null) {
		Toast.makeText(getBaseContext(), "Re-write the place, different spelling", Toast.LENGTH_LONG).show();
		return;
	    }
	    map.invalidate();
	    queryPresenter.setQuery(query);
	    queryPresenter.presentQuery();
	    query.setAndroidDelay(System.currentTimeMillis() - t1 - query.getServerDelay() - query.getNetworkDelay());
	    if (imm != null && imm.isActive()) {
		imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
	    }
	    try {
		getHelper().getQueryDao().create(query);
	    }
	    catch (SQLException e) {
		e.printStackTrace();
	    }
	}
    }

}
