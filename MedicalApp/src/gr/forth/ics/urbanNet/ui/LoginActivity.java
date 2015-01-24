package gr.forth.ics.urbanNet.ui;

//import android.support.v7.app.ActionBarActivity;
//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
public class LoginActivity extends Activity {

	Button button;
	EditText mEdit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		addListenerOnButton();
	}

	public void addListenerOnButton() {

		final Context context = this;

		button = (Button) findViewById(R.id.buttonClick);
		mEdit = (EditText) findViewById(R.id.editText1);

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Log.v("EditText", mEdit.getText().toString());
				if ((mEdit.getText().toString().contains("@"))) {
					Intent intent = new Intent(context, SecondActivity.class);
					startActivity(intent);
				} else {
					Intent intentErr = new Intent(context, ErrActivity.class);
					startActivity(intentErr);
				}
			}
		});
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.login, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
