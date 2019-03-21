package uk.ac.shef.oak.com4510.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import uk.ac.shef.oak.com4510.database.models.MetaData;
import uk.ac.shef.oak.com4510.database.models.Search;

/**
 * The DAO for the database. This defines all of the operations that can be cast to the database,
 * separated by model in this case.
 *
 * All database content inspired by the lectures and lab sheet beginning Week 5, by Fabio Ciravegna
 */
@Dao
public interface PhotoBuddyDAO {

    /**
     * MetaData Model
     */

    @Query("SELECT * FROM metadata WHERE file_location LIKE :filePath LIMIT 1")
    MetaData selectByFilePath(String filePath);

    @Query("SELECT * FROM metadata WHERE takenLongitude != 0.0 AND takenLatitude != 0.0")
    List<MetaData> selectAllImages();

    @Query("SELECT * FROM metadata WHERE title LIKE '%' || :title || '%'")
    List<MetaData> selectTitle(String title);

    @Query("SELECT * FROM metadata WHERE description LIKE '%' || :description || '%'")
    List<MetaData> selectDescription(String description);

    @Query("SELECT * FROM metadata WHERE dateCreated LIKE '%' || :date || '%'")
    List<MetaData> selectDate(String date);

    @Query("UPDATE metadata SET title = :title WHERE id = :id")
    void updateTitleEntry(int id, String title);

    @Query("UPDATE metadata SET description = :description WHERE id = :id")
    void updateDescriptionEntry(int id, String description);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNewEntry(MetaData metaData);

    @Query("DELETE FROM metadata WHERE file_location = :filePath")
    void deleteEntry(String filePath);

    /**
     * Search Model
     */

    @Insert
    void insertNewSearch(Search search);

    @Query("SELECT * FROM search ORDER BY dateCreated DESC")
    List<Search> selectRecentSearches();

    @Delete
    void deleteSearch(Search search);
}
