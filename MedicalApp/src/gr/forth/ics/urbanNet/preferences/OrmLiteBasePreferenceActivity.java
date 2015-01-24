package gr.forth.ics.urbanNet.preferences;

import android.content.Context;
import android.preference.PreferenceActivity;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Extends the {@link PreferenceActivity} class, adding the public getHelper() method, allowing
 * it to query the local database or save objects to it, using the OrmLite library.
 * 
 * @author michalis
 */
public class OrmLiteBasePreferenceActivity<H extends OrmLiteSqliteOpenHelper> extends PreferenceActivity {
	private H helper; // A reference to the helper object.
	
	/**
	 * Copy pasted from OrmLiteBaseActivity source code
	 * @return the helper object
	 */
	public synchronized H getHelper() {
		if (helper == null) {
			helper = getHelperInternal(this);
		}
		return helper;
	}
	/**
	 * Copy pasted from OrmLiteBaseActivity source code
	 * @return
	 */
	public ConnectionSource getConnectionSource() {
		return getHelper().getConnectionSource();
	}
	/**
	 * Copy pasted from OrmLiteBaseActivity source code
	 * @param context
	 * @return
	 */
	protected H getHelperInternal(Context context) {
		@SuppressWarnings("unchecked")
		H newHelper = (H) OpenHelperManager.getHelper(context);
		return newHelper;
	}
	/**
	 * Copy pasted from OrmLiteBaseActivity source code
	 * @param helper
	 */
	protected void releaseHelper(H helper) {
		if (helper != null) {
			OpenHelperManager.release();
			helper = null;
		}
	}
	
	/**
	 * Copy pasted from OrmLiteBaseActivity source code
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseHelper(helper);
	}

}
