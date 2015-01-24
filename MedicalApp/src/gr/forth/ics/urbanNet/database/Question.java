package gr.forth.ics.urbanNet.database;

//ackage gr.forth.ics.umap.database;

import java.io.Serializable;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "question")
public class Question implements Serializable {

    private static final long serialVersionUID = -5282199209528498459L;

    
	@DatabaseField(columnName = "id", generatedId = true)
    private int id;
    @DatabaseField(columnName = "answer_index")
    private int answer_index;
    @DatabaseField(columnName = "question_index")
    private int question_index;
    @DatabaseField(columnName = "duration")
    private long duration;
    @DatabaseField(columnName = "question_text")
    private String question_text;
    @DatabaseField(columnName = "uploaded")
    private int uploaded;
    @DatabaseField(columnName = "answer_text")
    private String answer_text;
    @DatabaseField(columnName = "latitude")
    private double latitude;
    @DatabaseField(columnName = "longitude")
    private double longitude;
    @DatabaseField(columnName = "timestamp")
    private String timestamp;


    public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Question() {
    }

    public Question(int num_answer, int answer, long duration, String text, String answer_text) {
	this.duration = duration;
	this.answer_index = answer;
	this.question_index = num_answer;
	this.question_text = text;
	this.answer_text = answer_text;
    }

    public int getAnswer_index() {
		return answer_index;
	}

	public void setAnswer_index(int answer_index) {
		this.answer_index = answer_index;
	}

	public int getQuestion_index() {
		return question_index;
	}

	public void setQuestion_index(int question_index) {
		this.question_index = question_index;
	}

	public String getAnswer_text() {
		return answer_text;
	}

	public void setAnswer_text(String answer_text) {
		this.answer_text = answer_text;
	}

	/**
     * @return the id
     */
    public int getId() {
	return id;
    }

    /**
     * @return the answer_id
     */
    public int getAnswer_id() {
	return answer_index;
    }

    /**
     * @return the question_id
     */
    public int getQuestion_id() {
	return question_index;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
	return duration;
    }

    /**
     * @return the question_text
     */
    public String getQuestion_text() {
	return question_text;
    }

    /**
     * @return the uploaded
     */
    public int getUploaded() {
	return uploaded;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
	this.id = id;
    }

    /**
     * @param answer_id the answer_id to set
     */
    public void setAnswer_id(int answer_id) {
	this.answer_index = answer_id;
    }

    /**
     * @param question_id the question_id to set
     */
    public void setQuestion_id(int question_id) {
	this.question_index = question_id;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
	this.duration = duration;
    }

    /**
     * @param question_text the question_text to set
     */
    public void setQuestion_text(String question_text) {
	this.question_text = question_text;
    }

    /**
     * @param uploaded the uploaded to set
     */
    public void setUploaded(int uploaded) {
	this.uploaded = uploaded;
    }
    
    
    public void setLocation(Location location) {
	this.longitude = location.getLongitude();
	this.latitude = location.getLatitude();
    }
    

	
    @Override
    public boolean equals(Object o) {
	if (o instanceof Question) {
	    Question question = (Question) o;
	    return this.id == question.id;
	}
	return false;
    }

	@Override
	public String toString() {
		return "Question [id=" + id
				+ ", answer_index=" + answer_index + ", question_index="
				+ question_index + ", duration=" + duration
				+ ", question_text=" + question_text + ", uploaded=" + uploaded
				+ ", answer_text=" + answer_text + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", timestamp=" + timestamp + "]";
	}
}