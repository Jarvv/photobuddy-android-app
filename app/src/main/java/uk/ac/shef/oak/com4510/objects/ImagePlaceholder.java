package uk.ac.shef.oak.com4510.objects;

import android.content.Context;

import java.util.List;

import uk.ac.shef.oak.com4510.adapters.ImageAdapter;
import uk.ac.shef.oak.com4510.adapters.SearchResultsAdapter;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;

/**
 * A class to be used by ImageLoader, which given the position of the Image within the ViewHolder
 * can be used to create a bitmap of this image.
 */
public class ImagePlaceholder {
    public int position;
    public ImageAdapter.View_Holder imgHolder;
    public SearchResultsAdapter.View_Holder srHolder;
    public List<ImageElement> items;
    public Context context;
    public PhotoBuddyViewModel photoBuddyViewModel;

    /**
     * The constructor for ImagePlaceholder for the ImageAdapter
     * @param position - position of the image within the adapter
     * @param holder - the ImageAdapter ViewHolder
     * @param items - the ImageElements within the adapter
     * @param context - the context of the activity
     */
    public ImagePlaceholder(int position, ImageAdapter.View_Holder holder, List<ImageElement> items, Context context, PhotoBuddyViewModel photoBuddyViewModel){
        this.position = position;
        this.imgHolder = holder;
        this.items = items;
        this.context = context;
        this.photoBuddyViewModel = photoBuddyViewModel;
    }

    /**
     * The constructor for ImagePlaceholder for the SearchResultsAdapter.
     * @param position  - position of the image within the adapter
     * @param holder - the SearchResultsAdapter ViewHolder
     * @param items - the ImageElements within the adapte
     * @param context - the context of the activity
     */
    public ImagePlaceholder(int position, SearchResultsAdapter.View_Holder holder, List<ImageElement> items, Context context, PhotoBuddyViewModel photoBuddyViewModel){
        this.position = position;
        this.srHolder = holder;
        this.items = items;
        this.context = context;
        this.photoBuddyViewModel = photoBuddyViewModel;
    }
}
