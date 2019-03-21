package uk.ac.shef.oak.com4510.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import uk.ac.shef.oak.com4510.database.models.MetaData;
import uk.ac.shef.oak.com4510.database.models.Search;

/**
 * Definition of the database, which includes both models of MetaData and Search. Explains how the
 * database is built by the system.
 *
 * All database content inspired by the lectures and lab sheet beginning Week 5, by Fabio Ciravegna
 */
@android.arch.persistence.room.Database(entities = {MetaData.class, Search.class}, version = 1, exportSchema = false)
public abstract class PhotoBuddyDatabase extends RoomDatabase {

    public abstract PhotoBuddyDAO photoBuddyDAO();

    // marking the instance as volatile to ensure atomic access to the variable
    private static volatile PhotoBuddyDatabase INSTANCE;

    public static PhotoBuddyDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PhotoBuddyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = android.arch.persistence.room.Room.databaseBuilder(context.getApplicationContext(),
                            PhotoBuddyDatabase.class, "photo_buddy_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     *
     * If you want to populate the database only when the database is created for the 1st time,
     * override RoomDatabase.Callback()#onCreate
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // do any init operation about any initialisation here
        }
    };

}
