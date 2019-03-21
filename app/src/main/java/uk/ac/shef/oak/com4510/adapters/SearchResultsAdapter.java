package uk.ac.shef.oak.com4510.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.ac.shef.oak.com4510.activities.ImageShow;
import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.activities.SearchResults;
import uk.ac.shef.oak.com4510.async.ImageLoader;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.objects.ImagePlaceholder;


public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.View_Holder> {

    private Context context;
    private List<ImageElement> searchResults;
    private PhotoBuddyViewModel photoBuddyViewModel;

    private int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private int KEEP_ALIVE = 1;
    private TimeUnit ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private final Executor CUSTOM_THREAD_POOL = new ThreadPoolExecutor(CORE_POOL_SIZE,
            CORE_POOL_SIZE*2, KEEP_ALIVE, ALIVE_TIME_UNIT, workQueue);

    public SearchResultsAdapter(Context context, List<ImageElement> searchResults, PhotoBuddyViewModel photoBuddyViewModel) {
        super();
        this.context = context;
        this.searchResults = searchResults;
        this.photoBuddyViewModel = photoBuddyViewModel;
    }

    @NonNull
    @Override
    public View_Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_search_results,
                viewGroup, false);
        return new SearchResultsAdapter.View_Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final View_Holder view_holder, int i) {
        // Load placeholder first
        view_holder.imageView.setImageDrawable(context.getDrawable(R.drawable.placeholder));

        // Load the bitmap
        view_holder.imageView.setTag(
                new ImageLoader().executeOnExecutor(CUSTOM_THREAD_POOL, new ImagePlaceholder(i,view_holder,searchResults,context,photoBuddyViewModel)));

        view_holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchResults searchResultsObj = ((SearchResults) context);

                // Call extractMeta
                searchResults.get(view_holder.getAdapterPosition()).extractMeta();
                Intent intent = new Intent(context, ImageShow.class);
                intent.putExtra("file", searchResults.get(view_holder.getAdapterPosition()).getFileLocation());
                searchResultsObj.startActivityForResult(intent, 100);

            }
        });
    }

    @Override
    public void onViewRecycled(SearchResultsAdapter.View_Holder holder){
        // Cancel the current task
        ((AsyncTask)(holder.imageView.getTag())).cancel(true);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class View_Holder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        View_Holder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.search_image_item);
        }
    }
}
