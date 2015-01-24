package gr.forth.ics.urbanNet.database;

import gr.forth.ics.urbanNet.network.Request;
import gr.forth.ics.urbanNet.network.Response;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The Query class corresponds to the "queries" table in the database. It represents the urbanNet queries that have been executed (sent to the server) in the past. It is used to
 * maintain a query history and allow the user to re-execute them easily.
 * @author katsarakis
 */
@DatabaseTable(tableName = "queries")
public class Query implements Serializable {
    /**
     * auto generated. Needed by Serializable interface
     */
    private static final long serialVersionUID = 3330027130932933921L;

    public static final int BEST_GSM_PROVIDER_BY_RSSI = 0;
    public static final int BEST_GSM_PROVIDER_BY_QoE = 1;
    public static final int BEST_PROVIDER_BY_QoE = 2;
    public static final int BEST_PROVIDER_BY_RSSI_SCORE = 3;
    public static final CharSequence[] queryType = { "GSM by RSSI", "GSM by QoE", "Customer score", "Sensor score" };

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(columnName = "query_geo_points")
    private String queryPolygon;

    @DatabaseField(columnName = "query_geo_points_name")
    private String queryPolygonName;

    @DatabaseField(columnName = "query_string")
    private int queryMode;

    @DatabaseField(columnName = "response_geo_points")
    private String responsePolygonString;

    @DatabaseField(columnName = "response_string")
    private String responseString;

    @DatabaseField(columnName = "is_favorite")
    private boolean isFavorite;

    @DatabaseField(columnName = "date")
    private Date date;
    private double androidDelay;
    private double networkDelay;
    private double serverDelay;
    private long untilTimestamp;

    /**
     * Below: Example of how relations are implemented using ORMLite m_organization, is a reference to another object in the application. ORMLite recognizes this as a foreign key
     * reference, and that the column actually refers to an id in another table. When you read a Person from the ORM, m_organization will refer to an Organization instance with
     * only its id field populated. You can populate the rest of the fields by calling refresh() on the organization DAO
     */
    // @DatabaseField(columnName = "organization_id", canBeNull = false)
    // private Organization m_organization;

    Query() {
	// needed by ormlite
    }

    /**
     * Constructor
     * @param queryGeoPoints
     * @param queryString
     */
    public Query(ArrayList<GeoPoint> queryGeoPoints, int queryMode) {
	setQueryPolygon(queryGeoPoints);
	this.queryPolygonName = null;
	this.queryMode = queryMode;
	this.date = new Date();
    }

    /**
     * Constructor
     * @param queryName
     * @param queryString
     */
    public Query(String name, int queryMode) {
	setQueryPolygonName(name);
	this.queryPolygon = null;
	this.queryMode = queryMode;
	this.date = new Date();
    }

    public static class ResponseString {
	float score;
	String name;
    }

    /**
     * @return the id
     */
    public int getId() {
	return id;
    }

    /**
     * @return The two-way network delay for the query request and response.
     */
    public double getNetworkDelay() {
	return networkDelay;
    }

    /**
     * @return The server delay for the execution of the query.
     */
    public double getServerDelay() {
	return serverDelay;
    }

    /**
     * @return the androidDelay
     */
    public double getAndroidDelay() {
	return androidDelay;
    }

    /**
     * Converts a List of GeoPoints to a String, in a proper format for the server.
     * @param queryGeoPoints
     */
    public void setQueryPolygon(ArrayList<GeoPoint> queryGeoPoints) {
	String queryPolygonString = "";
	for (GeoPoint queryGeoPoint : queryGeoPoints) {
	    queryPolygonString += queryGeoPoint.getLongitudeE6() / 1E6 + " " + queryGeoPoint.getLatitudeE6() / 1E6 + ", ";
	}
	queryPolygonString += queryGeoPoints.get(0).getLongitudeE6() / 1E6 + " " + queryGeoPoints.get(0).getLatitudeE6() / 1E6;
	this.queryPolygon = queryPolygonString;
    }

    /**
     * Set the qeuryPolygonName.
     * @param queryGeoPoints
     */
    public void setQueryPolygonName(String queryPolygonName) {
	this.queryPolygonName = queryPolygonName;
    }

