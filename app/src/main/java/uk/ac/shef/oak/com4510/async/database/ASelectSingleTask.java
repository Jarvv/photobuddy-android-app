package uk.ac.shef.oak.com4510.async.database;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import uk.ac.shef.oak.com4510.database.PhotoBuddyDAO;
import uk.ac.shef.oak.com4510.database.models.MetaData;

/**
 * An async task that loads a single result from the database when given the file path. Used mostly
 * by the ImageShow activity.
 */
public class ASelectSingleTask extends AsyncTask<String, Void, MetaData> {
    private PhotoBuddyDAO photoBuddyDAO;
    private MutableLiveData<MetaData> singleMetaData;

    public ASelectSingleTask(PhotoBuddyDAO photoBuddyDAO, MutableLiveData<MetaData> singleMetaData){
        this.photoBuddyDAO = photoBuddyDAO;
        this.singleMetaData = singleMetaData;
    }

    @Override
    protected MetaData doInBackground(final String... filePath){
        return photoBuddyDAO.selectByFilePath(filePath[0]);
    }

    @Override
    protected void onPostExecute(MetaData result)  {
        singleMetaData.setValue(result);
    }
}
