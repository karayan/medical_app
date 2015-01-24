package gr.forth.ics.urbanNet.location;


//import gr.forth.ics.urbanNet.R;
import gr.forth.ics.urbanNet.database.BatteryMeasurement;
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.network.NetworkServiceConnectable;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;

import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

import com.example.medicalapp.R;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
//import android.app.Service;
//import android.widget.Toast;

/**
 * A service monitoring and keeping the most current device location.
 * @author syntych
 */
public class LocationService extends OrmLiteBaseService<DatabaseHelper> implements NetworkServiceConnectable {

    /**
     * Describes the status of the battery of the device.
     * <p>
     * The first declared value corresponds to an empty battery. The values of the enum are declared in a sequence of increasing available energy.
     * </p>
     */
    public static enum BatteryStatus {
	LEFT_0_25, LEFT_25_50, LEFT_50_75, LEFT_75_100, USB_CHARGING, AC_CHARGING
    }

    /**
     * Describes the estimation of the device velocity.
     */
    public static enum VelocityEstimation {
	STATIONARY, PEDESTRIAN, VEHICLE
    }

    /**
     * Maximum velocity in m/s for each {@link VelocityEstimation} value.
     */
    public static final float[] MAX_VELOCITIES = { 0.6f/* 2.16km/h */, 2.7f/* 10km /h */, Float.MAX_VALUE };

    /**
     * Describes the rate the u-map client collects traces
     */
    public static enum MonitorRate {
	ZERO, LOW, MODERATE, HIGH, GREEDY
    }

    /**
     * Minimum time interval between location updates, for each {@link MonitorRate} value, in milliseconds
     */
    public static final long[] LOCATION_UPDATES_MIN_TIMES = { Long.MAX_VALUE /* never */, 600000/* 10 min */, 300000/* 5 min (60000 1 min) */, 20000/* 20 sec */, 10000 /* 10 sec */};
    /**
     * Minimum distance between location updates, for each {@link MonitorRate} value, in meters
     */
    public static final long[] LOCATION_UPDATES_MIN_DISTANCES = { Long.MAX_VALUE, 3, 3, 20, 5 };

    /** Minimum interval (in ms) between data uploads. */
    public static final int MIN_UPLOAD_INTERVAL = 1800000; /* 30 min */

    /**
     * <p>
     * Each time a new {@link Location} update arrives, a {@link TimerTask} object is scheduled to revert the velocityEstimation to STATIONARY, in case a predefined period of time
     * has elapsed and no Location update has arrived yet.
     * </p>
     * <p>
     * If a new Location arrives in time, the scheduled TimerTask object is canceled and a new one is scheduled.
     * </p>
     */
    private class RevertToStationaryTimerTask extends TimerTask {
	@Override
	public void run() {
	    if (Looper.myLooper() == null)/* Only one Looper may be created per Thread */{
		Looper.prepare(); // Needed to allow this thread use the LocationService public methods and fields.
	    }
	    velocityEstimation = VelocityEstimation.STATIONARY;
	    broadcastMonitorRateParamsUpdate();
	    updateMonitorRate();
	}
    }

    private BatteryStatus batteryStatus;
    private VelocityEstimation velocityEstimation;
    private MonitorRate monitorRate;
    private boolean inSession = false;
    private boolean isWifiConnected;
    private Location velocityEstimationStartLocation;
    public MonitorRate oldMonitorRate;
    public MonitorRate newMonitorRate;
    public float batteryPct;
    int charging = 0;

    private Timer timer;
    private RevertToStationaryTimerTask revertToStationaryTimerTask;

    private final IBinder locationServiceBinder = new LocationBinder();// Binder given to clients
    public static Location lastKnownLocation; // Last known location as estimated by the LocationService

    private LocationManager locationManager;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private GpsStatus gpsStatus;

    private boolean collectCellularTraces = false;
    private SignalStrength signalStrength;
    private long lastDataUploadTime;
    private SharedPreferences appPrefs;

    private NetworkServiceConnection networkServiceConnection;


    /**
     * @return the batteryStatus
     */
    public BatteryStatus getBatteryStatus() {
	return batteryStatus;
    }

