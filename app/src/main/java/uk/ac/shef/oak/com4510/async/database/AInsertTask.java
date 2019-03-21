package uk.ac.shef.oak.com4510.async.database;

import android.os.AsyncTask;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;
import uk.ac.shef.oak.com4510.database.models.MetaData;

/**
 * An async task which inserts a new metadata entry given the specified params.
 * Called from gallery when a new image is spotted.
 */
public class AInsertTask extends AsyncTask<MetaData, Void, Void> {
    private PhotoBuddyDAO photoBuddyDAO;

    public AInsertTask(PhotoBuddyDAO photoBuddyDAO) {
        this.photoBuddyDAO = photoBuddyDAO;
    }

    @Override
    protected Void doInBackground(final MetaData... params) {
        photoBuddyDAO.insertNewEntry(params[0]);
        return null;
    }
}