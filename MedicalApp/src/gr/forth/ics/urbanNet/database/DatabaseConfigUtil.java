package gr.forth.ics.urbanNet.database;

import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

/**
 * The OrmLiteConfigUtil utility class writes a ormlite_config.txt configuration file in the raw resource folder res/raw/ormlite_config.txt. It is used to remove the annotation
 * work from our android application and shift it to our powerful development computers, in order to make DAO creation an faster operation. ORMLite supports the loading of the data
 * configurations from a text configuration file. When a DAO is created, these configurations will be used. You will need to run this utility locally on your development box (not
 * in an Android device), whenever you make a change to one of your data classes. This means that right now, this must be done by hand to keep the configuration file in sync with
 * your database classes.
 * @see http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_4.html
 * @author katsarakis
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    private static final Class<?>[] classes = new Class[] { Query.class, Feedback.class, SceintFeedback.class, BatteryMeasurement.class, Question.class };

    public static void main(String[] args) throws SQLException, IOException {
	writeConfigFile("ormlite_config.txt", classes);
    }
}
