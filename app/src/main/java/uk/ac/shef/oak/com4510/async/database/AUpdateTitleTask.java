package uk.ac.shef.oak.com4510.async.database;

import android.os.AsyncTask;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;

/**
 * An async task that updates the title of a singular entry in the MetaData model when given an
 * ID and update string.
 */
public class AUpdateTitleTask extends AsyncTask<Object, Void, Void> {
    private PhotoBuddyDAO photoBuddyDAO;

    public AUpdateTitleTask(PhotoBuddyDAO photoBuddyDAO) {
        this.photoBuddyDAO = photoBuddyDAO;
    }

    @Override
    protected Void doInBackground(Object... params) {
        photoBuddyDAO.updateTitleEntry((int)params[0], (String)params[1]); // ID of the entry and update string.
        return null;
    }
}