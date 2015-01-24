package gr.forth.ics.urbanNet.ui;

import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.database.Question;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.location.LocationServiceConnection;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;
import gr.forth.ics.urbanNet.utilities.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.SQLException;
import android.graphics.Path.Direction;
import android.location.Location;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.example.medicalapp.R;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

public class QuestionnaireActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private List<String> questions;
	private List<String> Pass;
	//private List<String> Answers;
	private Map<Integer, Integer> Answers;
	private int question_number;
	private int show = 0;
	
	
	private int back_show = 0;
	private TextView body;
	private int number;
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private RadioButton radioButton3;
	private Button next;
	private Button back;
	private Button save;
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
	private boolean exist_goto2 = false;
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
	private int index_ans;
	private String prev_index;
	
	Location location; // location
	double latitude; // latitude
	double longitude; // longitude
	private GPSservice gps;
	BufferedReader reader = null;
	private SimpleDateFormat formatter = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss Z");
	private String dateFormatted;
	Intent intentSave;
	OutputStreamWriter outputStreamWriter;
	File file;
	File fileMap;
	File fileforBar;
	
	String addr;
	String category;

	NetworkServiceConnection networkServiceConnection;
	LocationServiceConnection locationServiceConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setTitle("Questionnaire");

		this.setContentView(R.layout.questionnaire_view);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		category = getIntent().getStringExtra("category");
		System.out.println("category:::: " + category);
		this.setTitle(category + " Questionnaire");


		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		body = (TextView) findViewById(R.id.txtBody);
		next = (Button) findViewById(R.id.btnNext);
		back = (Button) findViewById(R.id.btnBack);
		save = (Button) findViewById(R.id.btnSave);
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

		quest_number.setText(Integer.toString(show));//sunolikos arithmos erwthsewn.
		
		//orismoi arxeion
		file = new File(Environment.getExternalStorageDirectory(), "Pass"+category+".txt");
		fileMap = new File(Environment.getExternalStorageDirectory(), "Answers"+category+".txt");
		//fileforBar = new File(Environment.getExternalStorageDirectory(), "Bar");
		

		location = new Location("");
		location = LocationService.lastKnownLocation;

		// networkServiceConnection = new NetworkServiceConnection(this);
		// bindService(new Intent(this, NetworkService.class),
		// super.networkServiceConnection, Context.BIND_AUTO_CREATE);
		//
		// ComponentName myService = startService(new Intent(this,
		// NetworkService.class));
		// bindService(new Intent(this, NetworkService.class),
		// super.networkServiceConnection, BIND_AUTO_CREATE);
		//
		// // Need the location and network service
		// Intent intent = new Intent(this, LocationService.class);
		// bindService(intent, super.locationServiceConnection,
		// Context.BIND_AUTO_CREATE);

		// gps = new GPSservice(this);

		// check if GPS enabled
		// if(gps.canGetLocation()){
		// latitude = gps.getLatitude();
		// longitude = gps.getLongitude();

		new Thread(new Runnable() {
			@Override
			public void run() {
				GoogleResponse res1 = null;
				String latlng = location.getLatitude() + "," +location.getLongitude();
				try {
					res1 = new AddressConverter()
							.convertFromLatLong(latlng);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (res1 != null) {
					if (res1.getStatus().equals("OK")) {
						for (Result result : res1.getResults()) {
							System.out.println("address is :"
									+ result.getFormatted_address());
//							addr += result.getFormatted_address();
						}
						addr = res1.getResults()[0].getFormatted_address();
						System.out.println("addr::: "+addr);
						runOnUiThread(new Runnable() {
					        public void run()
					        {
					            Toast.makeText(QuestionnaireActivity.this, "GPS Conversion:Your Location is " + addr, Toast.LENGTH_LONG).show();
					        }
					    });
					} else {
						System.out.println(res1.getStatus());
					}
				}
			}
		}).start();

		Toast.makeText(
				getApplicationContext(),
				"GPS:Your Location is \n\nLat: " + location.getLatitude()
						+ "\nLong: " + location.getLongitude(),
				Toast.LENGTH_LONG).show();

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
		if(check()==1){
			finish();
		}

		loadQuestions(category);

		question_number = questions.size();
		//------------arxikopoihsh arxeiou Bar me 0/30 gia na min exei skoupidia stin arxi to arxeio---------
		try {					
			fileforBar = new File(Environment.getExternalStorageDirectory(), "Bar"+category+".txt");
			if (fileforBar.exists()) {
				 fileforBar.delete();
					}
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(fileforBar.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(Integer.toString(0));
			bw.flush();
	        bw.write('\n');
	        bw.flush();
	        bw.write(Integer.toString(question_number));
	        bw.flush();
			bw.close();

 
			System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		//----------------------------------------------------------------------------------------
		//-----emfanish erwthsewn kai minimatos gia to save koumi----------------------------------
		showQuestion();
//		Toast toast = Toast.makeText(getApplicationContext(),"Use save button to continue later", Toast.LENGTH_LONG);
//		toast.setGravity(Gravity.CENTER, 0, 0);
//		toast.show();
		
		int value;
		RadioButton radio;
		//an exoume xana apantish erwthseis xreiazetai o parakatw elegxos gia na bgainei epilegmenh h 
		//prwth erwthsh pou emfanizetai kai visible to next
		if (Answers.containsKey(show)){
			value = Answers.get(show);
			if (value == 1){
				radio = (RadioButton) findViewById(R.id.radioA1);
				radio.setChecked(true);
				next.setVisibility(View.VISIBLE);
			}
			else if (value == 2){
				radio = (RadioButton) findViewById(R.id.radioA2);
				radio.setChecked(true);
				next.setVisibility(View.VISIBLE);
			}
			else if (value == 3){
				radio = (RadioButton) findViewById(R.id.radioA3);
				radio.setChecked(true);
				next.setVisibility(View.VISIBLE);
			}
			else if (value == 4){
				radio = (RadioButton) findViewById(R.id.radioA4);
				radio.setChecked(true);
				next.setVisibility(View.VISIBLE);
			}
			else if (value == 5){
				radio = (RadioButton) findViewById(R.id.radioA5);
				radio.setChecked(true);
				next.setVisibility(View.VISIBLE);
			}
		}
		//-------------------------------------------------------------------------------------------
		//---koumpi save, ftiaxnei 3 arxeia, Pass(lista me tous arithmous twn erwthsewn pou exoun apantithei)
		//---Answers(Map<K,V>) opou key arithmos erwthshs kai  V h apantish tou xrhsth
		save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (file.exists()) {
					file.delete();
					}
				try {					
					file = new File(Environment.getExternalStorageDirectory(), "Pass"+category+".txt");
					
					// if file doesnt exists, then create it
					if (!file.exists()) {
						file.createNewFile();
					}
					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					for (int k = 0; k < Pass.size(); k++)  {
			        	 bw.write(Pass.get(k));
			        	 bw.write('\n');
			         }
					bw.close();
					//----------------------store the Answers Map-----------------------------------------
					System.out.println("Done");
					} 
				catch (IOException e) {
					e.printStackTrace();
					}
				
				//----------------------store the Answers Map-----------------------------------------
				 try{
					 if (fileMap.exists()) {
							fileMap.delete();
							}
					 if (!fileMap.exists()) {
							fileMap.createNewFile();
						}
					 fileMap = new File(Environment.getExternalStorageDirectory(), "Answers"+category+".txt");
					 FileOutputStream fos=new FileOutputStream(fileMap);
					 ObjectOutputStream oos=new ObjectOutputStream(fos);
					 oos.writeObject(Answers);
					 oos.flush();
					 oos.close();
					 fos.close();
					 }
				 catch(Exception e){
					 
				 	}
				//-------------file for bars in menu-----------------------------------------------        
				 try {					
					fileforBar = new File(Environment.getExternalStorageDirectory(), "Bar"+category+".txt");
					if (fileforBar.exists()) {
						fileforBar.delete();
						}
					// if file doesnt exists, then create it
					if (!file.exists()) {
						file.createNewFile();
						}
					FileWriter fw = new FileWriter(fileforBar.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(Integer.toString(find_max_show()));
					bw.flush();
				    bw.write('\n');
				    bw.flush();
				    bw.write(Integer.toString(question_number));
				    bw.flush();
					bw.close();
					System.out.println("Done");
					} 
				 catch (IOException e) {
						e.printStackTrace();
					}   
                finish();
			}            
		});
		
		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (radioGroup.getCheckedRadioButtonId() == R.id.radioA1) {
						question = new Question(number, 1, (time2 - time1),
								questions.get(show), radioButton1.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA2) {
						question = new Question(number, 2, (time2 - time1),
								questions.get(show), radioButton2.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA3) {
						question = new Question(number, 3, (time2 - time1),
								questions.get(show), radioButton3.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA4) {
						question = new Question(number, 4, (time2 - time1),
								questions.get(show), radioButton4.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA5) {
						question = new Question(number, 5, (time2 - time1),
								questions.get(show), radioButton5.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (java.sql.SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//---------gia tin eisodo tou 0 sti lista me tous arithmous ton erwthsewn 
				//gt to show=0 ginetai exw apo to next------------------------------------------------------------------------------------------------
				if (Pass.isEmpty()) {
					Pass.add(Integer.toString(0));
				}
				if (exist_goto == true && Pass.contains(Integer.toString(show))==false) {
					Pass.add(Integer.toString(next_show_number-1));
					show = show + 1;
					exist_goto = false;
				} else {
					show = show + 1;
					Pass.add(Integer.toString(show));
				}
				
				Answers.put(Integer.parseInt(Pass.get(Pass.size()-2)),index_ans);
				System.out.println("Pass arraylists values:::::: " + Pass);
				System.out.println("Answers arraylists values in next:::::: " + Answers);
				if (show <= questions.size() - 1) {
					
					//----------------emfanish apantisewn se erwthseis pou exoun apantithei------------------------------
					int value;
					RadioButton radio;
					showQuestion();
					if (Answers.containsKey(show)){
						value = Answers.get(show);
						if (value == 1){
							radio = (RadioButton) findViewById(R.id.radioA1);
							radio.setChecked(true);
						}
						else if (value == 2){
							radio = (RadioButton) findViewById(R.id.radioA2);
							radio.setChecked(true);
						}
						else if (value == 3){
							radio = (RadioButton) findViewById(R.id.radioA3);
							radio.setChecked(true);
						}
						else if (value == 4){
							radio = (RadioButton) findViewById(R.id.radioA4);
							radio.setChecked(true);
						}
						else if (value == 5){
							radio = (RadioButton) findViewById(R.id.radioA5);
							radio.setChecked(true);
						}
						
					}

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
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (java.sql.SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					//---- otan teleiwsoun tis erwthseis px 30/30(den patame save) opote to arxeio me tis
					//times gia tis mpares ftiaxnete edw
					try {					
						fileforBar = new File(Environment.getExternalStorageDirectory(), "Bar"+category+".txt");
						if (fileforBar.exists()) {
							 fileforBar.delete();
								}
						// if file doesnt exists, then create it
						if (!file.exists()) {
							file.createNewFile();
						}
						FileWriter fw = new FileWriter(fileforBar.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(Integer.toString(find_max_show()-1));
						System.out.println(question_number);
						bw.flush();
				        bw.write('\n');
				        bw.flush();
				        bw.write(Integer.toString(question_number));
				        bw.flush();
						bw.close();

			 
						System.out.println("Done");
			 
					} catch (IOException e) {
						e.printStackTrace();
					}
					
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
						question = new Question(number, 1, (time2 - time1),
								questions.get(show), radioButton1.getText()
										.toString());
						question.setLocation(location);
						// dateFormatted = formatter.format(date);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA2) {
						question = new Question(number, 2, (time2 - time1),
								questions.get(show), radioButton2.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA3) {
						question = new Question(number, 3, (time2 - time1),
								questions.get(show), radioButton3.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA4) {
						question = new Question(number, 4, (time2 - time1),
								questions.get(show), radioButton4.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					} else if (radioGroup.getCheckedRadioButtonId() == R.id.radioA5) {
						question = new Question(number, 5, (time2 - time1),
								questions.get(show), radioButton5.getText()
										.toString());
						question.setLocation(location);
						question.setTimestamp(formatter.format(
								new Date().getTime()).toString());
						getHelper().getQuestionDao().create(question);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (java.sql.SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// ---------------------------------------
				prev_index = Pass.get(Pass.size()-1);
				Pass.remove(prev_index);	
				System.out.println("Pass list in back  "+ Pass);			
				//-------------h sinartisi find_back_value-------------------------------
				find_back_value(show);
				String tmp_show;
				show = back_show;
				tmp_show = Integer.toString(back_show);
				System.out.println("#######show,tmp_show,back_show"+ show + tmp_show + back_show);
				showQuestion();
				//---------------------------------------------
				RadioButton radio;
				int cur_answer;
				if(show==0){
					cur_answer = Answers.get(0);
				}
				else{
				System.out.println("to show sto else   " + show);
				cur_answer = Answers.get(Integer.parseInt(tmp_show));
				}	
				System.out.println("Answers arraylists values in back:::::: " + Answers);
				if (cur_answer == 1){
					radio = (RadioButton) findViewById(R.id.radioA1);
					radio.setChecked(true);
				}
				else if (cur_answer == 2){
					radio = (RadioButton) findViewById(R.id.radioA2);
					radio.setChecked(true);
				}
				else if (cur_answer == 3){
					radio = (RadioButton) findViewById(R.id.radioA3);
					radio.setChecked(true);
				}
				else if (cur_answer == 4){
					radio = (RadioButton) findViewById(R.id.radioA4);
					radio.setChecked(true);
				}
				else if (cur_answer == 5){
					radio = (RadioButton) findViewById(R.id.radioA5);
					radio.setChecked(true);
				}				
				//--------------
				
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
				int tmp_show = -1;
				if (checkedId == R.id.radioA1) {
					
					index_ans = 1;
					}
				if (checkedId == R.id.radioA2) {
					index_ans = 2;
					}
				if (checkedId == R.id.radioA3) {
					index_ans = 3;
					}
				if (checkedId == R.id.radioA4) {
					index_ans = 4;
					}
				if (checkedId == R.id.radioA5) {
					index_ans = 5;				
					}
				if (checkedId == R.id.radioA1 && exist1 == true) {
					show = next_show_number - 1;
					}
				if (checkedId == R.id.radioA2 && exist2 == true) {
					show = next_show_number - 1;
					}
				if (checkedId == R.id.radioA3 && exist3 == true) {
					tmp_show =show;
					show = next_show_number - 1;
					}
				if (checkedId == R.id.radioA4 && exist4 == true) {
					show = next_show_number - 1;
					}
				if (checkedId == R.id.radioA5 && exist5 == true) {
					show = next_show_number - 1;
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
				// ------------------------------------------------------------
				if (checkedId == R.id.radioA1 && exist1 == true) {
					exist_goto = true;
				}
				if (checkedId == R.id.radioA2 && exist2 == true) {
					exist_goto = true;
				}
				if (checkedId == R.id.radioA3 && exist3 == true) {
					exist_goto = true;
				}
				if (checkedId == R.id.radioA4 && exist4 == true) {
					exist_goto = true;
				}
				if (checkedId == R.id.radioA4 && exist5 == true) {
					exist_goto = true;
				}
				if (checkedId == R.id.radioA5 && exist5 == true) {
					exist_goto = true;
				}
				//-------------------------	
			}

		});

	}

	private void loadQuestions(String category) {
		String question_file = null;

		System.out.println("category in load::: " + category);

		if (category.equals("Medical History"))
			question_file = "raw/questions_medical_history";
		else if (category.equals("Environmental"))
			question_file = "raw/questions_environmental";
		else if (category.equals("Cosmetics"))
			question_file = "raw/questions_cosmetics";
		else if (category.equals("Water"))
			question_file = "raw/questions_water";
		else if (category.equals("Lifestyle"))
			question_file = "raw/questions_lifestyle";
		else {
			Toast.makeText(getApplicationContext(),
					"Oops, something went wrong! Unknown category",
					Toast.LENGTH_LONG).show();
			return;
		}

		System.out.println("question file::: " + question_file);
		// if (question_file!=null){
		InputStream ins = getApplicationContext().getResources()
				.openRawResource(
						getApplicationContext().getResources().getIdentifier(
								question_file, "raw",
								getApplicationContext().getPackageName()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
		String line;
		questions = new ArrayList<String>();
		Pass = new ArrayList<String>();// edw
		Answers = new HashMap<Integer,Integer>();
		//----diavasma tou arxeiou twn erwthsewn----------------
		try {
			while ((line = reader.readLine()) != null) {
				questions.add(line);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//------diavasma tou arxeioy me tousarithmous twn erwthsewn pou exoun apantithei---------------
		try {
		    reader = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/Pass"+category+".txt"));
		    String text = null;

		    while ((text = reader.readLine()) != null) {
		        Pass.add(text);
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (reader != null) {
		            reader.close();
		        }
		    } catch (IOException e) {
		    }
		}
		if (file.exists()) {
			file.delete();
			}
		//print out the list
		System.out.println("hsPass einai:"  +  Pass);
		System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwww");
		//-------diabasma tou Map <erwthsh,apantish>-------------------------------------
		try{
	        File toRead=new File(Environment.getExternalStorageDirectory()+"/Answers"+category+".txt");
	        FileInputStream fis=new FileInputStream(toRead);
	        ObjectInputStream ois=new ObjectInputStream(fis);

	        Answers=(HashMap<Integer,Integer>)ois.readObject();

	        ois.close();
	        fis.close();
	        //print All data in MAP
	        for (Map.Entry<Integer, Integer> entry : Answers.entrySet()){
	            System.out.println(entry.getKey()+" : "+entry.getValue());
	        }
	    }catch(Exception e){}
		if (fileMap.exists()) {
			fileMap.delete();
			}
		System.out.println("hsAnswers einai:"  +  Answers);
		System.out.println("edwwwwwwwwwwwwwwwwwwwwwwwwwww");
		//--------------------------diavasma tou arxeiou me tis times gia tis mpares---------------------------------------------------------
		try {
			BufferedReader readerBar = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/Bar"+category+".txt"));
		    String text = null;
		    
				 
		    while ((text = readerBar.readLine()) != null) {
		        System.out.println("to Bar file periexei:  "+ text);
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
	}

	private void setRadioButtonsText(int i, String answer) {
		String ans = answer;
		String ans2 = answer;
		if (ans2.contains("goto")) {
			// exist_goto = true;
			String[] parts = ans2.split("goto");
			String show_number = parts[1];
			next_show_number = Integer.valueOf(parts[1]);
			//Answers.put(next_show_number-1-show-1, );
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
		if (Pass.size() == 1) {
			show = 0;
			Pass.clear();
			
		}
		else if (Pass.isEmpty())
		{
			show = 0;
		}
		else{show = Integer.parseInt(Pass.get(Pass.size()-1));}
		str1 = Integer.toString(show + 1);
		str2 = "/";
		str3 = Integer.toString(question_number);
		strall = str1 + str2 + str3;
		quest_number.setText(strall);
		
		String question = questions.get(show);
		String[] data = question.split("\t");
		body.setText(data[1]);

		for (int i = 2; i < question.split("\t").length; i++) {
			setRadioButtonsText(i - 1, data[i]);
		}
		if (show > 0) {//neo
			back.setVisibility(View.VISIBLE);
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
	private void find_back_value(int show_value)
	{
		int prev_max = -1;
		for (Map.Entry<Integer, Integer> entry : Answers.entrySet()) {
		    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			if(entry.getKey() == show_value){
				continue;
			}
			else if (entry.getKey()>prev_max && entry.getKey()<show_value){
				prev_max = entry.getKey();
			}
				
		}
		back_show = prev_max;
	}
	private int find_max_show()
	{
		int max = -1;
		for (Map.Entry<Integer, Integer> entry : Answers.entrySet()) {
		    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			if(entry.getKey()> max){
				max = entry.getKey(); 
			}
		}
		return (max+1);
	}
	private int check(){
		try {
			BufferedReader readerBar = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/Bar"+category+".txt"));
		    String text = null;
		    int num1,num2;
		    
				 num1 = Integer.parseInt(readerBar.readLine());
				 num2 = Integer.parseInt(readerBar.readLine());
		    if(num1>=num2)
		    {
		    	Toast toast = Toast.makeText(getApplicationContext(),"You have already answered this questionnaire", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
		    	return 1;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return 0;
	}
}
