package gr.forth.ics.urbanNet.network;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;

/**
 * Custom SSLSocketFactory class.
 */
public class UmapSSLSocketFactory extends SSLSocketFactory {

	SSLContext sslContext = SSLContext.getInstance("TLS");
	Context context;

	
	/**
	 * Constructor.
	 */
	public UmapSSLSocketFactory(Context context, KeyStore keystore, String keyStorePassword, KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
			UnrecoverableKeyException {
		
		super(keystore, keyStorePassword, truststore);
		this.context = context;

		// custom TrustManager,trusts all servers
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(truststore);

		Log.d("ServerConnection", "Loaded server certificates: " + truststore.size());

		// initialize key manager factory with the client certificate
		KeyManager keyManagers[] = null;
		KeyManagerFactory keyManagerFactory;
		if (keystore != null) {
			
			keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keystore, keyStorePassword.toCharArray());
			keyManagers = keyManagerFactory.getKeyManagers();
			
			Log.d("ServerConnection", "Loaded client certificates: " + keystore.size());
			
		}

		sslContext.init(keyManagers, trustManagerFactory.getTrustManagers(), null);

	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}

	/**
	 * Create new HttpClient with CustomSSLSocketFactory.
	 */
	public static DefaultHttpClient getNewHttpClient(Context context, KeyStore trustStore, KeyStore keyStore, String keyStorePassword, int port) {
		try {

			SSLSocketFactory socketFactory = new UmapSSLSocketFactory(context, keyStore, keyStorePassword, trustStore);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams.setUserAgent(params, "FORTH-ICS Mobile urbanNet Client app/1.0.0");

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("https", socketFactory, port));

			ClientConnectionManager conMan = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(conMan, params);

		} catch (Exception e) {
			return new DefaultHttpClient();
		}

	}

}