package gr.forth.ics.urbanNet.network;


public class Response {

    private int type;
    private String value;
    private Message msg;
    private String polygonString;
    /** Server delay */
    private double delay;
    /** Network delay */
    private double networkDelay;
    private String username;
    private String secret;
    private String server;

    public Response() {
	type = 1;
	value = "error";
	delay = -1;
	networkDelay = -1;
	polygonString = null;
	username = null;
	secret = null;
	server = null;
    }

    public boolean hasError() {
	return (this.type == 1);
    }

    public void setError(int i) {
	this.type = i;
    }

    public String errorMsg() {
	return this.value;
    }

    public double getNetworkDelay() {
	return networkDelay;
    }

    public void setNetworkDelay(double networkDelay) {
	this.networkDelay = networkDelay;
    }

    /**
     * @return the type
     */
    public int getType() {
	return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
	this.type = type;
    }

    /**
     * @return the value
     */
    public String getValue() {
	return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
	this.value = value;
    }

    /**
     * @return the msg
     */
    public Message getMsg() {
	return msg;
    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(Message msg) {
	this.msg = msg;
    }

    /**
     * @return the server delay
     */
    public double getServerDelay() {
	return delay;
    }

    /**
     * @param delay the server delay to set
     */
    public void setServerDelay(double delay) {
	this.delay = delay;
    }

    /**
     * @return the polygonString
     */
    public String getPolygonString() {
	return polygonString;
    }

    /**
     * @param polygonString the polygonString to set
     */
    public void setPolygonString(String polygonString) {
	this.polygonString = polygonString;
    }

    /**
     * @return the username
     */
    public String getUsername() {
	return username;
    }

    /**
     * @return the secret
     */
    public String getSecret() {
	return secret;
    }

    /**
     * @return the server
     */
    public String getServer() {
	return server;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
	this.username = username;
    }

    /**
     * @param secret the secret to set
     */
    public void setSecret(String secret) {
	this.secret = secret;
    }

    /**
     * @param server the server to set
     */
    public void setServer(String server) {
	this.server = server;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "Response [type=" + type + ", value=" + value + ", msg=" + msg + ", polygonString=" + polygonString + ", delay=" + delay + ", networkDelay=" + networkDelay + ", username=" + username + ", secret=" + secret + ", server=" + server + "]";
    }

}
