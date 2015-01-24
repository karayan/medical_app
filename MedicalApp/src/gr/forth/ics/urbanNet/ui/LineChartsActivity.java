package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

@SuppressLint("SimpleDateFormat")
public class LineChartsActivity extends Activity {
    private GraphicalView mChart;
    private Button PHBtn;
    private Button CondBtn;
    private Button Temp;
    private Button Chlr;
    private Button ORP;
    private boolean PHDisplay = true;
    private boolean CondDisplay = false;
    private boolean TempDisplay = false;
    private boolean ChlrDisplay = false;
    private boolean ORPDisplay = true;
    private XYMultipleSeriesRenderer multiRenderer;
    private XYMultipleSeriesDataset dataset;
    private XYSeries PHSeries;
    private XYSeries CondSeries;
    private XYSeries ChlrSeries;
    private XYSeries TempSeries;
    private XYSeries ORPSeries;
    private double maxPH;
    private double maxORP;
    private double maxTemp;
    private double maxCond;
    private double maxChl;
    private List<Double> maxYValueList;
    private XYSeriesRenderer XrendererPH;
    private XYSeriesRenderer XrendererTemp;
    private XYSeriesRenderer XrendererORP;
    private XYSeriesRenderer XrendererCond;
    private XYSeriesRenderer XrendererChlr;
    private LinearLayout chart_container;
    private TextView no_sensor;
    private ArrayList<String> seriesList = new ArrayList<String>();
    private int ChlrColor = Color.parseColor("#f1c40f");
    private int PHColor = Color.parseColor("#87D37C");
    private int CondColor = Color.parseColor("#C0392B");
    private int ORPColor = Color.parseColor("#59ABE3");
    private int TempColor = Color.parseColor("#8e44ad");
    private int j;
    private CheckBox writeFile;
    private ProgressDialog progressDialog;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private Thread getThread;
    private MulticastLock lock;
    private CONN_STATE state;
    private DatagramSocket s = null;
    private OutputStreamWriter myOutWriter;
    private FileOutputStream fOut;
    private LinearLayout btnLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.line_charts_view);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	CondBtn = (Button) findViewById(R.id.conductivity);
	Temp = (Button) findViewById(R.id.temperature);
	Chlr = (Button) findViewById(R.id.chlorine);
	ORP = (Button) findViewById(R.id.orp);
	PHBtn = (Button) findViewById(R.id.ph);
	btnLayout = (LinearLayout) findViewById(R.id.layout1);
	no_sensor = (TextView) findViewById(R.id.text_problem);
	chart_container = (LinearLayout) findViewById(R.id.chart);
	writeFile = (CheckBox) findViewById(R.id.write_file);
	writeFile.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    @Override
	    public void onCheckedChanged(CompoundButton arg0, boolean value) {
		try {
		    if (value) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			Date now = new Date();
			String fileName = "Line_Measurements" + formatter.format(now) + ".txt";
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
    }

    private void initCharts() {
	Log.d(this.getClass().getName(), "initCharts");
	j = 0;
	maxYValueList = new ArrayList<Double>();
	// Create XY Series for X Series.
	CondSeries = new XYSeries("Conductivity");
	ChlrSeries = new XYSeries("Chlorine");
	TempSeries = new XYSeries("Temp");
	ORPSeries = new XYSeries("ORP ");
	PHSeries = new XYSeries("PH ");

	// Create a Dataset to hold the XSeries.
	dataset = new XYMultipleSeriesDataset();

	// Create XYSeriesRenderer to customize XSeries
	XrendererCond = new XYSeriesRenderer();
	XrendererCond.setColor(CondColor);
	XrendererCond.setPointStyle(PointStyle.SQUARE);
	XrendererCond.setDisplayChartValues(true);
	XrendererCond.setDisplayChartValuesDistance(1);
	XrendererCond.setLineWidth(2);
	XrendererCond.setFillPoints(true);
	XrendererCond.setChartValuesTextSize(UrbanNetApp.small_size2);

	// Create XYSeriesRenderer to customize XSeries
	XrendererTemp = new XYSeriesRenderer();
	XrendererTemp.setColor(TempColor);
	XrendererTemp.setPointStyle(PointStyle.DIAMOND);
	XrendererTemp.setDisplayChartValues(true);
	XrendererTemp.setDisplayChartValuesDistance(1);
	XrendererTemp.setLineWidth(2);
	XrendererTemp.setFillPoints(true);
	XrendererTemp.setChartValuesTextSize(UrbanNetApp.small_size2);

	// Create XYSeriesRenderer to customize XSeries
	XrendererChlr = new XYSeriesRenderer();
	XrendererChlr.setColor(ChlrColor);
	XrendererChlr.setPointStyle(PointStyle.TRIANGLE);
	XrendererChlr.setDisplayChartValues(true);
	XrendererChlr.setDisplayChartValuesDistance(1);
	XrendererChlr.setLineWidth(2);
	XrendererChlr.setFillPoints(true);
	XrendererChlr.setDisplayBoundingPoints(true);
	XrendererChlr.setChartValuesTextAlign(Align.CENTER);
	XrendererChlr.setChartValuesTextSize(UrbanNetApp.small_size2);

	// Create XYSeriesRenderer to customize XSeries
	XrendererORP = new XYSeriesRenderer();
	XrendererORP.setColor(ORPColor);
	XrendererORP.setPointStyle(PointStyle.CIRCLE);
	XrendererORP.setDisplayChartValues(true);
	XrendererORP.setDisplayChartValuesDistance(1);
	XrendererORP.setLineWidth(2);
	XrendererORP.setFillPoints(true);
	XrendererORP.setChartValuesTextSize(UrbanNetApp.small_size2);

	// Create XYSeriesRenderer to customize XSeries
	XrendererPH = new XYSeriesRenderer();
	XrendererPH.setColor(PHColor);
	XrendererPH.setPointStyle(PointStyle.CIRCLE);
	XrendererPH.setDisplayChartValues(true);
	XrendererPH.setDisplayChartValuesDistance(1);
	XrendererPH.setLineWidth(2);
	XrendererPH.setFillPoints(true);
	XrendererPH.setChartValuesTextSize(UrbanNetApp.small_size2);

	// Create XYMultipleSeriesRenderer to customize the whole chart
	multiRenderer = new XYMultipleSeriesRenderer();
	multiRenderer.setChartTitle("Sensor measurments ");
	multiRenderer.setChartTitleTextSize(UrbanNetApp.large_size1);
	multiRenderer.setAntialiasing(true);
	multiRenderer.setShowGrid(true);
	multiRenderer.setXAxisMin(0.0);
	multiRenderer.setYAxisMin(0.0);
	multiRenderer.setXLabelsAlign(Align.CENTER);
	multiRenderer.setYLabelsAlign(Align.CENTER);
	multiRenderer.setXLabels(RESULT_OK);
	multiRenderer.setAxisTitleTextSize(UrbanNetApp.small_size2);
	multiRenderer.setZoomEnabled(false, false);
	multiRenderer.setLabelsTextSize(UrbanNetApp.small_size2);
	multiRenderer.setLegendTextSize(UrbanNetApp.medium_size1);
	multiRenderer.setPanEnabled(true, false);
    }

    private void displayCharts() {
	Log.d(this.getClass().getName(), "displayCharts");
	PHBtn.setBackgroundColor(PHColor);
	PHBtn.setTextColor(Color.BLACK);
	ORP.setBackgroundColor(ORPColor);
	ORP.setTextColor(Color.BLACK);
	seriesList.add("PH");
	seriesList.add("ORP");
	dataset.addSeries(PHSeries);
	dataset.addSeries(ORPSeries);

	// multiRenderer.setDisplayValues(true);
	multiRenderer.addSeriesRenderer(XrendererPH);
	multiRenderer.addSeriesRenderer(XrendererORP);
	multiRenderer.setYAxisMax(getMaxYValueList() + (int) (getMaxYValueList() * (20.0f / 100.0f)) + 2);
	multiRenderer.setPanLimits(new double[] { 0, j + 1, 0, getMaxYValueList() + (int) (getMaxYValueList() * (20.0f / 100.0f)) + 2 });
	// Creating an intent to plot line chart using dataset and
	// multipleRenderer
	mChart = ChartFactory.getLineChartView(getApplicationContext(), dataset, multiRenderer);
	chart_container.addView(mChart); // Adding click event to the Line Chart.

	mChart.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		// handle the click event on the chart
		Log.d(this.getClass().getName(), "mChart.setOnClickListener");

		SeriesSelection seriesSelection = mChart.getCurrentSeriesAndPoint();
		XrendererCond.setDisplayChartValues(false);
		XrendererTemp.setDisplayChartValues(false);
		XrendererChlr.setDisplayChartValues(false);
		XrendererORP.setDisplayChartValues(false);
		XrendererPH.setDisplayChartValues(false);
		String category = null;
		if (seriesSelection != null) {
		    // display information of the clicked point
		    String selection = seriesList.get(seriesSelection.getSeriesIndex());
		    if (selection.compareTo("Chlorine") == 0) {
			XrendererChlr.setDisplayChartValues(true);
			category = (int) seriesSelection.getValue() + " ml/cm";
		    }
		    else if (selection.compareTo("Conductivity") == 0) {
			XrendererCond.setDisplayChartValues(true);
			category = seriesSelection.getValue() + " ml/cm";

		    }
		    else if (selection.compareTo("Temp") == 0) {
			XrendererTemp.setDisplayChartValues(true);

			category = seriesSelection.getValue() + " \u2103";

		    }
		    else if (selection.compareTo("PH") == 0) {
			XrendererPH.setDisplayChartValues(true);
			category = seriesSelection.getValue() + "";
		    }
		    else if (selection.compareTo("ORP") == 0) {
			XrendererORP.setDisplayChartValues(true);
			category = seriesSelection.getValue() + " ml/cm";
		    }
		    Toast.makeText(getApplicationContext(), category, Toast.LENGTH_SHORT).show();
		}
		refreshCharts();
	    }
	});

	PHBtn.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		if (PHDisplay) {
		    PHBtn.setBackgroundColor(Color.GRAY);
		    PHBtn.setTextColor(Color.WHITE);
		    dataset.removeSeries(PHSeries);
		    multiRenderer.removeSeriesRenderer(XrendererPH);
		    PHDisplay = false;
		    removeMaxYValueList(maxPH);
		    seriesList.remove("PH");

		}
		else {// Adding data to the X Series.
		    seriesList.add("PH");
		    PHBtn.setBackgroundColor(PHColor);
		    PHBtn.setTextColor(Color.BLACK);
		    addMaxYValueList(maxPH);
		    dataset.addSeries(PHSeries);
		    multiRenderer.addSeriesRenderer(XrendererPH);
		    PHDisplay = true;
		}
		refreshCharts();
	    }
	});

	ORP.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		if (ORPDisplay) {
		    seriesList.remove("ORP");
		    ORP.setBackgroundColor(Color.GRAY);
		    ORP.setTextColor(Color.WHITE);
		    dataset.removeSeries(ORPSeries);
		    multiRenderer.removeSeriesRenderer(XrendererORP);
		    ORPDisplay = false;
		    removeMaxYValueList(maxORP);
		}
		else {// Adding data to the X Series.
		    seriesList.add("ORP");
		    ORP.setBackgroundColor(ORPColor);
		    ORP.setTextColor(Color.BLACK);
		    addMaxYValueList(maxORP);
		    dataset.addSeries(ORPSeries);
		    multiRenderer.addSeriesRenderer(XrendererORP);
		    ORPDisplay = true;
		}
		refreshCharts();
	    }
	});

	CondBtn.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		if (CondDisplay) {
		    seriesList.remove("Conductivity");
		    CondBtn.setBackgroundColor(Color.GRAY);
		    CondBtn.setTextColor(Color.WHITE);
		    dataset.removeSeries(CondSeries);
		    multiRenderer.removeSeriesRenderer(XrendererCond);
		    CondDisplay = false;
		    removeMaxYValueList(maxChl);
		}
		else {// Adding data to the X Series.
		    seriesList.add("Conductivity");
		    CondBtn.setBackgroundColor(CondColor);
		    CondBtn.setTextColor(Color.BLACK);
		    addMaxYValueList(maxChl);
		    dataset.addSeries(CondSeries);
		    multiRenderer.addSeriesRenderer(XrendererCond);
		    CondDisplay = true;
		}
		refreshCharts();
	    }
	});

	Temp.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		if (TempDisplay) {
		    seriesList.remove("Temp");
		    Temp.setBackgroundColor(Color.GRAY);
		    Temp.setTextColor(Color.WHITE);
		    dataset.removeSeries(TempSeries);
		    multiRenderer.removeSeriesRenderer(XrendererTemp);
		    TempDisplay = false;
		    removeMaxYValueList(maxTemp);
		}
		else {
		    seriesList.add("Temp");
		    Temp.setBackgroundColor(TempColor);
		    Temp.setTextColor(Color.BLACK);
		    addMaxYValueList(maxTemp);
		    dataset.addSeries(TempSeries);
		    multiRenderer.addSeriesRenderer(XrendererTemp);
		    TempDisplay = true;
		}
		refreshCharts();
	    }
	});

	Chlr.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		if (ChlrDisplay) {
		    seriesList.remove("Chlorine");
		    Chlr.setBackgroundColor(Color.GRAY);
		    Chlr.setTextColor(Color.WHITE);
		    dataset.removeSeries(ChlrSeries);
		    multiRenderer.removeSeriesRenderer(XrendererChlr);
		    ChlrDisplay = false;
		    removeMaxYValueList(maxChl);
		}
		else {
		    seriesList.add("Chlorine");
		    Chlr.setBackgroundColor(ChlrColor);
		    Chlr.setTextColor(Color.BLACK);
		    addMaxYValueList(maxChl);
		    dataset.addSeries(ChlrSeries);
		    multiRenderer.addSeriesRenderer(XrendererChlr);
		    ChlrDisplay = true;
		}
		refreshCharts();
	    }
	});
    }

    private void refreshCharts() {
	int min;
	if (j > 6) {
	    min = j - 6;
	}
	else {
	    min = 0;
	}
	multiRenderer.setXAxisMin(min);
	multiRenderer.setXAxisMax(j);
	multiRenderer.setYAxisMax(getMaxYValueList() + (int) (getMaxYValueList() * (20.0f / 100.0f)) + 1);
	multiRenderer.setPanLimits(new double[] { 0, j + 1, 0, getMaxYValueList() + (int) (getMaxYValueList() * (20.0f / 100.0f)) + 1 });
	mChart.refreshDrawableState();
	chart_container.refreshDrawableState();
	mChart.invalidate();
	chart_container.invalidate();
    }

    void loadData(String data) {
	Log.d(this.getClass().getName(), j + "   " + data);
	String[] substacles = data.split(",");

	double phValue = Double.parseDouble(substacles[1].split("\t")[1]);
	double conductivityValue = Double.parseDouble(substacles[2].split("\t")[1]);
	double tempValue = Double.parseDouble(substacles[3].split("\t")[1]);
	double orpValue = Double.parseDouble(substacles[4].split("\t")[1]);
	double chlorineValue = Double.parseDouble(substacles[5].split("\t")[1]);

	CondSeries.add(j, conductivityValue);
	if (maxCond < conductivityValue && CondDisplay) {
	    removeMaxYValueList(maxCond);
	    maxCond = conductivityValue;
	    addMaxYValueList(maxCond);
	}

	PHSeries.add(j, phValue);
	if (maxPH < phValue && PHDisplay) {
	    removeMaxYValueList(maxPH);
	    maxPH = phValue;
	    addMaxYValueList(maxPH);
	}

	ORPSeries.add(j, orpValue);
	if (maxORP < orpValue && ORPDisplay) {
	    removeMaxYValueList(maxORP);
	    maxORP = orpValue;
	    addMaxYValueList(maxORP);
	}

	ChlrSeries.add(j, chlorineValue);
	if (maxChl < chlorineValue && ChlrDisplay) {
	    removeMaxYValueList(maxChl);
	    maxChl = chlorineValue;
	    addMaxYValueList(maxChl);
	}

	TempSeries.add(j, tempValue);
	if (maxTemp < tempValue && TempDisplay) {
	    removeMaxYValueList(maxTemp);
	    maxTemp = tempValue;
	    addMaxYValueList(maxTemp);
	}
	if (j == 0) {// set the timestamp as beginning
	    String timeStart = substacles[0].split(" ")[1];
	    multiRenderer.addXTextLabel(j, timeStart.substring(0, timeStart.length() - 4));
	}
	else {
	    multiRenderer.addXTextLabel(j, j * 5 + "");
	}
	j++;

	if (writeFile.isChecked()) {
	    String saveSubstacles = substacles[0];// Set the time stamp
	    try {
		if (ORPDisplay) {
		    saveSubstacles += ", ORP\t" + orpValue;
		}
		if (PHDisplay) {
		    saveSubstacles += ", PH\t" + phValue;
		}
		if (CondDisplay) {
		    saveSubstacles += ", Conduactivity\t" + conductivityValue;
		}
		if (TempDisplay) {
		    saveSubstacles += ", Temperature\t" + tempValue;

		}
		if (ChlrDisplay) {
		    saveSubstacles += ", Chlorine\t" + chlorineValue;
		}
		myOutWriter.append(saveSubstacles);
	    }
	    catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    private void receiveUDP(final String ip) {
	final String[] text = new String[1];
	final byte[] message = new byte[1500];
	getThread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		try {
		    Log.d(this.getClass().getName(), "Receiving socket >>> at IP: " + ip + " and port: " + UrbanNetApp.server_port);
		    s = new DatagramSocket(UrbanNetApp.server_port);
		    DatagramPacket p = new DatagramPacket(message, message.length, InetAddress.getByName(ip), UrbanNetApp.server_port);
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
				    btnLayout.setVisibility(View.VISIBLE);
				}
				progressDialog.cancel();
				state = CONN_STATE.CONNECTED;
				loadData(text[0]);
				refreshCharts();
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
			btnLayout.setVisibility(View.GONE);
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
	    DatagramSocket socket = new DatagramSocket();
	    DatagramPacket p = new DatagramPacket(message, message.length, InetAddress.getByName(ip), UrbanNetApp.server_port);
	    Log.d(this.getClass().getName(), "Sending socket >>> at IP: " + ip + " and port: " + UrbanNetApp.server_port);
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


    private void removeMaxYValueList(double maxChl2) {
	maxYValueList.remove(maxChl2);
	Collections.sort(maxYValueList);
	Collections.reverse(maxYValueList);
    }

    private void addMaxYValueList(double maxPH2) {
	maxYValueList.add(maxPH2);
	Collections.sort(maxYValueList);
	Collections.reverse(maxYValueList);
    }

    private double getMaxYValueList() {
	if (!maxYValueList.isEmpty()) {
	    return maxYValueList.get(0);
	}
	return 35;
    }

    public String intToIp(int i) {

	return ((i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF));
    }

    private String broadcastNet(String ip) {
	String[] tem = ip.split("\\.");
	return (tem[0] + "." + tem[1] + "." + tem[2] + "." + "255");
    }

    @Override
    public void onStop() {
	super.onStop();
	EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }

    @Override
    public void onResume() {
	super.onResume();
	Log.d(this.getClass().getName(), "on Resume");
	progressDialog = ProgressDialog.show(LineChartsActivity.this, "", "Searching for sensors");
	wifiInfo = wifiManager.getConnectionInfo();
	if (wifiInfo.getBSSID() != null && wifiInfo.getNetworkId() != -1) {
	    lock = wifiManager.createMulticastLock("multicast");
	    lock.acquire();
	    lock.setReferenceCounted(true);
	    int ipAddress = wifiInfo.getIpAddress();
	    String ip = intToIp(ipAddress);
	    String lan = broadcastNet(ip);
	    initCharts();
	    displayCharts();
	    sendUDP(UrbanNetApp.server_IP);
	    receiveUDP(ip);
	}
	else {
	    progressDialog.cancel();
	    no_sensor.setText(R.string.socket_problem);
	    no_sensor.setVisibility(View.VISIBLE);
	    chart_container.setVisibility(View.GONE);
	    writeFile.setVisibility(View.GONE);
	    btnLayout.setVisibility(View.GONE);
	    state = CONN_STATE.NOT_CONNECTED;
	}
    }

    @Override
    public void onPause() {
	super.onPause();
	Log.d(this.getClass().getName(), "onPause");
	try {
	    dataset.clear();
	    multiRenderer.removeAllRenderers();
	    maxYValueList.clear();
	    chart_container.removeAllViews();
	    ChlrDisplay = TempDisplay = CondDisplay = false;
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
	catch (NullPointerException ne) {

	}
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	Log.d(this.getClass().getName(), "onResume");
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

}
