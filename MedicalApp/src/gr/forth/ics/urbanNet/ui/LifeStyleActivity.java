package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.database.Question;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.location.LocationServiceConnection;
import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.network.NetworkServiceConnectable;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.lang.Object;

//import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.SQLException;
import android.graphics.Path.Direction;
import android.location.Location;
import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

public class LifeStyleActivity extends
		OrmLiteBaseMapActivity<DatabaseHelper> implements
		NetworkServiceConnectable {
	private List<String> questions;
	private List<Integer> Pass;
	private int question_number;
	private int show = 0;
	private TextView body;
//	private int question_number;
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private RadioButton radioButton3;
	private Button next;
	private Button back;
	private RadioGroup radioGroup;
	private RadioButton radioButton4;
	private RadioButton radioButton5;
	EditText otherText;
	private Question question;
	private long time2;
	private long time1;
	TextView quest_number;
	String str1;
	String str2;
	String str3;
	String strall;
	private boolean exist_goto = false;
	private boolean exist_space1 = false;
	private boolean exist_space2 = false;
	private boolean exist_space3 = false;
	private boolean exist_space4 = false;
	private boolean exist_space5 = false;
	private boolean exist1 = false;
	private boolean exist2 = false;
	private boolean exist3 = false;
	private boolean exist4 = false;
	private boolean exist5 = false;
	private int next_show_number;
	private int index = -1;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude
	// private GPSservice gps;

	private SimpleDateFormat formatter = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss Z"); // Wed, 4 Jul 2001 12:08:56 -0700
											// (timezone)
	private String dateFormatted;

	NetworkServiceConnection networkServiceConnection;
	LocationServiceConnection locationServiceConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.lifestyle_activity);
		this.setTitle("Questionnaire");
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		body = (TextView) findViewById(R.id.txtBody);
		next = (Button) findViewById(R.id.btnNext);
		back = (Button) findViewById(R.id.btnBack);
		back.setClickable(false);
		back.setVisibility(View.GONE);
		radioGroup = (RadioGroup) findViewById(R.id.radioAnswers);
		radioButton1 = (RadioButton) findViewById(R.id.radioA1);
		radioButton2 = (RadioButton) findViewById(R.id.radioA2);
		radioButton3 = (RadioButton) findViewById(R.id.radioA3);
		radioButton4 = (RadioButton) findViewById(R.id.radioA4);
		radioButton5 = (RadioButton) findViewById(R.id.radioA5);
		quest_number = (TextView) findViewById(R.id.quest_mumber);
		otherText = (EditText) findViewById(R.id.denXerw);

		quest_number.setText(Integer.toString(show));

		location = new Location("");
		location = LocationService.lastKnownLocation;

