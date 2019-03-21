package uk.ac.shef.oak.com4510.async.database;

import android.os.AsyncTask;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;
import uk.ac.shef.oak.com4510.database.models.Search;

/**
 * An async which deletes a search entry with the specified search.
 * Called when the user presses the delete button in the recent searches.
 */
public class ADeleteSearchTask  extends AsyncTask<Search, Void, Void> {
    private PhotoBuddyDAO photoBuddyDAO;

    public ADeleteSearchTask(PhotoBuddyDAO photoBuddyDAO){
        this.photoBuddyDAO = photoBuddyDAO;
    }

    @Override
    protected Void doInBackground(final Search... search){
        photoBuddyDAO.deleteSearch(search[0]);
        return null;
    }
}
