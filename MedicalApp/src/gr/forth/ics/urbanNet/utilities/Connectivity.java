/**
 * 
 */
package gr.forth.ics.urbanNet.utilities;

import gr.forth.ics.urbanNet.network.CommunicationHandler;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * <h1>Deprecated. Delete this.</h1>
 * @author syntych
 */
public class Connectivity {	
	/**
	 * <h1>Deprecated. Delete this.</h1>
	 * <p>This method used to be called from the {link {@link CommunicationHandler#isOnline()} method.</p>
	 * @param context The context (usually NetworkService)
	 * @return if there is a network connection
	 */
	static public boolean isOnline(Context cont) {		
		ConnectivityManager cm = (ConnectivityManager)cont.getSystemService(Context.CONNECTIVITY_SERVICE);		
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }	    
	    return false;
	}	
}
