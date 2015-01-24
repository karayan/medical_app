package gr.forth.ics.urbanNet.network;

import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.security.CertificateManager;
import gr.forth.ics.urbanNet.utilities.Crypto;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ServerConnection {
    /** The url of the urbanNet server. */
    private static final String URL = "https://" + UrbanNetApp.U_MAP_SERVER_NAME + "/system/"; // "https://umap.ics.forth.gr/system/";
    /** The port that the urbanNet server listens to. */
    private static final int PORT = 443;
    // private static final String[] LOGIN_EXCEPTION_MESSAGES={"no CN match", "Wrong Device Hash", "Client has no certificate"};

    InputStream trustStoreStream, keyStoreStream;
    KeyStore trustStore, keyStore;
    /** The HTTP client used to communicate with the urbanNet server */
    DefaultHttpClient sClient;
    private String password;
    private CONN_TYPE type;
    private CertificateManager certMan;
    private boolean connected;

    public ServerConnection(CertificateManager certMan) {
	this.certMan = certMan;
	updateConnType();
	this.connected = false;
    }

    /**
     * If password is null, connect with single authentication
     */
    public void updateConnType() {
	try {
	    password = certMan.loadRandomPassword();
	    type = CONN_TYPE.mutual;
	    password = Crypto.getSHA256(password);
	}
	catch (IOException e) {
	    type = CONN_TYPE.single;
	}
    }

    public void setConnType(CONN_TYPE type) {
	this.type = type;
    }

    /**
     * Creates an HTTP client parameterized to communicate with the urbanNet server.
     * <ul>
     * <li>Loads the server keystore and assigns it to {@link ServerConnection#trustStore trustStore}</li>
     * <li>If connection type is "mutual", loads the client keystore and assigns it to {@link ServerConnection#keyStore keyStore}</li>
     * <li>Creates a new {@link DefaultHttpClient} and assigns it to {@link ServerConnection#sClient sClient}</li>
     * </ul>
     * @return true, if the connection was established.
     * @throws KeyStoreException
     * @throws GeneralSecurityException if something goes wrong with the creation of the KeyStores or loading of the Certificates
     * @throws IOException if exception occurs while loading the keyStores from the InputStreams
     */
    public void createHttpClient() throws KeyStoreException {
	Log.d(this.getClass().getName(), "Connecting: " + type);
	try {
	    this.trustStoreStream = this.certMan.getServerKeystore();
	    this.trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	    this.trustStore.load(this.trustStoreStream, "server".toCharArray());
	    this.keyStore = null;
	    if (type == CONN_TYPE.mutual) {
		try {
		    if (UrbanNetApp.SSL_AUTH_CLIENT) {
			// connect using mutual SSL authentication
			this.keyStoreStream = certMan.getClientKeystore();
			this.keyStore = KeyStore.getInstance("PKCS12");
			this.keyStore.load(this.keyStoreStream, password.toCharArray());
			this.sClient = UmapSSLSocketFactory.getNewHttpClient(certMan.getContext(), trustStore, keyStore, password, PORT);
		    }
		    else {
			// connect using single SSL authentication (bypassing mutual)
			this.sClient = UmapSSLSocketFactory.getNewHttpClient(certMan.getContext(), trustStore, null, null, PORT);
		    }
		}
		catch (Exception e) {
		    Log.e(this.getClass().getName(), "Attention! probably corrupted client certificate!");
		    // e.printStackTrace();
		    throw new KeyStoreException("clientKeyStoreError: " + e.getMessage(), e);
		}
	    }
	    else {
		this.sClient = UmapSSLSocketFactory.getNewHttpClient(certMan.getContext(), trustStore, null, null, PORT);
	    }
	}
	catch (Exception e) {
	    Log.e(this.getClass().getName(), "Attention! probably corrupted server certificate!");
	    // e.printStackTrace();
	    throw new KeyStoreException("serverKeyStoreError: " + e.getMessage(), e);
	}
	/* this.params = new BasicHttpParams(); HttpProtocolParams.setVersion(this.params, HttpVersion.HTTP_1_1); HttpProtocolParams.setContentCharset(this.params, "UTF-8");
	 * HttpProtocolParams.setUseExpectContinue(this.params, true); HttpProtocolParams.setUserAgent(this.params, "FORTH-ICS Mobile urbanNet Client app/1.0.0"); */
	/* this.connPerRoute = new ConnPerRouteBean(12); ConnManagerParams.setMaxConnectionsPerRoute(this.params, this.connPerRoute);
	 * ConnManagerParams.setMaxTotalConnections(this.params, 20); HttpConnectionParams.setStaleCheckingEnabled(this.params, false);
	 * HttpConnectionParams.setConnectionTimeout(this.params, 20 * 1000); HttpConnectionParams.setSoTimeout(this.params, 20 * 1000);
	 * HttpConnectionParams.setSocketBufferSize(this.params, 8192); */
	/* HttpClientParams.setRedirecting(this.params, false); this.schReg = new SchemeRegistry(); this.schReg.register(new Scheme("https", this.socketFactory, this.port));
	 * this.conMgr = new ThreadSafeClientConnManager(this.params, this.schReg); this.sClient = new DefaultHttpClient(this.conMgr, this.params); */
	// this.sClient = UmapSSLSocketFactory.getNewHttpClient(certMan.getContext(), trustStore, keyStore, password, PORT);
    }

    /**
     * Checks if an HTTPS connection with the urbanNet server is available.
     * <p>
     * (GETs the "single/test.php" to verify that the HTTPS connection is established.)
     * </p>
     * @return true if successful
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException
     */
    public boolean testConnection() throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	this.connected = true;
	Response result = this.getRes("single/test.php");
	if (result.hasError()) {
	    return (this.connected = false);
	}
	return this.connected;
    }

    /**
     * Sends a HTTP POST message to the urbanNet server and returns a raw byte array.
     * @param urlSuffix the specific file to handle the POST request.
     * @param req the request.
     * @return the byte array containing the response.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     */
    public byte[] post(String urlSuffix, Request req) throws SocketException, SSLException, ClientProtocolException, IOException {
	// Log.d(this.getClass().getName(), req.toString());
	HttpPost httpPost = new HttpPost(URL + urlSuffix);
	HttpResponse response;
	httpPost.setEntity(req.getJsonEntity());
	long time1 = System.currentTimeMillis();
	response = this.sClient.execute(httpPost);
	long time2 = System.currentTimeMillis();
	Log.d(this.getClass().getName(), "Post execution time: " + (time2 - time1));
	return EntityUtils.toByteArray(response.getEntity());
    }

    /**
     * Sends a HTTP POST message to the urbanNet server and returns a String.
     * @param urlSuffix the specific file to handle the POST request.
     * @param req the request.
     * @return the String response.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     */
    public String postStr(String urlSuffix, Request req) throws SocketException, SSLException, ClientProtocolException, IOException {
	// Log.d(this.getClass().getName(), req.toString());
	return new String(this.post(urlSuffix, req));
    }

    /**
     * Sends a HTTP POST message to the urbanNet server and returns a {@link Response} object.
     * @param urlSuffix the specific file to handle the POST request.
     * @param req the request.
     * @return the response.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException
     */
    public Response postRes(String urlSuffix, Request req) throws NotLoggedInException, LoginException, SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	// Log.d(this.getClass().getName(), req.toString());
	long t1 = System.currentTimeMillis();
	Response response = new Response();
	double serverAndNetworkDelay = 0;
	String str = "Sorry a problem happend";
	try {
	    byte[] tmp = this.post(urlSuffix, req);
	    serverAndNetworkDelay = System.currentTimeMillis() - t1;
	    str = new String(tmp);
	    response = new Gson().fromJson(str, Response.class);
	    Log.d(this.getClass().getName(), response.toString());
	}
	catch (JsonSyntaxException e) {
	    response.setError(1);
	    response.setValue(str);
	}
	catch (ConnectTimeoutException e) {
	    response.setError(1);
	    str = "server.problem";
	    response.setValue(str);
	}
	catch (HttpHostConnectException e) {
	    response.setError(1);
	    str = "server.problem";
	    response.setValue(str);
	}
	if (response.hasError() && response.getValue().contains("SQLSTATE")) {
	    response.setValue("server.problem");
	    throw new ServerException(response.getValue());
	}
	if (response.hasError() && response.getValue().contains("failed to execute the SQL statement")) {
	    response.setValue("cordinate.problem");
	    throw new ServerException(response.getValue());
	}
	if (response.hasError() && response.getValue().contains("Not logged in!"))
	    throw new NotLoggedInException(response.getValue());
	else if (response.hasError() && (response.getValue().contains("client not found") || response.getValue().contains("no CN match") || response.getValue().contains("Wrong Device Hash") || response.getValue().contains("Client has no certificate")))
	    throw new LoginException(response.getValue());
	else if (response.hasError() && !response.getValue().contains("none found")) throw new ServerException(response.getValue());
	double serverDelay = response.getServerDelay();
	response.setNetworkDelay(serverAndNetworkDelay - serverDelay);
	return response;
    }

    /**
     * Sends a HTTP POST message to the urbanNet server and returns a {@link Response} object.
     * <p>
     * Used mainly to POST InputStreamEntity objects, in order to post streaming data.
     * </p>
     * @param urlSuffix the specific file to handle the POST request.
     * @param entity the request in HttpEntity format.
     * @return the response.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException
     */
    public Response postEntityRes(String urlSuffix, HttpEntity entity) throws SocketException, SSLException, ClientProtocolException, IOException, ServerException, NotLoggedInException, LoginException {
	HttpPost httpPost = new HttpPost(URL + urlSuffix);
	HttpResponse httpResponse;
	httpPost.setEntity(entity);
	httpResponse = this.sClient.execute(httpPost);
	Response response = new Gson().fromJson(new String(EntityUtils.toByteArray(httpResponse.getEntity())), Response.class);
	if (response.hasError() && response.getValue().contains("Not logged in!"))
	    throw new NotLoggedInException(response.getValue());
	else if (response.hasError() && (response.getValue().contains("client not found") || response.getValue().contains("no CN match") || response.getValue().contains("Wrong Device Hash") || response.getValue().contains("Client has no certificate")))
	    throw new LoginException(response.getValue());
	else if (response.hasError()) throw new ServerException(response.getValue());
	return response;
    }

    /**
     * Sends a HTTP GET message to the urbanNet server and returns a raw byte array.
     * @param urlSuffix the specific file to get.
     * @return the byte array containing the response.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     */
    public byte[] get(String urlSuffix) throws SocketException, SSLException, ClientProtocolException, IOException {
	HttpGet httpGet = new HttpGet(URL + urlSuffix);
	HttpResponse response = null;
	response = this.sClient.execute(httpGet);
	return EntityUtils.toByteArray(response.getEntity());
    }

    /**
     * Sends a HTTP GET message to the urbanNet server and returns a String.
     * @param urlSuffix the specific file to get.
     * @return the String response.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     */
    public String getStr(String urlSuffix) throws SocketException, SSLException, ClientProtocolException, IOException {
	byte[] tmp = this.get(urlSuffix);
	return new String(tmp);
    }

    /**
     * Sends a HTTP GET message to the urbanNet server and returns a {@link Response} object.
     * @param urlSuffix the specific file to get.
     * @return the response.
     * @throws SocketException if host or port is unreachable
     * @throws SSLException if SSL related problem occurs
     * @throws ClientProtocolException if an HTTP protocol error occurs (e.g., non 2xx HTTP response)
     * @throws IOException if other type of IOException occurs
     * @throws ServerException
     */
    public Response getRes(String urlSuffix) throws SocketException, SSLException, ClientProtocolException, IOException, ServerException {
	byte[] tmp = this.get(urlSuffix);
	Response response = new Gson().fromJson(new String(tmp), Response.class);
	if (response.hasError() && response.getValue().contains("Not logged in!"))
	    throw new NotLoggedInException(response.getValue());
	else if (response.hasError() && (response.getValue().contains("client not found") || response.getValue().contains("no CN match") || response.getValue().contains("Wrong Device Hash") || response.getValue().contains("Client has no certificate")))
	    throw new LoginException(response.getValue());
	else if (response.hasError()) throw new ServerException(response.getValue());
	return response;
    }

    /**
     * @return the PHP session id of this urbanNet client.
     */
    public String getSessionId() {
	List<Cookie> cookies = this.sClient.getCookieStore().getCookies();
	if (!cookies.isEmpty()) {
	    Cookie c;
	    for (int i = 0; i < cookies.size(); i++) {
		c = cookies.get(i);
		if (c.getName().equals("PHPSESSID")) return c.getValue();
	    }
	}
	return null;
    }

    /**
     * Clears the cookies of the {@link ServerConnection#sClient}.
     */
    public void clearCookies() {
	this.sClient.getCookieStore().clear();
    }

}
