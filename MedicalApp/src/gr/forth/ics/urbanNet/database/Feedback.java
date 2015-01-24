package gr.forth.ics.urbanNet.database;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Base64;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The Provider class corresponds to the "providers" table in the database.
 * @author katsarakis
 */
@SuppressLint("NewApi")
@DatabaseTable(tableName = "feedback_table")
public class Feedback implements Serializable {
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
    @DatabaseField(columnName = "image", dataType = DataType.BYTE_ARRAY)
    private byte[] image;
    @DatabaseField(columnName = "score")
    private double score;
    @DatabaseField(columnName = "latitude")
    private double latitude;
    @DatabaseField(columnName = "longitude")
    private double longitude;
    @DatabaseField(columnName = "altitude", canBeNull = true)
    private double altitude;
    @DatabaseField(columnName = "accuracy")
    private float accuracy;
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
    @DatabaseField(columnName = "chlorine")
    private boolean chlorine;
    @DatabaseField(columnName = "chemical")
    private boolean chemical;
    @DatabaseField(columnName = "gasoline")
    private boolean gasoline;
    @DatabaseField(columnName = "sewer")
    private boolean sewer;
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
    @DatabaseField(columnName = "animal")
    private boolean animal;
    @DatabaseField(columnName = "plant")
    private boolean plant;
    @DatabaseField(columnName = "stain")
    private boolean stain;
    @DatabaseField(columnName = "water_name")
    private String watername;
    @DatabaseField(columnName = "uploaded")
    private int uploaded;

    public Feedback() {
	// needed by ormlite
	this.uploaded = 0;
	this.watername = " ";
	this.complaints = " ";
    }

    public Feedback(String complaints, Bitmap imBitmap, double score, Location location, long time, WATER type) {
	this.score = score;
	this.image = getBitmapAsByteArray(imBitmap);
	this.complaints = complaints;
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
	this.altitude = location.getAltitude();
	this.accuracy = location.getAccuracy();
	this.timestamp = time;
	this.waterType = type.ordinal();
	this.uploaded = 0;
    }

    public Feedback(Location location, long time, WATER type) {
	this.score = -1;
	this.image = getBitmapAsByteArray(null);
	this.complaints = " ";
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
	this.altitude = location.getAltitude();
	this.accuracy = location.getAccuracy();
	this.timestamp = time;
	this.waterType = type.ordinal();
	this.uploaded = 0;
    }

    public Feedback(String string, float rating, Location location, long time, WATER type) {
	this.score = rating;
	this.image = getBitmapAsByteArray(null);
	this.complaints = " ";
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
	this.altitude = location.getAltitude();
	this.accuracy = location.getAccuracy();
	this.timestamp = time;
	this.waterType = type.ordinal();
	this.uploaded = 0;
    }

    public Feedback(String string, Location location, long time, WATER type) {
	this.score = -1;
	this.image = getBitmapAsByteArray(null);
	this.complaints = string;
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
	this.altitude = location.getAltitude();
	this.accuracy = location.getAccuracy();
	this.timestamp = time;
	this.waterType = type.ordinal();
	this.uploaded = 0;
    }

    private String getBitmapAsString(Bitmap bitmap) {
	if (bitmap != null) {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    bitmap.compress(CompressFormat.PNG, 0, outputStream);
	    return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
	}
	else {
	    return "";
	}
    }

    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
	if (bitmap != null) {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    bitmap.compress(CompressFormat.PNG, 0, outputStream);
	    return outputStream.toByteArray();
	}
	else {
	    return new byte[0];
	}
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

    public Bitmap getImageBitmap() {
	return BitmapFactory.decodeByteArray(this.image, 0, this.image.length);
    }

    public byte[] getImageByte() {
	return this.image;
    }

    public String getImageString() {
	return Base64.encodeToString(this.image, Base64.DEFAULT);
    }

    /**
     * @return the complaints
     */
    public String getComplaints() {
	return complaints;
    }

    /**
     * @return the score
     */
    public double getScore() {
	return score;
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
     * @param image the image to set
     */
    public void setImage(Bitmap imBitmap) {
	this.image = getBitmapAsByteArray(imBitmap);
    }

    /**
     * @param score the score to set
     */
    public void setScore(double score) {
	this.score = score;
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
     * @return the chlorine
     */
    public boolean isChlorine() {
	return chlorine;
    }

    /**
     * @return the chemical
     */
    public boolean isChemical() {
	return chemical;
    }

    /**
     * @return the gasoline
     */
    public boolean isGasoline() {
	return gasoline;
    }

    /**
     * @return the sewer
     */
    public boolean isSewer() {
	return sewer;
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
     * @return the particles
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
     * @return the animal
     */
    public boolean isAnimal() {
	return animal;
    }

    /**
     * @return the plant
     */
    public boolean isPlant() {
	return plant;
    }

    /**
     * @return the stain
     */
    public boolean isStain() {
	return stain;
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
     * @param chlorine the chlorine to set
     */
    public void setChlorine(boolean chlorine) {
	this.chlorine = chlorine;
    }

    /**
     * @param chemical the chemical to set
     */
    public void setChemical(boolean chemical) {
	this.chemical = chemical;
    }

    /**
     * @param gasoline the gasoline to set
     */
    public void setGasoline(boolean gasoline) {
	this.gasoline = gasoline;
    }

    /**
     * @param sewer the sewer to set
     */
    public void setSewer(boolean sewer) {
	this.sewer = sewer;
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
     * @param particles the particles to set
     */
    public void setFparticles(boolean particles) {
	this.fparticles = particles;
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
     * @param animal the animal to set
     */
    public void setAnimal(boolean animal) {
	this.animal = animal;
    }

    /**
     * @param plant the plant to set
     */
    public void setPlant(boolean plant) {
	this.plant = plant;
    }

    /**
     * @param stain the stain to set
     */
    public void setStain(boolean stain) {
	this.stain = stain;
    }

    /**
     * @return the watername
     */
    public String getWatername() {
	return watername;
    }

    /**
     * @param watername the watername to set
     */
    public void setWatername(String watername) {
	this.watername = watername;
    }

    /**
     * @param selinari the selinari to set
     */

    public void setLocation(Location location) {
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
	this.altitude = location.getAltitude();
	this.accuracy = location.getAccuracy();
    }
    @Override
    public boolean equals(Object o) {
	if (o instanceof Feedback) {
	    Feedback feedback = (Feedback) o;
	    return this.id == feedback.id;
	}
	return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "Feedback [id=" + id + ", complaints=" + complaints + ", waterType=" + waterType + ", timestamp=" + timestamp + ", image=" + Arrays.toString(image) + ", score=" + score + ", latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude + ", accuracy=" + accuracy + ", red=" + red + ", green=" + green + ", black=" + black + ", bitter=" + bitter + ", salty=" + salty + ", sweet=" + sweet + ", chlorine=" + chlorine + ", chemical=" + chemical + ", gasoline=" + gasoline + ", sewer=" + sewer + ", lpressure=" + lpressure + ", npressure=" + npressure + ", hpressure=" + hpressure + ", fparticles=" + fparticles + ", sand=" + sand + ", milky=" + milky + ", rusty=" + rusty + ", animal=" + animal + ", plant=" + plant + ", stain=" + stain + ", watername=" + watername + ", uploaded=" + uploaded + "]";
    }



}
