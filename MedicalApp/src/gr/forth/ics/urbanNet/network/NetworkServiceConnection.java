package gr.forth.ics.urbanNet.network;

import gr.forth.ics.urbanNet.network.NetworkService.NetworkBinder;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * @author katsarakis
 *
 */
public class NetworkServiceConnection implements ServiceConnection {
	private NetworkServiceConnectable parent;
	private boolean bound;
	private NetworkService networkService;
	
	public NetworkServiceConnection(NetworkServiceConnectable parent) {
		this.parent= parent;
		this.bound= false;
	}
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		// We've bound to NetworkService, cast the IBinder and get NetworkService instance			
		NetworkBinder binder = (NetworkBinder) service;
		networkService = binder.getService();
		bound = true;
		parent.onNetworkServiceConnected();
	}
	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		bound = false;
		parent.onNetworkServiceDisonnected();
	}
	public NetworkService getService() {
		return networkService;
	}
	public boolean isBound() {
		return bound;
	}
}
