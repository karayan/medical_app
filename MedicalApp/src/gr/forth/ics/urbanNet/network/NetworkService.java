package gr.forth.ics.urbanNet.network;


//import gr.forth.ics.urbanNet.R;
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.database.Feedback;
import gr.forth.ics.urbanNet.database.Query;
import gr.forth.ics.urbanNet.json.FeedbackJsonSerializer;
import gr.forth.ics.urbanNet.json.QueryJsonSerializer;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.location.LocationServiceConnectable;
import gr.forth.ics.urbanNet.location.LocationServiceConnection;
import gr.forth.ics.urbanNet.log.SDLog;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.ui.MapViewActivity;
import gr.forth.ics.urbanNet.ui.ServiceAlertDialog;
import gr.forth.ics.urbanNet.utilities.CircularByteBuffer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;

import org.acra.ACRA;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.medicalapp.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import com.j256.ormlite.dao.Dao;

public class NetworkService extends OrmLiteBaseService<DatabaseHelper> implements LocationServiceConnectable, NetworkServiceConnectable {
    /** ??? */
    static final long QUERY_LIMIT = 50;
    static final int UPLOAD_NOTIFICATION_ID = 1111;
    private static final String SERIALS = "monitor.ser";
    private SharedPreferences appPrefs;
    String PREF_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/urbanNet/sidroid_prefs.txt";
    private boolean loginExpetion = false;

    /**
     * Should contain Class elements from the gr.forth.ics.umap.db package. The objects of these Classes, which are stored in the local database, will be periodically uploaded to
     * the urbanNet server.
     */
    private final Class[] dbClassArray = {};
    /** Binder given to clients */
    private final IBinder mBinder = new NetworkBinder();

    private CircularByteBuffer ccb;
    public static CommunicationHandler com;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private boolean currentlyUploading;
    private volatile int uploadedFieldNewValue, myInterruptionCount;

    private LocationServiceConnection locationServiceConnection;
    private NetworkServiceConnection networkServiceConnection;

    public class NetworkBinder extends Binder {
	public NetworkService getService() {
	    // Return this instance of LocalService so clients can call public
	    // methods
	    return NetworkService.this;
	}
    }

    @Override
    public IBinder onBind(Intent intent) {
	return mBinder;
    }

