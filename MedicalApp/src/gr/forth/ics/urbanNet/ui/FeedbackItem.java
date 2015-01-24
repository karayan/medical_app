package gr.forth.ics.urbanNet.ui;


public class FeedbackItem {

    private String name;
    private boolean state = false;
    private String value = "-100.0";
    private double max = 100.0;
    private double min = -100.0;
    private String groupName;

    public enum TYPE {
	radio, checkBox, textFieldNumber, radioText, textFieldString
    };

    private TYPE type;

    /**
     * @param name, the title of the item(ph,red)
     * @param state, is checked or not
     * @param type, a type from TYPE{radio, checkBox, textField{-100 to 100}}
     */
    public FeedbackItem(String name, boolean state, TYPE type) {
	this.state = state;
	this.name = name;
	this.type = type;
    }

    public FeedbackItem(String name, boolean state, TYPE type, double d, double e, String groupName) throws Exception {
	if (TYPE.textFieldNumber != type) {
	    throw new Exception("Not TYPE.textField");
	}
	this.state = state;
	this.name = name;
	this.type = type;
	this.max = d;
	this.min = e;
	this.groupName = groupName;
    }

    public FeedbackItem(String name, boolean state, TYPE type, String groupName) throws Exception {
	if (TYPE.radioText != type && TYPE.textFieldNumber != type && TYPE.textFieldString != type) {
	    throw new Exception("Not TYPE.textRadio or TYPE.textField or TYPE.stringTextField");
	}
	this.state = state;
	this.name = name;
	this.type = type;
	this.groupName = groupName;
    }


        /**
     * @return the groupName
     */
    public String getGroupName() {
	return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(String groupName) {
	this.groupName = groupName;
    }

    public boolean getState() {
	return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(boolean state) {
	this.state = state;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the type
     */
    public TYPE getType() {
	return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(TYPE type) {
	this.type = type;
    }

    /**
     * The value can be return only in the type==TYPE.field feedback object
     * @return the value
     * @throws Exception
     */
    public String getValue() throws Exception {
	if (this.type != TYPE.textFieldNumber && this.type != TYPE.textFieldString) {
	    throw new Exception("Not TYPE.textField ");
	}
	else {
	    return value;
	}
    }

    /**
     * The value can be set only in the type==TYPE.field feedback object
     * @param value the value to set
     * @throws Exception
     */
    public void setValue(String value) throws Exception {
	if (this.type != TYPE.textFieldNumber && this.type != TYPE.textFieldString) {
	    throw new Exception("Not TYPE.textField ");
	}
	else {
	    this.value = value;
	}
    }

    /**
     * The value can be set only in the type==TYPE.field feedback object
     * @param value the value to set
     * @throws Exception
     */
    public void setValue(double value) throws Exception {
	if (this.type != TYPE.textFieldNumber && this.type != TYPE.textFieldString) {
	    throw new Exception("Not TYPE.textField ");
	}
	else {
	    this.value = String.valueOf(value);
	}
    }
    /**
     * @return the max
     */
    public double getMax() {
	return max;
    }

    /**
     * @return the min
     */
    public double getMin() {
	return min;
    }

    /**
     * @param max the max to set
     * @throws Exception
     */
    public void setMax(double max) throws Exception {
	if (this.type != TYPE.textFieldNumber) {
	    throw new Exception("Not TYPE.textField ");
	}
	else {
	    this.max = max;
	}
    }

    /**
     * @param min the min to set
     * @throws Exception
     */
    public void setMin(double min) throws Exception {
	if (this.type != TYPE.textFieldNumber) {
	    throw new Exception("Not TYPE.textField ");
	}
	else {
	    this.min = min;
	}
    }


    public String toStringValues() {
	if (type != TYPE.textFieldNumber) {
	return name + "/" + state;
	}
	else {
	    return name+ "/" + value;
	}
    }

    public void changeState() {
	this.state = ((this.state) ? false : true);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString() */
    @Override
    public String toString() {
	return "FeedbackItem [name=" + name + ", state=" + state + ", value=" + value + ", max=" + max + ", min=" + min + ", groupName=" + groupName + ", type=" + type + "]";
    }

}
