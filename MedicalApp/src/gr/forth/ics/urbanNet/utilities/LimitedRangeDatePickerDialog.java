package gr.forth.ics.urbanNet.utilities;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

public class LimitedRangeDatePickerDialog extends DatePickerDialog {

	private Calendar minDate;
	private Calendar maxDate;
	private java.text.DateFormat mTitleDateFormat;

	public LimitedRangeDatePickerDialog(Context context, DatePickerDialog.OnDateSetListener callBack,
		int year, int monthOfYear, int dayOfMonth, Calendar minDate, Calendar maxDate) {
		super(context, callBack, year, monthOfYear, dayOfMonth);
		this.minDate = minDate;
		this.maxDate = maxDate;
		mTitleDateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL);
	}

	@Override
	public void onDateChanged(DatePicker view, int year, int month, int day) {
		if (year > maxDate.get(Calendar.YEAR) || month > maxDate.get(Calendar.MONTH) && year == maxDate.get(Calendar.YEAR) ||
			day > maxDate.get(Calendar.DAY_OF_MONTH) && year == maxDate.get(Calendar.YEAR) && month == maxDate.get(Calendar.MONTH)) {
			view.updateDate(maxDate.get(Calendar.YEAR), maxDate.get(Calendar.MONTH), maxDate.get(Calendar.DAY_OF_MONTH));
		}
		else if (year < minDate.get(Calendar.YEAR) || month < minDate.get(Calendar.MONTH) && year == minDate.get(Calendar.YEAR) ||
			day < minDate.get(Calendar.DAY_OF_MONTH) && year == minDate.get(Calendar.YEAR) && month == minDate.get(Calendar.MONTH)) {
			view.updateDate(minDate.get(Calendar.YEAR), minDate.get(Calendar.MONTH), minDate.get(Calendar.DAY_OF_MONTH));
		}
	}
}

