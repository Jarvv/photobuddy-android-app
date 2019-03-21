package uk.ac.shef.oak.com4510.util;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import java.util.Calendar;

/**
 * Simple helper that shows the date picker in the search.
 *
 * Credit: Android Developers, https://developer.android.com/guide/topics/ui/controls/pickers.
 */
public class SearchDatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public SearchView searchView;

    /**
     * When called, we set the current date as the default value and show the dialog to the
     * user.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    /**
     * When a date is picked we build a date string that can then be placed into the search box.
     */
    public void onDateSet(android.widget.DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        String monthString = Integer.toString(month+1);
        String dayString = Integer.toString(day);

        if(monthString.length() == 1) monthString = "0" + monthString;
        if(dayString.length() == 1) dayString = "0" + dayString;

        String query;
        // If there is already text, add the comma
        if (searchView.getQuery().length() > 0)
            query = searchView.getQuery() + ", " + dayString + "/" + monthString + "/" + Integer.toString(year);
        else // Otherwise just add the date as is.
            query = dayString + "/" + monthString + "/" + Integer.toString(year);
        searchView.setQuery(query, false);
    }

    public void setSearchView(SearchView searchView) {
        this.searchView = searchView;
    }
}