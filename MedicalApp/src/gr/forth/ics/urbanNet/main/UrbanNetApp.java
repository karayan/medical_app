package gr.forth.ics.urbanNet.main;

//import gr.forth.ics.urbanNet.MainActivity;
//import gr.forth.ics.urbanNet.R;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.log.SDLog;
import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.qoe.QoEService;
import gr.forth.ics.urbanNet.ui.IntroActivity;
import gr.forth.ics.urbanNet.ui.MainActivity;

import java.io.File;
import java.io.PrintStream;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import com.example.medicalapp.R;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.util.TypedValue;

@ReportsCrashes(formKey = "dFZuSjN4d3E0WVdiS09FY1F4T1JOOWc6MQ", formUri = "https://collector.tracepot.com/4523801e", mode = ReportingInteractionMode.DIALOG, resToastText = R.string.crash_toast_text_Dialog, // optional,
// displayed
// as
// soon
// as
// the
// crash
// occurs,
// before
// collecting
// data
// which
// can
// take
// a
// few
// seconds
resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, // optional.
																								// default
																								// is
																								// a
																								// warning
																								// sign
resDialogTitle = R.string.crash_dialog_title, // optional. default is your
												// application name
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when
																// defined, adds
																// a user text
																// field input
																// with this
																// text resource
																// as a label
resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast
													// message when the user
													// accepts to send a report.
)
public class UrbanNetApp extends Application {

	/**
	 * This class is used to store all required information to contact a
	 * urbanNet server.
	 */
	public static class Server {

		/**
		 * The DNS or IP address of this urbanNet server. This property is used
		 * for the construction of URLs for all communication with the server.
		 */
		private String hostname;

		/**
		 * This property stores the resource id (e.g., R.raw.server) of the BKS
		 * file containing this urbanNet server's Certificate Authority
		 * certificate. It is used by the CertificateManager class.
		 */
		private int bksRId;

		/** Constructor */
		public Server(String hostname, int resId) {
			this.hostname = hostname;
			this.bksRId = resId;
		}

		/** @return {@link #hostname} */
		public String getHostName() {
			return this.hostname;
		}

		/** @return {@link #bksRId} */
		public int getBksRId() {
			return this.bksRId;
		}
	}

	/**
	 * Array of available urbanNet servers.
	 */
	public static final Server[] SERVERS = { new Server("139.91.62.208",
			R.raw.ca) // 0, production srv, hosted at ITE VMs, running
						// urbanNet_server (v1)
	};
	/**
	 * Index for the {@link #SERVERS} array. It denotes which server to use.
	 */
	public static final int SRV_INX = 0;
	/**
	 * The name (DNS or IP address) of the server to use.
	 */
	public static final String U_MAP_SERVER_NAME = SERVERS[SRV_INX]
			.getHostName();
	/**
	 * When true, the mutual authentication feature of the SSL protocol will be
	 * used. When false, only the urbanNet server will be authenticated at the
	 * SSL layer. (The urbanNet client will still be authenticated in the
	 * application layer: PHP script requires password).
	 */
	public static final boolean SSL_AUTH_CLIENT = false;
	public static final int defaultColor = Color.rgb(51, 181, 229);
	public static final int defaultColor2 = Color.rgb(130, 208, 255);
	/**
	 * When true, instead of uploading the recently collected measurements, the
	 * data from the file "res/raw/traces.json" will be uploaded to the urbanNet
	 * server, and the server and total delay will be logged in the
	 * {@link UrbanNetApp#performanceFile performanceFile} file.
	 */
	public static final boolean isMeasuringPerformance = false;
	public static int nSendQueryRuns = 1;
	public static int nSendDataRuns = 1;
	public static final String APP_PREFERENCE_FILENAME = "appPrefs";

	private static File performanceFile;
	private static PrintStream outPerformance;
	private static Location lastKnownLocation = null;
	private SharedPreferences appPrefs;
	public static float small_size1;
	public static float small_size2;
	public static float medium_size1;
	public static float medium_size2;
	public static float large_size1;
	public static float large_size2;
	public static int server_port = 1500;
	public static String server_IP = "139.91.182.245";

	/**
	 * Sets the last known location of the device in the application context.
	 * 
	 * @param currLocation
	 *            The current location of the device.
	 */
	public void setLocation(Location currLocation) {

		lastKnownLocation = new Location(currLocation);

		Log.d(this.getClass().getName(), "set: " + lastKnownLocation.toString());

	}

	/**
	 * Returns the last known location of the device from the application
	 * context.
	 * 
	 * @return
	 */
	public Location getLocation() {

		Log.d(this.getClass().getName(), "get: " + lastKnownLocation.toString());

		return lastKnownLocation;

	}

	// just a test...
	public String test() {
		return "testing...";
	}

	@Override
	/**
	 * Called on startup
	 */
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();
		SDLog.reset(true);
		if (isMeasuringPerformance == true) {
			Log.e(this.getClass().getName(),
					"Attention! You forgot isMeasuringPerformance= true");
		} else {
			nSendQueryRuns = 1;
			nSendDataRuns = 1;
		}
		test();

		appPrefs = getSharedPreferences(APP_PREFERENCE_FILENAME, MODE_PRIVATE);
		SharedPreferences.Editor appPrefsEditor = appPrefs.edit();
		PackageInfo packageInfo;
		large_size1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				22, getResources().getDisplayMetrics());
		large_size2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				25, getResources().getDisplayMetrics());
		medium_size1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				15, getResources().getDisplayMetrics());
		medium_size2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				19, getResources().getDisplayMetrics());
		small_size1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7,
				getResources().getDisplayMetrics());
		small_size2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				11, getResources().getDisplayMetrics());

		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(),
					PackageManager.GET_META_DATA);
			if (appPrefs.getLong("lastRunVersionCode", 0) == packageInfo.versionCode) /*
																					 * first
																					 * run
																					 * the
																					 * app
																					 */{
				appPrefsEditor.putBoolean("monitoring", (true));
				appPrefsEditor.putBoolean("autoDataUploading", (true));
				appPrefsEditor.putBoolean("fingerprinting", (false));
				appPrefsEditor.putInt("queryHistorySize", 20);
				appPrefsEditor.putInt("rate", 0);
			}
			if (appPrefs.getLong("lastRunVersionCode", 0) < packageInfo.versionCode) /*
																					 * first
																					 * run
																					 * of
																					 * this
																					 * version
																					 * of
																					 * the
																					 * app
																					 */{
				// showDialog(DIALOG_CHANGELOG);
				appPrefsEditor.putLong("lastRunVersionCode",
						packageInfo.versionCode);
				appPrefsEditor.commit();
				Intent intent = new Intent(getApplicationContext(),
						gr.forth.ics.urbanNet.ui.MainActivity.class); // IntroActivity
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(this.getClass().getName(), "Error reading versionCode");
			e.printStackTrace();
		}

		startService(new Intent(this, NetworkService.class));
		if (appPrefs.getBoolean("monitoring", true)) {
			startService(new Intent(this, LocationService.class));
			startService(new Intent(this, QoEService.class));
		}
	}

}
