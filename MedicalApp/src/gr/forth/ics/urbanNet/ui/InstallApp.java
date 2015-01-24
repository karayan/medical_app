package gr.forth.ics.urbanNet.ui;

import gr.forth.ics.urbanNet.network.NetworkServiceConnection;
import gr.forth.ics.urbanNet.preferences.MainPreferenceActivity;

import java.io.File;
import java.io.FileOutputStream;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * The UpdateApp class is responsible for checking the server for new version and update it when is newer. The checking of the server is on CommunicationHandler.
 * @author Nikos Rapousis
 */
public class InstallApp extends AsyncTask<String, Void, Void> {

    private int UPDATE_NOTIFICATION_ID = 1;
    private int status = 0;
    private String error;
    private Context context;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private NetworkServiceConnection networkServiceConnection;
    private String app;
    private String PATH;

    public void setContext(Context context, NetworkServiceConnection networkServiceConnection) {
	this.context = context;
	this.networkServiceConnection = networkServiceConnection;

    }

    @Override
    public void onPreExecute() {

    }

    @Override
    protected Void doInBackground(String... arg) {
	app = arg[0];
	if (isConnective()) {
	    try {
		byte[] responseApp = this.networkServiceConnection.getService().getApp(app);
		mBuilder = new NotificationCompat.Builder(context);
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationON();
		PATH = Environment.getExternalStorageDirectory().getPath() + "/TempUrbanNet/";
		File file = new File(PATH);
		file.mkdir();
		File outputFile = new File(file, app);
		if (outputFile.exists()) {
		    outputFile.delete();
		}
		FileOutputStream fos = new FileOutputStream(outputFile);
		fos.write(responseApp, 0, responseApp.length);
		/* InputStream is = c.getInputStream(); byte[] buffer = new byte[1024]; int len1 = 0; while ((len1 = is.read(buffer)) != -1) { fos.write(buffer, 0, len1); } */
		fos.close();
		// is.close();

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(PATH + app)), "application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

	    }
	    catch (Exception e) {
		status = 1;
		error = e.getLocalizedMessage();
		e.printStackTrace();
	    }
	    catch (OutOfMemoryError e) {
		status = 1;
		error = "Ram problem. Force to close some applications";
	    }
	}
	else {
	    status = 2;
	}
	return null;
    }

    @Override
    public void onPostExecute(Void unused) {
	if (status == 1) {
	    Toast.makeText(context, "Error on " + app + " updating\n" + error, Toast.LENGTH_LONG).show();
	    mBuilder.setSmallIcon(android.R.drawable.stat_notify_error).setContentTitle(app).setContentText("Installing of " + app.replace(".apk", "") + " failed\n" + error);
	    mNotificationManager.notify(UPDATE_NOTIFICATION_ID, mBuilder.build());
	}
	else if (status == 2) {
	    Toast.makeText(context, "No Internet access", Toast.LENGTH_SHORT).show();
	}
	else {
	    notificationOFF();
	    // clearFiles();
	}

    }

    /**
     * Delete the downloaded files
     */
    private void clearFiles() {
	File file = new File(PATH + app);
	file.delete();
    }

    /**
     * Notification display for downloading process
     * @param mBuilder
     * @param mNotificationManager
     */
    private void notificationON() {
	mBuilder.setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle(app).setContentText("Install the latest version of" + app.replace(".apk", ""));
	// Creates an explicit intent for an Activity in your app
	Intent resultIntent = new Intent(context, MapViewActivity.class);
	// The stack builder object will contain an artificial back stack for the
	// started Activity.
	TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
	// Adds the back stack for the Intent (but not the Intent itself)
	stackBuilder.addParentStack(MainPreferenceActivity.class);
	// Adds the Intent that starts the Activity to the top of the stack
	stackBuilder.addNextIntent(resultIntent);
	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	mBuilder.setContentIntent(resultPendingIntent);
	mNotificationManager.notify(UPDATE_NOTIFICATION_ID, mBuilder.build());
    }

    private void notificationOFF() {
	mNotificationManager.cancelAll();
    }

    /**
     * The version of application is a number, as smaller as older the application is.
     * @param context
     * @return The current umap version
     */
    public String getVersionName(Context context) {
	PackageManager pm = context.getPackageManager();
	try {
	    PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
	    return pi.versionName;
	}
	catch (NameNotFoundException ex) {
	    return null;
	}
    }

    /**
     * Check for internet access
     * @return true if the internet access is established
     */
    public boolean isConnective() {
	ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	return (networkInfo != null);
    }

    /**
     * Compare the installed version vs the server version
     * @param version2 is the version of server
     * @return true if the server version is newer
     */
    public boolean isnewVersion(String version2) {
	String version = getVersionName(this.context);
	Log.d(this.getClass().getName(), "server " + version2);
	try {
	    int version1 = Integer.valueOf(version.replace(".", ""));
	    int version3 = Integer.valueOf(version2.replace(".", ""));
	    Log.d(this.getClass().getName(), " Current " + version1 + " vs server " + version3);
	    return version1 < version3;
	}
	catch (NullPointerException e) {
	    return false;
	}
	catch (NumberFormatException e) {
	    return false;
	}
    }
}
