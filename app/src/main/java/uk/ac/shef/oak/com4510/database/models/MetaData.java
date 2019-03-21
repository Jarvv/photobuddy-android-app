package uk.ac.shef.oak.com4510.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * The main model in the application, this holds the data for each image seen in the gallery,
 * and is called upon in ImageShow, Search and Map. Contains the following fields:
 * id - Unique identifier of the data
 * title - The title of the image
 * dateCreated - The date the file was created (not when added to database)
 * takenLatitude - Latitude of the image location (if present)
 * takenLongitude - Longitude of the image location (if present)
 * fileLocation - Where the image is stored on the device. Also a unique identifier for the data.
 *
 * All database content inspired by the lectures and lab sheet beginning Week 5, by Fabio Ciravegna
 */
@Entity(indices = {@Index(value = {"file_location"}, unique=true)})

public class MetaData {
    @PrimaryKey(autoGenerate = true)
    @android.support.annotation.NonNull
    private int id=0;

    private String title;
    private String dateCreated;
    private String description;
    private double takenLatitude;
    private double takenLongitude;

    @ColumnInfo(name = "file_location")
    private String fileLocation;

    @android.support.annotation.NonNull
    public int getId() {
        return id;
    }
    public void setId(@android.support.annotation.NonNull int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTakenLatitude() {
        return takenLatitude;
    }

    public void setTakenLatitude(double takenLatitude) {
        this.takenLatitude = takenLatitude;
    }

    public double getTakenLongitude() {
        return takenLongitude;
    }

    public void setTakenLongitude(double takenLongitude) {
        this.takenLongitude = takenLongitude;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

}
