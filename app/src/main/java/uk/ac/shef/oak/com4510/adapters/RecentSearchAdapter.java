package uk.ac.shef.oak.com4510.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.activities.SearchResults;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;
import uk.ac.shef.oak.com4510.database.models.Search;

/**
 * Adapter for the recent search recycler view in the SearchResults activity.
 */
public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.View_Holder> {

    private List<Search> searches;
    private Context context;
    private PhotoBuddyViewModel photoBuddyViewModel;

    /**
     * The constructor for the adapter, passes relevant data from the caller to the local
     * variables.
     * @param context - The context of the activity.
     * @param searches - A list of recent searches to add to the recycler view.
     * @param photoBuddyViewModel - The ViewModel to interact with the database.
     */
    public RecentSearchAdapter(Context context, List<Search> searches, PhotoBuddyViewModel photoBuddyViewModel) {
        super();
        this.searches = searches;
        this.context = context;
        this.photoBuddyViewModel = photoBuddyViewModel;
    }

    @NonNull
    @Override
    public View_Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_recent_search,
                viewGroup, false);
        RecentSearchAdapter.View_Holder holder = new RecentSearchAdapter.View_Holder(v);
        return holder;
    }

    /**
     * Sets up an entry into the recycler view. Sets the search text, date, a select and a delete
     * of the entry.
     * @param view_holder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull final View_Holder view_holder, final int i) {
        view_holder.searchString.setText(searches.get(i).getSearch());

        // Make the date nice to read for humans
        try {
            String dateString = searches.get(i).getDateCreated();
            DateFormat parserDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
            Date date = parserDf.parse(dateString);
            DateFormat formatDf = new SimpleDateFormat("EEEE dd MMMM yyyy, hh:mm aa");
            String parsedDate = formatDf.format(date);

            // Set date
            view_holder.dateString.setText(parsedDate);
        } catch(ParseException e) {
            e.printStackTrace();
        }

        view_holder.deleteButton.setOnClickListener(new View.OnClickListener(){
            /**
             * If the delete button is pressed we delete this recent search from the database.
             * @param v - The delete button pressed.
             */
            @Override
            public void onClick(View v) {
                photoBuddyViewModel.deleteSearch(searches.get(view_holder.getAdapterPosition()));
                searches.remove(view_holder.getAdapterPosition());
                if(searches.size() == 0) ((SearchResults) context).noSearchesText.setVisibility(View.VISIBLE);
                notifyDataSetChanged();
            }
        });

        view_holder.searchHolder.setOnClickListener(new View.OnClickListener() {
            /**
             * If the user taps on this search, we set it as the search box text and submit.
             * @param v - The whole entry view.
             */
            @Override
            public void onClick(View v) {
                String searchString = searches.get(view_holder.getAdapterPosition()).getSearch();
                ((SearchResults) context).searchBox.setQuery(searchString, true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    /**
     * A custom ViewHolder for the RecyclerView's ViewHolder, where the views are each element
     * in the entry.
     */
    class View_Holder extends RecyclerView.ViewHolder{
        LinearLayout searchHolder;
        TextView searchString;
        TextView dateString;
        ImageView deleteButton;

        View_Holder(View itemView){
            super(itemView);
            searchHolder = itemView.findViewById(R.id.recent_item);
            searchString = itemView.findViewById(R.id.recent_search_text);
            dateString = itemView.findViewById(R.id.recent_search_date);
            deleteButton = itemView.findViewById(R.id.recent_search_delete);
        }
    }
}