    /**
     * @param batteryStatus the batteryStatus to set
     */
    public void setBatteryStatus(BatteryStatus batteryStatus) {
	this.batteryStatus = batteryStatus;
    }

    /**
     * @return the velocityEstimation
     */
    public VelocityEstimation getVelocityEstimation() {
	return velocityEstimation;
    }

    /**
     * @param velocityEstimation the velocityEstimation to set
     */
    public void setVelocityEstimation(VelocityEstimation velocityEstimation) {
	this.velocityEstimation = velocityEstimation;
    }


    /**
     * Class used for the client Binder. Because we know this service always runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocationBinder extends Binder {
	public LocationService getService() {
	    // Return this instance of LocalService so clients can call public methods
	    return LocationService.this;
	}
    }

    /* Listens for battery information updates. */
    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    oldMonitorRate = monitorRate;
	    charging = 0;
	    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
	    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
	    int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
	    boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
	    boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
	    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	    batteryPct = level / (float) scale;
	    // String strStatus=
	    // "isCharging= "+isCharging+"\nusbCharge= "+usbCharge+"\nacCharge= "+acCharge+"\nbatteryPct"+batteryPct;
	    // Log.d(this.getClass().getName(), strStatus);
	    // Toast.makeText(getApplicationContext(), strStatus,
	    // Toast.LENGTH_LONG).show();
	    BatteryStatus previousBatteryStatus = batteryStatus;
	    if (acCharge) {
		batteryStatus = BatteryStatus.AC_CHARGING;
		charging = 2;
	    }
	    else if (usbCharge) {
		batteryStatus = BatteryStatus.USB_CHARGING;
		charging = 1;
	    }
	    else if (batteryPct >= 0.75)
		batteryStatus = BatteryStatus.LEFT_75_100;
	    else if (batteryPct >= 0.50)
		batteryStatus = BatteryStatus.LEFT_50_75;
	    else if (batteryPct >= 0.25)
		batteryStatus = BatteryStatus.LEFT_25_50;
	    else batteryStatus = BatteryStatus.LEFT_0_25;
	    if (previousBatteryStatus != batteryStatus) {
		broadcastMonitorRateParamsUpdate();
		updateMonitorRate();
	    }
	    else {
//		addEventBatteryMeasurement();													!!!!!!!!!!!! katerina
	    }
	}
    };

    /**
     * Listens for broadcasts from the ConnectivityManager
     */
    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
	    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	    boolean isWiFi = (isConnected) ? activeNetwork.getType() == ConnectivityManager.TYPE_WIFI : false;
	    isWifiConnected = isConnected && isWiFi;
	    if (isWifiConnected) {
		// Wait 5000 ms and call decideIfTimeToUploadData();
		(new Timer()).schedule(new TimerTask() {
		    @Override
		    public void run() {
			decideIfTimeToUploadData();
		    }
		}, 10000);
	    }
	}
    };

    /**
     * Receives Broadcasts about changes in the WiFi connection of the device.
     * <p>
     * When {@code (connection is established) && (velocityEstimation is stationary)
     * && (session is null)}, upload data to the server.
     * </p>
     */
    /* private BroadcastReceiver wifiConnectionReceiver = new BroadcastReceiver() {
     * @Override public void onReceive(Context context, Intent intent) { final String action = intent.getAction(); if
     * (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) { if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) { settinngWifiConnected(); }
     * else { isWifiConnected = false; } } } }; */

    /**
     * Listens for GPS status reports.
     * <p>
     * When GPS is not available, it switches to the {@link FingerprintService}.
     */
    /* GpsStatus.Listener gpsStatusListener= new GpsStatus.Listener() { public void onGpsStatusChanged(int event) { if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) { if
     * (appPrefs.getBoolean("fingerprinting", false) && fingerprintService != null) return; gpsStatus = locationManager.getGpsStatus(gpsStatus); int nSatsSnrGt20 =0; // Number of
     * Satellites with SNR>20 dB for (GpsSatellite gpsSat : gpsStatus.getSatellites()) { if (gpsSat.getSnr() > 20) nSatsSnrGt20++; if (nSatsSnrGt20 == 3) break; } if
     * (nSatsSnrGt20<3) { // TODO: switch from gps to fingerprinting locationManager.removeUpdates(locationListener); locationManager.removeGpsStatusListener(gpsStatusListener);
     * locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_MIN_TIMES[LocationService.this.monitorRate.ordinal()],
     * LOCATION_UPDATES_MIN_DISTANCES [LocationService.this.monitorRate.ordinal()], locationListener); // request updates from the network provider
     * fingerprintService.requestLocationUpdates (LOCATION_UPDATES_MIN_TIMES[LocationService.this.monitorRate.ordinal()], LOCATION_UPDATES_MIN_DISTANCES
     * [LocationService.this.monitorRate.ordinal()], locationListener); // request updates from the FingerprintService } } } }; */

    /* Listens for location updates. */
    private LocationListener locationListener = new LocationListener() {
	private boolean hasChanged;

	/**
	 * When a new location arrives...
	 * @param location The new location
	 */
	public void onLocationChanged(Location location) {
	    // Log.d("Location Changed", location.getProvider() + " - " + location.getAccuracy() + ": " + location.getLatitude() + " " + location.getLongitude());
	    String logString = "onLocationChanged(...) called\nLocationProvider= " + location.getProvider();
	    if (velocityEstimationStartLocation == null) {
		velocityEstimationStartLocation = location;
		logString += "\nvelocityEstimationStartLocation was null.";
		Log.w(this.getClass().getName(), logString);
		// Toast.makeText(getApplicationContext(), logString, Toast.LENGTH_LONG).show();
		velocityEstimationStartLocation = location;
	    }/* else if (velocityEstimationStartLocation.getAccuracy() -location.getAccuracy() > 10f) the new location has much better accuracy { logString+=
	      * "\nvelocityEstimationStartLocation had bad accuracy and was updated. (difference= " + (velocityEstimationStartLocation.getAccuracy()-location.getAccuracy ())+" m)";
	      * Log.w(this.getClass().getName(), logString); Toast.makeText(getApplicationContext(), logString, Toast.LENGTH_LONG).show(); velocityEstimationStartLocation=
	      * location; } */
	    else {
		VelocityEstimation oldVelocityEstimation = velocityEstimation;
		long movingInterval = location.getTime() - velocityEstimationStartLocation.getTime(); // in milliseconds
		long minEstimationInterval = Math.max(LocationService.LOCATION_UPDATES_MIN_TIMES[monitorRate.ordinal()], 60000/* 1 min */); // in milliseconds
		long maxEstimationInterval = 2 * minEstimationInterval;
		if (movingInterval > maxEstimationInterval) /* if the velocityEstimationStartLocation is too old */{
		    logString += "\nvelocityEstimationStartLocation is too old. (movingInterval= " + movingInterval / 1000 + " s).";
		    Log.w(this.getClass().getName(), logString);
		    // Toast.makeText(getApplicationContext(), logString, Toast.LENGTH_LONG).show();
		    velocityEstimationStartLocation = location;
		}
		else if (movingInterval > minEstimationInterval) /* if the interval is long enough to avoid too frequent estimations */{
		    logString += "\nOK. movingInterval in range. (" + movingInterval / 1000 + " s)";
		    float distance = location.distanceTo(velocityEstimationStartLocation); // in meters
		    float velocity = distance / (movingInterval / 1000); // in m/s
		    logString += "\nvelocity= " + velocity + " m/s.";
		    /* Update velocityEstimation */
		    if (velocity < 0)
			Log.e(this.getClass().getName(), "velocity<0");
		    else if (velocity <= MAX_VELOCITIES[VelocityEstimation.STATIONARY.ordinal()])
			velocityEstimation = VelocityEstimation.STATIONARY;
		    else if (velocity <= MAX_VELOCITIES[VelocityEstimation.PEDESTRIAN.ordinal()])
			velocityEstimation = VelocityEstimation.PEDESTRIAN;
		    else velocityEstimation = VelocityEstimation.VEHICLE;
		    /* Update velocityEstimationStartLocation */
		    velocityEstimationStartLocation = location;
		    logString += "\nvelocityEstimation=" + velocityEstimation;
		    Log.i(this.getClass().getName(), logString);
		    // Toast.makeText(getApplicationContext(), logString, Toast.LENGTH_LONG).show();
		}
		else /* if the interval is too short */{
		    logString += "\nmovingInterval too short: " + movingInterval / 1000 + "s.";
		    Log.w(this.getClass().getName(), logString);
		    // Toast.makeText(getApplicationContext(), logString, Toast.LENGTH_LONG).show();
		}
		/* Update monitor rate according to new velocity estimation */
		if (oldVelocityEstimation != velocityEstimation) {
		    /* In case velocityEstimationStartLocation has been received from the GPS provider and the new location is received from the Network provider, a false high
		     * speed may be esteemed. We set "velocityEstimationStartLocation= null" to make sure that the next two locations will be received from the same provider. */
		    velocityEstimationStartLocation = null;
		    broadcastMonitorRateParamsUpdate();
		    updateMonitorRate();
		}
		/* Reset revertToStationaryTimerTask */
		if (velocityEstimation != VelocityEstimation.STATIONARY) {
		    if (revertToStationaryTimerTask != null) revertToStationaryTimerTask.cancel();
		    long estimationInervalTimeOut = 2 * Math.max(LocationService.LOCATION_UPDATES_MIN_TIMES[monitorRate.ordinal()], 60000/* 1 min */) + 1000; // in milliseconds
		    revertToStationaryTimerTask = new RevertToStationaryTimerTask();
		    timer.purge();
		    timer.schedule(revertToStationaryTimerTask, estimationInervalTimeOut);
		}
	    }
	    /* Inform developers about the calculations */
	    // Log.d(this.getClass().getName(), logString);
	    // Toast.makeText(getApplicationContext(), logString, Toast.LENGTH_LONG).show();
	    /* Syntych code */
	    hasChanged = estimateNewLocation(location); // estimate (filter) the new location
	    if (hasChanged) /* if the new location was stored */{
		sendNewLocationMessage(); // send broadcast
		// writeTraceToDb();
	    }
	    // update the location value in the application context
	    ((UrbanNetApp) getApplicationContext()).setLocation(lastKnownLocation);
	    // Log.d(this.getClass().getName(), "Got New Location");
		}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	    Log.d(this.getClass().getName(), "New status for " + provider + ": " + status);
	    /* When the device is leaving the FORTH-ICS premises, switch from the FingerprintService to GPS location provider. */
	    /* if (provider==FingerprintService.PROVIDER && status==LocationProvider.TEMPORARILY_UNAVAILABLE) { // TODO: switch from fingerprinting to gps if (fingerprintService !=
	     * null) fingerprintService.removeUpdates(locationListener); // unregister from FingerprintService locationManager.requestLocationUpdates(LocationManager .GPS_PROVIDER,
	     * LOCATION_UPDATES_MIN_TIMES[LocationService.this.monitorRate .ordinal()], LOCATION_UPDATES_MIN_DISTANCES[LocationService.this.monitorRate .ordinal()],
	     * locationListener); // request updates from the GPS provider locationManager.addGpsStatusListener(gpsStatusListener); } */
		}

	public void onProviderEnabled(String provider) {
	    Log.d(this.getClass().getName(), provider + " location provider enabled by the user");
		}

	public void onProviderDisabled(String provider) {
	    Log.d(this.getClass().getName(), provider + " location provider disabled by the user");
	    // TODO: switch from fingerprinting to gps
	    /* if (provider==FingerprintService.PROVIDER) { if (fingerprintService != null) fingerprintService.removeUpdates(locationListener); // unregister from
	     * FingerprintService locationManager.requestLocationUpdates(LocationManager .GPS_PROVIDER, LOCATION_UPDATES_MIN_TIMES[LocationService.this.monitorRate .ordinal()],
	     * LOCATION_UPDATES_MIN_DISTANCES[LocationService.this.monitorRate .ordinal()], locationListener); // request updates from the GPS provider
	     * locationManager.addGpsStatusListener(gpsStatusListener); } */

		}
    };

    /**
     * Gets a new location and decides if it will be ignored or stored in the service's field.
     * <p>
     * If the new location provided by the system has changed slightly from the previous one, this service will ignore it for performance reasons. We don't want to store many
     * traces in our geo-database in the case the user stays stationary or moves in a small area (e.g., in a room).
     * </p>
     * @param current The updated location provided by the system (e.g., GPS, Network)
     * @return whether the new location was kept or not
     */
    private boolean estimateNewLocation(Location current) {
	// if the app just started and there is no lastKnownLocation, keep it!
	if (lastKnownLocation == null) {
	    Log.d(this.getClass().getName(), "Case 0. " + current.getAccuracy() + ": " + current.getLatitude() + " " + current.getLongitude());
	    lastKnownLocation = current;
	    return true;
	}
	// if the new location is more accurate keep it
	if (current.getAccuracy() < lastKnownLocation.getAccuracy()) {
	    Log.d(this.getClass().getName(), "Case 1. " + current.getAccuracy() + " VS " + lastKnownLocation.getAccuracy() + ": " + current.getLatitude() + " " + current.getLongitude());
	    lastKnownLocation = current;
	    return true;
	}
	/* if the new location distance from the last estimate is greater than the sum of their accuracies keep it */
	if (lastKnownLocation.distanceTo(current) > (current.getAccuracy() + lastKnownLocation.getAccuracy())) {
	    Log.d(this.getClass().getName(), "Case 2. " + current.getAccuracy() + ": " + current.getLatitude() + " " + current.getLongitude());
	    lastKnownLocation = current;
	    return true;
	}
	return false;
    }

    /**
     * Returns the most recent location stored in this service.
     * @return the current location
     */
    public Location getCurrentLocation() {
	if (UrbanNetApp.isMeasuringPerformance) {
	    // lastKnownLocation @FORTH-Hellas
	    lastKnownLocation.setLatitude(35.327451);
	    lastKnownLocation.setLongitude(25.121305);
	    lastKnownLocation.setAccuracy(100);
	}
	return lastKnownLocation;
    }

    private void sendNewLocationMessage() {
	Log.d(this.getClass().getName(), "Broadcasting: " + getString(R.string.ACTION_NEW_LOCATION_ESTIMATED));
	Intent intent = new Intent(getString(R.string.ACTION_NEW_LOCATION_ESTIMATED));
	// You can also include some extra data.
	// intent.putExtra("message", "This is my message!");
	LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /* Create a trace in the database each time the Mobile network's signal strength changes. Commented out because it causes very high rate of data recoding. */
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
	public void onSignalStrengthsChanged(SignalStrength signalStrength_local) {
	    // Log.d(this.getClass().getName(), "onSignalStrengthsChanged(...) called");
	    signalStrength = signalStrength_local;
	    // writeTraceToDb();
	};
    };

    /**
     * Used by writeTraceToDb() to measure the interval between the request for new scanResults, and their arrival.
     */
    private long scanStartTime, scanEndTime;

    /**
     * Decides if it is a convenient moment to upload the collected data. This method should be called when one of the condition factors changes.
     */
    private void decideIfTimeToUploadData() {
	Log.d(this.getClass().getName(), "decideIfTimeToUploadData() called. Let's check the conditions");
	boolean condition = (isWifiConnected && !inSession && velocityEstimation.equals(VelocityEstimation.STATIONARY) && batteryStatus.compareTo(BatteryStatus.LEFT_25_50) >= 0 && System.currentTimeMillis() - lastDataUploadTime > MIN_UPLOAD_INTERVAL && appPrefs.getBoolean("autoDataUploading", true));
	if (condition) {
	    Log.d(this.getClass().getName(), "Condition fullfilled so auto data upload ");
	    lastDataUploadTime = System.currentTimeMillis();
	    if (this.networkServiceConnection.isBound()) {
		this.networkServiceConnection.getService().uploadDataset(true);
	    }
	    else {
		Log.e(this.getClass().getName(), "NetworkService not bound.");
	    }
	}
	else {
	    Log.d(this.getClass().getName(), "Conditions not fullfilled.");

	}
	}

    /**
     * Updates the rate the application collects traces.
     */
    public void updateMonitorRate() {
	/* Temporarily store the previous rate */
	oldMonitorRate = this.monitorRate;
	/* Decide which will be the new rate */
	if (this.inSession == true && this.batteryStatus.compareTo(BatteryStatus.LEFT_50_75) >= 0) /* inSession and with charged battery */
	    this.monitorRate = MonitorRate.GREEDY;
	else if (this.inSession == true && this.batteryStatus.compareTo(BatteryStatus.LEFT_25_50) >= 0) /* inSession and with some energy in battery */
	    this.monitorRate = MonitorRate.HIGH;
	else if (this.velocityEstimation == VelocityEstimation.VEHICLE && this.batteryStatus.compareTo(BatteryStatus.LEFT_75_100) >= 0) /* moving fast and with charged battery */
	    this.monitorRate = MonitorRate.GREEDY;
	else if (this.velocityEstimation == VelocityEstimation.PEDESTRIAN && this.batteryStatus.compareTo(BatteryStatus.LEFT_75_100) >= 0) /* moving with pedestrian speed and with
																	    * charged battery */
	    this.monitorRate = MonitorRate.HIGH;
	else if (this.velocityEstimation.compareTo(VelocityEstimation.PEDESTRIAN) >= 0 && this.batteryStatus.compareTo(BatteryStatus.LEFT_25_50) >= 0) /* moving and with some
																		        * energy in battery */
	    this.monitorRate = MonitorRate.HIGH;
	else if (this.velocityEstimation == VelocityEstimation.STATIONARY && this.batteryStatus.compareTo(BatteryStatus.LEFT_50_75) >= 0) /* stationary with much battery */
	    this.monitorRate = MonitorRate.MODERATE;
	else if (this.velocityEstimation == VelocityEstimation.STATIONARY && this.batteryStatus.compareTo(BatteryStatus.LEFT_25_50) >= 0) /* stationary with some battery */
	    this.monitorRate = MonitorRate.LOW;
	else if (this.batteryStatus == BatteryStatus.LEFT_0_25) /* Low battery */
	    this.monitorRate = MonitorRate.ZERO;
	else
	/* default */
	this.monitorRate = MonitorRate.MODERATE;
	/* If the rate has been changed, adjust the requestLocationUpdates parameters. */
	if (oldMonitorRate != this.monitorRate) {
	    Log.d(this.getClass().getName(), "monitorRate= " + this.monitorRate.name());
	    // Toast.makeText(getApplicationContext(), "monitorRate= "+this.monitorRate.name(), Toast.LENGTH_LONG).show();
	    /* Remove all updates. */
	    locationManager.removeUpdates(locationListener);

	    /* Register for updates with new parameters. */
	    if (this.monitorRate.ordinal() > MonitorRate.ZERO.ordinal()) {
		// request updates from the network provider
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_MIN_TIMES[this.monitorRate.ordinal()], LOCATION_UPDATES_MIN_DISTANCES[this.monitorRate.ordinal()], locationListener);
	    }
	    if (this.monitorRate.ordinal() >= MonitorRate.HIGH.ordinal()) {
		// request updates from the GPS provider
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATES_MIN_TIMES[this.monitorRate.ordinal()], LOCATION_UPDATES_MIN_DISTANCES[this.monitorRate.ordinal()], locationListener);
		/* Log.i(this.getClass().getName(), "fingerprinting= "+appPrefs.getBoolean("fingerprinting", false)+", fingerprintService= "+fingerprintService); if
		 * (appPrefs.getBoolean("fingerprinting", false) && fingerprintService != null) { fingerprintService.requestLocationUpdates
		 * (LOCATION_UPDATES_MIN_TIMES[this.monitorRate.ordinal()], LOCATION_UPDATES_MIN_DISTANCES[this.monitorRate.ordinal()], locationListener); // request updates from
		 * the FingerprintService } else { locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, LOCATION_UPDATES_MIN_TIMES[this.monitorRate.ordinal()],
		 * LOCATION_UPDATES_MIN_DISTANCES[this.monitorRate.ordinal()], locationListener); // request updates from the GPS provider } */
	    }

	    broadcastMonitorRateUpdate();
	    newMonitorRate = this.monitorRate;
	}
