package gr.forth.ics.urbanNet.qoe;

import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.location.LocationServiceConnectable;
import gr.forth.ics.urbanNet.location.LocationServiceConnection;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.network.NetworkServiceConnectable;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;
import gr.forth.ics.urbanNet.utilities.UserID;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteBaseService;

public class QoEService extends OrmLiteBaseService<DatabaseHelper> implements LocationServiceConnectable, NetworkServiceConnectable {
    private ConnectivityManager connectivityManager;
    public String outgoingNum;
    private int count = 0;
    private SharedPreferences appPrefs;
    private long tem;
    private String id_user = null;
    private int minute = 60000;
    private int hour = minute * 60;
    private int m_interval = hour / 10;
    private Handler m_handler = new Handler();

    /**
     * Connection with LocationService
     */
    protected LocationServiceConnection locationServiceConnection = new LocationServiceConnection(this);

    /**
     * Connection with NetworkService
     */
    protected NetworkServiceConnection networkServiceConnection = new NetworkServiceConnection(this);

    /**
     * Runnable is a repeating thread that check the duration of plan
     */
    Runnable m_statusChecker = new Runnable() {

	@Override
	public void run() {

	    m_handler.postDelayed(m_statusChecker, m_interval);
	}
    };

    /**
     * Start the thread repeating
     */
    void startRepeatingTask() {
	m_statusChecker.run();
    }

    /**
     * Kill the thread of repeating
     */
    void stopRepeatingTask() {
	m_handler.removeCallbacks(m_statusChecker);
    }

    /**
     * called when the service is created for the first time
     */
    @Override
    public void onCreate() {
	super.onCreate();
	Log.i(this.getClass().getName(), "I am being created!");
	appPrefs = getSharedPreferences(UrbanNetApp.APP_PREFERENCE_FILENAME, MODE_PRIVATE);
	connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

	Intent intent = new Intent(this, LocationService.class);
	bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
	intent = new Intent(this, NetworkService.class);
	bindService(intent, networkServiceConnection, Context.BIND_AUTO_CREATE);
	registerReceiver(this.connectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	id_user = new UserID().getIdUser(getApplicationContext());
	startRepeatingTask();
    }

    @Override
    public void onDestroy() {
	Log.i(this.getClass().getName(), "I am being destroyed...");
	if (appPrefs.getBoolean("monitoring", true)) {

	}
	unregisterReceiver(connectionReceiver);

	// unregisterReceiver(gsmReceiver);
	if (locationServiceConnection.isBound()) {
	    unbindService(locationServiceConnection);
	}
	if (networkServiceConnection.isBound()) {
	    unbindService(networkServiceConnection);
	}
	super.onDestroy();
    }

    public void onPause() {
    }

    @Override
    public void onLocationServiceConnected() {

    }

    @Override
    public void onLocationServiceDisonnected() {
    }

    /**
     * Receives Broadcasts about changes in the WiFi or mobile connection of the device. This receiver is because some android mobile(our for example) initialize on zero the wifi
     * counter when switch off wifi.
     */
    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
		    NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		    String typeName = info.getTypeName();
		    boolean available = info.isAvailable();
		    if (info.isConnectedOrConnecting()) {
			Log.i(this.getClass().getName(), "Network Type: " + typeName + ", available: " + available);

			startRepeatingTask();
		    }
		    else {
			Log.i(this.getClass().getName(), "Network Type: " + typeName + ", available: " + available);

			stopRepeatingTask();
		    }
		}

	}
    };

    @Override
    public IBinder onBind(Intent arg0) {
	return null;
    }

    @Override
    public void onNetworkServiceConnected() {

    }

    @Override
    public void onNetworkServiceDisonnected() {
    }

}
