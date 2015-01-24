package gr.forth.ics.urbanNet.ui;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.android.maps.GeoPoint;
import com.google.gson.annotations.SerializedName;
//useful: http://bobbyprabowo.wordpress.com/2010/11/25/android-json-processing-using-gson-and-display-in-on-a-listview/
public class LocationModel {
	
	@SerializedName("found")
	private int found;
	
	@SerializedName("places")
	private Place [] places;
    
	public int getFound(){
    	return this.found;
    }
	public Place[] getPlaces(){
		return this.places;
	}
	/*public ArrayList<JsonGeoPoint> getPolygon(){
    	return this.polygon;
    }*/
	
	/*public void setPolygon(ArrayList<JsonGeoPoint> polygon) {
        this.polygon = polygon;
    }*/
	class Place {
		String title;
		JsonGeoPoint [] polygon;
		@Override
		public String toString() {
			return "Place [title=" + title + ", polygon="+ Arrays.toString(polygon) + "]";
		} 
	}
    class JsonGeoPoint{
    	@SerializedName("x")
    	double x;
    	@SerializedName("y")
    	double y;
		@Override
		public String toString() {
			return "JsonGeoPoint [x=" + x + ", y=" + y + "]";
		}
    }
    public ArrayList<GeoPoint> jsonPointToGeoPoint(JsonGeoPoint [] jsonGeoPoints){
    	ArrayList<GeoPoint> geoPointArrayList= new ArrayList<GeoPoint>();
    	for(int i=0;i<jsonGeoPoints.length;i++){
    		geoPointArrayList.add(new GeoPoint( (int) (jsonGeoPoints[i].y*1E6), (int) (jsonGeoPoints[i].x*1E6)));
		}
    	return geoPointArrayList;
    }
	@Override
	public String toString() {
		return "LocationModel [found=" + found + ", places="
				+ Arrays.toString(places) + "]";
	}
}