package uk.ac.shef.oak.com4510.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;
import uk.ac.shef.oak.com4510.database.models.MetaData;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.util.ImageScale;

/**
 * ImageShow is the activity that shows when you select an image from the Gallery or SearchResults.
 * In here you can edit title, description, view the date created (if available),
 * the map (if available), and other meta data relating to the camera. You can also zoom in on the
 * image, and delete if wanted.
 */
public class ImageShow extends AppCompatActivity  implements OnMapReadyCallback {
    private PhotoBuddyViewModel photoBuddyViewModel;

    private ImageView imageView;
    private ImageScale imageScale;
    private ImageElement el;
    private FloatingActionButton fabBack;
    private Activity activity;
    private RelativeLayout bottomSheet;
    private Toolbar toolbar;

    private TextInputEditText titleField;
    private EditText descriptionField;
    private LinearLayout dateLayout;
    TextView dateField;
    LinearLayout mapLayout;
    LinearLayout cameraInfoLayout;
    private TextView cameraInfoField;

    GoogleMap gMap;

    private MetaData imageMetaData;
    private boolean hidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);
        activity = this;

        // Load the image into the activity.
        Bundle b = getIntent().getExtras();
        String fileName = b.getString("file");
        el = new ImageElement(fileName);
        loadImage(el);

        // Set up the UI to compliment the image.
        fabBack = findViewById(R.id.fab_back);
        bottomSheet = findViewById(R.id.bottomSheet);
        toolbar = findViewById(R.id.image_show_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        fabBack.bringToFront();

        // Get the data related to the image from the database.
        photoBuddyViewModel = ViewModelProviders.of(this).get(PhotoBuddyViewModel.class);
        photoBuddyViewModel.selectSingleEntry(fileName).observe(this, new Observer<MetaData>() {
            @Override
            public void onChanged(@NonNull MetaData metaData) {
                imageMetaData = metaData;
                populateSheet(metaData);
                setupMap();
            }
        });

        handleBack();
    }

    /**
     * Initialises the map that is used in the pull up sheet.
     */
    public void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.sheet_map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Ensures that any entered data is saved to the database when the user backs out of the
     * activity.
     */
    @Override
    public void onBackPressed(){
        photoBuddyViewModel.updateTitleEntry(imageMetaData.getId(), titleField.getText().toString());
        photoBuddyViewModel.updateDescriptionEntry(imageMetaData.getId(), descriptionField.getText().toString());
        finish();
    }

    /**
     * Ensures that any entered data is saved to the database when the user presses the
     * back fab to exit the activity.
     */
    private void handleBack() {
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoBuddyViewModel.updateTitleEntry(imageMetaData.getId(), titleField.getText().toString());
                photoBuddyViewModel.updateDescriptionEntry(imageMetaData.getId(), descriptionField.getText().toString());
                finish();
            }
        });
    }

    /**
     * Loads the image in question into the activity.
     * @param el - The ImageElement object in which the image will be loaded from.
     */
    private void loadImage(ImageElement el) {
        imageView = findViewById(R.id.image_preview);
        imageScale = new ImageScale(this, imageView);

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(el.getFile()));
            imageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            finish(); // If the image has magically disappeared, just close this activity.
            Log.e("Error", "Cannot load file.");
        }
    }

    /**
     * Handles all of the data population of the bottom pull-up sheet. In here are plenty of checks
     * to see if the data is available or not before showing the particular object.
     * @param metaData - The MetaData object being used to populate hte fields.
     */
    public void populateSheet(MetaData metaData) {
        // Define the Views in questionLinearLayout
        titleField = findViewById(R.id.title_field);
        descriptionField = findViewById(R.id.description_field);
        dateLayout = findViewById(R.id.date_layout);
        dateField = findViewById(R.id.date_field);
        mapLayout = findViewById(R.id.map_layout);
        cameraInfoLayout = findViewById(R.id.camera_info_layout);
        cameraInfoField = findViewById(R.id.camera_info_field);

        // May still not be filled out by the user..
        // In this case, the 'Add a title!' prompt will remain shown
        if (metaData.getTitle() != null) titleField.setText(metaData.getTitle());

        // May still not be filled out by the user...
        // In this case, the 'Add a description!' prompt will remain shown
        if (metaData.getDescription() != null) descriptionField.setText(metaData.getDescription());

        // Populate the Date field.
        dateField.setText(getDateString(metaData.getDateCreated()));

        // If there is no locational data, we hide the map.
        if (metaData.getTakenLatitude() == 0.0 && metaData.getTakenLongitude() == 0.0)
            mapLayout.setVisibility(View.GONE);

        // Extract some camera info.
        ImageElement imageExif = new ImageElement(metaData.getFileLocation());
        imageExif.setFileLocationFromAbsolute();
        imageExif.extractMeta();

        if(imageExif.atLeastAvailable()) setupTextForMeta(imageExif);
        else cameraInfoLayout.setVisibility(View.GONE);

        // Looks for any changes in the text fields.
        handleBoxChanges(metaData);
    }

    /**
     * Simply looks out for any changes in the Title and Description boxes and updates the
     * database accordingly.
     * @param metaData - The MetaData object being watched.
     */
    public void handleBoxChanges(final MetaData metaData) {
        titleField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    TextInputEditText view = (TextInputEditText) v;
                    // Update field in database
                    photoBuddyViewModel.updateTitleEntry(metaData.getId(), view.getText().toString());
                }
            }
        });

        descriptionField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText view = (EditText) v;
                    // Update field in database
                    photoBuddyViewModel.updateDescriptionEntry(metaData.getId(), view.getText().toString());
                }
            }
        });
    }

    /**
     * Converts the data from the database into a human readable string.
     * @param givenString - The date string given from the database.
     */
    public String getDateString(String givenString){
        String parsedDate = "";
        try {
            DateFormat parserDf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            Date date = parserDf.parse(givenString);
            DateFormat formatDf = new SimpleDateFormat("EEEE dd MMMM yyyy, hh:mm aa");
            parsedDate = formatDf.format(date);
        } catch (ParseException e) { // there must be a problem with the string given (shouldn't happen)
            e.printStackTrace();
        } catch (NullPointerException n) { // There is no date so hide the date field.
            dateLayout.setVisibility(View.GONE);
        }
        return parsedDate;
    }

    /**
     * Organises the string for the Camera Metadata. Collates a few types and appends to the string.
     * @param imageExif - The ImageElement which holds all of the data.
     */
    public void setupTextForMeta(ImageElement imageExif) {
        String exifString = "";

        if (imageExif.getCamera() != null)
            exifString += imageExif.getCamera() + " / ";
        if (imageExif.getIso() != null)
            exifString += "ISO"+imageExif.getIso() +  " / ";
        if (imageExif.getAperture() != null)
            exifString += "F" + imageExif.getAperture() + " / ";
        if (imageExif.getFocalLength() != null)
            exifString += imageExif.getAperture() + " mm" + " / ";
        if (imageExif.getExposureTime() != null)
            exifString += imageExif.getExposureTime().substring(0,4) + " s";

        cameraInfoField.setText(exifString);
    }

    /**
     * Handles the touch of the image. If there is a tap, we either hide or show the UI.
     * We also check for a pinch to zoom, handled by ImageScale.
     * @param event - The touch event given from the system.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(!hidden){
                hidden = true;
                fabBack.hide();
                bottomSheet.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
            } else {
                hidden = false;
                fabBack.show();
                bottomSheet.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
            }
        }
        return imageScale.onTouch(imageView, event);
    }

    /**
     * Sets up the menu which lets you delete the image.
     * @param menu - The menu to be inflated.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_show_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles the selection of the delete or share button in the menu.
     * 1. If the user wants to delete, we show a confirmation to make sure that they really want to,
     * then proceed if yes.
     * 2. If the user wishes to share, we load the share intent with the image to pass on.
     *    Credit: Android developers, https://developer.android.com/training/sharing/send
     * @param item - The menu item selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.delete_image:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Delete this image? It will be removed from the file system so proceed with caution.")
                        .setPositiveButton("Yes", deleteClickListener)
                        .setNegativeButton("No", deleteClickListener).show();
                break;
            case R.id.share:
                Uri fileUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", el.getFile());
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(sharingIntent, "Share Image"));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Click listener created for deleting the image. Called when the dialog for delete
     * confirmation is shown. If yes, we delete from the database and close the activity with
     * a message to the parent, otherwise we just go back as we were.
     */
    DialogInterface.OnClickListener deleteClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    boolean deleted = el.getFile().delete();
                    if (deleted) {
                        photoBuddyViewModel.deleteEntry(el.getFileLocation());
                        setResult(Activity.RESULT_OK,
                                new Intent().putExtra("Redraw", true));
                        finish();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    /**
     * Sets the marker on the map when it has loaded.
     * @param googleMap - Reference to the google map object.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setAllGesturesEnabled(false);
        gMap.addMarker(new MarkerOptions().position(new LatLng(imageMetaData.getTakenLatitude(), imageMetaData.getTakenLongitude())));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(imageMetaData.getTakenLatitude(), imageMetaData.getTakenLongitude()), 16f));
    }
}