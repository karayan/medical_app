package gr.forth.ics.urbanNet.network;

import gr.forth.ics.urbanNet.security.CertificateManager;
import gr.forth.ics.urbanNet.utilities.Crypto;
import gr.forth.ics.urbanNet.utilities.UserID;

import java.io.IOException;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.util.Random;

import javax.net.ssl.SSLException;

import org.acra.ACRA;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * @author syntych
 */
public class CommunicationHandler {
    /** Max retries to find an available "username" for registration. */
    private static final int RETRIES = 10;

    private Context context;
    public ServerConnection conn;
    private CertificateManager certMan;
    // private String pass;
    /** HTTPS connection established */
    private boolean connected;
    /** PHP session established */
    private boolean loggedIn;
    private String client_name = "";
    private String client_password_hash = "";
    private String device_manufacturer = Build.MANUFACTURER;
    private String device_brand = Build.BRAND;
    private String device_model = Build.MODEL;
    private int device_screen_res_x;
    private int device_screen_res_y;

    /**
     * Creates a new CommunicationHandler, which will handle all communication with the urbanNet server.
     * <p>
     * After creating the object, call its {@link CommunicationHandler#connect() connect()} method
     * </p>
     * @param context usually the NetworkService
     */
    public CommunicationHandler(Context context) {
	this.context = context;
	this.loggedIn = false;
	this.certMan = new CertificateManager(context);
	// this.pass = certMan.loadRandomPassword();
	this.conn = new ServerConnection(certMan);
    }

