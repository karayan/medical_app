package gr.forth.ics.urbanNet.database;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * {@code BatteryMeasurement} contains all events that are created when battery change status. The battery status Charging {AC(1)/USB(0)/NO(2)}, velocity{STATIONARY(0),
 * PEDESTRIAN(1), VEHICLE(2)} and battery percentage. which are saved locally and periodically are uploaded in u-map server.
 * @author Nikos Rapousis
 */

@DatabaseTable(tableName = "battery_measurement")
public class BatteryMeasurement implements Serializable {
    private static final long serialVersionUID = 1234567453318163321L;
    @DatabaseField(columnName = "id", generatedId = true)
    private int id;
    @DatabaseField(columnName = "timestamp")
    private long timestamp;
    @DatabaseField(columnName = "new_monitore")
    private int newMonitore;
    @DatabaseField(columnName = "old_monitore")
    private int oldMonitore;
    @DatabaseField(columnName = "velocity_estimation")
    private int velocityEstimation;
    @DatabaseField(columnName = "in_session")
    private boolean inSession;
    @DatabaseField(columnName = "battery_pct")
    private float batteryPct;
    @DatabaseField(columnName = "charging")
    private int charging;
    @DatabaseField(columnName = "uploaded")
    private int uploaded;

    /**
 * 
 */
    public BatteryMeasurement() {

    }

    /**
     * @param timestamp
     * @param newMonitore
     * @param oldMonitore
     * @param velocityEstimation
     * @param inSession
     * @param batteryPct
     * @param charging
     */
    public BatteryMeasurement(long timestamp, int newMonitore, int oldMonitore, int velocityEstimation, boolean inSession, float batteryPct, int charging) {
	this.timestamp = timestamp;
	this.newMonitore = newMonitore;
	this.oldMonitore = oldMonitore;
	this.velocityEstimation = velocityEstimation;
	this.inSession = inSession;
	this.batteryPct = batteryPct;
	this.charging = charging;
    }

    /**
     * @param timestamp
     * @param newMonitore
     * @param oldMonitore
     * @param velocityEstimation
     * @param inSession
     * @param batteryPct
     * @param charging
     */
    public BatteryMeasurement(long timestamp, int oldMonitore, int velocityEstimation, boolean inSession, float batteryPct, int charging) {
	this.timestamp = timestamp;
	this.oldMonitore = oldMonitore;
	this.velocityEstimation = velocityEstimation;
	this.inSession = inSession;
	this.batteryPct = batteryPct;
	this.charging = charging;
    }

    /**
     * The timestamp, that event happened
     * @return the timestamp
     */
    public long getTimestamp() {
	return timestamp;
    }

    /**
     * @return the id
     */
    public int getId() {
	return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
	this.id = id;
    }

    /**
     * Get the last new monitore rate {ZERO(0), LOW{1}, MODERATE{2}, HIGH{3}, GREEDY{4}}
     * @return the newMonitore
     */
    public int getNewMonitore() {
	return newMonitore;
    }

    /**
     * Get the old monitore rate {ZERO(0), LOW{1}, MODERATE{2}, HIGH{3}, GREEDY{4}}
     * @return the oldMonitore
     */
    public int getOldMonitore() {
	return oldMonitore;
    }

    /**
     * Get the velovocity {STATIONARY(0), PEDESTRIAN(1), VEHICLE(2)}
     * @return the velocityEstimation
     */
    public int getVelocityEstimation() {
	return velocityEstimation;
    }

    /**
     * Get if the event has in Session(sipdroid) call
     * @return the inSession
     */
    public boolean getInSession() {
	return inSession;
    }

    /**
     * Return the percentage of the battery
     * @return the batteryPct
     */
    public double getBatteryPct() {
	return batteryPct;
    }

    /**
     * Get the status of charging{ AC(1)/USB(0)/NO(2)}
     * @return the charging
     */
    public int getCharging() {
	return charging;
    }

    /**
     * Set the timestamp ,that event happen
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
	this.timestamp = timestamp;
    }

    /**
     * Set the value {ZERO(0), LOW{1}, MODERATE{2}, HIGH{3}, GREEDY{4}}
     * @param newMonitore the newMonitore to set
     */
    public void setNewMonitore(int newMonitore) {
	this.newMonitore = newMonitore;
    }

    /**
     * Set the value {ZERO(0), LOW{1}, MODERATE{2}, HIGH{3}, GREEDY{4}}
     * @param oldMonitore the oldMonitore to set
     */
    public void setOldMonitore(int oldMonitore) {
	this.oldMonitore = oldMonitore;
    }

    /**
     * Set the value {STATIONARY(0), PEDESTRIAN(1), VEHICLE(2)}
     * @param velocityEstimation the velocityEstimation to set
     */
    public void setVelocityEstimation(int velocityEstimation) {
	this.velocityEstimation = velocityEstimation;
    }

    /**
     * Set true if is in session(sipdroid call),otherwise false
     * @param inSession the inSession to set
     */
    public void setInSession(boolean inSession) {
	this.inSession = inSession;
    }

    /**
     * set he percentage of the battery [0.0 - 1.0]
     * @param batteryPct the batteryPct to set
     */
    public void setBatteryPct(float batteryPct) {
	this.batteryPct = batteryPct;
    }

    /**
     * Set he status of charging {AC(1)/USB(0)/NO(2)}
     * @param charging the charging to set
     */
    public void setCharging(int charging) {
	this.charging = charging;
    }

    /**
     * @return the uploaded
     */
    public int getUploaded() {
	return uploaded;
    }

    /**
     * @param uploaded the uploaded to set
     */
    public void setUploaded(int uploaded) {
	this.uploaded = uploaded;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "MonitoreRate [id=" + id + ", timestamp=" + timestamp + ", newMonitore=" + newMonitore + ", oldMonitore=" + oldMonitore + ", velocityEstimation=" + velocityEstimation + ", inSession=" + inSession + ", batteryPct=" + batteryPct + ", charging=" + charging + ", uploaded=" + uploaded + "]";
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof BatteryMeasurement) {
	    BatteryMeasurement battery = (BatteryMeasurement) o;
	    return this.id == battery.id;
	}
	return false;
    }
}
