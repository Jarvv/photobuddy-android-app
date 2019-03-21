package uk.ac.shef.oak.com4510.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * The class that handles all of the permissions in the application.
 * From checking it's granted, to requesting permission, this class looks after the
 * 3 permissions: READ STORAGE, WRITE STORAGE, and LOCATION.
 *
 * All credit to Fabio Ciravegna who first presented the idea for this code in Lab session 4.
 */
public class Permissions {
    public final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    public final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;
    public final int ACCESS_FINE_LOCATION = 123;
    private int currentAPIVersion = Build.VERSION.SDK_INT;
    private Activity activity;

    public Permissions(Activity activity) {
        this.activity = activity;
    }

    /**
     * Check whether we have readability of external storage.
     * @return boolean - If we have permission or not.
     */
    public boolean checkReadExternalStorage() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check whether we have write-ability of external storage.
     * @return boolean - If we have permission or not.
     */
    public boolean checkWriteExternalStorage() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check whether we have location access.
     * @return boolean - If we have permission or not.
     */
    public boolean checkLocation() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request reading of external storage permissions, and display rationale if the user rejects.
     */
    public void requestReadExternalStorage() {
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (!checkReadExternalStorage()) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE))
                    displayRationale("Reading external storage permission is necessary.", new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                else
                    activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    /**
     * Request writing of external storage permissions, and display rationale if the user rejects.
     */
    public void requestWriteExternalStorage(){
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (!checkWriteExternalStorage()) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    displayRationale("Writing external storage permission is necessary.", new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                else
                    activity.requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    /**
     * Request location permissions, and display rationale if the user rejects.
     */
    public void requestLocation() {
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if(!checkLocation()) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION))
                    displayRationale("Allowing location when taking images will help you on the map.", new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
                else
                    activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
            }
        }
    }

    /**
     * Show to the user that the permission is required for the application to work.
     */
    public void displayRationale(String alertMessage, final String[] permission, final int pCode){
        android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(activity);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(alertMessage);
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(DialogInterface dialog, int which) {
                activity.requestPermissions(permission, pCode);
            }
        });
        android.support.v7.app.AlertDialog alert = alertBuilder.create();
        alert.show();
    }


}
