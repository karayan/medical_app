package gr.forth.ics.urbanNet.database;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Nikos Rapousis {@code SearchedPolygon} contains all the geo-points and name of the searched cities.
 */
@DatabaseTable(tableName = "geopoints")
public class SearchedPolygon implements Serializable {

    private static final long serialVersionUID = -5282188109528498967L;

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(columnName = "place")
    private String place;

    @DatabaseField(columnName = "polugon", dataType = DataType.SERIALIZABLE)
    private String polugon;

    SearchedPolygon() {
    }

    public SearchedPolygon(String place, ArrayList<GeoPoint> polugon) {
	this.place = place;
	if (polugon.isEmpty()) {
	    this.polugon = "";
	}
	if (polugon.size() > 2) {
	    this.polugon = polugon.toString().substring(1, polugon.toString().length() - 1).trim();
	}
    }

    /**
     * @return the place
     */
    public String getPlace() {
	return place;
    }

    /**
     * @param place the place to set
     */
    public void setPlace(String place) {
	this.place = place;
    }

    /**
     * @return the geo-point of the city
     */
    public ArrayList<GeoPoint> getPolygon() {
	ArrayList<GeoPoint> tem = new ArrayList<GeoPoint>();
	String[] geos = this.polugon.split(",");
	for (int i = 0; i < geos.length; i = i + 2) {
	    tem.add(new GeoPoint(Integer.parseInt(geos[i].trim()), Integer.parseInt(geos[i + 1].trim())));
	}
	return tem;
    }

    /**
     * @param set the geo-points of the city
     */
    public void setPolygon(ArrayList<GeoPoint> polugon) {
	this.polugon = polugon.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "GeoPoints [id=" + id + ", place=" + place + ", polugon=" + polugon + "]";
    }

}
