package uk.ac.shef.oak.com4510.async.database;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;
import uk.ac.shef.oak.com4510.database.models.MetaData;

/**
 * An async task that searches the database for the matching fields to the given query.
 * When given a string, the task searches for results in title, description and date.
 */
public class ASelectFieldsFromSearchTask extends AsyncTask<String, Void, List<MetaData>> {
    private PhotoBuddyDAO photoBuddyDAO;
    private MutableLiveData<List<MetaData>> searchFieldMetaData;

    public ASelectFieldsFromSearchTask(PhotoBuddyDAO photoBuddyDAO, MutableLiveData<List<MetaData>> searchFieldMetaData) {
        this.photoBuddyDAO = photoBuddyDAO;
        this.searchFieldMetaData = searchFieldMetaData;
    }

    /**
     * For the given query, add results of selectTitle and selectDescription to the list to be
     * returned. Then split the date into / separated chunks and flip to match how dates are
     * saved in the database, then add the results to this same list.
     * @param query - The search query to be searched against.
     */
    @Override
    protected List<MetaData> doInBackground(final String... query) {
        List<MetaData> backgroundList = new ArrayList<>();

        backgroundList.addAll(photoBuddyDAO.selectTitle(query[0]));
        backgroundList.addAll(photoBuddyDAO.selectDescription(query[0]));

        // Split and flip the date to match how it is in the database.
        // Cater for potential missing fields (IE no year/month.
        String[] dateSplit = query[0].split("/");
        String dateString;
        switch(dateSplit.length) {
            case 3: dateString = dateSplit[2] + ":" + dateSplit[1] + ":" + dateSplit[0]; break;
            case 2: dateString = dateSplit[1] + ":" + dateSplit[0]; break;
            case 1: dateString = dateSplit[0]; break;
            default:
                dateString = query[0]; break; // Default to the given string if no matches.
        }

        backgroundList.addAll(photoBuddyDAO.selectDate(dateString));
        return backgroundList;
    }

    @Override
    protected void onPostExecute(List<MetaData> result) {
        searchFieldMetaData.setValue(result);
    }
}