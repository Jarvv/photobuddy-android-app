package uk.ac.shef.oak.com4510.async.database;

import android.os.AsyncTask;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;

/**
 * An async task that updates the description in a singular entry in the MetaData model
 * when given the ID and new string.
 */
public class AUpdateDescriptionTask extends AsyncTask<Object, Void, Void> {
    private PhotoBuddyDAO photoBuddyDAO;

    public AUpdateDescriptionTask(PhotoBuddyDAO photoBuddyDAO) {
        this.photoBuddyDAO = photoBuddyDAO;
    }

    @Override
    protected Void doInBackground(Object... params) {
        photoBuddyDAO.updateDescriptionEntry((int)params[0], (String)params[1]); // ID in database and update string.
        return null;
    }
}
