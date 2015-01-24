package gr.forth.ics.urbanNet.location;

/**
 * @author katsarakis
 *
 */
public interface LocationServiceConnectable {
	/**
	 * This method is called when the connection with the LocationService is established
	 */
	abstract void onLocationServiceConnected();
	
	/**
	 * This method is called when the LocationServiceConnectable disconnects from the LocationService. 
	 */
	abstract void onLocationServiceDisonnected();
}
