package gr.forth.ics.urbanNet.database;

import java.io.Serializable;

import android.annotation.SuppressLint;
import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The Provider class corresponds to the "providers" table in the database.
 * @author katsarakis
 */
@SuppressLint("NewApi")
@DatabaseTable(tableName = "sceint_feedback_table")
public class SceintFeedback implements Serializable {
    public enum WATER {
	TAP, BOTTLE, NATURAL
    };

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;
    @DatabaseField(columnName = "complaints")
    private String complaints;
    @DatabaseField(columnName = "water_type")
    private int waterType;
    @DatabaseField(columnName = "timestamp")
    private long timestamp;
    @DatabaseField(columnName = "latitude")
    private double latitude;
    @DatabaseField(columnName = "longitude")
    private double longitude;
    @DatabaseField(columnName = "altitude", canBeNull = true)
    private double altitude;
    @DatabaseField(columnName = "accuracy")
    private float accuracy;
    @DatabaseField(columnName = "turbidity")
    private double turbidity;
    @DatabaseField(columnName = "temperature")
    private double temperature;
    @DatabaseField(columnName = "Br")
    private double br;
    @DatabaseField(columnName = "ph")
    private double ph;
    @DatabaseField(columnName = "Cl")
    private double cl;
    @DatabaseField(columnName = "na")
    private double na;
    @DatabaseField(columnName = "k")
    private double k;
    @DatabaseField(columnName = "mg")
    private double mg2;
    @DatabaseField(columnName = "no")
    private double no3;
    @DatabaseField(columnName = "a")
    private double a;
    @DatabaseField(columnName = "cod")
    private double COD;
    @DatabaseField(columnName = "Do")
    private double DO;
    @DatabaseField(columnName = "bod")
    private double BOD;
    @DatabaseField(columnName = "acidity")
    private double acidity;
    @DatabaseField(columnName = "p")
    private double p;
    @DatabaseField(columnName = "n")
    private double n;
    @DatabaseField(columnName = "h")
    private double h;
    @DatabaseField(columnName = "c")
    private double c;
    @DatabaseField(columnName = "ca")
    private double ca;
    @DatabaseField(columnName = "ephemeroptera")
    private boolean ephemeroptera;
    @DatabaseField(columnName = "plecoptera")
    private boolean plecoptera;
    @DatabaseField(columnName = "mollusca")
    private boolean mollusca;
    @DatabaseField(columnName = "trichoptera")
    private boolean trichoptera;
    @DatabaseField(columnName = "escherichia_coli")
    private boolean ecoli;
    @DatabaseField(columnName = "coliform_bacteria")
    private boolean cbacteria;
    @DatabaseField(columnName = "cryptosporidium")
    private boolean cryptosporidium;
    @DatabaseField(columnName = "giardia_lamblia")
    private boolean glamblia;
    @DatabaseField(columnName = "red")
    private boolean red;
    @DatabaseField(columnName = "green")
    private boolean green;
    @DatabaseField(columnName = "black")
    private boolean black;
    @DatabaseField(columnName = "bitter")
    private boolean bitter;
    @DatabaseField(columnName = "salty")
    private boolean salty;
    @DatabaseField(columnName = "sweet")
    private boolean sweet;
    @DatabaseField(columnName = "l_pressure")
    private boolean lpressure;
    @DatabaseField(columnName = "n_pressure")
    private boolean npressure;
    @DatabaseField(columnName = "h_pressure")
    private boolean hpressure;
    @DatabaseField(columnName = "f_particles")
    private boolean fparticles;
    @DatabaseField(columnName = "sand")
    private boolean sand;
    @DatabaseField(columnName = "milky")
    private boolean milky;
    @DatabaseField(columnName = "rusty")
    private boolean rusty;
    @DatabaseField(columnName = "water_name")
    private String watername;
    @DatabaseField(columnName = "uploaded")
    private int uploaded;

    public SceintFeedback() {
	// needed by ormlite
	this.uploaded = 0;
	this.watername = " ";
	this.complaints = " ";
    }

