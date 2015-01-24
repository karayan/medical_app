package gr.forth.ics.urbanNet.boot;

import gr.forth.ics.urbanNet.qoe.QoEService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShutdownReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent arg1) {
		Log.d(this.getClass().getName(), "------------------Shutown Android ----------------");
		if (context.stopService(new Intent(context, QoEService.class))) {
			Log.d(this.getClass().getName(), "------------------QoEService stopped ----------------");
		}
	}
}
