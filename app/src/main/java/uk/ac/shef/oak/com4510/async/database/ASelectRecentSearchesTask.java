package uk.ac.shef.oak.com4510.async.database;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;
import uk.ac.shef.oak.com4510.database.models.Search;

/**
 * A async task that selects all of the searches that the user has carried out.
 * Called by SearchResults activity when loaded.
 */
public class ASelectRecentSearchesTask extends AsyncTask<Void, Void, List<Search>> {
    private PhotoBuddyDAO photoBuddyDAO;
    private MutableLiveData<List<Search>> searches;

    public ASelectRecentSearchesTask(PhotoBuddyDAO photoBuddyDAO, MutableLiveData<List<Search>> searches){
        this.photoBuddyDAO = photoBuddyDAO;
        this.searches = searches;
    }

    @Override
    protected List<Search> doInBackground(final Void... params){
        return photoBuddyDAO.selectRecentSearches();
    }

    @Override
    protected void onPostExecute(List<Search> result)  {
        searches.setValue(result);
    }
}
