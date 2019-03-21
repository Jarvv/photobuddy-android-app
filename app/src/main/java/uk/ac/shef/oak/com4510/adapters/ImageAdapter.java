package uk.ac.shef.oak.com4510.adapters;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.async.ImageLoader;
import uk.ac.shef.oak.com4510.activities.ImageShow;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.objects.ImagePlaceholder;

/**
 * A custom ImageAdapter for the RecyclerView's ImageAdapter. Allows the creation and loading of
 * bitmaps for each image in the RecyclerView, as well as allowing users to click on the previews
 * to view the whole image in ImageShow.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.View_Holder>{
    private Fragment fragment;
    private List<ImageElement> items;
    private PhotoBuddyViewModel photoBuddyViewModel;

    private int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private int KEEP_ALIVE = 1;
    private TimeUnit ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private final Executor CUSTOM_THREAD_POOL = new ThreadPoolExecutor(CORE_POOL_SIZE,
            CORE_POOL_SIZE*2, KEEP_ALIVE, ALIVE_TIME_UNIT, workQueue);

    /**
     * The constructor for the ImageAdapter.
     * @param fragment - The fragment that the RecyclerView is a part of.
     * @param items - The list of ImageElements
     */
    public ImageAdapter(Fragment fragment, List<ImageElement> items){
        super();
        this.fragment = fragment;
        this.items = items;
        photoBuddyViewModel = ViewModelProviders.of(fragment).get(PhotoBuddyViewModel.class);
    }

    /**
     * A custom ViewHolder for the RecyclerView's ViewHolder, where the View is an ImageView of our
     * Image Item layout.
     */
    public class View_Holder extends RecyclerView.ViewHolder{
        public ImageView imageView;

        View_Holder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item);
        }
    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_gallery_image,
                parent, false);
        View_Holder holder = new View_Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final View_Holder holder, final int position) {
        // Use the provided View Holder on the onCreateViewHolder method to populate the
        // current row on the RecyclerView.

        // Load placeholder
        holder.imageView.setImageDrawable(fragment.getContext().getDrawable(R.drawable.placeholder));

        // Load the bitmap
        // Set the tag of the holder as the async task, this way when the view is recycled, the
        // task can be cancelled along with it.
        holder.imageView.setTag(new ImageLoader().executeOnExecutor(CUSTOM_THREAD_POOL,
                new ImagePlaceholder(position, holder, items, fragment.getContext(), photoBuddyViewModel)));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Insert image metadata to the database
                if (!items.get(position).isTakenWithCamera())
                    items.get(position).setFileLocationFromAbsolute();
                photoBuddyViewModel.insertSingleton(items.get(position));

                // Call extractMeta
                items.get(position).extractMeta();
                Intent intent = new Intent(fragment.getContext(), ImageShow.class);
                intent.putExtra("file", items.get(position).getFileLocation());
                fragment.startActivityForResult(intent, 100);

            }
        });

    }

    @Override
    public void onViewRecycled(View_Holder holder){
        // Cancel the current task
        ((AsyncTask)(holder.imageView.getTag())).cancel(true);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position){
        return items.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * given a list of photos, it creates a list of myElements
     * @param returnedPhotos
     * @return
     */
    public static List<ImageElement> getImageElements(List<File> returnedPhotos) {
        List<ImageElement> imageElementList= new ArrayList<>();
        for (File file: returnedPhotos){
            ImageElement element= new ImageElement(file);
            imageElementList.add(element);
        }

        return imageElementList;
    }
}
