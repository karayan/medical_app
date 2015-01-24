package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;
import gr.forth.ics.urbanNet.database.Feedback;
import gr.forth.ics.urbanNet.database.SceintFeedback;
import gr.forth.ics.urbanNet.main.UrbanNetApp;
import gr.forth.ics.urbanNet.ui.FeedbackItem.TYPE;
import gr.forth.ics.urbanNet.utilities.InputFilterMinMax;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class FeedbackItemAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<String> groups;
    private ArrayList<ArrayList<FeedbackItem>> feedbackItems;
    private LayoutInflater inflater;
    private FeedbackItem c;
    private HashMap<String, Integer> radioGroup = new HashMap<String, Integer>();
    private HashMap<String, View> radioGroupView = new HashMap<String, View>();
    private HashMap<String, Integer> texFieldStringId = new HashMap<String, Integer>();
    private HashMap<String, View> texFieldStringtView = new HashMap<String, View>();
    Object feedback;

    public FeedbackItemAdapter(Context context, ArrayList<String> groups, ArrayList<ArrayList<FeedbackItem>> feedbackItems, Feedback feedback) {
	this.context = context;
	this.groups = groups;
	this.feedbackItems = feedbackItems;
	this.feedback = feedback;
	inflater = LayoutInflater.from(context);
    }

    public FeedbackItemAdapter(Context context, ArrayList<String> groups, ArrayList<ArrayList<FeedbackItem>> feedbackItems, SceintFeedback sFeedback) {
	this.context = context;
	this.groups = groups;
	this.feedbackItems = feedbackItems;
	this.feedback = sFeedback;
	inflater = LayoutInflater.from(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
	return feedbackItems.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
	return groupPosition + childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
	c = (FeedbackItem) getChild(groupPosition, childPosition);
	View v = null;
	if (c.getType() == TYPE.checkBox) {
	    v = inflater.inflate(R.layout.checkbox, null);
	    final CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
	    cb.setText(c.getName());
	    cb.setChecked(c.getState());
	    cb.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    for (int i = 0; i < feedbackItems.size(); i++) {
			ArrayList<FeedbackItem> tem = feedbackItems.get(i);
			for (int j = 0; j < tem.size(); j++) {
			    FeedbackItem feed = tem.get(j);
			    if (feed.getName().compareTo(cb.getText().toString().trim()) == 0) {
				feed.changeState();
				setFeedbackReflectionValue(feed);
				break;
			    }
			}
		    }
		}
	    });
	}
	else if (c.getType() == TYPE.radio || c.getType() == TYPE.radioText) {
	    v = inflater.inflate(R.layout.radio_button, null);
	    final RadioButton radio = (RadioButton) v.findViewById(R.id.radioButton);
	    radio.setText(c.getName());
	    radio.setChecked(c.getState());
	    radio.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    for (int i = 0; i < feedbackItems.size(); i++) {// group level
			ArrayList<FeedbackItem> tem = feedbackItems.get(i);
			for (int j = 0; j < tem.size(); j++) {// item level
			    FeedbackItem feed = tem.get(j);
			    if (feed.getName().compareTo(radio.getText().toString().trim()) == 0) {
				feed.changeState();
				radio.setChecked(feed.getState());
				clearGroupRadioButton(i, radio.getText().toString().trim());
				clearOtherTextField();
				setFeedbackReflectionValue(feed);
				break;
			    }
			}
		    }
		}

	    });
	    radioGroup.put(radio.getText().toString(), radio.getId());
	    radioGroupView.put(radio.getText().toString(), v);
	}
	else if (c.getType() == TYPE.textFieldNumber || c.getType() == TYPE.textFieldString) {
	    v = inflater.inflate(R.layout.text_field, null);
	    final TextView text = (TextView) v.findViewById(R.id.textView);
	    final EditText number = (EditText) v.findViewById(R.id.editText);
	    if (c.getType() == TYPE.textFieldNumber) {
		number.setFilters(new InputFilter[] { new InputFilterMinMax(c.getMax(), c.getMin()) });
		try {
		    if (c.getValue().compareTo("-100.0") != 0) {
			number.setText(c.getValue());
		    }
		}
		catch (Exception e1) {
		    e1.printStackTrace();
		}
	    }
	    else {
		number.setInputType(InputType.TYPE_CLASS_TEXT);
		number.setText(" ");
	    }
	    text.setText(c.getName());

	    number.addTextChangedListener(new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		    try {

			    for (int i = 0; i < feedbackItems.size(); i++) {
				ArrayList<FeedbackItem> tem = feedbackItems.get(i);
				for (int j = 0; j < tem.size(); j++) {
				    FeedbackItem feed = tem.get(j);
				    if (feed.getName().compareTo(text.getText().toString().trim()) == 0) {
					try {
					    feed.setValue(textChange(s));
					}
					catch (NumberFormatException nfe) {
					    feed.setValue(s.toString());
					    clearGroupRadioButton(i, text.getText().toString().trim());
					}
					setFeedbackReflectionValue(feed);
					break;
				    }
				}
			    }

		    }
		    catch (Exception e) {
			e.printStackTrace();
		    }

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

	    });
	    texFieldStringId.put(text.getText().toString(), number.getId());
	    texFieldStringtView.put(text.getText().toString(), number);
	}
	v.setBackgroundColor(UrbanNetApp.defaultColor);

	return v;
    }

    private void clearGroupRadioButton(int i, String current_text) {
	ArrayList<FeedbackItem> tem = feedbackItems.get(i);
	String name;
	for (int k = 0; k < tem.size(); k++) {// item level
	    FeedbackItem feed = tem.get(k);
	    if (feed.getName().compareTo(current_text) != 0) {
		feed.setState(false);
		name = feed.getName();
		try {
		    int id = radioGroup.get(feed.getName());
		    View v = radioGroupView.get(feed.getName());
		    RadioButton button = (RadioButton) v.findViewById(id);
		    button.setChecked(false);
		    if (name.contains(" ")) {
			String[] tem1 = name.split(" ");
			name = tem1[0].charAt(0) + tem1[1];
		    }
		    Method method;
		    if (feed.getType() == TYPE.radio) {
			method = feedback.getClass().getMethod("set" + name, boolean.class);
			method.invoke(feedback, false);
		    }
		}
		catch (SecurityException e) {
		    e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
		    e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
		    e.printStackTrace();
		}
		catch (IllegalAccessException e) {
		    e.printStackTrace();
		}
		catch (InvocationTargetException e) {
		    e.printStackTrace();
		}
		catch (NullPointerException e) {}
	    }
	}

    }

    public double textChange(Editable s) {
	if (!isEmptyEditable(s)) {
	    try {
		return Double.parseDouble(s.toString());
	    }
	    catch (NumberFormatException nfe) {
		throw nfe;
	    }
	}
	else {
	    return 0;
	}
    }

    /**
     * For empty string in EditText
     * @param s
     * @return
     */
    private boolean isEmptyEditable(Editable s) {
	return s.toString().trim().length() == 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
	return feedbackItems.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
	return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
	return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
	return (groupPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
	View v = null;
	if (convertView != null)
	    v = convertView;
	else v = inflater.inflate(R.layout.group_row, parent, false);
	String gt = (String) getGroup(groupPosition);
	TextView feedbackGroup = (TextView) v.findViewById(R.id.childname);
	if (gt != null) feedbackGroup.setText(gt);
	return v;
    }

    @Override
    public boolean hasStableIds() {
	return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
	return true;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
    }

    private void setFeedbackReflectionValue(FeedbackItem feed) {
	String name = feed.getName();
	if (name.contains(" ")) {
	    String[] tem = name.split(" ");
	    name = tem[0].charAt(0) + tem[1];// radio
	}
	try {
	    if (feed.getType() == TYPE.radioText) {
		Method method = feedback.getClass().getMethod("set" + feed.getGroupName(), String.class);
		method.invoke(feedback, feed.getName());
	    }
	    else if (feed.getType() == TYPE.textFieldString) {
		Method method = feedback.getClass().getMethod("set" + feed.getGroupName(), String.class);
		method.invoke(feedback, feed.getValue());
	    }
	    else if (feed.getType() == TYPE.checkBox || feed.getType() == TYPE.radio) {
		Method method = feedback.getClass().getMethod("set" + name, boolean.class);
		method.invoke(feedback, feed.getState());
	    }
	    else if (feed.getType() == TYPE.textFieldNumber) {
		Method method = feedback.getClass().getMethod("set" + feed.getGroupName(), double.class);
		method.invoke(feedback, Double.parseDouble(feed.getValue()));
	    }
	}
	catch (SecurityException e) {
	    e.printStackTrace();
	}
	catch (NoSuchMethodException e) {
	    e.printStackTrace();
	}
	catch (IllegalArgumentException e) {
	    e.printStackTrace();
	}
	catch (IllegalAccessException e) {
	    e.printStackTrace();
	}
	catch (InvocationTargetException e) {
	    e.printStackTrace();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void clearOtherTextField() {
	try {
	    // Clear the other text field from the water brand
	    int id = texFieldStringId.get("Other");
	    View v = texFieldStringtView.get("Other");
	    EditText text = (EditText) v.findViewById(id);
	    text.setText("");
	}
	catch (NullPointerException e) {}
    }
}
