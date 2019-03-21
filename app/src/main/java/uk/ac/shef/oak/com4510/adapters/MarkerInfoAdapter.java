package uk.ac.shef.oak.com4510.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import uk.ac.shef.oak.com4510.activities.ImageShow;
import uk.ac.shef.oak.com4510.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * A custom GoogleMap InfoWindowAdapter in which the photo and its information will be shown to the
 * user when they click on a marker. When users click on the InfoWindow, they will be taken to the
 * associated ImageShow activity.
 */
public class MarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mMarkerView;
    private Fragment mFragment;
    private GoogleMap mMap;

    /**
     * The MarkerInfoAdapter constructor.
     * @param mapFragment - The fragment containing the Google Map.
     * @param mMap - The Google Map.
     */
    public MarkerInfoAdapter(Fragment mapFragment, GoogleMap mMap){
        this.mFragment = mapFragment;
        this.mMap = mMap;

        // Inflate the layout of the InfoWindow
        mMarkerView = LayoutInflater.from(mFragment.getContext()).inflate(R.layout.marker_info_contents,null);
    }

    @Override
    public View getInfoContents(Marker marker){
        // Get the TextView for the title of the photo. If no title is present, explain to the user
        // they can edit this by clicking on the InfoWindow.
        TextView tvTitle = mMarkerView.findViewById(R.id.infoTitle);
        if(marker.getTitle() == null) tvTitle.setText(R.string.marker_info_title);
        else tvTitle.setText(marker.getTitle());

        // Get the TextView for the description of the photo.
        TextView tvDescription = mMarkerView.findViewById(R.id.infoDescription);
        tvDescription.setText(marker.getSnippet());

        // Get the ImageView of the photo.
        ImageView ivImage = mMarkerView.findViewById(R.id.infoImage);
        final Object filePath = marker.getTag();

        // If it has a filepath, it is an image
        if (filePath != null){
            try {
                // Get the bitmap from the filepath from the marker tag
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(filePath.toString())));
                ivImage.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                Log.e("Error", "Cannot load file.");
            }

            // Set an onClickListener for the InfoWindow, which when clicked, will open up the
            // ImageShow activity for this photo.
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(mFragment.getContext(), ImageShow.class);
                    intent.putExtra("file", filePath.toString());
                    mFragment.startActivityForResult(intent, 100);
                }
            });

        } else{
            // Else it is is the location marker
            ivImage.setImageDrawable(null);
        }

        return mMarkerView;
    }

    @Override
    public View getInfoWindow(Marker marker){
        return null;
    }
}
