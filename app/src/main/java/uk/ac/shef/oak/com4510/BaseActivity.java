package uk.ac.shef.oak.com4510;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import uk.ac.shef.oak.com4510.activities.About;
import uk.ac.shef.oak.com4510.activities.Help;
import uk.ac.shef.oak.com4510.activities.SearchResults;
import uk.ac.shef.oak.com4510.activities.WelcomeSplash;
import uk.ac.shef.oak.com4510.adapters.ImageAdapter;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;
import uk.ac.shef.oak.com4510.fragments.Gallery;
import uk.ac.shef.oak.com4510.fragments.Map;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.util.Permissions;

/**
 * The BaseActivity acts as the apps 'home' activity. This activity is where the apps search-bar and menus are
 * defined and then with the use of the fragment placeholder in which fragments can be exchanged
 * whilst keeping the activities features.
 */
public class BaseActivity extends AppCompatActivity{
    private Activity activity;
    private BottomNavigationView bnv;
    private Permissions permissions;
    private List<ImageElement> myTakenList;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private PhotoBuddyViewModel photoBuddyViewModel;
    private int previousSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        activity = this;
        permissions = new Permissions(this);
        setupViewModel();

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bnv = findViewById(R.id.navigationView);

        // Set up drawer menu
        final DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);

        final ImageView menuIcon = findViewById(R.id.burger_button);

        // Setup EasyImage
        if(permissions.checkWriteExternalStorage())
            initEasyImage();

        // Open the Gallery Fragment, which acts as the 'home' fragment.
        // If the device has been rotated, check if the map fragment was open at the time, open that
        // instead.
        if(savedInstanceState != null && savedInstanceState.getInt("fragment") == R.id.navigation_map){
            Map map = new Map();
            getSupportFragmentManager().popBackStackImmediate();
            openFragment(map, "MAP");

        } else {
            Gallery gallery = new Gallery();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_placeholder,gallery, "GALLERY");
            transaction.commit();
        }

        // The bottom navigation menu
        bnv.setOnNavigationItemSelectedListener(
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    // If the user clicks on the item they are currently on, do not begin
                    // another transaction.
                    if(menuItem.getItemId() != bnv.getSelectedItemId()){
                        previousSelected = bnv.getSelectedItemId();
                        switch (menuItem.getItemId()) {
                            case R.id.navigation_gallery:
                                Gallery gallery = new Gallery();
                                openFragment(gallery, "GALLERY");
                                break;

                            case R.id.navigation_photo:
                                openEasyImageActivity();
                                break;

                            case R.id.navigation_map:
                                Map map = new Map();
                                openFragment(map, "MAP");
                                break;
                        }
                    }
                    return true;
                }
            }
        );

        // Open the Drawer Menu when the menu icon is clicked.
        menuIcon.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        );

        // Open the activity of choice when clicked on in the Drawer Menu
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    mDrawerLayout.closeDrawers();

                    switch (menuItem.getTitle().toString()) {
                        case "Introduction":
                            Intent welcomeIntent = new Intent(activity, WelcomeSplash.class);
                            activity.startActivity(welcomeIntent);
                            break;

                        case "Help":
                            Intent helpIntent = new Intent(activity, Help.class);
                            activity.startActivity(helpIntent);
                            break;

                        case "About":
                            Intent aboutIntent = new Intent(activity, About.class);
                            activity.startActivity(aboutIntent);
                            break;
                    }
                    return false;
                }
            }
        );

        handleSearch();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == Activity.RESULT_OK) {
            switch(bnv.getSelectedItemId()) {
                case R.id.navigation_gallery:
                    Gallery gallery = new Gallery();
                    openFragment(gallery, "GALLERY");
                    break;
                case R.id.navigation_map:
                    Map map = new Map();
                    openFragment(map, "MAP");
                    break;
            }
        }

        final Fragment fragInFocus = getSupportFragmentManager().findFragmentByTag("GALLERY");
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                if (fragInFocus != null && fragInFocus.isVisible()) {
                    ((Gallery)fragInFocus).onFragmentPhotosReturned(imageFiles, true);
                }else{
                    onActivityPhotosReturned(imageFiles);
                    getSupportFragmentManager().popBackStackImmediate();
                    bnv.setSelectedItemId(previousSelected);
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(activity);
                    if (photoFile != null) photoFile.delete();
                }

                if (fragInFocus != null && fragInFocus.isVisible()) {
                    setGallerySelected();
                }else{
                    getSupportFragmentManager().popBackStackImmediate();
                    bnv.setSelectedItemId(previousSelected);
                }

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Fragment fragInFocus = getSupportFragmentManager().findFragmentByTag("MAP");

        if(fragInFocus != null && fragInFocus.isVisible()){
            savedInstanceState.putInt("fragment", R.id.navigation_map);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissionList[], @NonNull int[] grantResults){
        switch (requestCode) {
            // Location permissions
            case 123: {
                // Location results granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If the map is in focus, and no pictures have been taken
                    Fragment fragInFocus = getSupportFragmentManager().findFragmentByTag("MAP");
                    if(fragInFocus != null && fragInFocus.isVisible() && myTakenList == null){
                        ((Map)fragInFocus).startLocationUpdates();
                    }else {
                        // A picture has been taken
                        mLocationRequest = new LocationRequest();
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    }
                } else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    permissions.displayRationale("Allowing location when taking images will help you on the map.",
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, permissions.ACCESS_FINE_LOCATION);
                }
            }
        }
    }

    /**
     * If back pressed on the Gallery fragment, close the app, else switch back to the Gallery
     * fragment.
     */
    @Override
    public void onBackPressed(){
        // The item currently selected in the BottomNav
        int selectedItemId = bnv.getSelectedItemId();

        if(selectedItemId == R.id.navigation_gallery){
            finish();
        }else if(selectedItemId == R.id.navigation_map) {
            bnv.setSelectedItemId(R.id.navigation_gallery);
            getSupportFragmentManager().popBackStackImmediate();
            super.onBackPressed();
        }
    }

    /**
     * Initialise EasyImage so that any taken pictures are saved to the external storage.
     */
    private void initEasyImage() {
        EasyImage.configuration(activity)
                .setImagesFolderName("PhotoBuddy")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true);
    }

    /**
     * Given a specified fragment, begin a transaction by replacing the current fragment with the
     * new fragment.
     * @param fragment - The fragment to be opened.
     */
    private void openFragment(Fragment fragment, String tag){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_placeholder,fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void handleSearch(){
        final TextView search = findViewById(R.id.search_tool);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate results fragment/activity
                Intent searchIntent = new Intent(activity, SearchResults.class);
                activity.startActivityForResult(searchIntent, 100);
            }
        });
    }

    private void setupViewModel(){
        photoBuddyViewModel = ViewModelProviders.of(this).get(PhotoBuddyViewModel.class);
    }

    /**
     * Opens EasyImage from the activity level.
     */
    public void openEasyImageActivity(){
        // Check if the device has a camera, if not then display a toast with an error
        // message.
        if(activity.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            EasyImage.openCamera(activity,0);
        }else{
            Toast toast = Toast.makeText(activity, R.string.no_camera, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
    }

    /**
     * Given a list of files, add their associated ImageElements and collate with the users location.
     * @param returnedPhotos - The photos taken by EasyImage.
     */
    private void onActivityPhotosReturned(List<File> returnedPhotos){
        List<ImageElement> takenPhotos = new ArrayList<>();
        takenPhotos.addAll(0,ImageAdapter.getImageElements(returnedPhotos));
        takenPhotos = resetURI(takenPhotos);
        collateLocation(takenPhotos);
    }

    /**
     * As EasyImage saves the taken photo to the cache, set the filepath of the ImageElement to the
     * photos absolute path instead (in the external storage).
     * @param myTakenList - A list of ImageElements.
     * @return - The list of image elements with their file location corrected.
     */
    public List<ImageElement> resetURI(List<ImageElement> myTakenList) {
        // Get the PhotoBuddy external storage directory
        File media = Environment.getExternalStorageDirectory();
        File directory = new File(media.getAbsolutePath() + "/Pictures/PhotoBuddy");

        // For each element, update the filepath to this directory instead of the cache
        for(ImageElement image: myTakenList) {
            File file = new File(directory, image.getFile().getName());
            image.setFileLocation(file.getAbsolutePath());
            image.setTakenWithCamera(true);
        }

        return myTakenList;
    }

    /**
     * Request for the users location if on a real device. Create a random location if on an
     * emulator.
     */
    public void collateLocation(List<ImageElement> takenPhotos) {
        myTakenList = takenPhotos;
        // If on an emulator, generate a random location for the picture
        if(Build.FINGERPRINT.contains("generic")){
            Random random = new Random();
            // Lat between -90 90
            double randLat = random.nextInt(90 + 1 + 90) - 90;
            // Lng between -180 180
            double randLng = random.nextInt(180 + 1 + 180) - 180;

            // Set the random location
            mCurrentLocation = new Location("");
            mCurrentLocation.setLatitude(randLat);
            mCurrentLocation.setLongitude(randLng);
            photoBuddyViewModel.insertNewEntry(myTakenList, mCurrentLocation);

            Fragment f = getSupportFragmentManager().findFragmentByTag("GALLERY");
            if(f != null && f.isVisible())
                setGallerySelected();

        } else {
            // Check that we have location permissions
            permissions.requestLocation();
            if (permissions.checkLocation()) {
                try {
                    mLocationRequest = new LocationRequest();
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Location mCurrentLocation;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            mCurrentLocation = locationResult.getLastLocation();
            // Insert new image data into database
            photoBuddyViewModel.insertNewEntry(myTakenList, mCurrentLocation);

            Fragment f = getSupportFragmentManager().findFragmentByTag("GALLERY");
            if(f != null && f.isVisible())
                setGallerySelected();
        }
    };

    public Activity getActivity() {
        return activity;
    }

    public void setGallerySelected(){
        bnv.setSelectedItemId(R.id.navigation_gallery);
    }

}