    /**
     * GSM and WiFi providers and scores in hashmap structure
     * @param response
     * @return GSM and WiFi providers with score in hashmap structure
     */
    public HashMap<String, Float> responeResultGSM_WiFi() {
	HashMap<String, Float> hm = new HashMap<String, Float>();
	String respes = getResponseString();
	String[] lines = respes.split(System.getProperty("line.separator"));
	String[] split;

	for (int i = 0; i < lines.length; i++) {
	    if (lines[i].contains("GSM")) {
		split = lines[i].split("GSM");
		// Split[1]==provider
		// split[0]==score
		hm.put(split[1], Float.parseFloat(split[0]));

	    }
	    else if (lines[i].contains("VoIP")) {
		split = lines[i].split("VoIP");
		// Split[1]==provider
		// split[0]==score
		hm.put(split[1].trim(), Float.parseFloat(split[0].trim()));
	    }

	}
	Log.d("QoEService", hm.toString());
	return hm;
    }

    /**
     * @param response
     * @return GSM providers and score
     */
    public HashMap<String, Float> responeResultGSM() {
	HashMap<String, Float> hm = new HashMap<String, Float>();
	String respes = getResponseString();
	String[] lines = respes.split(System.getProperty("line.separator"));
	String[] split;

	for (int i = 0; i < lines.length; i++) {
	    if (lines[i].contains("GSM")) {
		split = lines[i].split("GSM");
		// Split[1]==provider
		// split[0]==score
		hm.put(split[1].trim(), Float.parseFloat(split[0].trim()));

	    }
	}
	Log.d("QoEService", hm.toString());
	return hm;
    }

    public HashMap<String, Float> responeResultWiFi() {
	HashMap<String, Float> hm = new HashMap<String, Float>();
	String respes = getResponseString();
	String[] lines = respes.split(System.getProperty("line.separator"));
	String[] split;

	for (int i = 0; i < lines.length; i++) {
	    if (lines[i].contains("VoIP")) {
		split = lines[i].split("VoIP");
		// Split[1]==provider
		// split[0]==score
		Log.d("QoEService", split[0].trim() + "  " + split[1].trim());
		hm.put(split[1].trim(), Float.parseFloat(split[0].trim()));
	    }

	}
	Log.d("QoEService", hm.toString());
	return hm;
    }

    /**
     * Call this function when the response from server has arrived.
     * @param responseGeoPoints Spatial part of query Response
     * @param responseString Text part of query Response
     */
    public void setResponsePolygonString(ArrayList<GeoPoint> responseGeoPoints, int queryMode) {
	// set responsePolygonString
	String responsePolygonString = "";
	for (GeoPoint responseGeoPoint : responseGeoPoints) {
	    responsePolygonString += responseGeoPoint.getLongitudeE6() / 1E6 + " " + responseGeoPoint.getLatitudeE6() / 1E6 + ", ";
	}
	responsePolygonString += responseGeoPoints.get(0).getLongitudeE6() / 1E6 + " " + responseGeoPoints.get(0).getLatitudeE6() / 1E6;
	this.responsePolygonString = responsePolygonString;
	// set responseString
	this.queryMode = queryMode;
    }

    /**
     * @return the untilTimestamp
     */
    public long getUntilTimestamp() {
	return untilTimestamp;
    }

    /**
     * @param untilTimestamp the untilTimestamp to set
     */
    public void setUntilTimestamp(long untilTimestamp) {
	this.untilTimestamp = untilTimestamp;
    }

    /**
     * @return the queryGeoPoints as ArrayList<GeoPoint>
     */
    public ArrayList<GeoPoint> getQueryGeoPoints() {
	ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
	String[] latlngs = this.queryPolygon.split(",");
	for (int i = 0; i < (latlngs.length - 1); i++) {
	    String[] tmp = latlngs[i].trim().split(" ");
	    geoPoints.add(new GeoPoint((int) (Double.parseDouble(tmp[1]) * 1E6), (int) (Double.parseDouble(tmp[0]) * 1E6)));
	}
	return geoPoints;
    }

    /**
     * @return the queryGeoPoints as ArrayList<GeoPoint>
     */
    public ArrayList<GeoPoint> getQueryGeoPointsName() {
	ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
	String[] latlngs = this.responsePolygonString.split(",");
	for (int i = 0; i < (latlngs.length - 1); i++) {
	    String[] tmp = latlngs[i].trim().split(" ");
	    geoPoints.add(new GeoPoint((int) (Double.parseDouble(tmp[1]) * 1E6), (int) (Double.parseDouble(tmp[0]) * 1E6)));
	}
	return geoPoints;
    }

