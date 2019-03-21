package uk.ac.shef.oak.com4510.database.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
/**
 * Model used by the application to store the searches cast by the user in SearchResults activity.
 * Contains the following fields:
 * id - Unique identifier of the data
 * search - Search string cast by the user
 * dateCreated - The date the search was cast
 *
 * All database content inspired by the lectures and lab sheet beginning Week 5, by Fabio Ciravegna
 */

@Entity
public class Search {

    @PrimaryKey(autoGenerate = true)
    @android.support.annotation.NonNull
    private int id=0;

    private String search;
    private String dateCreated;

    @android.support.annotation.NonNull
    public int getId() {
        return id;
    }
    public void setId(@android.support.annotation.NonNull int id) {
        this.id = id;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
