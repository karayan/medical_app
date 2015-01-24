package gr.forth.ics.urbanNet.preferences;

//import gr.forth.ics.urbanNet.R;
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.network.NetworkServiceConnectable;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;
import gr.forth.ics.urbanNet.network.ServerException;
import gr.forth.ics.urbanNet.ui.InstallApp;
import gr.forth.ics.urbanNet.ui.IntroActivity;
import gr.forth.ics.urbanNet.ui.OrmLiteBaseMapActivity;
import gr.forth.ics.urbanNet.ui.QuestionnaireActivity;
import gr.forth.ics.urbanNet.utilities.UserID;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLException;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.medicalapp.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/**
 * Allows the user to set its preferences.
 */
public class MainPreferenceActivity extends
		OrmLiteBasePreferenceActivity<DatabaseHelper> implements
		NetworkServiceConnectable {
	SharedPreferences appPrefs;
	SharedPreferences.Editor appPrefsEditor;
	SharedPreferences trafficDemandprefs;
	private CheckBoxPreference monitoring, autoUpload, advancedMode;
	private ListPreference mapView, feedbackView;// ,queryHistorySize;
	private Preference aboutUs, upload, appVersion, update, didYouKnow,
			questionnaire;// , clearHistory;

	private NetworkServiceConnection networkServiceConnection;
	private NetworkInfo networkInfo;
	private String id_user;
	private InstallApp installApp;
	Context context;
	Tracker umapTrackerEvent;
	Map<String, String> params = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		setTitle("Settings");
		super.onCreate(savedInstanceState);
		umapTrackerEvent = GoogleAnalytics.getInstance(this).getTracker(
				"UA-49826422-1");
		EasyTracker.getInstance(this).activityStart(this);
		networkServiceConnection = new NetworkServiceConnection(this);
		bindService(new Intent(this, NetworkService.class),
				networkServiceConnection, Context.BIND_AUTO_CREATE);
		addPreferencesFromResource(R.xml.main_preferences);
		appPrefs = getSharedPreferences(UrbanNetApp.APP_PREFERENCE_FILENAME,
				MODE_PRIVATE);
		appPrefsEditor = appPrefs.edit();
		trafficDemandprefs = getSharedPreferences("MAP_PREFERENCE_FILENAME",
				MODE_PRIVATE);
		id_user = new UserID().getIdUser(getApplicationContext());
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		autoUpload = (CheckBoxPreference) findPreference("autoDataUploading");
		advancedMode = (CheckBoxPreference) findPreference("advancedMode");
		// queryHistorySize = (ListPreference)
		// findPreference("queryHistorySize");
		mapView = (ListPreference) findPreference("mapViewPreference");
		feedbackView = (ListPreference) findPreference("feedbackPreference");
		aboutUs = findPreference("aboutUs");
		didYouKnow = findPreference("didYouKnow");
		upload = findPreference("upload");
		update = findPreference("update");
		// clearHistory = (Preference) findPreference("clearHistory");
		questionnaire = (Preference) findPreference("questionnaire");
		appVersion = findPreference("appVersion");
		context = getApplicationContext();
		autoUpload
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						appPrefsEditor.putBoolean("autoDataUploading",
								((Boolean) newValue));
						appPrefsEditor.commit();
						params.put(
								Fields.HIT_TYPE,
								com.google.analytics.tracking.android.HitTypes.EVENT);
						params.put(Fields.EVENT_CATEGORY,
								"MainPreference Activity");
						params.put(Fields.EVENT_ACTION,
								"AutoUploading is enable: " + (newValue));
						params.put(Fields.EVENT_LABEL, "AutoUploading");
						umapTrackerEvent.send(params);
						params.clear();
						return true;
					}
				});

		advancedMode
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						// New_MapViewActivity.class)
						boolean advancedMode = (Boolean) newValue;
						// if(advancedMode)
						appPrefsEditor.putBoolean("advancedMode", advancedMode);
						appPrefsEditor.commit();
						params.put(
								Fields.HIT_TYPE,
								com.google.analytics.tracking.android.HitTypes.EVENT);
						params.put(Fields.EVENT_CATEGORY,
								"MainPreference Activity");
						params.put(Fields.EVENT_ACTION, "AdvancedMode is "
								+ newValue.toString());
						params.put(Fields.EVENT_LABEL, "AdvancedMode");
						umapTrackerEvent.send(params);
						params.clear();
						return true;
					}

				});

		/*
		 * queryHistorySize.setOnPreferenceChangeListener(new
		 * OnPreferenceChangeListener() {
		 * 
		 * @Override public boolean onPreferenceChange(Preference preference,
		 * Object newValue) { int intNewValue = Integer.parseInt(((String)
		 * newValue)); appPrefsEditor.putInt("queryHistorySize", intNewValue);
		 * appPrefsEditor.commit(); return true; } });
		 */

		mapView.setDefaultValue(appPrefs.getString("mapViewPreference",
				"Roadmap"));
		mapView.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				String value = (String) newValue;
				appPrefsEditor.putString("mapViewPreference", value);
				appPrefsEditor.commit();
				OrmLiteBaseMapActivity.setMapView(value.equals("Satellite"));
				finish();
				return true;
			}
		});

		feedbackView.setDefaultValue(appPrefs.getString("feedbackPreference",
				"Customer"));
		feedbackView
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						String value = (String) newValue;
						appPrefsEditor.putString("feedbackPreference", value);
						appPrefsEditor.commit();
						finish();
						return true;
					}
				});
		/**
		 * Upload manually the QoE scores. Also check for cycling billing
		 * expiration and upload if has expire
		 */
		upload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// upload data to the urbanNet server
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo == null) {
					Toast.makeText(context, "There is no Internet connection",
							Toast.LENGTH_LONG).show();
				} else {
					networkServiceConnection.getService();
					// QoE and complaints upload
					if (networkServiceConnection.isBound()
							&& NetworkService.com.isLoggedIn()) {
						if (UrbanNetApp.isMeasuringPerformance) {
							networkServiceConnection.getService()
									.runSendDataTest(UrbanNetApp.nSendDataRuns);
						} else {
							networkServiceConnection.getService()
									.uploadDataset(true);
						}
					} else {
						networkServiceConnection.getService();
						if (networkServiceConnection.isBound()
								&& NetworkService.com.isLoggedIn() == false) {
							try {
								networkServiceConnection.getService();
								NetworkService.com.loginClient();
								networkServiceConnection
										.getService()
										.uploadDataToServerWithReflectionAndThreads(
												false);
							} catch (SocketException e) {
								e.printStackTrace();
							} catch (SSLException e) {
								e.printStackTrace();
							} catch (ClientProtocolException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							} catch (ServerException e) {
								e.printStackTrace();
							}
						}
					}
				}

				return true;
			}

		});

		update.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo == null) {
					Toast.makeText(context, "There is no Internet connection",
							Toast.LENGTH_LONG).show();
				} else if (networkServiceConnection.isBound()) {
					try {
						String newversion = networkServiceConnection
								.getService().getUpdateVersionCode();
						InstallApp installApp = new InstallApp();
						installApp.setContext(getApplicationContext(),
								networkServiceConnection);
						if (installApp.isnewVersion("0")) {
							Toast.makeText(getApplicationContext(),
									"New version is downloading",
									Toast.LENGTH_LONG).show();
							installApp.execute("urbanNet.apk");
						} else {
							Toast.makeText(getApplicationContext(),
									"Your version is the latest",
									Toast.LENGTH_LONG).show();
						}
					} catch (SocketException e) {
						e.printStackTrace();
					} catch (SSLException e) {
						e.printStackTrace();
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ServerException e) {
						e.printStackTrace();
					}
				}
				return true;
			}
		});
		questionnaire
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent();
						intent.setClass(MainPreferenceActivity.this,
								QuestionnaireActivity.class);
						startActivity(intent);
						return true;
					}
				});
		aboutUs.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setClass(MainPreferenceActivity.this,
						AboutUsActivity.class);
				startActivity(intent);
				return true;
			}
		});
		didYouKnow
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent();
						intent.setClass(MainPreferenceActivity.this,
								IntroActivity.class);
						startActivity(intent);
						return true;
					}
				});

		/*
		 * clearHistory.setOnPreferenceClickListener(new
		 * OnPreferenceClickListener() {
		 * 
		 * @Override public boolean onPreferenceClick(Preference preference) {
		 * 
		 * new
		 * AlertDialog.Builder(MainPreferenceActivity.this).setTitle("Clear History"
		 * )
		 * .setMessage("All stored queries will be deleted.").setIcon(android.R.
		 * drawable.ic_dialog_alert).setPositiveButton(android.R.string.yes, new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int whichButton) { try {
		 * getHelper().getQueryDao
		 * ().delete(getHelper().getQueryDao().queryBuilder
		 * ().where().eq("is_favorite", false).query()); } catch (SQLException
		 * e) { e.printStackTrace(); } }
		 * }).setNegativeButton(android.R.string.no, null).show(); return true;
		 * } });
		 */
	}

	@Override
	protected void onDestroy() {
		unbindService(networkServiceConnection);
		super.onDestroy();
	}

	@Override
	public void onNetworkServiceConnected() {
	}

	@Override
	public void onNetworkServiceDisonnected() {
	}

	/**
	 * Adds the menu. Menu is the bottom menu which becomes visible by pressing
	 * the menu-button.
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
