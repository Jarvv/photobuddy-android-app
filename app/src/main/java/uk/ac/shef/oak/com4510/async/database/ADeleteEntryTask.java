package uk.ac.shef.oak.com4510.async.database;

import android.os.AsyncTask;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;

/**
 * An async task which deletes an entry with the specified filepath.
 * Called when a user requests to delete an image in ImageShow
 */
public class ADeleteEntryTask extends AsyncTask<String, Void, Void> {
    private PhotoBuddyDAO photoBuddyDAO;

    public ADeleteEntryTask(PhotoBuddyDAO photoBuddyDAO){
        this.photoBuddyDAO = photoBuddyDAO;
    }

    @Override
    protected Void doInBackground(final String... filePath){
        photoBuddyDAO.deleteEntry(filePath[0]);
        return null;
    }
}