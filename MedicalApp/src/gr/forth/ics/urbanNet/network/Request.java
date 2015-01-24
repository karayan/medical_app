package gr.forth.ics.urbanNet.network;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public class Request {

    private HashMap<String, String> pairs;
    private JSONObject json;

    public Request() {
	pairs = new HashMap<String, String>();
    }

    public void setValue(String name, String value) {
	pairs.put(name, value);
    }

    public String getValue(String name) {
	return pairs.get(name);
    }

    public void setValue(String name, int i) {
	setValue(name, "" + i);
    }

    public void setValue(String name, long i) {
	setValue(name, "" + i);
    }

    public StringEntity getJsonEntity() {
	json = new JSONObject(pairs);
	StringEntity se;
	try {

	    se = new StringEntity(json.toString());

	}
	catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	    return null;
	}
	se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	return se;

    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "Request [pairs=" + pairs + ", json=" + json + "]";
    }

    public void clear() {
	pairs.clear();

    }

}
