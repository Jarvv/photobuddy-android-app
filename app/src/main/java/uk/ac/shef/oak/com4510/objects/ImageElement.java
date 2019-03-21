package uk.ac.shef.oak.com4510.objects;

import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;

/**
 * Contains the data that is passed from MetaData and performs operations such as extra metadata
 * extraction from the image. Used mostly to populate the Gallery and Search recycler views.
 */
public class ImageElement {
    public File file;

    // Metadata
    private String fileLocation;
    private String dateCreated;
    private String aperture;
    private String iso;
    private String exposureTime;
    private String camera;
    private String focalLength;

    private boolean takenWithCamera = false;

    public ImageElement(File file){
        this.file = file;
    }

    public ImageElement(String fileName) {
        this.file = new File(fileName);
    }

    /**
     * Extracts any extra meta data that may be useful to the image data such as camera model,
     * and picture options used at the time of taking.
     */
    public void extractMeta(){
        try {
            ExifInterface exif = new ExifInterface(fileLocation);
            dateCreated = exif.getAttribute(ExifInterface.TAG_DATETIME);
            aperture = exif.getAttribute(ExifInterface.TAG_APERTURE);
            iso = exif.getAttribute(ExifInterface.TAG_ISO);
            exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            camera = exif.getAttribute(ExifInterface.TAG_MODEL);
            focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A simple method to check whether we need to include the camera information in the ImageShow
     * activity. Having this ensures we don't have blank space on the content card that is pulled
     * up by the user.
     */
    public boolean atLeastAvailable() {
        return aperture != null && iso != null &&
               exposureTime != null && camera != null
               && focalLength != null;
    }

    /**
     * Getters and Setters
     */
    public void setFileLocationFromAbsolute() {
        this.fileLocation = file.getAbsolutePath();
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getAperture() {
        return aperture;
    }

    public String getIso() {
        return iso;
    }

    public String getExposureTime() {
        return exposureTime;
    }

    public String getCamera() {
        return camera;
    }

    public String getFocalLength() {
        return focalLength;
    }

    public boolean isTakenWithCamera() {
        return takenWithCamera;
    }

    public void setTakenWithCamera(boolean takenWithCamera) {
        this.takenWithCamera = takenWithCamera;
    }
}