    /**
     * @return the queryGeoPoints as String
     */
    public String getQueryPolygonString() {
	return queryPolygon;
    }

    /**
     * Converts the responsePolygonString to a List of GeoPoints
     * @return GeoPoints
     */
    public ArrayList<GeoPoint> getResponseGeoPoints() {
	ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
	String[] latlngs = this.responsePolygonString.split(",");
	for (int i = 0; i < (latlngs.length - 1); i++) {
	    String[] tmp = latlngs[i].trim().split(" ");
	    geoPoints.add(new GeoPoint((int) (Double.parseDouble(tmp[1]) * 1E6), (int) (Double.parseDouble(tmp[0]) * 1E6)));
	}
	return geoPoints;
    }

    /**
     * @return
     */
    public String getResponsePolygonString() {
	return responsePolygonString;
    }

    /**
     * @return the queryString
     */
    public int getQueryMode() {
	return queryMode;
    }

    /**
     * @return the responseString
     */
    public String getResponseString() {
	try {
	    String s = "";
	    DecimalFormat df = new DecimalFormat("0.00");
	    ResponseString[] responseStringList = (new Gson()).fromJson(responseString, ResponseString[].class);
	    for (ResponseString r : responseStringList) {
		s += df.format(r.score) + "\t" + r.name + "\n";
	    }
	    return s;
	}
	catch (JsonSyntaxException e) {
	    return responseString;
	}
    }

    public boolean isFavorite() {
	return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
	this.isFavorite = isFavorite;
    }

    public Date getDate() {
	return date;
    }

    /**
     * Returns a Response object, to use it in CommunicationHandler.getBestProvider()
     * @return response object
     */
    public Request getRequest() {
	Request request = new Request();
	if (this.queryPolygon == null && this.queryPolygonName != null) {// is a name polygon query
	    request.setValue("queryPolygonName", queryPolygonName);
	}
	else if (this.queryPolygon != null && this.queryPolygonName == null) {// is a polygon query
	    request.setValue("queryPolygon", queryPolygon);
	}
	else {
	    throw new IllegalArgumentException("No well formed query");
	}
	request.setValue("mode", queryMode);
	request.setValue("until", untilTimestamp);
	return request;
    }

    public void setResponseString(Response response) {
	responseString = response.getValue();
    }

    public void setResponseString(String string) {
	responseString = string;
    }

    public void setResponsePolygonString(String polygonstring) {
	responsePolygonString = polygonstring;
    }

    /**
     * @param androidDelay the androidDelay to set
     */
    public void setAndroidDelay(double androidDelay) {
	this.androidDelay = androidDelay;
    }

    public void setServerDelay(double serverDelay) {
	this.serverDelay = serverDelay;
    }

    public void setNetworkDelay(double networkDelay) {
	this.networkDelay = networkDelay;
    }

    /**
     * @return the queryPolygon
     */
    public String getQueryPolygon() {
	return queryPolygon;
    }

    /**
     * @param queryPolygon the queryPolygon to set
     */
    public void setQueryPolygon(String queryPolygon) {
	this.queryPolygon = queryPolygon;
    }

    /**
     * @return the queryPolygonName
     */
    public String getQueryPolygonName() {
	return queryPolygonName;
    }

    /**
     * @param queryMode the queryMode to set
     */
    public void setQueryMode(int queryMode) {
	this.queryMode = queryMode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "Query [id=" + id + ", queryPolygon=" + queryPolygon + ", queryPolygonName=" + queryPolygonName + ", queryMode=" + queryMode + ", responsePolygonString=" + responsePolygonString + ", responseString=" + responseString + ", isFavorite=" + isFavorite + ", date=" + date + ", androidDelay=" + androidDelay + ", networkDelay=" + networkDelay + ", serverDelay=" + serverDelay + ", untilTimestamp=" + new Date(untilTimestamp).toLocaleString() + "]";
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof Query) {
	    Query query = (Query) o;
	    return this.id == query.id;
	}
	return false;
    }
}
