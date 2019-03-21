package uk.ac.shef.oak.com4510.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.location.Location;

import java.util.List;

import uk.ac.shef.oak.com4510.database.models.Search;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.database.models.MetaData;

/**
 * The ViewModel that connects the UI to the database operations for the application. Contains
 * the entry of each database operation, which then passes through to the repository. As in the
 * Repository file, the operations are split up into a model basis.
 *
 * All database content inspired by the lectures and lab sheet beginning Week 5, by Fabio Ciravegna
 */
public class PhotoBuddyViewModel extends AndroidViewModel {
    private final PhotoBuddyRepository photoBuddyRepository;
    private LiveData<List<MetaData>> metaDataLiveData;
    private LiveData<MetaData> singleMetaDataLiveData;
    private LiveData<List<MetaData>> searchFieldsData;
    private LiveData<List<Search>> recentSearchesLiveData;

    public PhotoBuddyViewModel(Application application) {
        super(application);
        photoBuddyRepository = new PhotoBuddyRepository(application);
    }

    /**
     * MetaData Model
     */

    public LiveData<MetaData> selectSingleEntry(String filePath){
        if(singleMetaDataLiveData == null){
            singleMetaDataLiveData = photoBuddyRepository.selectSingleEntry(filePath);
        }
        return singleMetaDataLiveData;
    }

    public LiveData<List<MetaData>> selectEntries(){
        metaDataLiveData = photoBuddyRepository.selectAllEntries();
        return  metaDataLiveData;
    }

    public LiveData<List<MetaData>> selectFieldsFromSearch(String query) {
        searchFieldsData = photoBuddyRepository.selectFieldsFromSearch(query);
        return searchFieldsData;
    }

    public void updateTitleEntry(int id, String title){
        photoBuddyRepository.updateTitleEntry(id, title);
    }

    public void updateDescriptionEntry(int id, String title){
        photoBuddyRepository.updateDescriptionEntry(id, title);
    }

    public void insertNewEntry(List<ImageElement> photos, Location currentLocation) {
        photoBuddyRepository.insertNewEntry(photos, currentLocation);
    }

    public void insertSingleton(ImageElement photo) {
        photoBuddyRepository.insertSingleton(photo);
    }

    public void deleteEntry(String filePath) {
        photoBuddyRepository.deleteEntry(filePath);
    }

    /**
     * Search Model
     */

    public void insertNewSearch(String search) {
        photoBuddyRepository.insertNewSearch(search);
    }

    public LiveData<List<Search>> selectRecentSearches() {
        recentSearchesLiveData = photoBuddyRepository.selectRecentSearches();
        return recentSearchesLiveData;
    }

    public void deleteSearch(Search search) {
        photoBuddyRepository.deleteSearch(search);
    }
}