    /**
     * Establishes an HTTPS connection with the urbanNet server
     * @throws GeneralSecurityException if something goes wrong with the creation of the KeyStores or loading of the Certificates
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException <ul>
     * <li>if exception occurs while loading the keyStores from the InputStreams</li>
     * <li>if other network-type exception occurs..</li>
     * </ul>
     * @throws ServerException if server responds with an error message
     */
    public void connect() throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	try {
	    this.conn.createHttpClient();
	}
	catch (Throwable t) {
	    t.printStackTrace();
	    this.conn.setConnType(CONN_TYPE.single);
	    try {
		this.conn.createHttpClient();
	    }
	    catch (Throwable t1) {
		t1.printStackTrace();
	    }
	}
	this.connected = this.conn.testConnection();
	Log.d(this.getClass().getName(), "Connected: " + this.connected);
    }

    /**
     * Generates a 16-Byte random string.
     * @return the generated String.
     */
    private String getRandomString() {
	Random rand = new Random();
	byte[] buf = new byte[16];
	rand.nextBytes(buf);
	return Crypto.bin2hex(buf);
    }

    /**
     * Deprecated. See {@link CertificateManager#userCertExists()}.
     */
    public Boolean userCertExists() {
	return this.certMan.userCertExists();
    }

    /**
     * Registers the urbanNet client (at PHP session level).
     * @return the server Response
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException if server responds with an error message
     */
    public void registerClient() throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	Response result = new Response();
	Request req = new Request();
	String pass = "";
	int i;
	/* Generate a random "username" and check if it is available. */
	for (i = 0; i < RETRIES; i++) {
	    pass = getRandomString();
	    client_name = "Samsung"; //new UserID().getIdUser(context);
	    Log.i(this.getClass().getName(), client_name + "\n" + client_name.length());
	    req.setValue("clientName", client_name);
	    result = this.conn.postRes("single/checkavailability.php", req);
	    if (!result.hasError()) {
		break;
	    }
	}
	Log.d(this.getClass().getName(), "Check availability message: " + result.getValue());
	if (i >= RETRIES) {
	    result.setType(1);
	    result.setValue("No available username found.");
	    return;
	}
	/* Register */
	DisplayMetrics displayMetrics = new DisplayMetrics();
	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the
	wm.getDefaultDisplay().getMetrics(displayMetrics);
	device_screen_res_x = displayMetrics.widthPixels;
	device_screen_res_y = displayMetrics.heightPixels;
	Log.d(this.getClass().getName(), "The screen resolutions " + device_screen_res_x + " vs " + device_screen_res_y);
	String country = "GR";
	client_password_hash = Crypto.getSHA256(pass);
	req.setValue("clientPasswordHash", client_password_hash);
	req.setValue("deviceModel", device_model);
	req.setValue("deviceBrand", device_brand);
	req.setValue("deviceManufacturer", device_manufacturer);
	req.setValue("deviceScreenResX", device_screen_res_x);
	req.setValue("deviceScreenResY", device_screen_res_y);
	req.setValue("country", country);
	try {
	    result = this.conn.postRes("single/register.php", req);
	    result = this.conn.postRes("single/signcert.php", req);
	    byte[] keystore = this.conn.get("single/certs/" + Crypto.getSHA256(this.conn.getSessionId()) + "/" + client_name + ".p12");
	    /* Save credentials */
	    try {
		certMan.saveRandomPassword(pass);
		certMan.saveClientKeystore(keystore);
	    }
	    catch (IOException e) /* Problem saving the credentials */{
		e.printStackTrace();
		this.certMan.deleteClientKeystoreAndRandomPassword();
		throw e;
	    }
	}
	catch (ServerException se) /* Server responded an error during registration */{
	    Toast.makeText(this.context, se.getMessage(), Toast.LENGTH_LONG).show();
	    ACRA.getErrorReporter().handleSilentException(se);
	    throw se;
	}
	/* Delete this PHP session (which uses single SSL) for security reasons. */
	req.clear();
	req.setValue("sessid", Crypto.getSHA256(this.conn.getSessionId()));
	result = this.conn.postRes("single/deletesess.php", req);
	if (result.hasError()) {
	    return;
	}
	this.conn.clearCookies();
	Log.d(this.getClass().getName(), "Register message: " + result.getValue());
	// return result;
    }

    /**
     * Checks if there is an network connection
     * @return true if there is.
     */
    public boolean isOnline() {
	ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo netInfo = cm.getActiveNetworkInfo();
	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	    return true;
	}
	return false;
    }

    /**
     * Checks if there is an HTTPS connection with the urbanNet server
     * @return true if there is
     */
    public boolean isConnected() {
	return this.connected;
    }

    /**
     * Checks if there is a PHP session established.
     * @return true if there is
     */
    public boolean isLoggedIn() {
	return this.loggedIn;
    }

    /**
     * Logs in, creating a PHP session with the urbanNet server.
     * @return true if successful.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException if server responds with an error message
     */
    public void loginClient() throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	try {
	    String pass;
	    try {
		pass = certMan.loadRandomPassword();
	    }
	    catch (IOException e) /* fail to load randomPassword */{
		e.printStackTrace();
		throw new LoginException(e);
	    }

	    client_name = "Samsung"; //new UserID().getIdUser(context);
	    client_password_hash = Crypto.getSHA256(pass);
	    Request req = new Request();
	    req.setValue("clientName", client_name);
	    req.setValue("clientPasswordHash", client_password_hash);
	    req.setValue("remember", "true");
	    try {
		this.conn.postRes("mutual/login.php", req);
		this.loggedIn = true;
		// return true;
	    }
	    catch (LoginException le) /* server responds with login error */{
		throw le;
	    }
	    catch (IOException ioe) /* network connection error */{
		throw ioe;
	    }
	}
	catch (LoginException e)/* fail to load randomPassword OR server responded with login error */{
	    e.printStackTrace();
	    ACRA.getErrorReporter().handleSilentException(e);
	    // try {
	    this.conn.setConnType(CONN_TYPE.single);
	    try {
		this.conn.createHttpClient();
		this.registerClient();
	    }
	    catch (KeyStoreException serverKeystoreThrowable) {
		(new KeyStoreException("Server keystore Exception!", serverKeystoreThrowable)).printStackTrace();
	    }
	    this.conn.updateConnType();
	    try {
		this.conn.createHttpClient();
		this.loginClient();
	    }
	    catch (KeyStoreException clientKeystoreThrowable) {
		(new KeyStoreException("Client keystore Exception immediately after registration.", clientKeystoreThrowable)).printStackTrace();
	    }
	}
    }

    /**
     * Logs out, terminating the PHP session.
     * @return the server response.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException if server responds with an error message
     */
    public Response logout() throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	this.loggedIn = false;
	return this.conn.getRes("mutual/logout.php");
    }

    /**
     * @param entity
     * @return
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException if server responds with an error message
     */
    public Response sendData(HttpEntity entity) throws SocketException, SSLException, ClientProtocolException, IOException, ServerException, NotLoggedInException, LoginException {
	return this.conn.postEntityRes("mutual/savetraces.php", entity);
    }

    /**
     * @param request
     * @return
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException if server responds with an error message
     */
    public Response sendData(Request request) throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	return this.conn.postRes("mutual/savetraces.php", request);
    }

    /**
     * @param req
     * @return
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException if server responds with an error message
     */
    public Response getScoreFeedback(Request req) throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	try {
	    return this.conn.postRes("mutual/polygonquery.php", req);
	}
	catch (NotLoggedInException e) {
	    e.printStackTrace();
	    this.loginClient();
	    return this.conn.postRes("mutual/polygonquery.php", req);
	}
    }

    /**
     * Get the version of the urbanNet which is on the server.
     * @return integer versioncode
     * @throws SocketException
     * @throws SSLException
     * @throws ClientProtocolException
     * @throws IOException
     * @throws ServerException
     */
    public String getVersionCode() throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	return this.conn.getRes("single/ApkInfo.php").getValue();
    }

    /**
     * Get unique device hashing for table identification on server.
     * @return the unique client_name
     */
    public String getclientName() {
	return this.client_name;
    }

    public byte[] getApp(String app) throws SocketException, SSLException, ClientProtocolException, IOException {
	return this.conn.get("single/" + app);
    }

}
