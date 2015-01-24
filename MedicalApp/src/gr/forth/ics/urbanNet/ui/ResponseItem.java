package gr.forth.ics.urbanNet.ui;

import android.graphics.drawable.Drawable;

public class ResponseItem {
    private int imageId;
    private String title;
    private String rate;
    private Drawable dr_image;
    private int type;

    public enum ResponseTYPE {
	TAP, BOTTLE, NATURAL, ERROR
    };

    public ResponseItem() {
    }

    public ResponseItem(int imageId, String title, String rate, ResponseTYPE type) {
	this.imageId = imageId;
	this.title = title;
	this.rate = rate;
	this.dr_image = null;
	this.type = type.ordinal();
    }

    public ResponseItem(Drawable dr, String title, String rate, ResponseTYPE type) {
	this.imageId = 0;
	this.title = title;
	this.rate = rate;
	this.dr_image = dr;
	this.type = type.ordinal();
    }

    public int getImageId() {
	return imageId;
    }

    public Drawable getDrawbale() {
	return this.dr_image;
    }

    public void setImageId(int imageId) {
	this.imageId = imageId;
    }

    public void setDrwable(Drawable imageDr) {
	this.dr_image = imageDr;
    }

    public String getRate() {
	return rate;
    }

    public void setRate(String rate) {
	this.rate = rate;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public void set(int imageId, String title, String rate, int type) {
	this.imageId = imageId;
	this.title = title;
	this.rate = rate;
	this.dr_image = null;
	this.type = type;
    }

    public void set(Drawable dr, String title, String rate, int type) {
	this.imageId = 0;
	this.title = title;
	this.rate = rate;
	this.dr_image = dr;
	this.type = type;
    }

    public float getNumericRate() {
	try {
	    return Float.parseFloat(this.rate);
	}
	catch (NumberFormatException e) {
	    return -1;
	}
	catch (NullPointerException e) {
	    return -1;
	}
    }

    /**
     * Type 1 is TAP 2 is Bottle
     * @param type
     */
    public void setType(ResponseTYPE type) {
	this.type = type.ordinal();
    }

    /**
     * @return 1 is TAP 2 is Bottle
     */
    public ResponseTYPE getType() {
	return ResponseTYPE.values()[this.type];
    }
}
