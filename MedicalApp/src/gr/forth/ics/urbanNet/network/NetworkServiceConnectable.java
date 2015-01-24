package gr.forth.ics.urbanNet.network;

/**
 * @author katsarakis
 *
 */
public interface NetworkServiceConnectable {
	/**
	 * This method is called when the connection with the NetworkService is established
	 */
	abstract void onNetworkServiceConnected();
	
	/**
	 * This method is called when the NetworkServiceConnectable disconnects from the NetworkService. 
	 */
	abstract void onNetworkServiceDisonnected();
}
