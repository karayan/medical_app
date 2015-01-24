package gr.forth.ics.urbanNet.utilities;

import android.content.Context;
import android.telephony.TelephonyManager;

public class UserID {
    public UserID() {
	super();
    }

    /**
     * Get the hash of the sim card.
     * @return the unique hash id per sim card user . If there is no sim card return the ID Device
     */
    public String getIdUser(Context context) {
//	return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    	return "Samsung";
    }

}
