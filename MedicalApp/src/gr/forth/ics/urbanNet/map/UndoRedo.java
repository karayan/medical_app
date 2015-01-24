package gr.forth.ics.urbanNet.map;


import java.util.ArrayList;
import java.util.Stack;

import com.google.android.maps.GeoPoint;

/**
 * implementation of undo/redo.
 * We keep in the undo array (stack) the moves history.
 * If someone fire the undo function, we remove the last move from the moves history
 * and we add it to the redo array (stack). If you make a new move the redo stack
 * is automatically erased. 
 * 
 * @author chrysohous
 *
 */

public class UndoRedo {

	private Stack<MoveAction> undo;
	private Stack<MoveAction> redo;
	private ArrayList<GeoPoint> geoPoints;
	
	public UndoRedo(ArrayList<GeoPoint> geoPoints){
		undo = new Stack<MoveAction>();
		redo = new Stack<MoveAction>();
		this.geoPoints = geoPoints;
	}
	
	public UndoRedo(ArrayList<GeoPoint> geoPoints, Stack<MoveAction> undo, Stack<MoveAction> redo){
		this.undo = undo;
		this.redo = redo;
		this.geoPoints = geoPoints;
	}
	
	public void updateMovePoint(GeoPoint from, GeoPoint to, int index){
		redo.removeAllElements();
		MoveAction move = new MovedPoint(from, to, index);
		undo.push(move);
	}
	
	public void updateAddPoint(GeoPoint at, int index){
		redo.removeAllElements();
		AddPoint move = new AddPoint(at, index);
		undo.push(move);
	}
	
	public boolean undo(){
		
		if(!canUndo()){
			return false;
		}
		
		MoveAction pop = undo.pop();
		pop.actionUndo(geoPoints);
	    redo.push(pop);
		return true;
	}
	
	public boolean redo(){
		
		if(!canRedo()){
			return false;
		}

		MoveAction pop = redo.pop();
		pop.actionRedo(geoPoints);
		undo.push(pop);
		return true;
	}
	
	public boolean canUndo(){
		return !undo.isEmpty();
	}
	
	public boolean canRedo(){
		return !redo.isEmpty();
	}
	
	public UndoRedo clone(ArrayList<GeoPoint> geoPoints){

		Stack<MoveAction> undo = new Stack<MoveAction>();
		for( MoveAction i: this.undo ){
			undo.add(i.clone());
		}

		Stack<MoveAction> redo = new Stack<MoveAction>();
		for( MoveAction i: this.redo ){
			redo.add(i.clone());
		}
		
		return new UndoRedo( geoPoints, undo, redo);
	}
}
