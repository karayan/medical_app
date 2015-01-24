package gr.forth.ics.urbanNet.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * The Database helper class is used to manage the creation and upgrading of our database. This class also provides the DAOs used by the other classes. Every time the database
 * schema is updated, the private static final int DATABASE_VERSION should be increased by 1.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "urbanNetClientDatabase.db";
    private static final int DATABASE_VERSION = 06;
    public static final int NOT_UPLOADED = 0;
    public static final int CONSIDERED_UPLOADED = 1;
    public static final int CONFIRMED_UPLOADED = 2;
    public static final int NOT_UPLOADED_BECAUSE_OF_SQL_ERROR = -1;
    private String[] precached = {};
    private Dao<Query, Integer> queryDao;
    private Dao<Feedback, Integer> feedbackDao;
    private Dao<SearchedPolygon, Integer> geoPointsDao;
    private Dao<BatteryMeasurement, Integer> batteryDao;
    private Dao<Question, Integer> questionDao;
    private Dao<SceintFeedback, Integer> sceintFeedbackDao;
    private Context context;

    public DatabaseHelper(Context context) {
	/* In case you don't use the DatabaseConfigUtil */
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
	this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
	// this.connectionSource= connectionSource;
	try {
//	    TableUtils.createTable(connectionSource, Query.class);
//	    TableUtils.createTable(connectionSource, Feedback.class);
//	    TableUtils.createTable(connectionSource, SearchedPolygon.class);
//	    TableUtils.createTable(connectionSource, BatteryMeasurement.class);
	    TableUtils.createTable(connectionSource, Question.class);
//	    TableUtils.createTable(connectionSource, SceintFeedback.class);
//	    preCachedNames();
	}
	catch (SQLException e) {
	    Log.e(DatabaseHelper.class.getName(), "Unable to create databases", e);
	}
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
	try {
//	    TableUtils.dropTable(connectionSource, Query.class, true);
//	    TableUtils.dropTable(connectionSource, Feedback.class, true);
//	    TableUtils.dropTable(connectionSource, SearchedPolygon.class, true);
//	    TableUtils.dropTable(connectionSource, BatteryMeasurement.class, true);
	    TableUtils.dropTable(connectionSource, Question.class, true);
//	    TableUtils.dropTable(connectionSource, SceintFeedback.class, true);
	    onCreate(sqliteDatabase, connectionSource);
	}
	catch (SQLException e) {
	    Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
	}
    }

    /* public ConnectionSource getConnectionSource() { return this.connectionSource; } */

    public Dao<Query, Integer> getQueryDao() throws SQLException {
	if (queryDao == null) {
	    queryDao = getDao(Query.class);
	}
	return queryDao;
    }

    public Dao<Feedback, Integer> getFeedbackDao() throws SQLException {
	if (feedbackDao == null) {
	    feedbackDao = getDao(Feedback.class);
	}
	return feedbackDao;
    }

    public Dao<SearchedPolygon, Integer> getSearchedPolygonDao() throws SQLException {
	if (geoPointsDao == null) {
	    geoPointsDao = getDao(SearchedPolygon.class);
	}
	return geoPointsDao;
    }

    public Dao<BatteryMeasurement, Integer> getBatteryMeasurementDao() throws SQLException {
	if (batteryDao == null) {
	    batteryDao = getDao(BatteryMeasurement.class);
	}
	return batteryDao;
    }

    public Dao<Question, Integer> getQuestionDao() throws SQLException {
	if (questionDao == null) {
	    questionDao = getDao(Question.class);
	}
	return questionDao;
    }

    public Dao<SceintFeedback, Integer> getSceintFeedbackDao() throws SQLException {
	if (sceintFeedbackDao == null) {
	    sceintFeedbackDao = getDao(SceintFeedback.class);
	}
	return sceintFeedbackDao;
    }

    private void preCachedPolygons() {
	String[] data;
	InputStream ins = context.getResources().openRawResource(context.getResources().getIdentifier("raw/geo_city", "raw", context.getPackageName()));
	BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
	String line;
	try {
	    while ((line = reader.readLine()) != null) {
		data = line.split("-");
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		String[] latlngs = data[1].split(", ");
		for (int j = 0; j < (latlngs.length - 1); j++) {
		    String[] tmp = latlngs[j].trim().split(",");
		    geoPoints.add(new GeoPoint((int) (Double.parseDouble(tmp[1]) * 1E6), (int) (Double.parseDouble(tmp[0]) * 1E6)));
		}
		SearchedPolygon tem = new SearchedPolygon(data[0], geoPoints);
		try {
		    geoPointsDao = getSearchedPolygonDao();
		    geoPointsDao.create(tem);
		}
		catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	}
	catch (IOException e1) {
	    e1.printStackTrace();
	}
    }

    private void preCachedNames() {
	String[] data;
	InputStream ins = context.getResources().openRawResource(context.getResources().getIdentifier("raw/cities", "raw", context.getPackageName()));
	BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
	String line;
	try {
	    while ((line = reader.readLine()) != null) {
		data = line.split(",");
		for (int j = 0; j < (data.length - 1); j++) {
		    SearchedPolygon tem = new SearchedPolygon(data[j], new ArrayList<GeoPoint>());
		    try {
			geoPointsDao = getSearchedPolygonDao();
			geoPointsDao.create(tem);
		    }
		    catch (SQLException e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	catch (IOException e1) {
	    e1.printStackTrace();
	}
    }

}
