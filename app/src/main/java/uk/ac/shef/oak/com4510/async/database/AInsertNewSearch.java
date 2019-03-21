package uk.ac.shef.oak.com4510.async.database;

import android.os.AsyncTask;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;
import uk.ac.shef.oak.com4510.database.models.Search;

/**
 * An async which inserts a search entry with the specified search.
 * Called by the SearchResults activity. Only called when the user presses enter on their
 * keyboard or when an image is selected.
 */
public class AInsertNewSearch extends AsyncTask<Search, Void, Void> {
    private PhotoBuddyDAO photoBuddyDAO;

    public AInsertNewSearch(PhotoBuddyDAO photoBuddyDAO) {
        this.photoBuddyDAO = photoBuddyDAO;
    }

    @Override
    protected Void doInBackground(final Search... params) {
        photoBuddyDAO.insertNewSearch(params[0]);
        return null;
    }
}