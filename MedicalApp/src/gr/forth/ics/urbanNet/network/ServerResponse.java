package gr.forth.ics.urbanNet.network;

import java.util.HashMap;
import java.util.Map;

public class ServerResponse {
    private HashMap<String, String> responseData;

    /**
     * @param responseData
     */
    public ServerResponse() {
	this.responseData = new HashMap<String, String>();
    }

    /**
     * @return the responseData
     */
    public Map<String, String> getResponseData() {
	return responseData;
    }

    /**
     * @return the responseData
     */
    public String getData(String field) {
	return responseData.get(field);
    }

    /**
     * @param responseData the responseData to set
     */
    public void setResponseDataValue(String field, String value) {
	this.responseData.put(field, value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "ServerResponse [responseData=" + responseData + "]";
    }
}
