package gr.forth.ics.urbanNet.database;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

public class OrmLiteBaseClass<H extends OrmLiteSqliteOpenHelper> {
	
	private H helper; // A reference to the helper object.
	protected Context context;

	public OrmLiteBaseClass(Context context) {
		this.context= context;
	}
	
	/**
	 * Copy pasted from OrmLiteBaseActivity source code
	 * 
	 * @return the helper object
	 */
	public synchronized H getHelper() {
		if (helper == null) {
			helper = getHelperInternal(context);
		}
		return helper;
	}

	/**
	 * Copy pasted from OrmLiteBaseActivity source code
	 * 
	 * @return
	 */
	public ConnectionSource getConnectionSource() {
		return getHelper().getConnectionSource();
	}

	/**
	 * Copy pasted from OrmLiteBaseActivity source code
	 * 
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
	 * 
	 * @param helper
	 */
	protected void releaseHelper(H helper) {
		if (helper != null) {
			OpenHelperManager.release();
			helper = null;
		}
	}

}