//	addEventBatteryMeasurement();				katerina!!!!!!!!!!!!
	}

    /**
     * Broadcast the new monitor rate.
     */
    private void broadcastMonitorRateUpdate() {
	Intent broadcast = new Intent(getString(R.string.ACTION_MONITOR_RATE_UPDATE));
	broadcast.putExtra("monitorRate", this.monitorRate);
	sendBroadcast(broadcast);
    }

    private void broadcastMonitorRateParamsUpdate() {
	Intent broadcast = new Intent(getString(R.string.ACTION_MONITOR_RATE_PARAMS_UPDATE));
	broadcast.putExtra("batteryStatus", this.batteryStatus);
	broadcast.putExtra("velocityEstimation", this.velocityEstimation);
	broadcast.putExtra("inSession", this.inSession);
	sendBroadcast(broadcast);
    }

    public MonitorRate getMonitorRate() {
	return this.monitorRate;
    }

    public String getMonitorRateString() {
	return "monitorRate= " + monitorRate.name() + "\nbatteryStatus= " + batteryStatus.name() + "\nvelocityEstimation= " + velocityEstimation.name() + "\ninSession= " + inSession;
    }

    @Override
    public IBinder onBind(Intent arg0) {
	return locationServiceBinder;
    }

    @Override
    /**
     * called when the service is created for the first time
     */
    public void onCreate() {
	super.onCreate();
	Log.i(this.getClass().getName(), "I am being created!");
	appPrefs = getSharedPreferences(UrbanNetApp.APP_PREFERENCE_FILENAME, MODE_PRIVATE);

	this.batteryStatus = BatteryStatus.LEFT_0_25;
	this.velocityEstimation = VelocityEstimation.STATIONARY;
	this.monitorRate = MonitorRate.ZERO;
	this.inSession = false;
	this.timer = new Timer();
	this.lastDataUploadTime = 0l;

	networkServiceConnection = new NetworkServiceConnection(this);
	bindService(new Intent(this, NetworkService.class), networkServiceConnection, Context.BIND_AUTO_CREATE);

	locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	connectivityManager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);

	this.isWifiConnected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

	this.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	// this.registerReceiver(this.wifiConnectionReceiver, new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));
	this.registerReceiver(this.connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

	updateMonitorRate();
    }

    /* private void settinngWifiConnected() { final Timer timer = new Timer(); TimerTask task = new TimerTask() {
     * @Override public void run() { ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); if ( conMgr.getActiveNetworkInfo() != null
     * && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected() ) { isWifiConnected = true; decideIfTimeToUploadData(); timer.cancel();
     * timer.purge(); } } }; timer.scheduleAtFixedRate(task, 10000, 15000); } */

    private void addEventBatteryMeasurement() {
	try {
	    if (newMonitorRate == null) return;
	    BatteryMeasurement batteryMeasurement = new BatteryMeasurement(new Date().getTime(), newMonitorRate.ordinal(), oldMonitorRate.ordinal(), velocityEstimation.ordinal(), inSession, batteryPct, charging);
	    /* if (newMonitorRate != null) { batteryMeasurement.setNewMonitore(newMonitorRate.ordinal()); } */
	    getHelper().getBatteryMeasurementDao().create(batteryMeasurement);
	    // Log.d(this.getClass().getName(), "BatteryMeasurement= "+batteryMeasurement.toString());
	}
	catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    @Override
    /**
     * called whenever the service is started or restarted
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
	return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
	super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
	Log.i(this.getClass().getName(), "I am being destroyed...");
	locationManager.removeUpdates(locationListener);
	unregisterReceiver(batteryInfoReceiver);
	// unregisterReceiver(wifiConnectionReceiver);
	unregisterReceiver(connectivityReceiver);
	unbindService(networkServiceConnection);
	// unbindService(fingerprintServiceConnection);
	super.onDestroy();
    }

    @Override
    public void onNetworkServiceConnected() {
    }

    @Override
    public void onNetworkServiceDisonnected() {
    }

}
