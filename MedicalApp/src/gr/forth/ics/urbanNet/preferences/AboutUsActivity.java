package gr.forth.ics.urbanNet.preferences;

//import gr.forth.ics.urbanNet.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.medicalapp.R;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * @author root
 */
public class AboutUsActivity extends Activity {
    TextView appVersionTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
		EasyTracker.getInstance(this).activityStart(this);
	setContentView(R.layout.about_us_view);
	setTitle("About urbanNet");
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	appVersionTextView = (TextView) findViewById(R.id.appVersionTextView);
	try {
	    PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
	    appVersionTextView.setText(pi.versionName);
	}
	catch (PackageManager.NameNotFoundException e) {
	    Log.e(this.getClass().getName(), "Version name not found in package", e);
	}
    }

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
}
