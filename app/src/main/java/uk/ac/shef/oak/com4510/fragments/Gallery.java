package uk.ac.shef.oak.com4510.fragments;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import uk.ac.shef.oak.com4510.BaseActivity;
import uk.ac.shef.oak.com4510.adapters.ImageAdapter;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.util.Permissions;
import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * The Gallery fragment gives the user the ability to view all of their photos from their external
 * media storage in a grid format. Users are able to take their own photos using EasyImage in which
 * the users location will be stored with this photo in the model. Users can click on any ImageElement
 * to open up the ImageShow activity.
 */
public class Gallery extends Fragment {

    private List<ImageElement> myPictureList = new ArrayList<>();
    private List<ImageElement> myTakenList = new ArrayList<>();
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;
    private FragmentActivity listener;
    private Permissions permissions;
    private Activity activity;
    private Fragment fragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            this.listener = (FragmentActivity) context;
            activity = getActivity();
            permissions = new Permissions(activity);
            fragment = this;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceStatee) {
        return inflater.inflate(R.layout.content_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){

        // Checks the orientation of the screen to specify the number of columns for the RecyclerView
        // grid
        int numberOfColumns = 0;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            numberOfColumns = 8;
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            numberOfColumns = 4;
        }

        // Set up the RecyclerView
        mRecyclerView = view.findViewById(R.id.gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(activity, numberOfColumns));
        mAdapter = new ImageAdapter(this,myPictureList);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        if(savedInstanceState != null){
            // Scroll to existing position before rotation.
            mRecyclerView.scrollToPosition(savedInstanceState.getInt("position"));
        }

        permissions.requestReadExternalStorage();
        if(permissions.checkReadExternalStorage() ){
            onFragmentPhotosReturned(getGalleryImages(), false);
        }

        // Open the camera using the fab
        FloatingActionButton fab = view.findViewById(R.id.fab_camera);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ((BaseActivity)activity).openEasyImageActivity();
            }
        });
    }

    /**
     * Get the current position of the RecyclerView.
     * @return - The position of the first visible item in the RecyclerView
     */
    private int getCurrentItem(){
        return ((GridLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 100 && resultCode == Activity.RESULT_OK) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissionList[], @NonNull int[] grantResults){
        if(requestCode == permissions.REQUEST_READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            permissions.requestWriteExternalStorage();
            onFragmentPhotosReturned(getGalleryImages(), false);
        } else if(requestCode == permissions.REQUEST_READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            permissions.displayRationale("Reading external storage permission is necessary.", new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, permissions.REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current item position so that the user does not lose their progress in the RecyclerView.
        savedInstanceState.putInt("position", getCurrentItem());
        super.onSaveInstanceState(savedInstanceState);
    }


    /**
     * Given a list of files, add their associated ImageElements to the ImageAdapter.
     * @param returnedPhotos - A list of Files of photos.
     * @param attachCurrentLocation - When a photo is taken from EasyImage, attempt to attach the
     *                              current location.
     */
    public void onFragmentPhotosReturned(List<File> returnedPhotos, Boolean attachCurrentLocation) {
        // When taken from the camera, try and attach a location associated to the photo
        if(attachCurrentLocation) {
            // Add to the taken list and reset the URI for each file.
            myTakenList.addAll(0,ImageAdapter.getImageElements(returnedPhotos));
            myTakenList = ((BaseActivity)activity).resetURI(myTakenList);
            myPictureList.addAll(0, myTakenList);

            // Check that we have location permissions
            permissions.requestLocation();
            ((BaseActivity)activity).collateLocation(myTakenList);
        } else {
            myPictureList.addAll(0,ImageAdapter.getImageElements(returnedPhotos));
        }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Get's all photos from the users external media storage, in descending date order.
     * @return - The list of all the photos in the users external gallery.
     */
    public List<File> getGalleryImages(){
        List<File> listOfAllImages = new ArrayList<File>();

        // External media storage URI
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Create a cursor which collects all photos from all folders in the external media storage
        String column = "_data";
        String[] projection = {column};
        Cursor cursor = fragment.getActivity().getContentResolver().query(uri, projection, null,
                null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        int columnIndex = cursor.getColumnIndex(column);

        // For each file the cursor finds, add the image to the list
        while (cursor.moveToNext()) {
            File image = new File(cursor.getString(columnIndex));

            if(image.exists())
                listOfAllImages.add(image);
        }

        return listOfAllImages;
    }
}
