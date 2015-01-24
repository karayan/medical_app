package gr.forth.ics.urbanNet.ui;

//import gr.forth.ics.urbanNet.R;
import com.example.medicalapp.R;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class ResponseListAdapter extends ArrayAdapter<ResponseItem> {
    Context context;

    public ResponseListAdapter(Context context, int resourceId, List<ResponseItem> items) {
	super(context, resourceId, items);
	this.context = context;
    }

    /* private view holder class */
    private class ViewHolder {
	ImageView providerModeIcon;
	TextView providerTitle;
	TextView providerRate;
	RatingBar providerRatingBar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) throws IndexOutOfBoundsException {
	ViewHolder holder = null;
	ResponseItem item = getItem(position);
	LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	if (convertView == null) {
	    convertView = mInflater.inflate(R.layout.providers_list, null);
	    holder = new ViewHolder();
	    holder.providerTitle = (TextView) convertView.findViewById(R.id.provider_title);
	    holder.providerModeIcon = (ImageView) convertView.findViewById(R.id.provider_mode_icon);
	    holder.providerRatingBar = (RatingBar) convertView.findViewById(R.id.provider_rating_bar);
	    holder.providerRate = (TextView) convertView.findViewById(R.id.provider_rate);
	    convertView.setTag(holder);
	}
	else {
	    holder = (ViewHolder) convertView.getTag();
	}

	holder.providerTitle.setText(item.getTitle());
	holder.providerModeIcon.setImageResource(item.getImageId());
	if (item.getNumericRate() == -1) {
	    holder.providerRatingBar.setVisibility(View.GONE);
	    holder.providerRate.setText(item.getRate());
	}
	else {
	    holder.providerRatingBar.setRating(item.getNumericRate());
	    holder.providerRate.setText("(" + item.getRate() + " / 5)");
	}
	return convertView;
    }

}