    public SceintFeedback(Location location, long time, WATER type) {
	this.complaints = "";
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
	this.altitude = location.getAltitude();
	this.accuracy = location.getAccuracy();
	this.timestamp = time;
	this.waterType = type.ordinal();
	this.uploaded = 0;
    }

    public SceintFeedback(String string, Location location, long time, WATER type) {
	this.complaints = string;
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
	this.altitude = location.getAltitude();
	this.accuracy = location.getAccuracy();
	this.timestamp = time;
	this.waterType = type.ordinal();
	this.uploaded = 0;
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
     * @return the complaints
     */
    public String getComplaints() {
	return complaints;
    }

    /**
     * @return the uploaded
     */
    public int getUploaded() {
	return uploaded;
    }

    /**
     * @param complaints the complaints to set
     */
    public void setComplaints(String complaints) {
	this.complaints = complaints;
    }

    /**
     * @param uploaded the uploaded to set
     */
    public void setUploaded(int uploaded) {
	this.uploaded = uploaded;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
	return latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
	return longitude;
    }

    /**
     * @return the altitude
     */
    public double getAltitude() {
	return altitude;
    }

    /**
     * @return the accuracy
     */
    public float getAccuracy() {
	return accuracy;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
	this.latitude = latitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
	this.longitude = longitude;
    }

    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(double altitude) {
	this.altitude = altitude;
    }

    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(float accuracy) {
	this.accuracy = accuracy;
    }

    /**
     * @return the waterType
     */
    public int getWaterType() {
	return waterType;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
	return timestamp;
    }

    /**
     * @param waterType the waterType to set
     */
    public void setWaterType(int waterType) {
	this.waterType = waterType;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
	this.timestamp = timestamp;
    }

    public void setLocation(Location location) {
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
	this.altitude = location.getAltitude();
	this.accuracy = location.getAccuracy();
    }

    /**
     * @return the turbidity
     */
    public double getTurbidity() {
	return turbidity;
    }

    /**
     * @return the temperature
     */
    public double getTemperature() {
	return temperature;
    }

    /**
     * @return the br
     */
    public double getBr() {
	return br;
    }

    /**
     * @return the ph
     */
    public double getPh() {
	return ph;
    }

    /**
     * @return the cl
     */
    public double getCl() {
	return cl;
    }

    /**
     * @return the na
     */
    public double getNa() {
	return na;
    }

    /**
     * @return the k
     */
    public double getK() {
	return k;
    }

    /**
     * @return the mg2
     */
    public double getMg2() {
	return mg2;
    }

    /**
     * @return the no3
     */
    public double getNo3() {
	return no3;
    }

    /**
     * @return the a
     */
    public double getA() {
	return a;
    }

    /**
     * @return the cOD
     */
    public double getCOD() {
	return COD;
    }

    /**
     * @return the dO
     */
    public double getDO() {
	return DO;
    }

    /**
     * @return the bOD
     */
    public double getBOD() {
	return BOD;
    }

    /**
     * @return the acidity
     */
    public double getAcidity() {
	return acidity;
    }

    /**
     * @return the p
     */
    public double getP() {
	return p;
    }

    /**
     * @return the n
     */
    public double getN() {
	return n;
    }

    /**
     * @return the h
     */
    public double getH() {
	return h;
    }

    /**
     * @return the c
     */
    public double getC() {
	return c;
    }

    /**
     * @return the ca
     */
    public double getCa() {
	return ca;
    }

    /**
     * @return the ephemeroptera
     */
    public boolean isEphemeroptera() {
	return ephemeroptera;
    }

    /**
     * @return the plecoptera
     */
    public boolean isPlecoptera() {
	return plecoptera;
    }

    /**
     * @return the mollusca
     */
    public boolean isMollusca() {
	return mollusca;
    }

    /**
     * @return the trichoptera
     */
    public boolean isTrichoptera() {
	return trichoptera;
    }

    /**
     * @return the ecoli
     */
    public boolean isEcoli() {
	return ecoli;
    }

    /**
     * @return the coliformbacteria
     */
    public boolean isCbacteria() {
	return cbacteria;
    }

    /**
     * @return the cryptosporidium
     */
    public boolean isCryptosporidium() {
	return cryptosporidium;
    }

    /**
     * @return the giardialamblia
     */
    public boolean isGlamblia() {
	return glamblia;
    }

    /**
     * @return the red
     */
    public boolean isRed() {
	return red;
    }

    /**
     * @return the green
     */
    public boolean isGreen() {
	return green;
    }

    /**
     * @return the black
     */
    public boolean isBlack() {
	return black;
    }

    /**
     * @return the bitter
     */
    public boolean isBitter() {
	return bitter;
    }

    /**
     * @return the salty
     */
    public boolean isSalty() {
	return salty;
    }

    /**
     * @return the sweet
     */
    public boolean isSweet() {
	return sweet;
    }

    /**
     * @return the lpressure
     */
    public boolean isLpressure() {
	return lpressure;
    }

    /**
     * @return the npressure
     */
    public boolean isNpressure() {
	return npressure;
    }

    /**
     * @return the hpressure
     */
    public boolean isHpressure() {
	return hpressure;
    }

    /**
     * @return the fparticles
     */
    public boolean isFparticles() {
	return fparticles;
    }

    /**
     * @return the sand
     */
    public boolean isSand() {
	return sand;
    }

    /**
     * @return the milky
     */
    public boolean isMilky() {
	return milky;
    }

    /**
     * @return the rusty
     */
    public boolean isRusty() {
	return rusty;
    }

    /**
     * @return the watername
     */
    public String getWatername() {
	return watername;
    }

    /**
     * @param turbidity the turbidity to set
     */
    public void setTurbidity(double turbidity) {
	this.turbidity = turbidity;
    }

    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(double temperature) {
	this.temperature = temperature;
    }

    /**
     * @param br the br to set
     */
    public void setBr(double br) {
	this.br = br;
    }

    /**
     * @param ph the ph to set
     */
    public void setPh(double ph) {
	this.ph = ph;
    }

    /**
     * @param cl the cl to set
     */
    public void setCl(double cl) {
	this.cl = cl;
    }

    /**
     * @param na the na to set
     */
    public void setNa(double na) {
	this.na = na;
    }

    /**
     * @param k the k to set
     */
    public void setK(double k) {
	this.k = k;
    }

    /**
     * @param mg2 the mg2 to set
     */
    public void setMg2(double mg2) {
	this.mg2 = mg2;
    }

    /**
     * @param no3 the no3 to set
     */
    public void setNo3(double no3) {
	this.no3 = no3;
    }

    /**
     * @param a the a to set
     */
    public void setA(double a) {
	this.a = a;
    }

    /**
     * @param cOD the cOD to set
     */
    public void setCOD(double cOD) {
	COD = cOD;
    }

    /**
     * @param dO the dO to set
     */
    public void setDO(double dO) {
	DO = dO;
    }

    /**
     * @param bOD the bOD to set
     */
    public void setBOD(double bOD) {
	BOD = bOD;
    }

    /**
     * @param acidity the acidity to set
     */
    public void setAcidity(double acidity) {
	this.acidity = acidity;
    }

    /**
     * @param p the p to set
     */
    public void setP(double p) {
	this.p = p;
    }

    /**
     * @param n the n to set
     */
    public void setN(double n) {
	this.n = n;
    }

    /**
     * @param h the h to set
     */
    public void setH(double h) {
	this.h = h;
    }

    /**
     * @param c the c to set
     */
    public void setC(double c) {
	this.c = c;
    }

    /**
     * @param ca the ca to set
     */
    public void setCa(double ca) {
	this.ca = ca;
    }

    /**
     * @param ephemeroptera the ephemeroptera to set
     */
    public void setEphemeroptera(boolean ephemeroptera) {
	this.ephemeroptera = ephemeroptera;
    }

    /**
     * @param plecoptera the plecoptera to set
     */
    public void setPlecoptera(boolean plecoptera) {
	this.plecoptera = plecoptera;
    }

    /**
     * @param mollusca the mollusca to set
     */
    public void setMollusca(boolean mollusca) {
	this.mollusca = mollusca;
    }

    /**
     * @param trichoptera the trichoptera to set
     */
    public void setTrichoptera(boolean trichoptera) {
	this.trichoptera = trichoptera;
    }

    /**
     * @param ecoli the ecoli to set
     */
    public void setEcoli(boolean ecoli) {
	this.ecoli = ecoli;
    }

    /**
     * @param coliformbacteria the coliformbacteria to set
     */
    public void setCbacteria(boolean coliformbacteria) {
	this.cbacteria = coliformbacteria;
    }

    /**
     * @param cryptosporidium the cryptosporidium to set
     */
    public void setCryptosporidium(boolean cryptosporidium) {
	this.cryptosporidium = cryptosporidium;
    }

    /**
     * @param giardialamblia the giardialamblia to set
     */
    public void setGlamblia(boolean giardialamblia) {
	this.glamblia = giardialamblia;
    }

    /**
     * @param red the red to set
     */
    public void setRed(boolean red) {
	this.red = red;
    }

    /**
     * @param green the green to set
     */
    public void setGreen(boolean green) {
	this.green = green;
    }

    /**
     * @param black the black to set
     */
    public void setBlack(boolean black) {
	this.black = black;
    }

    /**
     * @param bitter the bitter to set
     */
    public void setBitter(boolean bitter) {
	this.bitter = bitter;
    }

    /**
     * @param salty the salty to set
     */
    public void setSalty(boolean salty) {
	this.salty = salty;
    }

    /**
     * @param sweet the sweet to set
     */
    public void setSweet(boolean sweet) {
	this.sweet = sweet;
    }

    /**
     * @param lpressure the lpressure to set
     */
    public void setLpressure(boolean lpressure) {
	this.lpressure = lpressure;
    }

    /**
     * @param npressure the npressure to set
     */
    public void setNpressure(boolean npressure) {
	this.npressure = npressure;
    }

    /**
     * @param hpressure the hpressure to set
     */
    public void setHpressure(boolean hpressure) {
	this.hpressure = hpressure;
    }

    /**
     * @param fparticles the fparticles to set
     */
    public void setFparticles(boolean fparticles) {
	this.fparticles = fparticles;
    }

    /**
     * @param sand the sand to set
     */
    public void setSand(boolean sand) {
	this.sand = sand;
    }

    /**
     * @param milky the milky to set
     */
    public void setMilky(boolean milky) {
	this.milky = milky;
    }

    /**
     * @param rusty the rusty to set
     */
    public void setRusty(boolean rusty) {
	this.rusty = rusty;
    }

    /**
     * @param watername the watername to set
     */
    public void setWatername(String watername) {
	this.watername = watername;
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof SceintFeedback) {
	    SceintFeedback feedback = (SceintFeedback) o;
	    return this.id == feedback.id;
	}
	return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "SceintFeedback [id=" + id + ", complaints=" + complaints + ", waterType=" + waterType + ", timestamp=" + timestamp + ", latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude + ", accuracy=" + accuracy + ", turbidity=" + turbidity + ", temperature=" + temperature + ", br=" + br + ", ph=" + ph + ", cl=" + cl + ", na=" + na + ", k=" + k + ", mg2=" + mg2 + ", no3=" + no3 + ", a=" + a + ", COD=" + COD + ", DO=" + DO + ", BOD=" + BOD + ", acidity=" + acidity + ", p=" + p + ", n=" + n + ", h=" + h + ", c=" + c + ", ca=" + ca + ", ephemeroptera=" + ephemeroptera + ", plecoptera=" + plecoptera + ", mollusca=" + mollusca + ", trichoptera=" + trichoptera + ", ecoli=" + ecoli + ", cbacteria=" + cbacteria + ", cryptosporidium=" + cryptosporidium + ", glamblia=" + glamblia + ", red=" + red + ", green=" + green + ", black=" + black + ", bitter=" + bitter + ", salty=" + salty + ", sweet=" + sweet + ", lpressure=" + lpressure + ", npressure=" + npressure + ", hpressure=" + hpressure + ", fparticles=" + fparticles + ", sand=" + sand + ", milky=" + milky + ", rusty=" + rusty + ", watername=" + watername + ", uploaded=" + uploaded + "]";
    }

}
