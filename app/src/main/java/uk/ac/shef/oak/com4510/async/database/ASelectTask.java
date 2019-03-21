package uk.ac.shef.oak.com4510.async.database;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;
import uk.ac.shef.oak.com4510.database.models.MetaData;

/**
 * An async task that returns all of the images in the database. Used by the map fragment to load
 * pins that are nearby.
 */
public class ASelectTask extends AsyncTask<Void, Void, List<MetaData>> {
    private PhotoBuddyDAO photoBuddyDAO;
    private MutableLiveData<List<MetaData>> newMetaData;

    public ASelectTask(PhotoBuddyDAO photoBuddyDAO, MutableLiveData<List<MetaData>> newMetaData){
        this.photoBuddyDAO = photoBuddyDAO;
        this.newMetaData = newMetaData;
    }

    @Override
    protected List<MetaData> doInBackground(final Void... params){
        return photoBuddyDAO.selectAllImages();
    }

    @Override
    protected void onPostExecute(List<MetaData> result)  {
        newMetaData.setValue(result);
    }
}