package gr.forth.ics.urbanNet.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;

import javax.net.ssl.SSLException;

import org.apache.http.client.ClientProtocolException;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.location.LocationService;
import gr.forth.ics.urbanNet.location.LocationServiceConnection;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.network.NetworkService;
import gr.forth.ics.urbanNet.network.NetworkServiceConnectable;
import gr.forth.ics.urbanNet.network.NetworkServiceConnection;
import gr.forth.ics.urbanNet.network.ServerException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.Menu;
//import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;

public class QuestActivity extends OrmLiteBaseMapActivity<DatabaseHelper>
		implements NetworkServiceConnectable {
	Button Details;
	Button History;
	Button Environ;
	Button Cosmetics;
	Button Water;
	Button Lifestyle;
	Button Upload;

	NetworkServiceConnection networkServiceConnection;
	LocationServiceConnection locationServiceConnection;

	private NetworkInfo networkInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.questionnaire);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		addListenerOnButton();

		networkServiceConnection = new NetworkServiceConnection(this);
		// startService(new Intent(this, NetworkService.class));
		bindService(new Intent(this, NetworkService.class),
				super.networkServiceConnection, Context.BIND_AUTO_CREATE);

		// Need the location and network service
		Intent intent = new Intent(this, LocationService.class);
		bindService(intent, super.locationServiceConnection,
				Context.BIND_AUTO_CREATE);

		// inflate progress bars
		inflateProgressBars();

	}

	public void addListenerOnButton() {
		final Context context = this;

		Details = (Button) findViewById(R.id.buttonDetails);
		History = (Button) findViewById(R.id.buttonHistory);
		Environ = (Button) findViewById(R.id.buttonEnviron);
		Cosmetics = (Button) findViewById(R.id.buttonCosmetics);
		Water = (Button) findViewById(R.id.buttonWater);
		Lifestyle = (Button) findViewById(R.id.buttonlife);
		Upload = (Button) findViewById(R.id.buttonUpload);
		Upload.setVisibility(View.GONE);

		Details.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Log.v("EditText", mEdit.getText().toString());
				Intent i = new Intent(context, UserDetails.class);
				i.putExtra("category", "user_details");
				startActivity(i);
			}
		});

		History.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Log.v("EditText", mEdit.getText().toString());
				Intent i = new Intent(context, QuestionnaireActivity.class);
				i.putExtra("category", "Medical History");
				startActivity(i);
			}
		});
		Environ.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Log.v("EditText", mEdit.getText().toString());
				Intent i = new Intent(context, QuestionnaireActivity.class);
				i.putExtra("category", "Environmental");
				startActivity(i);
			}
		});

		Cosmetics.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Log.v("EditText", mEdit.getText().toString());
				Intent i = new Intent(context, QuestionnaireActivity.class);
				i.putExtra("category", "Cosmetics");
				startActivity(i);
			}
		});

		Water.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Log.v("EditText", mEdit.getText().toString());
				Intent i = new Intent(context, QuestionnaireActivity.class);
				i.putExtra("category", "Water");
				startActivity(i);
			}
		});

		Lifestyle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Log.v("EditText", mEdit.getText().toString());
				Intent i = new Intent(context, QuestionnaireActivity.class);
				i.putExtra("category", "Lifestyle");
				startActivity(i);
			}
		});

		// Upload.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// System.out
		// .println("======================  onclick: upload to server =================");
		// if (networkServiceConnection.getService() != null) {
		// networkServiceConnection.getService().uploadDataset(true);
		// }
		// System.out
		// .println("======================  test after upload =================");
		//
		// }
		// });

		Upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// networkServiceConnection = new NetworkServiceConnection(
				// QuestActivity.this);
				// startService(new Intent(getApplicationContext(),
				// NetworkService.class));
				bindService(
						new Intent(QuestActivity.this, NetworkService.class),
						networkServiceConnection, Context.BIND_AUTO_CREATE);
				//
				// // Need the location and network service
				// Intent intent = new Intent(QuestActivity.this,
				// LocationService.class);
				// bindService(intent, locationServiceConnection,
				// Context.BIND_AUTO_CREATE);

				// TODO Auto-generated method stub
				// upload data to the urbanNet server

				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						networkInfo = connMgr.getActiveNetworkInfo();
						if (networkInfo == null) {
							Toast.makeText(context,
									"There is no Internet connection",
									Toast.LENGTH_LONG).show();
						} else {
							networkServiceConnection.getService();
							// QoE and complaints upload
							if (networkServiceConnection.isBound()
									&& NetworkService.com.isLoggedIn()) {
								System.out
										.println("service is bound and logged in");
								if (UrbanNetApp.isMeasuringPerformance) {
									System.out.println("test: is measuring");
									networkServiceConnection.getService()
											.runSendDataTest(
													UrbanNetApp.nSendDataRuns);
								} else {
									System.out
											.println("test: is not measuring");
									networkServiceConnection.getService()
											.uploadDataset(true);
								}
								QuestActivity.this
										.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												// TODO Auto-generated method
												// stub
												Toast.makeText(
														getApplicationContext(),
														"Uploaded data to umap server",
														Toast.LENGTH_LONG)
														.show();
											}
										});
							} else {
								System.out
										.println("service is not bound!!!!!!!!!!!!!!!!!!!!!!!");
								networkServiceConnection.getService();
								if (networkServiceConnection.isBound()
										&& NetworkService.com.isLoggedIn() == false) {
									System.out.println("test: false");
									try {
										System.out.println("test: in try");
										networkServiceConnection.getService();
										NetworkService.com.loginClient();
										networkServiceConnection
												.getService()
												.uploadDataToServerWithReflectionAndThreads(
														false);
										QuestActivity.this
												.runOnUiThread(new Runnable() {

													@Override
													public void run() {
														// TODO Auto-generated
														// method stub
														Toast.makeText(
																getApplicationContext(),
																"Uploaded data to umap server",
																Toast.LENGTH_LONG)
																.show();
													}
												});

									} catch (SocketException e) {
										e.printStackTrace();
									} catch (SSLException e) {
										e.printStackTrace();
									} catch (ClientProtocolException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									} catch (ServerException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}

				});
				thread.start();
			}

		});
	}

	/**
	 * Upload manually the QoE scores. Also check for cycling billing expiration
	 * and upload if has expire
	 */
	// Upload.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// // upload data to the urbanNet server
	// ConnectivityManager connMgr = (ConnectivityManager)
	// getSystemService(Context.CONNECTIVITY_SERVICE);
	// networkInfo = connMgr.getActiveNetworkInfo();
	// if (networkInfo == null) {
	// Toast.makeText(context, "There is no Internet connection",
	// Toast.LENGTH_LONG).show();
	// } else {
	// networkServiceConnection.getService();
	// // QoE and complaints upload
	// if (networkServiceConnection.isBound()
	// && NetworkService.com.isLoggedIn()) {
	// if (UrbanNetApp.isMeasuringPerformance) {
	// networkServiceConnection.getService()
	// .runSendDataTest(UrbanNetApp.nSendDataRuns);
	// } else {
	// networkServiceConnection.getService()
	// .uploadDataset(true);
	// }
	// } else {
	// networkServiceConnection.getService();
	// if (networkServiceConnection.isBound()
	// && NetworkService.com.isLoggedIn() == false) {
	// try {
	// networkServiceConnection.getService();
	// NetworkService.com.loginClient();
	// networkServiceConnection
	// .getService()
	// .uploadDataToServerWithReflectionAndThreads(
	// false);
	// } catch (SocketException e) {
	// e.printStackTrace();
	// } catch (SSLException e) {
	// e.printStackTrace();
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (ServerException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	// }
	// });
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.upload, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();

		System.out
				.println("====================== RESUME: upload to server =================");

		inflateProgressBars();

		bindService(new Intent(QuestActivity.this, NetworkService.class),
				networkServiceConnection, Context.BIND_AUTO_CREATE);
		//
		// // Need the location and network service
		// Intent intent = new Intent(QuestActivity.this,
		// LocationService.class);
		// bindService(intent, locationServiceConnection,
		// Context.BIND_AUTO_CREATE);

		// TODO Auto-generated method stub
		// upload data to the urbanNet server

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo == null) {
					Toast.makeText(getApplicationContext(),
							"There is no Internet connection",
							Toast.LENGTH_LONG).show();
				} else {
					networkServiceConnection.getService();
					// QoE and complaints upload
					if (networkServiceConnection.isBound()
							&& NetworkService.com.isLoggedIn()) {
						System.out.println("service is bound and logged in");
						if (UrbanNetApp.isMeasuringPerformance) {
							System.out.println("test: is measuring");
							networkServiceConnection.getService()
									.runSendDataTest(UrbanNetApp.nSendDataRuns);
						} else {
							System.out.println("test: is not measuring");
							networkServiceConnection.getService()
									.uploadDataset(true);
						}
						QuestActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(getApplicationContext(),
										"Uploaded data to umap server",
										Toast.LENGTH_LONG).show();
							}
						});
					} else {
						System.out
								.println("service is not bound!!!!!!!!!!!!!!!!!!!!!!!");
						networkServiceConnection.getService();
						if (networkServiceConnection.isBound()
								&& NetworkService.com.isLoggedIn() == false) {
							System.out.println("test: false");
							try {
								System.out.println("test: in try");
								networkServiceConnection.getService();
								NetworkService.com.loginClient();
								networkServiceConnection
										.getService()
										.uploadDataToServerWithReflectionAndThreads(
												false); // TODO get response to
														// show toast
								QuestActivity.this
										.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												// TODO Auto-generated method
												// stub
												Toast.makeText(
														getApplicationContext(),
														"Uploaded data to umap server",
														Toast.LENGTH_LONG)
														.show();
											}
										});

							} catch (SocketException e) {
								e.printStackTrace();
							} catch (SSLException e) {
								e.printStackTrace();
							} catch (ClientProtocolException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							} catch (ServerException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

		});
		thread.start();

		System.out
				.println("======================  test after upload =================");
	}

	//
	// @Override
	// public void onRestart(){
	// super.onRestart();
	//
	// System.out.println("====================== RESTART: upload to server =================");
	// // networkServiceConnection.getService().uploadDataset(true);
	// System.out.println("======================  test after upload =================");
	// }
	//
	@Override
	public void onDestroy() {
		super.onDestroy();

		System.out
				.println("======================  DESTROY: unbind service =================");
		// if (networkServiceConnection.getService() != null)
		// unbindService(networkServiceConnection);
		System.out
				.println("======================  test after unbind =================");
	}

	@Override
	public void onPause() {
		super.onPause();

		System.out
				.println("======================  PAUSE: unbind service =================");
		if (networkServiceConnection.getService() != null)
			unbindService(networkServiceConnection);
		System.out
				.println("======================  test after unbind =================");
	}

	public void inflateProgressBars() {
		Button buttonMed = (Button) findViewById(R.id.buttonHistory);
		Button buttonEnv = (Button) findViewById(R.id.buttonEnviron);
		Button buttonCos = (Button) findViewById(R.id.buttonCosmetics);
		Button buttonWater = (Button) findViewById(R.id.buttonWater);
		Button buttonLife = (Button) findViewById(R.id.buttonlife);

		ProgressBar progress_medical = (ProgressBar) findViewById(R.id.progressBar_medical_history);
		ProgressBar progress_environmental = (ProgressBar) findViewById(R.id.progressBar_environmental);
		ProgressBar progress_cosmetics = (ProgressBar) findViewById(R.id.progressBar_cosmetics);
		ProgressBar progress_water = (ProgressBar) findViewById(R.id.progressBar_water);
		ProgressBar progress_lifestyle = (ProgressBar) findViewById(R.id.progressBar_lifestyle);

		double[] stats = { 0, 0, 0 };

		stats = compute_percentage("BarMedical History.txt");

		buttonMed.setText("Medical History (" + (int) stats[0] + "/"
				+ (int) stats[1] + ")");
		progress_medical.setProgress((int) (stats[2]));
		setProgressBarColor(progress_medical, stats[2]);

		stats = compute_percentage("BarEnvironmental.txt");

		buttonEnv.setText("Environmental (" + (int) stats[0] + "/"
				+ (int) stats[1] + ")");
		progress_environmental.setProgress((int) stats[2]);
		setProgressBarColor(progress_environmental, (int) stats[2]);

		stats = compute_percentage("BarCosmetics.txt");

		buttonCos.setText("Cosmetics (" + (int) stats[0] + "/" + (int) stats[1]
				+ ")");
		progress_cosmetics.setProgress((int) stats[2]);
		setProgressBarColor(progress_cosmetics, stats[2]);

		stats = compute_percentage("BarWater.txt");

		buttonWater.setText("Water (" + (int) stats[0] + "/" + (int) stats[1]
				+ ")");
		progress_water.setProgress((int) stats[2]);
		setProgressBarColor(progress_water, (int) stats[2]);

		stats = compute_percentage("BarLifestyle.txt");

		buttonLife.setText("Lifestyle (" + (int) stats[0] + "/"
				+ (int) stats[1] + ")");
		progress_lifestyle.setProgress((int) stats[2]);
		setProgressBarColor(progress_lifestyle, (int) stats[2]);
	}

	public void setProgressBarColor(ProgressBar progress, double percentage) {
		if (percentage == 100)
			progress.getProgressDrawable().setColorFilter(
					Color.rgb(49, 153, 151), Mode.SRC_IN);
		else
			progress.getProgressDrawable().setColorFilter(
					Color.rgb(49, 153, 151), Mode.SRC_IN);
	}

	double[] compute_percentage(String filename) {
		double percentage;
		int n1 = 0, n2 = 0;
		double stats[] = { 0, 0, 0 };
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(
					Environment.getExternalStorageDirectory() + "/" + filename));
			try {
				if (reader != null) {
					String line = reader.readLine();
					System.out.println("Line 1 " + line);
					if (line != "")
						n1 = Integer.parseInt(line);
					line = reader.readLine();
					System.out.println("Line 2 " + line);
					if (line != "")
						n2 = Integer.parseInt(line);
				}
				reader.close();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		}

		if (n1 != 0 && n2 != 0) {
			percentage = (double) (n1 * 100) / n2;
			stats[0] = n1;
			stats[1] = n2;
			stats[2] = percentage;
		} else {
			percentage = 0;
			stats[0] = n1;
			stats[1] = n2;
			stats[2] = percentage;
		}
		System.out.println("percentage for " + filename + " == " + percentage);
		return stats;
	}

	//
	@Override
	public void onLocationServiceConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationServiceDisonnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkServiceConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkServiceDisonnected() {
		// TODO Auto-generated method stub

	}
}
