package gr.forth.ics.urbanNet.ui;

import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.network.ServerException;

import java.io.IOException;
import java.net.SocketException;

import javax.net.ssl.SSLException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;

public class ServiceAlertDialog extends Activity {
    Bundle bun;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	EasyTracker.getInstance(this).activityStart(this);
	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ServiceAlertDialog.this);
	try {
	    bun = getIntent().getExtras();
	    Log.d(this.getClass().getName(), bun.getString("alert_title") + "  " + bun.getString("alert_message") + "  " + bun.getString("alert_neutralButton"));
	    alertDialogBuilder.setTitle(bun.getString("alert_title"));
	    alertDialogBuilder.setMessage(bun.getString("alert_message"));
	    if (bun.getString("alert_neutralButton") != null) {
		alertDialogBuilder.setNeutralButton(bun.getString("alert_neutralButton"), new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			try {

			    NetworkService.com.registerClient();
			    NetworkService.com.loginClient();
			}
			catch (SocketException e) {
			    e.printStackTrace();
			}
			catch (SSLException e) {
			    e.printStackTrace();
			}
			catch (ClientProtocolException e) {
			    e.printStackTrace();
			}
			catch (IOException e) {
			    e.printStackTrace();
			}
			catch (ServerException e) {
			    e.printStackTrace();
			}
			dialog.dismiss();
			finish();
		    }
		});
	    }
	    alertDialogBuilder.setCancelable(true);
	    alertDialogBuilder.setOnCancelListener(new OnCancelListener() {

		@Override
		public void onCancel(DialogInterface arg0) {
		    arg0.dismiss();
		    finish();
		}
	    });
	    alertDialogBuilder.show();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    finish();
	}
    }

    @Override
    public void onStop() {
	super.onStop();
	EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }
}
