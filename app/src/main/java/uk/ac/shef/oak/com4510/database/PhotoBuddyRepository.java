package uk.ac.shef.oak.com4510.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.ac.shef.oak.com4510.async.database.ADeleteSearchTask;
import uk.ac.shef.oak.com4510.async.database.ASelectFieldsFromSearchTask;
import uk.ac.shef.oak.com4510.async.database.ASelectRecentSearchesTask;
import uk.ac.shef.oak.com4510.async.database.ADeleteEntryTask;
import uk.ac.shef.oak.com4510.async.database.AInsertNewSearch;
import uk.ac.shef.oak.com4510.async.database.AInsertTask;
import uk.ac.shef.oak.com4510.async.database.ASelectSingleTask;
import uk.ac.shef.oak.com4510.async.database.ASelectTask;
import uk.ac.shef.oak.com4510.async.database.AUpdateDescriptionTask;
import uk.ac.shef.oak.com4510.async.database.AUpdateTitleTask;
import uk.ac.shef.oak.com4510.database.models.Search;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.database.models.MetaData;

/**
 * Contains any required logic for operations passed from the ViewModel that needs to be carried out
 * before contacting the DAO. Each entry in the DAO has a definition here. Again, each definition
 * is split by model.
 *
 * All database content inspired by the lectures and lab sheet beginning Week 5, by Fabio Ciravegna
 */
class PhotoBuddyRepository extends ViewModel {
    private final PhotoBuddyDAO photoBuddyDAO;
    private MutableLiveData<List<MetaData>> newMetaData;
    private MutableLiveData<MetaData> singleMetaData;
    private MutableLiveData<List<MetaData>> searchFieldsData;
    private MutableLiveData<List<Search>> recentSearchesMetaData;

    PhotoBuddyRepository(Application application) {
        PhotoBuddyDatabase db = PhotoBuddyDatabase.getDatabase(application);
        photoBuddyDAO = db.photoBuddyDAO();
        newMetaData = new MutableLiveData();
        singleMetaData = new MutableLiveData();
        recentSearchesMetaData = new MutableLiveData();
        searchFieldsData = new MutableLiveData();
    }

    /**
     * MetaData Model
     */

    /**
     * Call the async task that selects each entry in the MetaData model.
     */
    LiveData<List<MetaData>> selectAllEntries() {
        newMetaData = new MutableLiveData();
        new ASelectTask(photoBuddyDAO, newMetaData).execute();

        return newMetaData;
    }

    /**
     * Calls the async task that selects a single entry in the MetaData model.
     * @param filePath - filePath to be queried against.
     */
    LiveData<MetaData> selectSingleEntry(String filePath) {
        new ASelectSingleTask(photoBuddyDAO, singleMetaData).execute(filePath);
        return singleMetaData;
    }

    /**
     * Calls the async task that queries the database for the title, description, and date of
     * all entries.
     * @param query - Query string passed by the SearchResults activity.
     */
    LiveData<List<MetaData>> selectFieldsFromSearch(String query) {
        searchFieldsData = new MutableLiveData<>();
        new ASelectFieldsFromSearchTask(photoBuddyDAO, searchFieldsData).execute(query);
        return searchFieldsData;
    }

    /**
     * Calls the async task that updates a title of the given MetaData reference.
     * @param id - The ID of the MetaData to be changed
     * @param title - The new title to be replaced
     */
    void updateTitleEntry(int id, String title) {
        new AUpdateTitleTask(photoBuddyDAO).execute(id, title);
    }

    /**
     * Calls the async task that updates a description of the given MetaData reference.
     * @param id - The ID of the MetaData to be changed
     * @param description - The new description to be replaced
     */
    void updateDescriptionEntry(int id, String description) {
        new AUpdateDescriptionTask(photoBuddyDAO).execute(id, description);
    }

    /**
     * Called when an image is taken with the camera and has location attached.
     * @param photos - The list of new photos to be added.
     * @param location - The object of the location found.
     */
    void insertNewEntry(List<ImageElement> photos, Location location) {

        for (ImageElement photo : photos) {
            // Extract any extra meta that we need to store.
            photo.extractMeta();

            MetaData metaData = new MetaData();

            metaData.setDateCreated(photo.getDateCreated());
            metaData.setFileLocation(photo.getFileLocation());
            metaData.setTakenLatitude(location.getLatitude());
            metaData.setTakenLongitude(location.getLongitude());

            // Insert into database
            new AInsertTask(photoBuddyDAO).execute(metaData);
        }
    }

    /**
     * Called when an image is found in the gallery.
     * @param photo - The new photo to be added to the MetaData model.
     */
    void insertSingleton(ImageElement photo) {
        // Extract any extra meta that we need to store.
        photo.extractMeta();

        MetaData metaData = new MetaData();
        metaData.setDateCreated(photo.getDateCreated());
        metaData.setFileLocation(photo.getFileLocation());

        // Insert into database
        new AInsertTask(photoBuddyDAO).execute(metaData);
    }

    /**
     * Calls the async task that deletes a single entry in the MetaData model
     * when given the filePath.
     * @param filePath - The filePath whose entry will be deleted.
     */
    void deleteEntry(String filePath) {
        new ADeleteEntryTask(photoBuddyDAO).execute(filePath);
    }

    /**
     * Search Model
     */

    /**
     * Calls the async task that inserts a new search in the Search model.
     * @param search - The search string to be added to the model.
     */
    void insertNewSearch(String search) {
        Search newSearch = new Search();
        newSearch.setSearch(search);

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
        String timestamp = simpleDateFormat.format(date);
        newSearch.setDateCreated(timestamp);

        new AInsertNewSearch(photoBuddyDAO).execute(newSearch);
    }

    /**
     * Calls the async task that selects all of the searches in date order.
     */
    LiveData<List<Search>> selectRecentSearches() {
        recentSearchesMetaData = new MutableLiveData();
        new ASelectRecentSearchesTask(photoBuddyDAO, recentSearchesMetaData).execute();
        return recentSearchesMetaData;
    }

    /**
     * Calls the async task that deletes a singular Search from the Search model.
     * @param search - The search object to be deleted from the model.
     */
    void deleteSearch(Search search) {
        new ADeleteSearchTask(photoBuddyDAO).execute(search);
    }
}