    public void uploadDataset(final boolean autoUpload) {
	// Create a listener and give it to the UploadTask object, so that
	// it can asynchronously notify about the outcome of the uploading.
	UploadTask.ResultListener listener = new UploadTask.ResultListener() {
	    @Override
	    public void onSuccess() {
		Log.d(this.getClass().getName(), "UploadTask.ResultListener.onSuccess() was called.");
		notificationManager.cancel(UPLOAD_NOTIFICATION_ID);
		// TODO: release Wake and Wifi locks
	    }

	    @Override
	    public void onError(Exception e) {
		Log.w(this.getClass().getName(), "UploadTask.ResultListener.onError(...) was called.");
		e.printStackTrace();
		ACRA.getErrorReporter().handleSilentException(e);
		notificationBuilder.setContentText("Data uploading fail. Please try again.");
		notificationManager.notify(UPLOAD_NOTIFICATION_ID, notificationBuilder.build());
		// TODO: release Wake and Wifi locks
	    }
	};
	// Displaying a continuing activity indicator in the notification bar
	notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload, NotificationCompat.PRIORITY_LOW).setContentText("Uploading in progress").setOngoing(true).setProgress(0, 0, true);
	notificationManager.notify(UPLOAD_NOTIFICATION_ID, notificationBuilder.build());
	// Create and start the UploadTask object.
	try {
	    // TODO: acquire Wake and Wifi locks
	    UploadTask ut = UploadTask.createInstance(getHelper(), com, listener, autoUpload);
	    ut.start();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    ACRA.getErrorReporter().handleSilentException(e);
	    notificationBuilder.setContentText("Data uploading fail. Please try again.");
	    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notificationBuilder.build());
	    // TODO: release Wake and Wifi locks
	}
    }

    /**
     * Iterates through the elements of the {@link NetworkService#dbClassArray dbClassArray}, retrieves their objects from the local database, serializes them in JSON format and
     * sends them to the urbanNet server.
     */
    public void uploadDataToServerWithReflectionAndThreads(final boolean autoUpload) {
	uploadedFieldNewValue = 0;
	myInterruptionCount = 0;

	ccb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
	/* Displaying a continuing activity indicator in the notification bar */
	notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload/* R.drawable. notification_icon */, NotificationCompat.PRIORITY_LOW).setContentText("Uploading in progress").setOngoing(true).setProgress(0, 0, true);
	notificationManager.notify(UPLOAD_NOTIFICATION_ID, notificationBuilder.build());

	if (this.currentlyUploading) {
	    Log.w(this.getClass().getName(), "Attemption to execute uploadDataToServerWithReflection() multiple times in parallel. Retrurning.");
	    return;
	}
	this.currentlyUploading = true;

	/* This thread will update the "uploaded" field of the considered uploaded data, as soon as the other two threads have finished their execution. */
	final Thread dbUpdaterThread = new Thread(new Runnable() {
	    /**
	     * Will sleep until both threads below interrupt it and
	     */
	    @Override
	    public synchronized void run() {
		while (true) {
		    try {
			wait();
		    }
		    catch (InterruptedException e) {
			Log.d(this.getClass().getName(), "Interuption No" + myInterruptionCount);
			if (myInterruptionCount >= 2) {
			    break;
			}
		    }
		}
		updateUploadedField();
	    }

	    /**
	     * Selects all objects from all database tables which are CONSIDERED_UPLOADED, and updates their "uploaded" fields.
	     * @param value The new value.
	     */
	    public void updateUploadedField() {
		Log.i(this.getClass().getName(), System.currentTimeMillis() + ": dbUpdaterThread started updating the \"uploaded\" fields with value: " + uploadedFieldNewValue);
		try {
		    List<Object> queryResults;
		    Method getDao;
		    Method setUploaded;
		    for (Class dbClass : dbClassArray) {
			getDao = NetworkService.this.getHelper().getClass().getMethod("get" + dbClass.getSimpleName() + "Dao");
			Dao dao = (Dao) getDao.invoke(NetworkService.this.getHelper());
			long offset = 0;
			while (true) {

			    queryResults = dao.query(dao.queryBuilder().offset(offset).limit(QUERY_LIMIT).where().eq("uploaded", DatabaseHelper.CONSIDERED_UPLOADED).prepare());
			    offset += QUERY_LIMIT;
			    if (queryResults.isEmpty()) break;
			    for (Object obj : queryResults) {

				if (dbClass == Feedback.class) continue;
				setUploaded = (dbClass.cast(obj)).getClass().getMethod("setUploaded", int.class);
				setUploaded.invoke(dbClass.cast(obj), uploadedFieldNewValue); // consider the
				dao.update(obj);

			    }
			}
		    }

		}
		catch (SQLException e) {
		    e.printStackTrace();
		    ACRA.getErrorReporter().handleSilentException(e);
		}
		catch (NoSuchMethodException e) {
		    e.printStackTrace();
		    ACRA.getErrorReporter().handleSilentException(e);
		}
		catch (InvocationTargetException e) {
		    e.printStackTrace();
		    ACRA.getErrorReporter().handleSilentException(e);
		}
		catch (IllegalAccessException e) {
		    e.printStackTrace();
		    ACRA.getErrorReporter().handleSilentException(e);
		}
		catch (SecurityException e) {
		    e.printStackTrace();
		    ACRA.getErrorReporter().handleSilentException(e);
		}
		finally {
		    Log.i(this.getClass().getName(), System.currentTimeMillis() + ": dbUpdaterThread finished.");
		    NetworkService.this.currentlyUploading = false;
		    if (loginExpetion) {
			uploadDataToServerWithReflectionAndThreads(autoUpload);
			loginExpetion = false;
		    }
		}
	    }
	});
	dbUpdaterThread.start();

	/* This thread retrieves data from the sqlite database and puts it in the outputStream of ccb. We use a new thread, to avoid freezing the ui. */
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		Log.i(this.getClass().getName(), System.currentTimeMillis() + ": Data retrieving thread started.");
		try {
		    GsonBuilder gsonBuilder = new GsonBuilder();
		    gsonBuilder.registerTypeAdapter(Feedback.class, new FeedbackJsonSerializer());
		    gsonBuilder.registerTypeAdapter(Query.class, new QueryJsonSerializer());
		    Gson gson = gsonBuilder.create();
		    // Gson gson = new Gson(); // For debugging

		    // Important time
		    long startWritingToCcb = System.currentTimeMillis();
		    Log.d(this.getClass().getName(), "Start writing to ccb.");

		    // For debugging
		    /* File sdCard = Environment.getExternalStorageDirectory(); File dir = new File(sdCard.getAbsolutePath() + "/umap_performance"); dir.mkdirs(); File file = new
		     * File(dir, "[2012-10-25]data.json"); OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file)); */
		    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ccb.getOutputStream());
		    JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);
		    jsonWriter.setIndent("  "); // comment it out when in
		    // production
		    Log.d(this.getClass().getName(), "Size: " + ccb.getSize() + " - Available: " + ccb.getAvailable());
		    /* Start of Advanced Reflection Code */
		    jsonWriter.beginObject();
		    try {
			List<Object> queryResults = new ArrayList<Object>();
			Method getDao;
			Method setUploaded;

			for (Class dbClass : dbClassArray) {
			    getDao = NetworkService.this.getHelper().getClass().getMethod("get" + dbClass.getSimpleName() + "Dao");
			    Dao dao = (Dao) getDao.invoke(NetworkService.this.getHelper());
			    jsonWriter.name(dbClass.getSimpleName() + "s").beginArray();

			    long offset = 0;
			    while (true) {

				queryResults = dao.query(dao.queryBuilder().offset(offset).limit(QUERY_LIMIT).where().eq("uploaded", DatabaseHelper.NOT_UPLOADED).prepare());
				offset += QUERY_LIMIT;
				if (queryResults.isEmpty()) break;
				for (Object obj : queryResults) {

				    gson.toJson(obj, dbClass, jsonWriter); // serialize the object for uploading
				    if (dbClass == Feedback.class) continue;
				    setUploaded = (dbClass.cast(obj)).getClass().getMethod("setUploaded", int.class);
				    setUploaded.invoke(dbClass.cast(obj), DatabaseHelper.CONSIDERED_UPLOADED); // consider the
				    dao.update(obj);

				}
			    }
			    jsonWriter.endArray();
			}
			jsonWriter.name("autoUpload").value(autoUpload);
		    }
		    catch (SQLException e) {
			e.printStackTrace();
			ACRA.getErrorReporter().handleSilentException(e);
		    }
		    catch (NoSuchMethodException e) {
			e.printStackTrace();
			ACRA.getErrorReporter().handleSilentException(e);
		    }
		    catch (InvocationTargetException e) {
			e.printStackTrace();
			ACRA.getErrorReporter().handleSilentException(e);
		    }
		    catch (IllegalAccessException e) {
			e.printStackTrace();
			ACRA.getErrorReporter().handleSilentException(e);
		    }
		    catch (SecurityException e) {
			e.printStackTrace();
			ACRA.getErrorReporter().handleSilentException(e);
		    }
		    /* End of Advanced Reflection Code */
		    jsonWriter.endObject();
		    jsonWriter.close();
		    Log.d(this.getClass().getName(), "Size: " + ccb.getSize() + " - Available: " + ccb.getAvailable());
		    outputStreamWriter.close();

		    // Important time
		    long endWritingToCcb = System.currentTimeMillis();
		    long durWritingToCcb = endWritingToCcb - startWritingToCcb;
		    Log.d(this.getClass().getName(), "End writing to ccb. Duration: " + durWritingToCcb);

		}
		catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
		catch (JsonIOException e) {
		    e.printStackTrace();
		}
		finally {
		    Log.i(this.getClass().getName(), System.currentTimeMillis() + ": Data retrieving thread finished. Let's interrupt the dbUpdaterThread.");
		    myInterruptionCount++;
		    dbUpdaterThread.interrupt();
		}
	    }
	}).start();

	/* This thread reads data from the inputStream of ccb and sends it to the urbanNet backend server. We use a new thread, to avoid freezing the ui. */
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		Log.i(this.getClass().getName(), System.currentTimeMillis() + ": POSTing thread started.");
		InputStreamEntity inputStreamEntity;
		if (UrbanNetApp.isMeasuringPerformance) {
		    inputStreamEntity = new InputStreamEntity(getResources().openRawResource(R.raw.traces), -1);
		}
		else {
		    inputStreamEntity = new InputStreamEntity(ccb.getInputStream(), -1);
		}
		inputStreamEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

		Response response = new Response();
		notificationBuilder.setSmallIcon(android.R.drawable.stat_notify_error, NotificationCompat.PRIORITY_MIN).setOngoing(false).setProgress(0, 0, false);
		try {
		    try {
			long startPostingTime = System.currentTimeMillis(); // Test
			response = com.sendData(inputStreamEntity);
			long endPostingTime = System.currentTimeMillis(); // Test
			long serverAndNetworkdelay = endPostingTime - startPostingTime;
			if (UrbanNetApp.isMeasuringPerformance) SDLog.fn(this, /* com.getLastRequestDelay() + " " + */serverAndNetworkdelay + " " + (int) (response.getServerDelay() * 1000), SDLog.SEND_DATA_FILE);
			Log.d(this.getClass().getName(), "Data Successfully uploaded. Server says: " + response.getValue());
			uploadedFieldNewValue = DatabaseHelper.CONFIRMED_UPLOADED;
			notificationManager.cancel(UPLOAD_NOTIFICATION_ID);
		    }
		    catch (NotLoggedInException e1) {
			uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
			e1.printStackTrace();
			NetworkService.com.loginClient();
			loginExpetion = true;

		    }
		    catch (LoginException e1) {
			uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
			e1.printStackTrace();
			registerAlertDialog();
			notificationManager.cancel(UPLOAD_NOTIFICATION_ID);
		    }
		}
		catch (ServerException e) {
		    SDLog.fn(this, "Exception: " + "Error: urbanNet server says: " + e.getMessage(), SDLog.SEND_DATA_FILE);
		    if (e.getMessage().contains("sql error")) {
			uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED_BECAUSE_OF_SQL_ERROR;
		    }
		    else {
			uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
		    }
		    notificationBuilder.setContentText("Data uploading fail. Please try again.");
		    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notificationBuilder.build());
		    e.printStackTrace();
		    ACRA.getErrorReporter().handleSilentException(e);
		}
		catch (SSLProtocolException e) {
		    SDLog.fn(this, "Exception: " + e.getMessage(), SDLog.SEND_DATA_FILE);
		    uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
		    notificationBuilder.setContentText("Data uploading fail. Please try again.");
		    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notificationBuilder.build());
		    e.printStackTrace();
		}
		catch (ClientProtocolException e) {
		    SDLog.fn(this, "Exception: " + e.getMessage(), SDLog.SEND_DATA_FILE);
		    uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
		    notificationBuilder.setContentText("Data uploading fail. Please try again.");
		    e.printStackTrace();
		    ACRA.getErrorReporter().handleSilentException(e);
		}
		catch (IOException e) {
		    SDLog.fn(this, "Exception: " + e.getMessage(), SDLog.SEND_DATA_FILE);
		    uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
		    notificationBuilder.setContentText("Data uploading fail. Please try again.");
		    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notificationBuilder.build());
		    e.printStackTrace();
		}
		finally {
		    Log.i(this.getClass().getName(), System.currentTimeMillis() + ": POSTing thread finishd. Let's interrupt the dbUpdaterThread.");
		    myInterruptionCount++;
		    dbUpdaterThread.interrupt();
		}
	    }
	}).start();
    }

    /**
     * Sends a query to the urbanNet server
     * @param query A {@link Query} object.
     * @return The response-answer from the urbanNet server.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException if server responds with an error message
     */
    public Response sendQuery(Query query) throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	Response response = NetworkService.com.getScoreFeedback(query.getRequest());
	return response;
    }

    /**
     * Ask the server for the urbanNet version code, when is zero there is problem with connection
     * @return integer version code
     * @throws SocketException
     * @throws SSLException
     * @throws ClientProtocolException
     * @throws IOException
     * @throws ServerException
     */
    public String getUpdateVersionCode() throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	return NetworkService.com.getVersionCode();
    }

    /**
     * This method was used to upload the same JSON file (containing colleced data) multiple times, for the performance analysis published in the MobiArch'12 paper
     * @param nRuns How many times to upload the data.
     */
    public void runSendDataTest(final int nRuns) {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    if (UrbanNetApp.isMeasuringPerformance) Thread.sleep(0/* 10000 */);
		    Log.d(this.getClass().getName(), "Data test started.");
		    SDLog.writeDate(SDLog.SEND_DATA_FILE);
		    for (int i = 0; i < nRuns; i++) {
			Log.d(this.getClass().getName(), "Data test iteration No" + i + ".");
			uploadDataToServerWithReflectionAndThreads(true);
			Thread.sleep(2000);
		    }
		}
		catch (InterruptedException e) {
		    e.printStackTrace();
		}
		finally {
		    // Toast.makeText(getApplicationContext(),
		    // "Queries test completed", Toast.LENGTH_SHORT).show();
		    Log.d(this.getClass().getName(), "Data test comleted.");
		}
	    }
	}).start();
    }

    @Override
    public void onCreate() {
	super.onCreate();
	Log.i(this.getClass().getName(), "I am being created!");
	this.currentlyUploading = false;
	appPrefs = getSharedPreferences(UrbanNetApp.APP_PREFERENCE_FILENAME, MODE_PRIVATE);
	/* Initialize objects used for status bar notifications */
	notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	notificationBuilder = new NotificationCompat.Builder(this).setAutoCancel(true);
	// Creates an explicit intent for an Activity in your app
	Intent resultIntent = new Intent(this, MapViewActivity.class);
	// The stack builder object will contain an artificial back stack for
	// the started Activity.
	// This ensures that navigating backward from the Activity leads out of
	// your application to the Home screen.
	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
	// Adds the back stack for the Intent (but not the Intent itself)
	stackBuilder.addParentStack(MapViewActivity.class);
	// Adds the Intent that starts the Activity to the top of the stack
	stackBuilder.addNextIntent(resultIntent);
	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	notificationBuilder.setContentIntent(resultPendingIntent).setContentTitle("urbanNet: uploading data");
	/* Connect to the urbanNet server */
	
	final Handler handler = new Handler();
	//attention!!!!!!!!!!! run on new thread to avoid NetworkOnMainThreadException for new Android API versions
	Thread thread = new Thread() {
	    public void run() {
			try {
				com = new CommunicationHandler(getApplicationContext());
				com.connect();
			    com.loginClient();
			}
			catch (ServerException e) {
			    e.printStackTrace();
			}
			catch (SSLException ssl) {
			    TaskStackBuilder temstackBuilder = TaskStackBuilder.create(NetworkService.this);
			    Intent temresultIntent = new Intent(Settings.ACTION_DATE_SETTINGS);
			    // Adds the Intent that starts the Activity to the top of the stack
			    temstackBuilder.addNextIntent(temresultIntent);
			    PendingIntent temresultPendingIntent = temstackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			    notificationBuilder.setContentIntent(temresultPendingIntent).setContentTitle("Date & time settings");
			    notificationBuilder.setSmallIcon(android.R.drawable.stat_notify_error, NotificationCompat.PRIORITY_MIN).setOngoing(false).setProgress(0, 0, false).setContentText("Set the current time and date");
			    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notificationBuilder.build());
			}
			catch (IOException e) /* Network related Exception */{
			    e.printStackTrace();
			}
//			handler.postDelayed(this, 1000);
	    };
	};
