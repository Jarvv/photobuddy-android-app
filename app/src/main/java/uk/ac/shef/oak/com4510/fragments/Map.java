package uk.ac.shef.oak.com4510.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import uk.ac.shef.oak.com4510.adapters.ImageAdapter;
import uk.ac.shef.oak.com4510.adapters.MarkerInfoAdapter;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;
import uk.ac.shef.oak.com4510.database.models.MetaData;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.util.Permissions;
import uk.ac.shef.oak.com4510.R;

import static java.lang.Thread.sleep;

/**
 * The Map fragment contains a Google Map fragment in which locations of photos from the gallery
 * can be plotted onto, and a RecyclerView of the photos that the user can currently see. These
 * markers can be clicked on to open up a InfoWindow which will display information of this photo.
 */
public class Map extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private List<ImageElement> myPictureList = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView textView;
    private Permissions permissions;
    private Activity activity;
    private Fragment fragment;
    private FragmentActivity listener;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Marker locationMarker;
    private List<Marker> markers = new ArrayList<>();
    private BitmapDescriptor locationBitmap;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private static GoogleMap mMap;

    public static GoogleMap getMap() {
        return mMap;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.listener = (FragmentActivity) context;
            activity = getActivity();
            permissions = new Permissions(activity);
            fragment = this;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(fragment.getContext());
        locationBitmap = bitmapDescriptorFromDrawable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceStatee) {
        return inflater.inflate(R.layout.content_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up the RecyclerView
        mRecyclerView = view.findViewById(R.id.map_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new ImageAdapter(this, myPictureList);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        textView = view.findViewById(R.id.no_images);

        // Set up the Fab which updates the users position on the map
        FloatingActionButton fab = view.findViewById(R.id.fab_location);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Build.FINGERPRINT.contains("generic")) {
                    startLocationUpdates();
                }
                else{
                    Toast toast = Toast.makeText(activity, R.string.no_location, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }

            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new MarkerInfoAdapter(this, mMap));
        mMap.setOnCameraIdleListener(this);
        // If not on emulator, use the GPS
        if(!Build.FINGERPRINT.contains("generic")) {
            startLocationUpdates();
        }
        collectMarkerMetaData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if(requestCode == 100 && resultCode == Activity.RESULT_OK) {
            //resetMarkers();
        //}
        // Re-plot the markers any time the user exits ImageShow, either from deleting or editing
        // the photo.
        resetMarkers();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (permissions.checkLocation()) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } else {
            permissions.requestLocation();
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            mCurrentLocation = locationResult.getLastLocation();
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

            plotLocationMarker();

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 14.0f));
            stopLocationUpdates();
        }
    };

    /**
     * Stop any location updates from happening.
     */
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Plot a marker on the users location.
     */
    private void plotLocationMarker() {
        if (mMap != null && mCurrentLocation != null && mLastUpdateTime != null)
            if (locationMarker == null) {
                // If its the first time plotting, create the marker.
                locationMarker = mMap.addMarker(new MarkerOptions()
                        .position((new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())))
                        .title("My Location")
                        .snippet(mLastUpdateTime)
                        .icon(locationBitmap));
            } else {
                // Otherwise update the location and time.
                locationMarker.setPosition(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                locationMarker.setSnippet(mLastUpdateTime);
            }
    }

    /**
     * Reset the markers on the map by clearing the existing markers on the map and re-plotting them.
     */
    public void resetMarkers() {
        // Clear the map
        mMap.clear();

        // Location marker will be cleared too so re-add this to the map
        locationMarker = null;
        plotLocationMarker();

        // Set new markers for the images
        collectMarkerMetaData();
    }

    /**
     * Using the ViewModel, select entries which have a location which can be used in plotting markers
     * on the map.
     */
    public void collectMarkerMetaData() {
        final PhotoBuddyViewModel myViewModel = ViewModelProviders.of(this).get(PhotoBuddyViewModel.class);
        myViewModel.selectEntries().observe(this, new Observer<List<MetaData>>() {
            @Override
            public void onChanged(@Nullable List<MetaData> metaDataList) {
                plotPhotoMarkers(metaDataList);
            }
        });
    }

    /**
     * Using the information provided within the MetaData, plot markers onto the map.
     * @param metaDataList - A list of the photo's MetaData obtained from the ViewModel
     */
    private void plotPhotoMarkers(List<MetaData> metaDataList) {
        // Clear the references to any existing markers.
        markers.clear();
        for (MetaData metaData : metaDataList) {
            if (mMap != null && new File(metaData.getFileLocation()).exists()) {
                // Using the information provided in the MetaData, plot a marker
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(metaData.getTakenLatitude(), metaData.getTakenLongitude()))
                        .title(metaData.getTitle())
                        .snippet(metaData.getDescription()));

                marker.setTag(metaData.getFileLocation());
                markers.add(marker);
            }
        }

        // If any markers are on screen, show the pictures in the bottomsheet.
        onCameraIdle();
    }

    /**
     * Creates a BitmapDescriptor from a custom drawable that can be used as a custom marker on the
     * Google Map.
     * @return - The BitmapDescriptor for the drawable.
     */
    private BitmapDescriptor bitmapDescriptorFromDrawable() {
        // Get the drawable and specify the bounds for the bitmap
        Drawable drawable = ContextCompat.getDrawable(fragment.getContext(), R.drawable.ic_person_pin_circle_24dp);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        // Create a bitmap
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onCameraIdle() {
        // When the camera is idle, collate the filepath's of the photos from the markers currently
        // shown on the screen.
        List<String> filePaths = new ArrayList<>();
        if (mMap != null) {
            // This is the current user-viewable region of the map
            LatLngBounds latLongBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

            // For each marker, if it is within these bounds then add the filepath to the list.
            for (int i = 0; i < markers.size(); i++) {
                Marker marker = markers.get(i);
                if (latLongBounds.contains(marker.getPosition()) && marker.getTag() != null) {
                    filePaths.add(marker.getTag().toString());
                }
            }

        }
        getMapShownImages(filePaths);
    }


    /**
     * Create a list of ImageElements used by the ImageAdapter that shows the current photos that
     * are visible as markers on the map.
     * @param filePaths - The list of filepath's.
     */
    private void getMapShownImages(List<String> filePaths) {
        List<ImageElement> newList = new ArrayList<>();

        // For each path, create an associated ImageElement
        for (String filePath : filePaths) {
            ImageElement element = new ImageElement(filePath);
            if (element.file.exists())
                newList.add(element);
        }

        // Clear the current list and add the new list
        myPictureList.clear();
        myPictureList.addAll(0, newList);

        // If the list is empty then display this to the user as a string.
        if (myPictureList.isEmpty()) textView.setText(R.string.no_images);
        else textView.setText(null);

        mAdapter.notifyDataSetChanged();
    }

}
