package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.ui.ResponseItem.ResponseTYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author Giannis Apostolidis
 * @author Michalis Katsarakis
 * @author Nikolaos Rapousis
 * <p>
 * An object of this class is given a
 * <ul>
 * <li>String response following the format<br/>
 * <code>"[score]\t[service type]\t[network name]\n"</code></li>
 * <li>List<ScanResult> scanResults.</li>
 * </ul>
 * Its main functionality is to
 * <ol>
 * <li>parse the response string into network names</li>
 * <li>classify them according to their
 * <ul>
 * <li>type (i.e., GSM, WiFi)</li>
 * <li>current availability (i.e., included or not in the ScanResults)</li>
 * <li>configuration (e.g., authentication info remembered)</li>
 * <li>status (i.e., currently connected on, or not),</li>
 * </ul>
 * </li>
 * <li>create a corresponding AdapterView containing View items,</li>
 * <li>define appropriate onClick funtionality to these items.</li>
 * </ol>
 * </p>
 */
public class QueryResponse implements OnItemClickListener {
    private MapViewActivity mapViewActivity;
    private Context context;
    private ListView listView;
    private boolean isCurrLocInShownArea;
    boolean mob_avail;
    boolean wifi_avail;

    /** Array of query response lines. */
    private String[] data;

    /** Array of {@link ResponseItem} objects. */
    private ArrayList<ResponseItem> items;
    private ResponseListAdapter adapter;

    /**
     * Constructor method
     * <p>
     * The main functionality of this function is (a) to find the names of the networks at which the device is currently connected, (b)
     * </p>
     * @param mapViewActivity the parent New_MapViewActivity object.
     * @param context The context in which {@link Toast toasts} will be shown.
     * @param listView ListView object, which will contain the items with the recommended networks.
     * @param scanResults List of fresh {@link ScanResult} objects, to find the currently in-range networks.
     * @param response The query response from the urbanNet server.
     */
    public QueryResponse(MapViewActivity mapViewActivity, Context context, ListView listView, String response, boolean isCurrLocInShownArea) {
	this.mapViewActivity = mapViewActivity;
	this.context = context;
	this.listView = listView;
	WifiManager wifiManager = (WifiManager) mapViewActivity.getSystemService(Context.WIFI_SERVICE);
	wifi_avail = (wifiManager.getConnectionInfo().getNetworkId() != -1);
	ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	mob_avail = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();

	// Parse urbanNet server response
	if (response != null) {
	    Log.d(this.getClass().getName(), "String response = " + response);
	    if (response.contains("false") || response.contains("Search a larger region")) {
		items = new ArrayList<ResponseItem>();
		data = "No relevant data".split("\n");
		setResponse("");
		items.add(new ResponseItem(R.drawable.ic_new_info, "No recommendations available", "Search using a larger region", ResponseTYPE.ERROR));
	    }
	    else if (response.contains("org.bouncycastle")) {
		// Response with error from server,cause by time
		items = new ArrayList<ResponseItem>();
		data = "Adjust your time and date".split("\n");
		items.add(new ResponseItem(R.drawable.ic_new_info, data[0], "Go at phone Settings-> Date & time", ResponseTYPE.ERROR));
	    }
	    else if (response.contains("not set")) {
		// Response with error from server,cause by time
		items = new ArrayList<ResponseItem>();
		data = "Problem with the server".split("\n");
		items.add(new ResponseItem(R.drawable.ic_new_info, data[0], "Try again in 5 seconds", ResponseTYPE.ERROR));
	    }
	    else if (response.contains("cordinate.problem")) {
		items = new ArrayList<ResponseItem>();
		data = "Zoom problem".split("\n");
		items.add(new ResponseItem(R.drawable.ic_new_info, data[0], "Coordinates are out of range", ResponseTYPE.ERROR));
	    }
	    else if (response.contains("server.problem")) {
		items = new ArrayList<ResponseItem>();
		data = "Problem with the server".split("\n");
		items.add(new ResponseItem(R.drawable.ic_new_info, data[0], "The server is down, please try again later", ResponseTYPE.ERROR));
	    }
	    else {
		setResponse(response);
	    }
	}
	else if (wifi_avail == false && mob_avail == false) {
	    // Device has neither mobile nor WiFi connection to the Internet,
	    // and thus no urbanNet server response can be received
	    items = new ArrayList<ResponseItem>();
	    data = "No Internet connection.\n".split("\n");
	    items.add(new ResponseItem(android.R.drawable.stat_notify_error, data[0], "Established connection was lost", ResponseTYPE.ERROR));
	}

	// Create the appropriate ListView objects for the UI and set the
	// onClick listeners.
	adapter = new ResponseListAdapter(mapViewActivity, R.layout.providers_list, items);
	listView.setAdapter(adapter);
	listView.setOnItemClickListener(this);
    }

    public void setResponse(String response) {
	// response format: "[score]\t[service type]\t[name name]\n"
	data = response.split("\n");
	ArrayList<ResponseItem> responseList = new ArrayList<ResponseItem>();
	items = new ArrayList<ResponseItem>();
	String[] parseData;
	ResponseItem tem_item = null;
	for (int i = 0; i < data.length; i++) {
	    if (data[i].compareTo("") == 0) continue;
	    parseData = parsing(data[i]);
	    if (parseData[2].compareTo("TAP") == 0) {
		tem_item = new ResponseItem(R.drawable.tap_water, parseData[0], parseData[1], ResponseTYPE.TAP);
	    }
	    else if (parseData[2].compareTo("BOTTLE") == 0) {
		tem_item = new ResponseItem(R.drawable.bottle_water, parseData[0], parseData[1], ResponseTYPE.BOTTLE);
	    }
	    else if (parseData[2].compareTo("NATURAL") == 0) {
		tem_item = new ResponseItem(R.drawable.natural_water, parseData[0], parseData[1], ResponseTYPE.NATURAL);
	    }
	    responseList.add(tem_item);
	}

	final Comparator<ResponseItem> providerListComperatorSsid = new Comparator<ResponseItem>() {
	    @Override
	    public int compare(ResponseItem arg0, ResponseItem arg1) {
		return arg0.getTitle().compareTo(arg1.getTitle());
	    }
	};

	final Comparator<ResponseItem> providerListComperator = new Comparator<ResponseItem>() {
	    @Override
	    public int compare(ResponseItem arg0, ResponseItem arg1) {
		int comparement = ((int) ((arg1.getNumericRate() - arg0.getNumericRate()) * 1000));
		if (comparement == 0) {
		    return providerListComperatorSsid.compare(arg0, arg1);
		}
		else return comparement;
	    }
	};

	Collections.sort(responseList, providerListComperator);
	if (!responseList.isEmpty()) items.addAll(responseList);

    }

    /**
     * Parses the score of a network from a line of the query response<br/>
     * <code>"[Name]\t[score]\t[type]\n"</code><br/>
     * @param string
     * @return the rate of network
     */
    private String[] parsing(String string) {
	return string.split("\t");
    }

    public void clearAdapter() {
	adapter.clear();
    }

    public void refreshView() {
	listView.refreshDrawableState();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

    }
}