//	handler.postDelayed(r, 1000);
	
	thread.start();
	
	/* BroadcastReceiver receiver2 = new BroadcastReceiver() {
	 * @Override public void onReceive(Context context, Intent intent) { ConnectivityManager connectivityManager = (ConnectivityManager)
	 * context.getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo(); NetworkInfo mobNetInfo =
	 * connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); if (activeNetInfo != null) { Toast.makeText(context, "Active Network Type : " +
	 * activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show(); } if (mobNetInfo != null) { Toast.makeText(context, "Mobile Network Type : " + mobNetInfo.getTypeName(),
	 * Toast.LENGTH_SHORT).show(); } } }; */

	/* //login to the system Response result = com.loginClient(); Log.d(this.getClass().getName(), "Login: '"+result.value+"'"); //testing the connection //result =
	 * com.conn.getRes("mutual/getuser.php"); if(result!=null) Log.d(this.getClass().getName(), "User: '"+result.value+"'"); */

	/* Log.d(this.getClass().getName(), "Network Service is being created"); try { uploadCellularTracesUsingFile(); } catch (ClientProtocolException e) { catch block
	 * e.printStackTrace(); } catch (SQLException e) { catch block e.printStackTrace(); } catch (IOException e) { catch block e.printStackTrace(); } */

	locationServiceConnection = new LocationServiceConnection(this);
	bindService(new Intent(this, LocationService.class), locationServiceConnection, Context.BIND_AUTO_CREATE);
	networkServiceConnection = new NetworkServiceConnection(this);
	bindService(new Intent(this, NetworkService.class), networkServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
	if (locationServiceConnection.isBound()) {
	    unbindService(locationServiceConnection);
	}
	if (networkServiceConnection.isBound()) {
	    unbindService(networkServiceConnection);
	}
	Log.i(this.getClass().getName(), "I am being destroyed...");
	super.onDestroy();
    }

    @Override
    public void onLocationServiceConnected() {

    }

    @Override
    public void onLocationServiceDisonnected() {
    }

    /**
     * The ID which is saved in server, for each client of urbanNet.
     * @return the unique id of the user.
     */
    public String getIdUser() {
	return com.getclientName();
    }

    @Override
    public void onNetworkServiceConnected() {

    }

    @Override
    public void onNetworkServiceDisonnected() {
    }

    public byte[] getApp(String app) throws SocketException, SSLException, IOException, ServerException {
	return NetworkService.com.getApp(app);
    }

    private void registerAlertDialog() {
	Bundle bun = new Bundle();
	bun.putString("alert_title", "Account");
	bun.putString("alert_message", "Somehting has gone wrong with your registration.\nRegister again.");
	bun.putString("alert_neutralButton", "Register");
	Intent i = new Intent(getApplicationContext(), ServiceAlertDialog.class);
	i.putExtras(bun);
	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	startActivity(i);
    }

}
