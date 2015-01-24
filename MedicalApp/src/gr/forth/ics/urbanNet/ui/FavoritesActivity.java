package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import gr.forth.ics.urbanNet.database.DatabaseHelper;
import gr.forth.ics.urbanNet.database.Query;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.medicalapp.R;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.j256.ormlite.dao.Dao;

/**
 * @author katsarakis
 */
public class FavoritesActivity extends OrmLiteBaseListActivity<DatabaseHelper> /* implements OnDismissListener */{

    private QueryArrayAdapter queryArrayAdapter;
    ArrayList<Query> queryList;
    TextView noQueries;

    /**
     * Custom adapter for displaying an ArrayList of Query objects.
     * @see http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
     * @see http://windrealm.org/tutorials/android/listview-with-checkboxes-without-listactivity.php
     * @author katsarakis
     */
    private class QueryArrayAdapter extends ArrayAdapter<Query> {
	private LayoutInflater inflater;

	public QueryArrayAdapter(Context context, int textViewResourceId, List<Query> queryList) {
	    super(context, textViewResourceId, queryList);
	    // Cache the LayoutInflate to avoid asking for a new one each time.
	    inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    Query query = this.getItem(position); // query to display

	    // Create a new history_item view
	    convertView = inflater.inflate(R.layout.historyfavorites_item, null);
	    convertView.setMinimumHeight(100);

	    // Find the children elements of the current history_item view
	    CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.favoriteCheckBox);
	    TextView topText = (TextView) convertView.findViewById(R.id.toptext);
	    TextView middleText = (TextView) convertView.findViewById(R.id.middletext);
	    TextView bottomText = (TextView) convertView.findViewById(R.id.bottomtext);
	    LinearLayout favoriteTextViews = (LinearLayout) convertView.findViewById(R.id.favoriteTextViews);

	    // Set text on the above TextViews elements
	    // topText.setText(this.getItem(position).getQueryMode());
	    topText.setText(Query.queryType[query.getQueryMode()]);
	    middleText.setText(DateFormat.getDateTimeInstance().format(this.getItem(position).getDate()));
	    // middleText.setText("near FORTH-Hellas");
	    // bottomText.setText(DateFormat.getDateTimeInstance().format(this.getItem(position).getDate()));

	    bottomText.setVisibility(View.GONE);

	    // If CheckBox is toggled, update the query it is tagged with.
	    checkBox.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    CheckBox cb = (CheckBox) v;
		    Query query = (Query) cb.getTag();
		    query.setFavorite(cb.isChecked());
		    try {
			getHelper().getQueryDao().update(query);
		    }
		    catch (SQLException e) {
			e.printStackTrace();
		    }

		}
	    });

	    // If the text of the history_item is clicked, send the query.
	    // Idea: Maybe instead of 2 Textviews to use a Button?
	    favoriteTextViews.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    LinearLayout fvt = (LinearLayout) v;
		    Query query = (Query) fvt.getTag();
		    Intent intent = new Intent(getApplicationContext(), MapViewActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    Bundle b = new Bundle();
		    b.putSerializable("query", query);
		    intent.putExtras(b);
		    startActivity(intent);
		    finish();
		}
	    });

	    // Tag the CheckBox with the Query it is displaying, so that we can
	    // access the query in onClick() when the CheckBox is toggled.
	    checkBox.setTag(query);
	    checkBox.setChecked(query.isFavorite());

	    // Tag the CheckBox with the Query it is displaying, so that we can
	    // access the query in onClick() when the CheckBox is toggled.
	    favoriteTextViews.setTag(query);

	    return convertView;
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	setContentView(R.layout.favorites_view);
	noQueries = (TextView) findViewById(R.id.textView1);
	loadQueries();
    }

    @Override
    public void onResume() {
	super.onResume();
	loadQueries();
    }

    private void loadQueries() {
	noQueries.setVisibility(View.GONE);
	try {
	    // get our dao
	    Dao<Query, Integer> queryDao = getHelper().getQueryDao();
	    queryList = (ArrayList<Query>) queryDao.queryBuilder().orderBy("date", false).where().eq("is_favorite", true).query();
	    if (queryList.size() == 0) {
		noQueries.setText("No Favorite Queries");
		noQueries.setVisibility(View.VISIBLE);
	    }
	    else {
		this.queryArrayAdapter = new QueryArrayAdapter(getApplicationContext(), R.layout.historyfavorites_item, queryList);
		setListAdapter(this.queryArrayAdapter);
	    }
	}
	// catch (SQLException e) {
	catch (Exception e) {
	    noQueries.setText("Problem on retrieving queries");
	    noQueries.setVisibility(View.VISIBLE);
	    e.printStackTrace();
	}
    }

    @Override
    public void onBackPressed() {
	Intent intent = new Intent(getApplicationContext(), MapViewActivity.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	startActivity(intent);
	finish();
    }

}
