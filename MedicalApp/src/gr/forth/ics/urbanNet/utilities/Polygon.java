package gr.forth.ics.urbanNet.utilities;

import java.util.ArrayList;

import android.graphics.Point;

public class Polygon {
	
	public static ArrayList<Point> getPolygon(Point center, float r, int vertexCount, double startAngle){
		
		ArrayList<Point> polygonPoints = new ArrayList<Point>();
		double addAngle = 2 * Math.PI / vertexCount;
		double angle = startAngle;
		for (int i = 0; i < vertexCount; i++) {
			polygonPoints.add(new Point((int) Math.round(r * Math.cos(angle)) + center.x, (int) Math.round(r * Math.sin(angle)) + center.y));
			angle += addAngle;
		}
		
		return polygonPoints;
		
	}

}
