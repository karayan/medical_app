package gr.forth.ics.urbanNet.network;

/**
 * Author Nikos Rapousis
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

//@author Nikos Rapousis
public class AutoNetwork {
    int MIN_RSSI = -100;
    int MAX_RSSI = -45;
    private WifiManager wifiManager;
    private List<WifiConfiguration> savedPasswords;
    private List<String> configNetworks = new ArrayList<String>();
    private List<String> confList;
    private List<ScanResult> wifiList;
    private Context context;
    HashMap<String, Integer> wifiListId;

    public AutoNetwork(WifiManager wifimanager, Context context) {
	this.wifiManager = wifimanager;
	this.savedPasswords = getListSavedPass(wifimanager);
	this.configNetworks = configured_SSID();
	this.context = context;
    }

    // --------------------------function Member----------------------//

    /**
     * Set the Wifimanager
     * @param wifiManager
     */
    public void setWifiManager(WifiManager wifiManager) {
	this.wifiManager = wifiManager;
    }

    /**
     * Set the configured networks
     * @param savedPasswords
     */
    public void setListSavedPass(List<WifiConfiguration> savedPasswords) {
	this.savedPasswords = savedPasswords;
    }

    /**
     * @param wifiManager
     * @return the names of saved networks
     */
    public List<WifiConfiguration> getListSavedPass(WifiManager wifiManager) {
	return wifiManager.getConfiguredNetworks();
    }

    /**
     * Set the configured connections
     * @param mylist
     */
    public void setListSSID(List<String> mylist) {
	this.configNetworks = mylist;
    }

    /**
     * @return The current connection signal rssi
     */
    public int getSignalStrength() {
	return wifiManager.getConnectionInfo().getRssi();
    }

    /**
     * @param networks_umap
     * @param appPrefs
     * @return true if new connection establish,false otherwise
     * @throws IOException
     */
    public boolean check_to_disconnect(HashMap<String, Float> networks_umap, SharedPreferences appPrefs) throws IOException {
	int choice = appPrefs.getInt("network", 0);
	int rate = appPrefs.getInt("rate", 0);

	// SAVED NETWORKS IN WIFICONFIGUARATION MODE
	savedPasswords = wifiManager.getConfiguredNetworks();
	// TAKE A LIST OF CONFIGURATED NETWORKS
	confList = configured_SSID();
	// networks to connect on the area
	wifiList = wifiManager.getScanResults();
	switch (choice) {
	case 1:
	    return connectByCost(networks_umap, wifiList, savedPasswords, confList);
	case 2: {
	    if (rate == 100) {
		return connectByQoE(networks_umap, wifiList, savedPasswords, confList);
	    }
	    else if (rate == 0) {
		return connectByRSSI(wifiList, savedPasswords, confList);
	    }
	    else {
		return connectByQoE_RSSI(rate, networks_umap, wifiList, savedPasswords, confList);
	    }
	}
	case 3:
	    return connectByNOQoE(networks_umap, wifiList, savedPasswords, confList);
	default:
	    return connectByRSSI(wifiList, savedPasswords, confList);
	}

    }

    /**
     * @param networks_umap
     * @param wifiList
     * @param listOfsaved
     * @param confList
     * @return true if new connection establish,false otherwise
     */
    public boolean connectByCost(HashMap<String, Float> networks_umap, List<ScanResult> wifiList, List<WifiConfiguration> listOfsaved, List<String> confList) {
	return true;
    }

    /**
     * connect from the best to worst QoE based on urbanNet response query. If all these fail then connects to the best RSSI.
     * @param networks_umap
     * @param wifiList
     * @param ListOfsaved
     * @param confList
     * @return true if new connection establish,false otherwise
     */

    public boolean connectByQoE(HashMap<String, Float> networks_umap, List<ScanResult> wifiList, List<WifiConfiguration> ListOfsaved, List<String> confList) {
	Log.d(this.getClass().getName(), "connectByQoE");
	Set<String> networks = networks_umap.keySet();
	String network;
	boolean connEstablished = false;
	Iterator<String> iterator = networks.iterator();
	int i = 0, posSavedNetwork, posScanResult = getnet_ID();
	List<String> SSIDWifiArea = new ArrayList<String>();
	while (i < wifiList.size()) {
	    SSIDWifiArea.add(wifiList.get(i).SSID);
	    i++;
	}

	while (iterator.hasNext() && connEstablished == false) {
	    network = iterator.next();
	    posSavedNetwork = confList.indexOf(network);
	    posScanResult = SSIDWifiArea.indexOf(network);
	    if (posScanResult != -1 && (posSavedNetwork != -1 || isSecured(wifiList.get(posScanResult)) == false)) {
		if (isSecured(wifiList.get(posScanResult))) {
		    connEstablished = connectToNetwork(ListOfsaved.get(posSavedNetwork));

		}
		else {
		    WifiConfiguration freenet = new WifiConfiguration();
		    freenet.SSID = "\"" + wifiList.get(posScanResult).SSID + "\"";
		    freenet.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		    connEstablished = connectToNetwork(freenet);
		}
	    }

	}
	if (!connEstablished) {
	    return connectByRSSI(wifiList, ListOfsaved, confList);
	}
	return true;
    }

    /**
     * connect from the best to worst on networks that are not contained in QoE list of urbanNet response query. Discover new networks If all these fail then connects to the best
     * RSSI.
     * @param networks_umap
     * @param wifiList
     * @param ListOfsaved
     * @param confList
     * @return true if new connection establish,false otherwise
     */

    public boolean connectByNOQoE(HashMap<String, Float> networks_umap, List<ScanResult> wifiList, List<WifiConfiguration> ListOfsaved, List<String> confList) {
	Log.d(this.getClass().getName(), "connectByNOQoE");
	Set<String> networks = networks_umap.keySet();
	ScanResult net;
	boolean connEstablished = false;
	int i = 0, posSavedNetwork;
	int netId = getnet_ID();
	boolean boolScanResult;

	while (i < wifiList.size() && connEstablished == false) {
	    net = wifiList.get(i);
	    posSavedNetwork = confList.indexOf(net.SSID);
	    boolScanResult = networks.contains(net.SSID);

	    if (boolScanResult == false && (posSavedNetwork != -1 || isSecured(net) == false)) {

		if (isSecured(net)) {
		    connEstablished = connectToNetwork(ListOfsaved.get(posSavedNetwork));

		}
		else {
		    WifiConfiguration freenet = new WifiConfiguration();
		    freenet.SSID = "\"" + net.SSID + "\"";
		    freenet.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		    connEstablished = connectToNetwork(freenet);
		}
	    }
	    i++;
	}
	if (!connEstablished) {
	    return connectByRSSI(wifiList, ListOfsaved, confList);
	}
	return connEstablished;
    }

    /**
     * connect from best to worst RSSI network.
     * @param net
     * @param ListOfsaved
     * @param wifiList
     * @param confList
     * @return true if new connection establish,false otherwise
     */

    public boolean connectByRSSI(List<ScanResult> wifiList, List<WifiConfiguration> ListOfsaved, List<String> confList) {
	Log.d(this.getClass().getName(), "connectByRSSI");
	int cursignal = getSignalStrength();
	int netId = getnet_ID();
	int pos, i = 0, ssignal;
	ScanResult net;
	boolean connEstablished = false;
	while (i < wifiList.size() && connEstablished == false) {
	    net = wifiList.get(i);
	    ssignal = net.level;
	    pos = confList.indexOf(net.SSID);
	    // if better rssi and is on configured or is free then try to connect
	    if ((ssignal > cursignal) && (pos != -1 || isSecured(net) == false)) {

		if (isSecured(net)) {
		    connEstablished = connectToNetwork(ListOfsaved.get(pos));

		}
		else {
		    WifiConfiguration freenet = new WifiConfiguration();
		    freenet.SSID = "\"" + net.SSID + "\"";
		    freenet.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		    connEstablished = connectToNetwork(freenet);

		}
		Log.d(this.getClass().getName(), "Connection based on RSSI result establish: " + connEstablished);
	    }
	    i++;
	}
	return connEstablished;
    }

    /**
     * connect from best to worst RSSI network.
     * @param net
     * @param ListOfsaved
     * @param wifiList
     * @param confList
     * @return true if new connection establish,false otherwise
     */

    public boolean connectBestRSSI() {
	savedPasswords = wifiManager.getConfiguredNetworks();
	// TAKE A LIST OF CONFIGURATED NETWORKS
	confList = configured_SSID();
	// networks to connect on the area
	wifiList = wifiManager.getScanResults();
	Log.d(this.getClass().getName(), "connectBestRSSI");
	int cursignal = getSignalStrength();
	int netId = getnet_ID();
	int pos, i = 0, ssignal;
	ScanResult net;
	boolean connEstablished = false;
	while (i < wifiList.size() && connEstablished == false) {
	    net = wifiList.get(i);
	    ssignal = net.level;
	    pos = confList.indexOf(net.SSID);
	    Log.d(this.getClass().getName(), net.SSID + "RSSI" + ssignal);
	    // if better rssi and is on configured or is free then try to connect
	    if ((ssignal > cursignal) && (pos != -1 || isSecured(net) == false)) {

		if (isSecured(net)) {
		    connEstablished = connectToNetwork(savedPasswords.get(pos));

		}
		else {
		    WifiConfiguration freenet = new WifiConfiguration();
		    freenet.SSID = "\"" + net.SSID + "\"";
		    freenet.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		    connEstablished = connectToNetwork(freenet);

		}
	    }
	    i++;
	}
	return connEstablished;
    }

    /**
     * Connect from best to worst mixed scored network.The mixed score is percentage of QoE and RSSI.
     * @param networks_umap
     * @param ListOfsaved
     * @param wifiList
     * @param confList
     * @return true if new connection establish,false otherwise
     */

    public boolean connectByQoE_RSSI(int rate, HashMap<String, Float> networks_umap, List<ScanResult> wifiList, List<WifiConfiguration> ListOfsaved, List<String> confList) {
	Log.d(this.getClass().getName(), "connect ByQoE & RSSI");
	Set<String> networks = networks_umap.keySet();
	float score, oldscore;
	float newrate = 2.5f;
	float oldrate = 2.5f;
	String netname = get_SSID();
	int pos, i = 0;
	int netId = getnet_ID();
	ScanResult net = null;
	boolean connEstablished = false;
	Map<ScanResult, Float> scoreNetwork = new HashMap<ScanResult, Float>();

	// Check if there is rate from urbanNet for the connect network
	if (networks.contains(netname)) {
	    oldrate = networks_umap.get(netname);

	}
	oldscore = oldrate * rate / 100 + calculateSignalLevel(getSignalStrength(), 5) * (100 - rate) / 100;
	while (i < wifiList.size()) {
	    net = wifiList.get(i);
	    // Check if there is rate from urbanNet for the connect network
	    if (networks.contains(net.SSID)) {
		newrate = networks_umap.get(net.SSID);
		score = (newrate * rate / 100 + calculateSignalLevel(net.level, 5) * (100 - rate) / 100);
	    }
	    else {
		score = (newrate * rate / 100 + calculateSignalLevel(net.level, 5) * (100 - rate) / 100);
	    }
	    pos = confList.indexOf(net.SSID);
	    // if the connection possible add
	    if ((pos != -1 || isSecured(net) == false)) {
		scoreNetwork.put(net, Math.abs(score));
	    }
	    i++;
	}
	SortedSet<Entry<ScanResult, Float>> sortValue = entriesSortedByValues(scoreNetwork);
	// if better score and is on configured or is free then try to connect
	Entry<ScanResult, Float> bestScoredNetwork = sortValue.first();

	while (oldscore < bestScoredNetwork.getValue() && connEstablished == false) {

	    if (isSecured(bestScoredNetwork.getKey())) {
		pos = confList.indexOf(bestScoredNetwork.getKey().SSID);
		connEstablished = connectToNetwork(ListOfsaved.get(pos));

	    }
	    else {
		WifiConfiguration freenet = new WifiConfiguration();
		freenet.SSID = "\"" + bestScoredNetwork.getKey().SSID + "\"";
		freenet.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		connEstablished = connectToNetwork(freenet);
	    }
	    sortValue.remove(bestScoredNetwork);
	    bestScoredNetwork = sortValue.first();
	}
	return connEstablished;
    }

    /**
     * @return return the result of disconnection
     */
    public boolean disconnect_WiFi() {
	return wifiManager.disconnect();

    }

    /**
     * @return SSID of current connected network
     */
    public String get_SSID() {
	return wifiManager.getConnectionInfo().getSSID();

    }

    /**
     * @return unique network id
     */
    public int getnet_ID() {
	return wifiManager.getConnectionInfo().getNetworkId();
    }

    /**
     * @return configured networks names
     */
    public List<String> configured_SSID() {
	for (int j = 0; j < savedPasswords.size(); j++) {
	    String ssid = savedPasswords.get(j).SSID.substring(1, savedPasswords.get(j).SSID.length() - 1);
	    configNetworks.add(ssid);
	}
	return configNetworks;
    }

    public HashMap<String, Integer> getConfiguredWiFi() {
	List<WifiConfiguration> listWifi = wifiManager.getConfiguredNetworks();
	HashMap<String, Integer> list = new HashMap();
	for (WifiConfiguration i : listWifi) {
	    list.put(i.SSID, i.networkId);
	}
	return list;
    }

    /**
     * Connect at the wifiConfiguation if possible
     * @param wifiConfiguration
     * @return True if connection establish
     */
    public boolean connectToNetwork(WifiConfiguration wifiConfiguration) {
	String SSID;
	// And finally, you might need to enable it, so Android conntects to it:
	this.wifiListId = getConfiguredWiFi();
	if (!wifiListId.containsKey(wifiConfiguration.SSID)) {
	    int Id = wifiManager.addNetwork(wifiConfiguration);
	    this.wifiListId.put(wifiConfiguration.SSID, Id);
	}
	boolean enabled = wifiManager.enableNetwork(wifiListId.get(wifiConfiguration.SSID).intValue(), true);
	ConnectivityManager connMgr = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	if (networkInfo != null) {
	    Log.d(this.getClass().getName(), "isConnectedOrConnecting " + networkInfo.isConnectedOrConnecting());
	}
	return enabled;
    }

    private String getWifiConfSSID(WifiConfiguration wifiConfiguration) {
	return wifiConfiguration.SSID.replace("\"", "").trim();
    }

    /**
     * @param scanResult
     * @return true if secured network
     */
    public boolean isSecured(ScanResult scanResult) {
	final String cap = scanResult.capabilities;
	final String[] securityModes = { "WEP", "PSK", "EAP", "TKIP" };

	for (int i = 0; i < securityModes.length; i++) {
	    if (cap.contains(securityModes[i])) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Sort by value the HashMaps.
     * @param map
     * @return Map sorted by value.
     */
    static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
	SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(new Comparator<Map.Entry<K, V>>() {
	    @Override
	    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
		int res = e1.getValue().compareTo(e2.getValue());
		return res != 0 ? res : 1; // Special fix to preserve
					   // items with equal values
	    }
	});
	sortedEntries.addAll(map.entrySet());
	return sortedEntries;
    }

    public int calculateSignalLevel(int rssi, int numLevels) {
	if (rssi <= MIN_RSSI) {
	    return 0;
	}
	else if (rssi >= MAX_RSSI) {
	    return numLevels - 1;
	}
	else {
	    float inputRange = (MAX_RSSI - MIN_RSSI);
	    float outputRange = (numLevels - 1);
	    if (inputRange != 0) return (int) ((rssi - MIN_RSSI) * outputRange / inputRange);
	}
	return 0;
    }
}