//		networkServiceConnection = new NetworkServiceConnection(this);
//		bindService(new Intent(this, NetworkService.class),
//				super.networkServiceConnection, Context.BIND_AUTO_CREATE);
//		
//		ComponentName myService = startService(new Intent(this, NetworkService.class));
//		bindService(new Intent(this, NetworkService.class), super.networkServiceConnection, BIND_AUTO_CREATE);
//
//		// Need the location and network service
//		Intent intent = new Intent(this, LocationService.class);
//		bindService(intent, super.locationServiceConnection,
//				Context.BIND_AUTO_CREATE);

		// gps = new GPSservice(this);

		// check if GPS enabled
		// if(gps.canGetLocation()){
		// latitude = gps.getLatitude();
		// longitude = gps.getLongitude();
		Toast.makeText(getApplicationContext(),
				 "GPS:Your Location is \n\nLat: " + location.getLatitude() + "\nLong: " + location.getLongitude(),
				 Toast.LENGTH_LONG).show();
		//
		// location.setLatitude(latitude);
		// location.setLongitude(longitude);
		// }
		//

		List<Question> list;
		try {
			list = getHelper().getQuestionDao().queryBuilder().query();
			// if (list.size() >= 10) {
			// new
			// AlertDialog.Builder(MedHistory.this).setTitle("Questionnaire u-map").setMessage("You have already complete the questionnaire once.\nDo you want to try again?").setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Yes",
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog, int id) {
			// }
			// }).setNegativeButton("No", new DialogInterface.OnClickListener()
			// {
			// public void onClick(DialogInterface dialog, int id) {
			// finish();
			// }
			// }).show();
			// }
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		loadQuestions();

		question_number = questions.size();
		showQuestion();
		str1 = Integer.toString(1);
		str2 = "/";
		str3 = Integer.toString(question_number);
		strall = str1 + str2 + str3;
		quest_number.setText(strall);
		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (radioGroup.getCheckedRadioButtonId() == R.id.radioA1) {
						question = new Question(show, 1, (time2 - time1),
								questions.get(show), radioButton1.getText()
										.toString() + otherText.getText());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA2) {
						question = new Question(show, 2, (time2 - time1),
								questions.get(show), radioButton2.getText()
										.toString() + otherText.getText().toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA3) {
						question = new Question(show, 3, (time2 - time1),
								questions.get(show), radioButton3.getText()
										.toString() + otherText.getText().toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA4) {
						question = new Question(show, 4, (time2 - time1),
								questions.get(show), radioButton4.getText()
										.toString() + otherText.getText().toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA5) {
						question = new Question(show, 5, (time2 - time1),
								questions.get(show), radioButton5.getText()
										.toString() + otherText.getText().toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (java.sql.SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				show++;

				if (show <= questions.size() - 1) {
					showQuestion();

					str1 = Integer.toString(show + 1);
					str2 = "/";
					str3 = Integer.toString(question_number);
					strall = str1 + str2 + str3;
					quest_number.setText(strall);

				} else if (show == questions.size()) {
					clearRadioGroup();
					next.setVisibility(View.VISIBLE);
					next.setClickable(true);
					next.setText("Finish");
					body.setText("\n\nThank you for your contribution!!");

					try {
						List<Question> tem = getHelper().getQuestionDao()
								.queryBuilder().query();
						Log.d(this.getClass().getName(), "Saved questions "
								+ tem.size());
						for (int i = 0; i < tem.size(); i++) {
							Log.d("Questions" + i, " " + tem.get(i));
						}

						Log.d("questions size ", tem.toString());
						
						// networkServiceConnection.getService().uploadDataToServerWithReflectionAndThreads(true);
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (java.sql.SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("====================== upload to server =================");
//					networkServiceConnection.getService().uploadDataset(true);
					System.out.println("======================  test after upload =================");

					finish();
				}
				if (show > 0) {
					back.setVisibility(View.VISIBLE);
				}
			}
		});
		// -----------------------------------------------------------------------------------------------------------------------------
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (radioGroup.getCheckedRadioButtonId() == R.id.radioA1) {
						question = new Question(show, 1, (time2 - time1),
								questions.get(show), radioButton1.getText()
										.toString());
						question.setLocation(location);
						// dateFormatted = formatter.format(date);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA2) {
						question = new Question(show, 2, (time2 - time1),
								questions.get(show), radioButton2.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA3) {
						question = new Question(show, 3, (time2 - time1),
								questions.get(show), radioButton3.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA4) {
						question = new Question(show, 4, (time2 - time1),
								questions.get(show), radioButton4.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA5) {
						question = new Question(show, 5, (time2 - time1),
								questions.get(show), radioButton5.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						// question.setClient_id(1);
						getHelper().getQuestionDao().create(question);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (java.sql.SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				str1 = Integer.toString(show);
				str2 = "/";
				str3 = Integer.toString(question_number);
				strall = str1 + str2 + str3;
				quest_number.setText(strall);

				// if (exist_goto == true && Pass.contains(show)==true){
				// praxe sto array list
				// index = Pass.indexOf(show);

				// show = Pass.get(index-1);

				/*
				 * else{
				 * 
				 * }
				 */
				show--;
				showQuestion();
				if (show == 0) {
					back.setVisibility(View.GONE);
				} else {
					back.setVisibility(View.VISIBLE);
				}
			}
		});

		// --------------------------------------------------------------------------------------------------------------------------------
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				time2 = System.currentTimeMillis();
				next.setClickable(true);
				next.setVisibility(View.VISIBLE);
				if (checkedId == R.id.radioA1 && exist1 == true) {
					// Pass.add(show);
					show = next_show_number - 1;
					// Pass.add(show+1);
				}
				if (checkedId == R.id.radioA2 && exist2 == true) {
					// Pass.add(show);
					show = next_show_number - 1;
					// Pass.add(show+1);
				}
				if (checkedId == R.id.radioA3 && exist3 == true) {
					// Pass.add(show);
					show = next_show_number - 1;
					// Pass.add(show+1);
				}
				if (checkedId == R.id.radioA4 && exist4 == true) {
					// Pass.add(show);
					show = next_show_number - 1;
					// Pass.add(show+1);
				}
				if (checkedId == R.id.radioA4 && exist4 == true) {
					// Pass.add(show);
					show = next_show_number - 1;
					// Pass.add(show+1);
				}
				if (checkedId == R.id.radioA5 && exist5 == true) {
					// Pass.add(show);
					show = next_show_number - 1;
					// Pass.add(show+1);
				}
				// ----------------------------------------------------------

				if (checkedId == R.id.radioA1 && exist_space1 == true) {
					otherText.setVisibility(View.VISIBLE);
				}
				if (checkedId == R.id.radioA2 && exist_space2 == true) {
					otherText.setVisibility(View.VISIBLE);
				}
				if (checkedId == R.id.radioA3 && exist_space3 == true) {
					otherText.setVisibility(View.VISIBLE);
				}
				if (checkedId == R.id.radioA4 && exist_space4 == true) {
					otherText.setVisibility(View.VISIBLE);
				}
				if (checkedId == R.id.radioA4 && exist_space5 == true) {
					otherText.setVisibility(View.VISIBLE);
				}
				if (checkedId == R.id.radioA5 && exist_space5 == true) {
					otherText.setVisibility(View.VISIBLE);
				}
				// ------------------------------------------------------------------------------------------
				if (checkedId == R.id.radioA1 && exist_space1 == false) {
					otherText.setVisibility(View.GONE);
				}
				if (checkedId == R.id.radioA2 && exist_space2 == false) {
					otherText.setVisibility(View.GONE);
				}
				if (checkedId == R.id.radioA3 && exist_space3 == false) {
					otherText.setVisibility(View.GONE);
				}
				if (checkedId == R.id.radioA4 && exist_space4 == false) {
					otherText.setVisibility(View.GONE);
				}
				if (checkedId == R.id.radioA4 && exist_space5 == false) {
					otherText.setVisibility(View.GONE);
				}
				if (checkedId == R.id.radioA5 && exist_space5 == false) {
					otherText.setVisibility(View.GONE);
				}
			}

		});

	}

	private void loadQuestions() {
		InputStream ins = getApplicationContext().getResources()
				.openRawResource(
						getApplicationContext().getResources().getIdentifier(
								"raw/questions5", "raw",
								getApplicationContext().getPackageName()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
		String line;
		questions = new ArrayList<String>();
		Pass = new ArrayList<Integer>();// edw
		try {
			while ((line = reader.readLine()) != null) {
				questions.add(line);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void setRadioButtonsText(int i, String answer) {
		String ans = answer;
		String ans2 = answer;
		if (ans2.contains("goto")) {
			exist_goto = true;
			String[] parts = ans2.split("goto");
			String show_number = parts[1];
			next_show_number = Integer.valueOf(parts[1]);
		}
		if (answer.contains("_space")) {
			ans = answer.replaceAll("_space", "");

		}
		if (i == 1) {
			if (answer.contains("space")) {
				// exist_space = true;
				otherText = (EditText) findViewById(R.id.denXerw);
				// otherText.setVisibility(View.VISIBLE);
				otherText.requestFocus();
				exist_space1 = true;
			}
			if (ans.contains("goto")) {// edw

				ans = ans.replaceAll("goto[0-9][0-9]|goto[0-9]", "");
				exist1 = true;
			}
			radioButton1.setText(ans);
			radioButton1.setVisibility(View.VISIBLE);

		}
		if (i == 2) {
			if (answer.contains("space")) {
				// exist_space = true;
				otherText = (EditText) findViewById(R.id.denXerw);
				// otherText.setVisibility(View.VISIBLE);
				otherText.requestFocus();
				exist_space2 = true;
			}
			if (ans.contains("goto")) {// edw
				ans = ans.replaceAll("goto[0-9][0-9]|goto[0-9]", "");
				exist2 = true;
			}
			radioButton2.setText(ans);
			radioButton2.setVisibility(View.VISIBLE);

		}
		if (i == 3) {
			if (answer.contains("space")) {
				// exist_space = true;
				otherText = (EditText) findViewById(R.id.denXerw);
				// otherText.setVisibility(View.VISIBLE);
				otherText.requestFocus();
				exist_space3 = true;
			}
			if (ans.contains("goto")) {// edw
				ans = ans.replaceAll("goto[0-9][0-9]|goto[0-9]", "");
				exist3 = true;
			}
			radioButton3.setText(ans);
			radioButton3.setVisibility(View.VISIBLE);

		}
		if (i == 4) {
			if (answer.contains("space")) {
				// exist_space = true;
				otherText = (EditText) findViewById(R.id.denXerw);
				// otherText.setVisibility(View.VISIBLE);
				otherText.requestFocus();
				exist_space4 = true;
			}
			if (ans.contains("goto")) {// edw
				ans = ans.replaceAll("goto[0-9][0-9]|goto[0-9]", "");
				exist4 = true;
			}
			radioButton4.setText(ans);
			radioButton4.setVisibility(View.VISIBLE);

		}
		if (i == 5) {
			if (answer.contains("space")) {
				// exist_space = true;
				otherText = (EditText) findViewById(R.id.denXerw);
				// otherText.setVisibility(View.VISIBLE);
				otherText.requestFocus();
				exist_space5 = true;
			}
			if (ans.contains("goto")) {// edw
				ans = ans.replaceAll("goto[0-9][0-9]|goto[0-9]", "");
				exist5 = true;
			}
			radioButton5.setText(ans);
			radioButton5.setVisibility(View.VISIBLE);

		}

	}

	private void showQuestion() {
		clearRadioGroup();
		String question = questions.get(show);
		String[] data = question.split("\t");
		body.setText(data[1]);

		for (int i = 2; i < question.split("\t").length; i++) {
			setRadioButtonsText(i - 1, data[i]);
		}
	}

	private void clearRadioGroup() {
		radioGroup.clearCheck();
		next.setClickable(false);
		next.setVisibility(View.GONE);

		findViewById(R.id.denXerw).setVisibility(View.INVISIBLE);

		radioButton1.setVisibility(View.GONE);
		radioButton2.setVisibility(View.GONE);
		radioButton3.setVisibility(View.GONE);
		radioButton4.setVisibility(View.GONE);
		radioButton5.setVisibility(View.GONE);
		otherText.setText("");
		otherText.setVisibility(View.GONE);
		exist1 = false;// edw
		exist2 = false;
		exist3 = false;
		exist4 = false;
		exist5 = false;
		exist_space1 = false;
		exist_space2 = false;
		exist_space3 = false;
		exist_space4 = false;
		exist_space5 = false;
	}

	@Override
	public void onLocationServiceConnected() {
		// TODO Auto-generated method stub
		System.out.println("location service connected!!!");
	}

	@Override
	public void onLocationServiceDisonnected() {
		// TODO Auto-generated method stub
		System.out.println("location service disconnected!!!");
	}

	@Override
	public void onNetworkServiceConnected() {
		// TODO Auto-generated method stub
		System.out.println("network service connected!!!");
	}

	@Override
	public void onNetworkServiceDisonnected() {
		// TODO Auto-generated method stub
		System.out.println("network service disconnected!!!");
	}
}
