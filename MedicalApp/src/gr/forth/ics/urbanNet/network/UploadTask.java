package gr.forth.ics.urbanNet.network;

import gr.forth.ics.urbanNet.database.BatteryMeasurement;
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.database.Feedback;
import gr.forth.ics.urbanNet.database.Question;
import gr.forth.ics.urbanNet.database.SceintFeedback;
import gr.forth.ics.urbanNet.log.SDLog;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.utilities.CircularByteBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * This class defines the procedure of uploading a stand-alone dataset to the
 * urbanNet server. Each object can be executed only once, by calling its
 * start() method. The procedure is implemented using 3 threads that run in
 * parallel: a) {@link UploadTask#dbReader} retrieves data from the database and
 * writes them in a circular buffer, namely, {@link UploadTask#ccb}, b)
 * {@link UploadTask#poster} reads data from the {@link UploadTask#ccb} and
 * sends them to the server using an HTTP POST entity, and c)
 * {@link UploadTask#dbUpdater} waits for the first two threads to finish, and
 * then marks the tuples that were uploaded, so that they won't be uploaded
 * again. <h3>How to use this class</h3>
 * <ol>
 * <li>Implement a ResultListener.</li>
 * <li>Call createInstance(...) and assign the returned object on a variable.</li>
 * <li>Call start() on the returned {@link UploadTask} object.</li>
 * </ol>
 * <p>
 * During the execution of the UploadTask, a WakeLock and a WiFi lock are
 * recommended to be acquired.
 * </p>
 */
public class UploadTask {
	/**
	 * How many objects will be retrieved by each execution of a prepared query.
	 */
	private static final long QUERY_LIMIT = 100;

	/**
	 * Path of the directory where JSON files will be written by writeFile(...)
	 * defined in {@link UploadTask#poster} or red by the readFile(...) defined
	 * in {@link UploadTask#dbReader}.
	 */
	private static final String FILES_PREFIX = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/urbanNet/json_datasets/";

	/**
	 * Instances of this class can be thrown during the execution of the
	 * uploading procedure. Examples where this type of exception is thrown, are
	 * a) when the number of updated objects is different from the number of
	 * uploaded objects for a db class, and b)when the start() method is called
	 * twice on the same {@link UploadTask} instance.
	 */
	public static class UploadTaskException extends Exception {
		private static final long serialVersionUID = -608255686532453081L;

		UploadTaskException() {
			super();
		}

		UploadTaskException(String msg) {
			super(msg);
		}
	}

	/**
	 * Each entity that wants to create an {@link UploadTask} object needs to
	 * implement this interface. It is used to notify that entity about the
	 * outcome of the uploading. If everything goes well, the onSuccess() method
	 * will be called. If an exception is caught, the onError(...) method will
	 * be called, also passing the caught exception.
	 */
	public interface ResultListener {
		void onSuccess();

		void onError(Exception e);
	}

	/**
	 * Class modeling the relations between database tables. A tuple indicates
	 * that the class {@link Relation#dbClass} has a column named
	 * {@link Relation#foreignCol} that is a foreign key to a
	 * {@link Relation#foreignClass} object.
	 * <p>
	 * Objects of the {@link Relation} class are stored in the
	 * {@link UploadTask#RELATIONS} array.
	 * </p>
	 */
	static class Relation {
		Class dbClass;
		Class foreignClass;
		String foreignCol;

		public Relation(Class dbClass, Class foreignDbClass,
				String foreignDbTable) {
			this.dbClass = dbClass;
			this.foreignClass = foreignDbClass;
			this.foreignCol = foreignDbTable;
		}
	}

	/**
	 * Constant array of {@link Relation} objects; one for each database class.
	 * This array helps retrieving a number of tuples from a db table using an
	 * id range, if the table has no foreign keys (e.g., Trace, Question), or
	 * only the tuples that refer already-written-in-ccb foreign objects (e.g.,
	 * only WifiTraces that refer already-written-in-ccb Traces). Foreign key
	 * hierarchy: 1.Trace | +-Session | | | +-GsmCall | | | +-SipdroidCall | | |
	 * +-RtpArrival | +-RtpDeparture | +-PingPongRtt | +-RtpPacketLossEvent |
	 * +-CellularTrace | | | +-CellularNetworkOperator | +-WifiTrace
	 * 2.BatteryMeasurement 3.Question
	 */
	private static final Relation[] RELATIONS = {
			// new Relation(Trace.class, null, null),
			// new Relation(WifiTrace.class, Trace.class, "trace"),
			// new Relation(CellularTrace.class, Trace.class, "trace"),
			// new Relation(CellularNetworkOperator.class, null, null),

			// Session doesn't have fkey constraints, but the objs to be
			// uploaded
			// are defined while iterating through the Trace objs.
			// new Relation(Session.class, Session.class, "id"),
			// new Relation(GsmCall.class, Session.class, "session"),
			// new Relation(SipdroidCall.class, Session.class, "session"),

			// new Relation(RtpArrival.class, SipdroidCall.class,
			// "sipdroid_call"),
			// new Relation(RtpDeparture.class, SipdroidCall.class,
			// "sipdroid_call"),
			// new Relation(PingPongRtt.class, SipdroidCall.class,
			// "sipdroid_call"),
			// new Relation(RtpPacketLossEvent.class, SipdroidCall.class,
			// "sipdroid_call"),

//			new Relation(BatteryMeasurement.class, null, null),
			new Relation(Question.class, null, null),
//			new Relation(Feedback.class, null, null),
//			new Relation(SceintFeedback.class, null, null), 
			};

	/**
	 * Only one instance is allowed to run
	 */
	private static UploadTask currentInstance;

	/** Used to perform database operations. */
	private DatabaseHelper dbHelper;

	/** Used to communicate with the server. */
	private CommunicationHandler com;

	/**
	 * Listener to inform when the uploading is complete, or if an error occurs.
	 */
	private ResultListener resultListener;

	/**
	 * <em>true</em> if this instance was created by an automatic decision for
	 * upload, or <em>false</em> if it was triggered by a user action, through
	 * the GUI.
	 */
	private boolean autoUpload;

	/**
	 * When the {@link UploadTask#start()} method of this instance is called,
	 * this property becomes true. Calling the start() method again will cause
	 * the throwing of an UploadTaskException.
	 */
	private boolean alreadyExecuted;

	/** Timestamps in ms denoting the beginning and the end of the execution. */
	private long tStart, tEnd;

	/**
	 * The new value to be assigned on the "uploaded" columns of the db tables.
	 * This assignment will be done by the {@link UploadTask#dbUpdater} thread.
	 */
	private volatile int uploadedFieldNewValue;

	/**
	 * Counts how many times the {@link UploadTask#dbUpdater} thread has been
	 * interrupted. On the 2nd interruption, it starts updating the database.
	 */
	private volatile int myInterruptionCount;

	/**
	 * This circular buffer has an InputStream and an OutputStream. The
	 * {@link UploadTask#dbReader} writes using the OutputStream and at the same
	 * time the {@link UploadTask#poster} reads using the InputStream.
	 */
	private CircularByteBuffer ccb;

	/** {@link Gson} object for JSON serialization */
	private Gson gson;

	/**
	 * Structure with ids that have are included in the JSON Trace: {1,2,3,
	 * ...}, CellularTrace: {1,2,3, ...}, ...
	 */
	private volatile HashMap<Class, TreeSet<Long>> idMap;

	/**
	 * Class for easy preparing and executing of parameterized SELECT queries on
	 * each database class. <h3>SQL query:</h3> SELECT * FROM <Class> table
	 * WHERE uploaded= {@link DatabaseHelper#NOT_UPLOADED} AND id >= :minIdArg
	 * AND id < :maxIdArg ORDER BY id ASC
	 */
	class PrepSelectQuery {
		Dao dao;
		PreparedQuery q;
		String foreignColName;
		SelectArg minIdArg, maxIdArg, foreignIdsArg;

		PrepSelectQuery(Dao dao, Class c, String foreignColName)
				throws SQLException {
			this.dao = dao;
			this.foreignColName = foreignColName;
			QueryBuilder qb = dao.queryBuilder();
			if (foreignColName == null) {
				minIdArg = new SelectArg();
				maxIdArg = new SelectArg();
				qb.orderBy("id", true).where()
						.eq("uploaded", DatabaseHelper.NOT_UPLOADED).and()
						.ge("id", minIdArg).and().lt("id", maxIdArg);
			} else {
				foreignIdsArg = new SelectArg();
				foreignIdsArg.setValue(new ArrayList<Long>());// TODO: delete
				// This type of prepared query does not work. Don't know why.
				// query() will prepare and execute a new query.
				qb.orderBy("id", true).where()
						.eq("uploaded", DatabaseHelper.NOT_UPLOADED).and()
						.in(foreignColName, foreignIdsArg);
			}
			this.q = qb.prepare();
			// Log.d(this.getClass().getName(), q.getStatement());
		}

		List<Object> query(Object[] argv) throws SQLException,
				IllegalArgumentException {
			if (argv.length == 2) {
				minIdArg.setValue(argv[0]);
				maxIdArg.setValue(argv[1]);
			} else if (argv.length == 1) {
				List<Long> fIds = ((List<Long>) argv[0]);
				foreignIdsArg.setValue(fIds); // foreignIdsArg.setValue(argv[0]);
				// Log.d(this.getClass().getName(),
				// "foreignIdsArg.getSqlArgValue()= "+foreignIdsArg.getSqlArgValue().toString());
				return dao.query(dao.queryBuilder().orderBy("id", true).where()
						.eq("uploaded", DatabaseHelper.NOT_UPLOADED).and()
						.in(foreignColName, fIds).prepare());
			} else {
				throw new IllegalArgumentException(
						"Wrong size of argument vector");
			}
			return dao.query(q);
		}
	}

	/** Structure holding a prepared SELECT query for every table. */
	private HashMap<Class, PrepSelectQuery> prepdSelectMap;

	/**
	 * Class for easy preparing and executing of parameterized UPDATE queries on
	 * each database class. <h3>SQL query:</h3> UPDATE <Class> table SET
	 * uploaded= :uploaded WHERE id IN ( :ids )
	 */
	class PrepUpdateQuery {
		Dao dao;

		// PreparedUpdate u;
		// SelectArg idsArg, uploadedArg;

		PrepUpdateQuery(Dao dao, Class c) throws SQLException {
			this.dao = dao;
			UpdateBuilder<Object, Integer> ub = dao.updateBuilder();
			// idsArg= new SelectArg();
			// uploadedArg= new SelectArg();
			// ub.updateColumnValue("uploaded", uploadedArg).where().eq("id",
			// idsArg);
			// ub.updateColumnValue("uploaded", uploadedArg).where().in("id",
			// idsArg);
			// this.u= ub.prepare();
		}

		int query(TreeSet<Long> ids, Object uploaded) throws SQLException {
			UpdateBuilder<Object, Integer> ub = dao.updateBuilder();
			ub.where().in("id", ids);
			ub.updateColumnValue("uploaded", uploaded);
			PreparedUpdate<Object> pu = ub.prepare();
			return dao.update(pu);
		}
	}

	/** Structure holding a prepared UPLOAD query for every table. */
	private HashMap<Class, PrepUpdateQuery> prepdUploadMap;

	/**
	 * <h2>Thread 1/3: Database reader</h2> This thread retrieves data from the
	 * database and passes it to the {@link UploadTask#poster} thread. This
	 * thread is responsible for retrieving a stand-alone subset of the
	 * database. Uploaded tuples should not be uploaded again.
	 */
	private final Thread dbReader = new Thread(new Runnable() {
		/**
		 * This method should call either the readFile(...) or the
		 * readDatabase() method. Starts executing the active part of the class'
		 * code. This method is called when a thread is started that has been
		 * created with a class which implements Runnable.
		 */
		@Override
		public void run() {
			Log.i(this.getClass().getName(), System.currentTimeMillis()
					+ ": DbReader thread started.");
			try {
				readDatabase();
			} catch (Exception e) {
				uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
				onError(e);
			}
			Log.i(this.getClass().getName(), System.currentTimeMillis()
					+ ": DbReader thread finished. Let's interrupt dbUpdater.");
			myInterruptionCount++;
			dbUpdater.interrupt();
		}

		/**
		 * Opens a file for reading and writes its contents in the ccb. Used for
		 * performance analysis or testing.
		 * 
		 * @param filename
		 *            The name of the file to open. Just filename, not entire
		 *            path. The file will be searched in
		 *            {@link UploadTask#FILES_PREFIX}.
		 * @throws IOException
		 *             If the file is not found, or reading fails.
		 */
		@SuppressWarnings("unused")
		void readFile(String filename) throws IOException {
			String filepath = FILES_PREFIX + filename;
			Log.w(this.getClass().getName(), "Reading file '" + filepath
					+ "' and writing contents in ccb.");
			byte[] buffer = new byte[1024];
			InputStream is = new FileInputStream(new File(filepath));
			OutputStream os = ccb.getOutputStream();
			int bytesRead = 0;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
		}

		/**
		 * Retrieves un-uploaded objects from database tables, serializes them
		 * in JSON format, and writes them in ccb.
		 * 
		 * @throws IOException
		 * @throws NoSuchMethodException
		 * @throws InvocationTargetException
		 * @throws IllegalAccessException
		 * @throws SecurityException
		 * @throws IllegalArgumentException
		 * @throws SQLException
		 */
		void readDatabase() throws IOException, IllegalArgumentException,
				SecurityException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SQLException {
			// Create a jsonWriter to write in the ccb.
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ccb
					.getOutputStream());
			JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);
			jsonWriter.setIndent("  "); // comment it out when in production

			long startWritingToCcb = System.currentTimeMillis();
			Log.d(this.getClass().getName(), "Start writing to ccb.");
			Log.d(this.getClass().getName(), "ccb size= " + ccb.getSize()
					+ "bytes. Available bytes in ccb= " + ccb.getAvailable());

			// Start root element-object of JSON dataset.
			jsonWriter.beginObject();

			long id, minId, maxId, minIdx, maxIdx, offset;
			Class c, fc;
			String fKey;
			Method getId, getSession;
			List<Object> queryResults, tmpObjList;
			for (int cInx = 0; cInx < RELATIONS.length; cInx++) {
				c = RELATIONS[cInx].dbClass;
				fc = RELATIONS[cInx].foreignClass;
				fKey = RELATIONS[cInx].foreignCol;

				// Start an Object[] property for the objs of this Class c.
				jsonWriter.name(c.getSimpleName() + "s").beginArray();

				// Get the smallest and the greatest id value of this dbClass
				// that has not been uploaded.
				PrepSelectQuery prepSelQ = prepdSelectMap.get(c);
				tmpObjList = prepSelQ.dao.query(prepSelQ.dao.queryBuilder()
						.limit(1).orderBy("id", true).where()
						.eq("uploaded", DatabaseHelper.NOT_UPLOADED).prepare());
				minId = (tmpObjList.isEmpty()) ? 0 : (Integer) c.getMethod(
						"getId").invoke(c.cast(tmpObjList.get(0)));
				tmpObjList = prepSelQ.dao.query(prepSelQ.dao.queryBuilder()
						.limit(1).orderBy("id", false).where()
						.eq("uploaded", DatabaseHelper.NOT_UPLOADED).prepare());
				maxId = (tmpObjList.isEmpty()) ? 0 : (Integer) c.getMethod(
						"getId").invoke(c.cast(tmpObjList.get(0)));

				// ArrayList to store the ids of the objects being uploaded.
				TreeSet<Long> dbClassUpldIdSet = idMap.get(c);
				offset = minId;

				// Traces should be handled specially, to find the referred
				// Sessions, and make sure that all traces referring these
				// sessions are included in the dataset. (e.g., if a Session
				// has two traces, both traces are uploaded).
				/*
				 * if (c==Trace.class){ // If there are many un-uploaded traces,
				 * don't upload them all now. //maxId= Math.min(minId+500,
				 * maxId); // max id not included // TODO: check this // The
				 * following loop retrieves some ORM objects of a specific //
				 * class, only few in each iteration (for memory efficiency), //
				 * and writes them in ccb. while (true) { // Retrieve a data
				 * chunk queryResults= prepSelQ.query(new Long[]{offset,
				 * offset+QUERY_LIMIT}); for (Object obj : queryResults) { //
				 * Add trace.id in idMap id= (Integer)
				 * c.getMethod("getId").invoke(c.cast(obj));
				 * dbClassUpldIdSet.add(id); // Add session.id in idMap Session
				 * s= (Session) c.getMethod("getSession").invoke(c.cast(obj));
				 * if (s!=null) { id= (Integer)
				 * Session.class.getMethod("getId").invoke(s);
				 * idMap.get(Session.class).add(id); } } // Retrieve another
				 * chunk or break if (offset+QUERY_LIMIT >= maxId) break;
				 * offset+=QUERY_LIMIT; } // Let tIds= idMap.get(Trace), sIds=
				 * idMap.get(Session). // Now both tIds and sIds are populated,
				 * but there might be // a not-in-tIds trace referring a session
				 * included in sIds. // We should find these traces and add them
				 * in tIds. // // SELECT * // FROM traces // WHERE
				 * traces.session IN (sIds) // AND traces.id NOT IN (tIds) //
				 * ORDER BY id ASC Log.d(this.getClass().getName(),
				 * "Adding traces that are in idMap.get(Session.class) but not in idMap.get(Trace.class)"
				 * ); Dao d= prepSelQ.dao; queryResults= d.query(
				 * d.queryBuilder().orderBy("id", true).where().in("session",
				 * idMap.get(Session.class)).and().notIn("id",
				 * idMap.get(Trace.class)).prepare() ); for (Object obj :
				 * queryResults) { // Add trace.id in idMap id= (Long)
				 * c.getMethod("getId").invoke(c.cast(obj));
				 * //Log.v(this.getClass().getName(), (c.cast(obj)).toString());
				 * dbClassUpldIdSet.add(id); } // Now both tIds and sIds are
				 * completely populated. // Let's write traces in ccb.
				 * ArrayList<Long> idsToRetrieve= new ArrayList<Long>();
				 * idsToRetrieve.addAll(idMap.get(Trace.class)); minId=
				 * idsToRetrieve.isEmpty() ? 0 : idsToRetrieve.get(0); maxId=
				 * idsToRetrieve.isEmpty() ? 0 :
				 * idsToRetrieve.get(idsToRetrieve.size()-1);
				 * //Log.d(this.getClass().getName(),
				 * "Trace and Session id sets are complete. TraceIdsToRetrieve= ["
				 * +
				 * minId+", ...,"+maxId+"] ("+idsToRetrieve.size()+" elements)."
				 * ); minIdx= 0; maxIdx= idsToRetrieve.size(); // The following
				 * loop retrieves some Trace objects, only few // in each
				 * iteration (for memory efficiency), and writes // them in ccb.
				 * offset= minIdx; while (true) { // Retrieve a data chunk int
				 * minChunkIdx= ((int)offset); int maxChunkIdx=
				 * ((int)Math.min(offset+QUERY_LIMIT, maxIdx)); List subList=
				 * idsToRetrieve.subList( minChunkIdx, maxChunkIdx ); long
				 * minTChunckId= subList.isEmpty() ? 0 : (Long) subList.get(0);
				 * long maxTChunckId= subList.isEmpty() ? 0 : (Long)
				 * subList.get(subList.size()-1);
				 * Log.d(this.getClass().getName(),
				 * c.getSimpleName()+".id=["+minTChunckId
				 * +", ..., "+(maxTChunckId)+"]"); // subList(min, max) does not
				 * return the max element. // prepSelQ.query(new Long[]{min,
				 * max}) also does not return the max element. // This causes
				 * two elements excluded at the end of the chuck(min, max). //
				 * To avoid this +1 is added on maxChunkIdx. queryResults=
				 * prepSelQ.query(new Long[]{minTChunckId, maxTChunckId+1}); //
				 * Write the data chuck for (Object obj : queryResults) { id=
				 * (long) ((Integer) c.getMethod("getId").invoke(c.cast(obj)));
				 * //Log.v(this.getClass().getName(), (c.cast(obj)).toString());
				 * gson.toJson(obj, c, jsonWriter); } // Retrieve another chunk
				 * or break if (offset+QUERY_LIMIT >= maxIdx) break;
				 * offset+=QUERY_LIMIT; } }
				 */
				// The following block will be executed for RELATIONS with
				// null-valued foreignClass (i.e., CellularNetworkOperator,
				// BatteryMeasurement, and Question). For these RELATIONS, all
				// un-uploaded objects are retrieved.
				if (fKey == null) {
					// Find min and max id values to retrieve
					tmpObjList = prepSelQ.dao.query(prepSelQ.dao.queryBuilder()
							.limit(1).orderBy("id", true).where()
							.eq("uploaded", DatabaseHelper.NOT_UPLOADED)
							.prepare());
					minId = (tmpObjList.isEmpty()) ? 0 : (Integer) c.getMethod(
							"getId").invoke(c.cast(tmpObjList.get(0)));
					tmpObjList = prepSelQ.dao.query(prepSelQ.dao.queryBuilder()
							.limit(1).orderBy("id", false).where()
							.eq("uploaded", DatabaseHelper.NOT_UPLOADED)
							.prepare());
					maxId = (tmpObjList.isEmpty()) ? 0 : (Integer) c.getMethod(
							"getId").invoke(c.cast(tmpObjList.get(0)));

					// The following loop retrieves some ORM objects of a
					// specific
					// class, only few in each iteration (for memory
					// efficiency),
					// and writes them in ccb.
					offset = minId;
					while (true) {
						// Retrieve a data chunk
						Log.d(this.getClass().getName(), c.getSimpleName()
								+ ".id=[" + offset + ", ..., "
								+ (offset + QUERY_LIMIT - 1) + "]");
						queryResults = prepSelQ.query(new Long[] { offset,
								offset + QUERY_LIMIT });

						// Write the data chuck
						for (Object obj : queryResults) {
							id = (long) ((Integer) c.getMethod("getId").invoke(
									c.cast(obj)));
							dbClassUpldIdSet.add(id);
							// Log.v(this.getClass().getName(),
							// (c.cast(obj)).toString());
							gson.toJson(obj, c, jsonWriter);
						}

						// Retrieve another chunk or break
						if (offset + QUERY_LIMIT >= maxId)
							break;
						offset += QUERY_LIMIT;
					}
				}
				// The following block will be executed for RELATIONS with
				// foreignClass constraints. Only the objects referring
				// already-written-in-ccb objects will be uploaded (e.g.,
				// wifiTraces referring specific traces; traces specified in
				// idMap.get(Trace.class) ).
				else {
					List<Long> idsToRetrieve = new ArrayList<Long>();
					idsToRetrieve.addAll(idMap.get(fc));
					minIdx = 0;
					maxIdx = idsToRetrieve.size();

					// The following loop retrieves some ORM objects of a
					// specific
					// class, only few in each iteration (for memory
					// efficiency),
					// and writes them in ccb.
					offset = minIdx;
					while (true) {
						// Retrieve a data chunk
						int minChunkIdx = ((int) offset);
						int maxChunkIdx = ((int) Math.min(offset + QUERY_LIMIT,
								maxIdx));
						List subList = idsToRetrieve.subList(minChunkIdx,
								maxChunkIdx);
						long minChunckId = subList.isEmpty() ? 0
								: (Long) subList.get(0);
						long maxChunckId = subList.isEmpty() ? 0
								: (Long) subList.get(subList.size() - 1);
						Log.d(this.getClass().getName(), c.getSimpleName()
								+ "." + fKey + ".id=[" + minChunckId
								+ ", ..., " + maxChunckId + "]");
						queryResults = prepSelQ.query(new List[] { subList });

						// Write the data chuck
						for (Object obj : queryResults) {
							id = (long) ((Integer) c.getMethod("getId").invoke(
									c.cast(obj)));
							dbClassUpldIdSet.add(id);
							// Log.v(this.getClass().getName(),
							// (c.cast(obj)).toString());
							gson.toJson(obj, c, jsonWriter);
						}

						// Retrieve another chunk or break
						if (offset + QUERY_LIMIT >= maxIdx)
							break;
						offset += QUERY_LIMIT;
					}
				}

				// End Object[] property
				jsonWriter.endArray();
			}

			jsonWriter.name("autoUpload").value(autoUpload);

			// End root element-object of JSON dataset.
			jsonWriter.endObject();

			long endWritingToCcb = System.currentTimeMillis();
			long durWritingToCcb = endWritingToCcb - startWritingToCcb;
			Log.d(this.getClass().getName(), "End writing to ccb. Duration: "
					+ durWritingToCcb);
			Log.d(this.getClass().getName(), "ccb size= " + ccb.getSize()
					+ "bytes. Available bytes in ccb= " + ccb.getAvailable());

			jsonWriter.close();
		}

	});

	/**
	 * <h2>Thread 2/3: Data POSTer</h2> This thread sends the retrieved data to
	 * the server via HTTP POST.
	 */
	private final Thread poster = new Thread(new Runnable() {
		/**
		 * This method should call either the writeFile() or the
		 * writeHttpPostStream() method. Starts executing the active part of the
		 * class' code. This method is called when a thread is started that has
		 * been created with a class which implements Runnable.
		 */
		@Override
		public void run() {
			Log.i(this.getClass().getName(), System.currentTimeMillis()
					+ ": Poster thread started.");
			try {
				// writeFile();
				writeHttpPostStream();
			} catch (Exception e) {
				uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
				onError(e);
			}
			Log.i(this.getClass().getName(), System.currentTimeMillis()
					+ ": Poster thread finished. Let's interrupt dbUpdater.");
			myInterruptionCount++;
			dbUpdater.interrupt();
		}

		/**
		 * Reads the contents of the ccb and writes them in a file.
		 * 
		 * @throws IOException
		 */
		@SuppressWarnings("unused")
		void writeFile() throws IOException {
			File fileDir = new File(FILES_PREFIX);
			fileDir.mkdirs();
			File file = new File(fileDir, System.currentTimeMillis() + ".json");
			Log.w(this.getClass().getName(), "Writing ccb contents in file '"
					+ file.getPath() + "'.");

			InputStream is = ccb.getInputStream();
			OutputStream os = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;

			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
		}

		/**
		 * Reads the contents of the ccb and writes them in a HTTP POST entity
		 * to the urbanNet server.
		 * 
		 * @throws SocketException
		 * @throws SSLException
		 * @throws ClientProtocolException
		 * @throws NotLoggedInException
		 * @throws LoginException
		 * @throws IOException
		 * @throws ServerException
		 */
		void writeHttpPostStream() throws SocketException, SSLException,
				ClientProtocolException, NotLoggedInException, LoginException,
				IOException, ServerException {
			InputStreamEntity inputStreamEntity = new InputStreamEntity(ccb
					.getInputStream(), -1);
			inputStreamEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			Response response;

			long startPostingTime = System.currentTimeMillis();
			response = com.sendData(inputStreamEntity);
			long endPostingTime = System.currentTimeMillis(); // Test

			long serverAndNetworkdelay = endPostingTime - startPostingTime;
			Log.d(this.getClass().getName(),
					"Data Successfully uploaded. Server says: "
							+ response.getValue());
			if (UrbanNetApp.isMeasuringPerformance)
				SDLog.fn(
						this,
						serverAndNetworkdelay + " "
								+ ((int) (response.getServerDelay() * 1000)),
						SDLog.SEND_DATA_FILE);
		}
	});

	/**
	 * <h2>Thread 3/3: Database updater</h2> Waits until
	 * {@link UploadTask#dbReader} and {@link UploadTask#poster} threads have
	 * finished their job, and then updates the "upload" columns on the database
	 * tables to mark which tuples have been uploaded.
	 */
	private final Thread dbUpdater = new Thread(new Runnable() {
		/**
		 * On the second interuption must call updateUploadedField(). The
		 * property {@link UploadTask#uploadedFieldNewValue} is used. Starts
		 * executing the active part of the class' code. This method is called
		 * when a thread is started that has been created with a class which
		 * implements Runnable.
		 */
		@Override
		public synchronized void run() {
			Log.i(this.getClass().getName(),
					System.currentTimeMillis()
							+ ": DbUpdater thread started. Waiting dbReader and poster threads to finish.");
			while (true) {
				try {
					wait();
				} catch (InterruptedException e) {
					Log.d(this.getClass().getName(), "Interuption No"
							+ myInterruptionCount);
					if (myInterruptionCount >= 2) {
						break;
					}
				}
			}
			Log.i(this.getClass().getName(), System.currentTimeMillis()
					+ ": DbUpdater interupted twice. Strarting work.");
			try {
				// printIdMap();
				updateUploadedField();
				onSuccess();
			} catch (Exception e) {
				onError(e);

			}
			Log.i(this.getClass().getName(), System.currentTimeMillis()
					+ ": DbUpdater finished.");
			tEnd = System.currentTimeMillis();
			Log.i(this.getClass().getName(), "UploadTask execution duration: "
					+ (tEnd - tStart) + " ms.");
			Log.i(this.getClass().getName(), "AppScopeUploadFinished");
		}

		/**
		 * Prints the idMap in LogCat.
		 */
		@SuppressWarnings("unused")
		void printIdMap() {
			Log.d(this.getClass().getName(), idMap.toString());
		}

		/**
		 * Updates the "upload" property of all database objects whose ids are
		 * included in {@link UploadTask#idMap}, setting the value to be
		 * {@link UploadTask#uploadedFieldNewValue}, using a sinle transaction.
		 * 
		 * @throws SQLException
		 *             If the transaction fails.
		 * @throws UploadTaskException
		 *             If the number of updated objects of a db table is
		 *             different from the size of the respective
		 *             {@link UploadTask#idMap} element.
		 */
		void updateUploadedField() throws SQLException, UploadTaskException {
			TransactionManager.callInTransaction(
					dbHelper.getConnectionSource(), new Callable<Void>() {
						public Void call() throws SQLException,
								UploadTaskException {
							Class c;
							PrepUpdateQuery u;
							int objsUpdated;
							for (int cInx = 0; cInx < RELATIONS.length; cInx++) {
								c = RELATIONS[cInx].dbClass;
								u = prepdUploadMap.get(c);
								objsUpdated = u.query(idMap.get(c),
										((Integer) uploadedFieldNewValue));
								Log.d(this.getClass().getName(), "idMap.get("
										+ c.getSimpleName() + ").size()="
										+ idMap.get(c).size() + ". "
										+ objsUpdated + " objs were updated.");
								if (idMap.get(c).size() != objsUpdated)
									throw new UploadTaskException(
											"Database update problem: "
													+ "idMap.get("
													+ c.getSimpleName()
													+ ").size()="
													+ idMap.get(c).size()
													+ ". " + objsUpdated
													+ " objs were updated.");
							}
							return null;
						}
					});
		}
	});

	/**
	 * Starts the dataset uploading procedure. This method makes sure the client
	 * is logged-in, and then calls the start() method of all three threads. An
	 * {@link UploadTask} object can be started only once.
	 * 
	 * @throws UploadTaskException
	 */
	public void start() throws UploadTaskException {
		if (!alreadyExecuted) {
			alreadyExecuted = true;
			uploadedFieldNewValue = DatabaseHelper.CONFIRMED_UPLOADED;

			// Ensuring that the client is logged-in, before attempting to
			// upload a big file with measurements.
			try {
				com.loginClient();
			} catch (ServerException se) {
				if (se.getMessage().contains("Already logged in"))
					Log.d(this.getClass().getName(),
							"Client already logged in.");
				else
					se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			tStart = System.currentTimeMillis();
			dbUpdater.start();
			poster.start();
			dbReader.start();
			currentInstance = null;
		} else {
			throw new UploadTaskException("Cannot run twice.");
		}
	}

	/**
	 * Is called by {@link UploadTask#dbUpdater} at the end of its execution, if
	 * everything has gone well. It will notify the resultListener.
	 */
	private void onSuccess() {
		resultListener.onSuccess();
	}

	/**
	 * Is called if an Exception has been thrown during the execution of any of
	 * the 3 threads. It will notify the resultListener about the exception.
	 * 
	 * @param e
	 *            The caught Exception.
	 */
	private void onError(Exception e) {
		resultListener.onError(e);
	}

	/**
	 * Private constructor.
	 * 
	 * @param dbHelper
	 * @param com
	 * @param listener
	 * @param autoUpload
	 * @throws Exception
	 */
	private UploadTask(DatabaseHelper dbHelper, CommunicationHandler com,
			ResultListener listener, boolean autoUpload) throws Exception {
		Log.d(this.getClass().getName(), "Running private constructor.");
		currentInstance = this;
		myInterruptionCount = 0;

		this.dbHelper = dbHelper;
		this.com = com;
		this.autoUpload = autoUpload;
		this.uploadedFieldNewValue = DatabaseHelper.NOT_UPLOADED;
		this.resultListener = listener;

		ccb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
		prepdSelectMap = new HashMap<Class, PrepSelectQuery>();
		prepdUploadMap = new HashMap<Class, PrepUpdateQuery>();
		idMap = new HashMap<Class, TreeSet<Long>>();

		Dao dao;
		PreparedQuery q;
		PreparedUpdate u;
		GsonBuilder gsonBuilder = new GsonBuilder();
		try {
			for (int cInx = 0; cInx < RELATIONS.length; cInx++) {
				Class c = RELATIONS[cInx].dbClass;
				String fCol = RELATIONS[cInx].foreignCol;

				// Get dao object.
				Method getDao = dbHelper.getClass().getMethod(
						"get" + c.getSimpleName() + "Dao");
				dao = (Dao<Object, Integer>) getDao.invoke(dbHelper);

				// Create and store a prepared SELECT query.
				prepdSelectMap.put(c, new PrepSelectQuery(dao, c, fCol));

				// Create and store a prepared UPDATE query.
				prepdUploadMap.put(c, new PrepUpdateQuery(dao, c));

				// Create a set for obj ids being uploaded, and id it in idMap.
				TreeSet<Long> dbClassUpldIdSet = new TreeSet<Long>();
				idMap.put(c, dbClassUpldIdSet);

				// Register a JsonSerializer for gson
				Class jsonSer = Class.forName("gr.forth.ics.urbanNet.json."
						+ c.getSimpleName() + "JsonSerializer");
				gsonBuilder.registerTypeAdapter(c, jsonSer.newInstance());
			}
			gson = gsonBuilder.create();
		} catch (Exception e) {
			currentInstance = null;
			throw e;
		}
	}

	/**
	 * If the class has no instance currently, this method will create one using
	 * the private constructor and return it. If there is already an instance,
	 * an UploadTaskException will be thrown.
	 * 
	 * @param dbHelper
	 * @param com
	 * @param listener
	 * @param autoUpload
	 * @return The newly created instance.
	 * @throws Exception
	 */
	public static synchronized UploadTask createInstance(
			DatabaseHelper dbHelper, CommunicationHandler com,
			ResultListener listener, boolean autoUpload) throws Exception {
		if (currentInstance == null) {
			currentInstance = new UploadTask(dbHelper, com, listener,
					autoUpload);
			return currentInstance;
		} else {
			throw new UploadTaskException("An instance already exists");
		}
	}

}
