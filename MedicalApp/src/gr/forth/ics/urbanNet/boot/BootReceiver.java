package gr.forth.ics.urbanNet.boot;

import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.qoe.QoEService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//start location service
		context.startService(new Intent(context, LocationService.class));
		//start QoE service
		context.startService(new Intent(context, QoEService.class));
	Log.d(this.getClass().getName(), "Booted: urbanNet services started");
	}
}
