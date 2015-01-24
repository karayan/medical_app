package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.ui.TabSensorsChartsActivity.CONN_STATE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.example.medicalapp.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class BarChartsActivity extends Activity {

    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private Thread getThread;
    private WifiManager.MulticastLock lock;
    private XYSeries ph;
    private XYSeries conductivity;
    private XYSeries ORP;
    private XYSeries temperature;
    private XYSeries chlorine;
    private XYMultipleSeriesDataset dataset;
    private XYSeriesRenderer phRenderer;
    private XYSeriesRenderer chlorineRenderer;
    private XYSeriesRenderer temperatureRenderer;
    private XYSeriesRenderer conductivityRenderer;
    private XYSeriesRenderer ORPRenderer;
    private XYMultipleSeriesRenderer multiRenderer;
    private GraphicalView mChart;
    private LinearLayout chart_container;
    public static DatagramSocket s = null;
    private ProgressDialog progressDialog;
    private TextView no_sensor;
    private CONN_STATE state;
    private CheckBox writeFile;
    private OutputStreamWriter myOutWriter;
    private FileOutputStream fOut;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	setContentView(R.layout.bar_charts_view);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	chart_container = (LinearLayout) findViewById(R.id.chart);
	no_sensor = (TextView) findViewById(R.id.text);
	writeFile = (CheckBox) findViewById(R.id.write_file);
	writeFile.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    @Override
	    public void onCheckedChanged(CompoundButton arg0, boolean value) {
		try {
		    if (value) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			Date now = new Date();
			String fileName = "Bar_Measurements" + formatter.format(now) + ".txt";
			File storagePath = new File(Environment.getExternalStorageDirectory() + "/UrbanNet/");
			storagePath.mkdirs();
			File myFile = new File(storagePath, fileName);
			myFile.createNewFile();
			fOut = new FileOutputStream(myFile);
			myOutWriter = new OutputStreamWriter(fOut);
		    }
		    else {
			myOutWriter.close();
			fOut.close();
		    }
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	});
	initCharts();
    }

    private void initCharts() {
	// Creating an XYSeries
	ph = new XYSeries("ph  ");
	conductivity = new XYSeries("Conductivity  ");
	ORP = new XYSeries("ORP  ");
	temperature = new XYSeries("Temperature  ");
	chlorine = new XYSeries("Ion Cl  ");

	// Creating a dataset to hold each series
	dataset = new XYMultipleSeriesDataset();

	// Creating XYSeriesRenderer
	phRenderer = new XYSeriesRenderer();
	phRenderer.setColor(Color.parseColor("#CCCCCC"));
	phRenderer.setFillPoints(true);
	phRenderer.setLineWidth(4);
	phRenderer.setChartValuesTextAlign(Align.CENTER);
	phRenderer.setDisplayChartValues(true);
	phRenderer.setChartValuesTextSize(UrbanNetApp.medium_size1);

	chlorineRenderer = new XYSeriesRenderer();
	chlorineRenderer.setColor(Color.parseColor("#00CC99"));
	chlorineRenderer.setFillPoints(true);
	chlorineRenderer.setLineWidth(4);
	chlorineRenderer.setChartValuesTextAlign(Align.CENTER);
	chlorineRenderer.setDisplayChartValues(true);
	chlorineRenderer.setChartValuesTextSize(UrbanNetApp.medium_size1);

	temperatureRenderer = new XYSeriesRenderer();
	temperatureRenderer.setColor(Color.parseColor("#FF9933"));
	temperatureRenderer.setFillPoints(true);
	temperatureRenderer.setLineWidth(4);
	temperatureRenderer.setChartValuesTextAlign(Align.CENTER);
	temperatureRenderer.setDisplayChartValues(true);
	temperatureRenderer.setChartValuesTextSize(UrbanNetApp.medium_size1);

	conductivityRenderer = new XYSeriesRenderer();
	conductivityRenderer.setColor(Color.parseColor("#FFFF99"));
	conductivityRenderer.setFillPoints(true);
	conductivityRenderer.setLineWidth(4);
	conductivityRenderer.setChartValuesTextAlign(Align.CENTER);
	conductivityRenderer.setDisplayChartValues(true);
	conductivityRenderer.setChartValuesTextSize(UrbanNetApp.medium_size1);

	ORPRenderer = new XYSeriesRenderer();
	ORPRenderer.setColor(Color.parseColor("#3333FF"));
	ORPRenderer.setFillPoints(true);
	ORPRenderer.setLineWidth(4);
	ORPRenderer.setChartValuesTextAlign(Align.CENTER);
	ORPRenderer.setDisplayChartValues(true);
	ORPRenderer.setChartValuesTextSize(UrbanNetApp.medium_size1);

	ph.add(1, 0);
	conductivity.add(2, 0);
	temperature.add(3, 0);
	ORP.add(4, 0);
	chlorine.add(5, 0);
	// Adding Income Series to the dataset
	dataset.addSeries(ph);
	dataset.addSeries(ORP);
	dataset.addSeries(conductivity);
	dataset.addSeries(chlorine);
	dataset.addSeries(temperature);

	// Creating a XYMultipleSeriesRenderer to customize the whole chart
	multiRenderer = new XYMultipleSeriesRenderer();
	multiRenderer.setZoomEnabled(false, false); // disable pinch to zoom
	multiRenderer.setXAxisMin(0); // for x axis to start with some space
	multiRenderer.setXAxisMax(6);
	multiRenderer.setYAxisMin(0);
	multiRenderer.setYAxisMax(36.9);
	multiRenderer.setGridColor(Color.GRAY); // grid line color
	// multiRenderer.setShowGridX(true); // to show the grid line
	multiRenderer.setShowGridY(true); // to show the grid line
	multiRenderer.setPanEnabled(false, false);
	multiRenderer.setPanLimits(new double[] { 0, 6, 0, 6 });
	multiRenderer.setChartTitle("Sensor measurments ");
	multiRenderer.setChartTitleTextSize(UrbanNetApp.large_size1);
	multiRenderer.setLabelsColor(Color.WHITE); // changing the x,y title color
	multiRenderer.setXLabelsColor(Color.WHITE); // changing the color of x label
	// multiRenderer.setXTitle(" Substances"); // title for x
	multiRenderer.setXLabels(0);
	multiRenderer.setYLabelsAlign(Align.CENTER);// labels to display right
	multiRenderer.setYLabelsColor(0, Color.WHITE);// changing the color of y label
	// multiRenderer.setYTitle("Score");// title for y
	multiRenderer.setAxisTitleTextSize(UrbanNetApp.small_size2);
	multiRenderer.setBarWidth(12);
	multiRenderer.setDisplayValues(true);
	multiRenderer.setFitLegend(true);
	multiRenderer.setAntialiasing(true);
	multiRenderer.setBarSpacing(1);
	multiRenderer.addSeriesRenderer(ORPRenderer);
	multiRenderer.addSeriesRenderer(chlorineRenderer);
	multiRenderer.addSeriesRenderer(phRenderer);
	multiRenderer.addSeriesRenderer(temperatureRenderer);
	multiRenderer.addSeriesRenderer(conductivityRenderer);
	multiRenderer.setLabelsTextSize(UrbanNetApp.small_size2);
	multiRenderer.setLegendTextSize(UrbanNetApp.medium_size1);

	mChart = ChartFactory.getBarChartView(getApplicationContext(), dataset, multiRenderer, Type.STACKED);
	chart_container.addView(mChart); // Adding click event to the Line Chart.
    }

    private void receiveUDP(final String ip) {
	final String[] text = new String[1];
	final byte[] message = new byte[1500];
	getThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		try {
		    s = new DatagramSocket(UrbanNetApp.server_port);
		    DatagramPacket p = new DatagramPacket(message, message.length, InetAddress.getByName(ip), UrbanNetApp.server_port);
		    Log.d(this.getClass().getName(), "Receiving socket >>> at IP: " + ip + " and port: " + UrbanNetApp.server_port);
		    s.setSoTimeout(10000);// waiting for 2 periods of the sensor send data
		    while (true) {
			s.receive(p);
			text[0] = new String(message, 0, p.getLength());
			runOnUiThread(new Runnable() {
			    @Override
			    public void run() {
				if (no_sensor.getVisibility() == View.VISIBLE) {
				    no_sensor.setVisibility(View.GONE);
				    chart_container.setVisibility(View.VISIBLE);
				    writeFile.setVisibility(View.VISIBLE);
				}
				progressDialog.cancel();
				state = CONN_STATE.CONNECTED;
				loadData(text[0]);
			    }
			});

		    }
		}
		catch (SocketException e) {
		    Log.e(this.getClass().getName(), "An unexpected error occurred", e);
		    state = CONN_STATE.NOT_CONNECTED;
		    stop();
		    progressDialog.cancel();
		}
		catch (SocketTimeoutException e2) {
		    progressDialog.cancel();
		    stop();
		    if (state == CONN_STATE.CONNECTED) {
			state = CONN_STATE.LOST_CONNECTION;
		    }
		    else {

		    }
		    Log.d(this.getClass().getName(), "Time out!!! closing socket......");
		}
		catch (IOException e) {
		    state = CONN_STATE.NOT_CONNECTED;
		    Log.e(this.getClass().getName(), "An unexpected error occurred", e);
		    stop();
		    progressDialog.cancel();
		}
		finally {
		    if (s != null) s.close();
		    try {
			if (fOut != null) {
			    myOutWriter.close();
			    fOut.close();
			}
		    }
		    catch (IOException e) {
			e.printStackTrace();
		    }

		}

	    }

	    private void stop() {
		runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			if (state == CONN_STATE.LOST_CONNECTION) {
			    no_sensor.setText(R.string.sensors_problem);
			}
			else {
			    no_sensor.setText(R.string.no_sensors);
			}
			no_sensor.setVisibility(View.VISIBLE);
			chart_container.setVisibility(View.GONE);
			writeFile.setVisibility(View.GONE);
			state = CONN_STATE.DISCONNECTED;
		    }
		});
	    }

	});
	getThread.start();
    }

    private void sendUDP(final String ip) {
	int ipAddress = wifiInfo.getIpAddress();
	String ip_cleint = intToIp(ipAddress);
	final byte[] message = ip_cleint.getBytes();

	try {
	    Log.d(this.getClass().getName(), "Sending socket >>> at IP: " + ip + " and port: " + UrbanNetApp.server_port);
	    DatagramSocket socket = new DatagramSocket();
	    DatagramPacket p = new DatagramPacket(message, message.length, InetAddress.getByName(ip), UrbanNetApp.server_port);
	    socket.send(p);
	    socket.close();
	}
	catch (SocketException e) {
	    Log.e(this.getClass().getName(), "An unexpected error occurred", e);
	}
	catch (SocketTimeoutException e2) {
	    Log.d(this.getClass().getName(), "Time out!!! closing socket......");
	}
	catch (IOException e) {
	    Log.e(this.getClass().getName(), "An unexpected error occurred", e);
	}
	finally {
	}

    }

    private String intToIp(int i) {

	return ((i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF));
    }

    private String broadcastNet(String ip) {
	String[] tem = ip.split("\\.");
	return (tem[0] + "." + tem[1] + "." + tem[2] + "." + "255");
    }

    private void loadData(String data) {
	String[] substacles = data.split(",");
	double phValue = Double.parseDouble(substacles[1].split("\t")[1]);
	double conductivityValue = Double.parseDouble(substacles[2].split("\t")[1]);
	double tempValue = Double.parseDouble(substacles[3].split("\t")[1]);
	double orpValue = Double.parseDouble(substacles[4].split("\t")[1]);
	double clorineValue = Double.parseDouble(substacles[5].split("\t")[1]);

	if (writeFile.isChecked()) {
	    try {
		myOutWriter.append(data);
	    }
	    catch (IOException e) {
		e.printStackTrace();
	    }
	}

	try {
	    dataset.clear();
	    multiRenderer.removeAllRenderers();
	}
	catch (NullPointerException ne) {}
	ph = new XYSeries("ph  ");
	conductivity = new XYSeries("Conductivity  ");
	ORP = new XYSeries("ORP  ");
	temperature = new XYSeries("Temperature  ");
	chlorine = new XYSeries("Chlorine ");
	ph.add(1, phValue);
	conductivity.add(2, conductivityValue);
	temperature.add(3, tempValue);
	ORP.add(4, orpValue);
	chlorine.add(5, clorineValue);
	// Adding Income Series to the dataset
	dataset.addSeries(ph);
	dataset.addSeries(ORP);
	dataset.addSeries(conductivity);
	dataset.addSeries(chlorine);
	dataset.addSeries(temperature);
	multiRenderer.addSeriesRenderer(ORPRenderer);
	multiRenderer.addSeriesRenderer(chlorineRenderer);
	multiRenderer.addSeriesRenderer(phRenderer);
	multiRenderer.addSeriesRenderer(temperatureRenderer);
	multiRenderer.addSeriesRenderer(conductivityRenderer);
	refreshCharts();
    }

    private void refreshCharts() {
	mChart.refreshDrawableState();
	chart_container.refreshDrawableState();
	mChart.invalidate();
	chart_container.invalidate();
    }

    @Override
    public void onBackPressed() {
	super.onBackPressed();
	try {
	    if (s != null && lock != null && lock.isHeld()) {
		lock.release();
		getThread.stop();
		s.disconnect();
		s.close();
	    }
	    if (fOut != null) {
		myOutWriter.close();
		fOut.close();
	    }
	}
	catch (UnsupportedOperationException ue) {
	    ue.printStackTrace();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	Log.d(this.getClass().getName(), "onDestroy");
	try {
	    if (s != null && lock != null && lock.isHeld()) {
		lock.release();
		getThread.stop();
		s.disconnect();
		s.close();
	    }
	    if (fOut != null) {
		myOutWriter.close();
		fOut.close();
	    }
	}
	catch (UnsupportedOperationException ue) {
	    ue.printStackTrace();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void onPause() {
	super.onPause();
	Log.d(this.getClass().getName(), "onPause");
	try {
	    if (s != null && lock != null && lock.isHeld()) {
		lock.release();
		s.disconnect();
		s.close();
		getThread.stop();

	    }
	    if (fOut != null) {
		myOutWriter.close();
		fOut.close();
	    }
	}
	catch (UnsupportedOperationException ue) {
	    ue.printStackTrace();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void onResume() {
	super.onResume();
	Log.d(this.getClass().getName(), "on Resume");
	progressDialog = ProgressDialog.show(BarChartsActivity.this, "", "Searching for sensors");
	wifiInfo = wifiManager.getConnectionInfo();
	if (wifiInfo.getBSSID() != null && wifiInfo.getNetworkId() != -1) {
	    lock = wifiManager.createMulticastLock("Main");
	    int ipAddress = wifiInfo.getIpAddress();
	    lock.acquire();
	    lock.setReferenceCounted(true);
	    String ip = intToIp(ipAddress);
	    String lan = broadcastNet(ip);
	    sendUDP(UrbanNetApp.server_IP);
	    receiveUDP(ip);
	}
	else {
	    progressDialog.cancel();
	    no_sensor.setText(R.string.socket_problem);
	    no_sensor.setVisibility(View.VISIBLE);
	    chart_container.setVisibility(View.GONE);
	    writeFile.setVisibility(View.GONE);
	    state = CONN_STATE.NOT_CONNECTED;
	}
    }
}
