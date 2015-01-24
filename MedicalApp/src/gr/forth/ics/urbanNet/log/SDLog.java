package gr.forth.ics.urbanNet.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

/**
 * Writes performance test data in sdCard
 * 
 * @author syntych
 *
 */
public class SDLog {
	
	public static final int SEND_DATA_FILE=0;
	public static final int SEND_QUERIES_FILE=1;

    static File logFile[] = null;
    static PrintStream logTo[] = null;
	
    public synchronized static boolean reset(boolean append){
    	
    	logFile = new File[2];
    	logTo = new PrintStream[2];
    	
    	try {
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/umap_performance");
			dir.mkdirs();
			
			logFile[SEND_DATA_FILE] = new File(dir, "sendData_log.txt");
			logTo[SEND_DATA_FILE] = new PrintStream(new FileOutputStream(logFile[SEND_DATA_FILE]), append);
			logFile[SEND_QUERIES_FILE] = new File(dir, "sendQueries_log.txt");
			logTo[SEND_QUERIES_FILE] = new PrintStream(new FileOutputStream(logFile[SEND_QUERIES_FILE]), append);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    	
    }
    
    public static synchronized void writeDate(int i){
    	
    	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss ");
    	String date = dateFormat.format(new Date());
    	
    	logTo[i].println("\n%{ "+ date +" %}");
    	
    }
    
    /**
     * Writes a string followed by a new line character, both in logfile and in System.out
     * @param the object which sends the message, usually "this"
     * @param msg the message to be displayed 
     */
    public static void fn(Object obj, String msg, int i) {
    	System.out.println("Class: " + obj.getClass().getSimpleName() + ": " + msg);
		fn(msg, i);
    }
    
    public static void fn(Object obj, String msg, boolean printDate, int i) {
    	System.out.println("Class: " + obj.getClass().getSimpleName() + ": " + msg);
		fn(msg, printDate, i);
    }
    
    public synchronized static void fn(String msg, int i) {
    	logTo[i].println(msg);
    }
    
    /**
     * Writes a string followed by a new line character, only in logfile
     * @param msg the message to be displayed 
     */
    public synchronized static void fn(String msg, boolean printDate, int i) {
    	
    	String date = "";
		
    	if(printDate){
    		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss ");
        	date = dateFormat.format(new Date());
    	}
    
		logTo[i].println(date + msg);
		
    }
    
    /**
     * Writes a string followed by a space character, both in logfile and in System.out
     * @param the object which sends the message, usually "this"
     * @param msg the message to be displayed 
     */
    public static void f(Object obj, String msg, int i) {
    	System.out.println("Class: " + obj.getClass().getSimpleName() + ": " + msg);
		f(msg, i);
    }
    
    /**
     * Writes a string followed by a space character, only in logfile
     * @param msg the message to be displayed 
     */
    public synchronized static void f(String msg, int i) {
		logTo[i].print(msg + " ");
    }
    
    /**
     * Writes a string, only in System.out
     * @param the object which sends the message, usually "this"
     * @param msg the message to be displayed 
     */
	public static void d(Object obj, String msg){
		System.out.println("Class: " + obj.getClass().getSimpleName() + ": " + msg);
	}
	
}
