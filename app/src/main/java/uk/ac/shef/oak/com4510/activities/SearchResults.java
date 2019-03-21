package uk.ac.shef.oak.com4510.activities;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.adapters.RecentSearchAdapter;
import uk.ac.shef.oak.com4510.adapters.SearchResultsAdapter;
import uk.ac.shef.oak.com4510.database.PhotoBuddyViewModel;
import uk.ac.shef.oak.com4510.database.models.MetaData;
import uk.ac.shef.oak.com4510.database.models.Search;
import uk.ac.shef.oak.com4510.objects.ImageElement;
import uk.ac.shef.oak.com4510.util.SearchDatePicker;

/**
 * SearchResults is the activity that pops up when you want to search for images.
 * You are presented with recent searches, and your search results as you type.
 * In this there are 2 recycler views, one for recent searches and one for the results.
 */
public class SearchResults extends AppCompatActivity {

    public SearchView searchBox;
    private LinearLayout recentSearchLayout;
    private LinearLayout searchResultsLayout;
    private PhotoBuddyViewModel photoBuddyViewModel;

    RecyclerView recentsRecyclerView;
    RecyclerView searchResultsRecyclerView;
    private RecyclerView.Adapter recentsAdapter;
    private RecyclerView.Adapter searchResultsAdapter;

    Activity activity;
    private List<Search> recentsList = new ArrayList<>();
    private List<ImageElement> searchResults = new ArrayList<>();
    public TextView noSearchesText;
    public TextView noResultsText;
    public TextView searchResultTitle;
    public String adapterSearchQuery;
    private ImageView datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        activity = this;
        handleBack();

        photoBuddyViewModel = ViewModelProviders.of(this).get(PhotoBuddyViewModel.class);

        searchBox = findViewById(R.id.search_tool);

        // Set up the recent searches recycler view and adapter.
        recentSearchLayout = findViewById(R.id.recent_searches_layout);
        recentsRecyclerView = findViewById(R.id.recent_searches_recycler_view);
        recentsRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        recentsAdapter = new RecentSearchAdapter(this, recentsList, photoBuddyViewModel);
        recentsRecyclerView.setAdapter(recentsAdapter);
        noSearchesText = findViewById(R.id.no_searches_text);

        // Set up the search results recycler view and adapter.
        searchResultsLayout = findViewById(R.id.search_results_layout);
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(activity, 4));
        searchResultsRecyclerView.setItemViewCacheSize(20);
        searchResultsRecyclerView.setDrawingCacheEnabled(true);
        searchResultsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        searchResultsAdapter = new SearchResultsAdapter(this, searchResults, photoBuddyViewModel);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        // Text fields that inform the user when there are no results, or the current search being carried out
        searchResultTitle = findViewById(R.id.search_results_title);
        noResultsText = findViewById(R.id.no_results_text);

        datePicker = findViewById(R.id.date_picker);
        handleDatePicker();

        // Get previous searches.
        loadRecents();

        // Handle the searching.
        handleSearching();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If an image is deleted in ImageShow, we perform the search again.
        if(requestCode == 100 && resultCode == Activity.RESULT_OK) {
            performSearch(adapterSearchQuery);
        }
    }

    /**
     * Handles the back button being pressed by the user, returns them to either the Gallery
     * or Map.
     */
    public void handleBack() {
        ImageView backButton = findViewById(R.id.search_return);

        backButton.setOnClickListener(new View.OnClickListener(){
            /**
             * If the back button is pressed, we tell the parent fragment to redraw as an
             * image may have been deleted in ImageShow.
             * @param v - The back button in the activity header.
             */
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK,
                        new Intent().putExtra("Redraw", true));
                finish();
            }
        });
    }

    /**
     * Loads the recent searches, the default view of the activity.
     */
    public void loadRecents() {
        recentSearchLayout.setVisibility(View.VISIBLE);
        searchResultsLayout.setVisibility(View.GONE);

        photoBuddyViewModel.selectRecentSearches().observe(this, new Observer<List<Search>>() {
            /**
             * If we have searches, we add them to the recycler adapter.
             * If not, we just display a message saying that there are no results.
             * @param searches
             */
            @Override
            public void onChanged(List<Search> searches) {
                if(searches.size() != 0) {
                    noSearchesText.setVisibility(View.GONE);
                    recentsList.clear();
                    recentsList.addAll(searches);
                    recentsAdapter.notifyDataSetChanged();
                } else {
                    noSearchesText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Contains the event handlers to perform search, save search, and reset search.
     */
    public void handleSearching() {
        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            /**
             * Called when the user presses enter on their keyboard. We perform and
             * save search here.
             * @param s - The search string from the search box.
             */
            @Override
            public boolean onQueryTextSubmit(String s) {
                performSearch(s);
                saveSearch(s);
                return false;
            }

            /**
             * Whenever text is changed in the text box, we call performSearch to give
             * real-time results.
             * @param s - The search string from the search box.
             */
            @Override
            public boolean onQueryTextChange(String s) {
                performSearch(s);
                return false;
            }
        });

        ImageView searchClose = searchBox.findViewById(R.id.search_close_btn);
        searchClose.setOnClickListener(new View.OnClickListener() {
            /**
             * If the close button is pressed in the search box, we wipe the contents,
             * clear focus and load the recent searches.
             * @param v - The close button in the search box.
             */
            @Override
            public void onClick(View v) {
                searchBox.setQuery("", false);
                searchBox.clearFocus();
                loadRecents();
            }
        });
    }

    /**
     * Listens for a click of the date picker button in the search header.
     * Then handles the date being selected and adds to the search box.
     */
    public void handleDatePicker() {
        datePicker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DialogFragment searchDatePicker = new SearchDatePicker();
                ((SearchDatePicker) searchDatePicker).setSearchView(searchBox);
                searchDatePicker.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    /**
     * Contains the logic to perform the search to the database and fill the adapter.
     * @param searchString - The search string passed from the search box.
     */
    public void performSearch(final String searchString) {
        // Resets the adapter so that we don't have old search results shown.
        searchResults.clear();
        searchResultsAdapter.notifyDataSetChanged();

        // Sets the Results For: string in the results view.
        String titleString = getResources().getString(R.string.search_results_title) + " " + searchString;
        searchResultTitle.setText(titleString);

        recentSearchLayout.setVisibility(View.GONE);
        searchResultsLayout.setVisibility(View.VISIBLE);

        adapterSearchQuery = searchString;

        List<String> commaSeparated = Arrays.asList(searchString.split(","));
        // Just if user focus is still in search but there is no search input, we can handle this.
        if (searchString.length() > 0) {
            // For each item in the search string
            for (final String query : commaSeparated) {
                // If the sub-item isn't just white space
                if (query.trim().length() > 0) {
                    // We search the database for images
                    photoBuddyViewModel.selectFieldsFromSearch(query.trim()).observe(this, new Observer<List<MetaData>>() {
                        /**
                         * When we have something returned, we create ImageElement objects
                         * and add them to the array for the adapter.
                         * @param results - List of results from the database.
                         */
                        @Override
                        public void onChanged(List<MetaData> results) {
                            List<ImageElement> converted = getImageElements(results);
                            checkAndAdd(converted);
                            searchResultsAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    noResultsText.setVisibility(View.VISIBLE);
                }
            }
        } else {
            recentSearchLayout.setVisibility(View.VISIBLE);
            searchResultsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * With a list of results, this method simply checks the image isn't already in the adapter to
     * avoid duplicates.
     * @param results - List of results sent from the database.
     */
    public void checkAndAdd(List<ImageElement> results) {
        for(ImageElement imageElement : results) {
            boolean present = false;
            for(ImageElement presentImage : searchResults)
                if(presentImage.getFileLocation().equals(imageElement.getFileLocation())) present = true;

            if(!present) searchResults.add(0, imageElement);
        }

        if(searchResults.size() == 0) noResultsText.setVisibility(View.VISIBLE);
        else noResultsText.setVisibility(View.GONE);

    }

    /**
     * Called to save the search into the database.
     * Used by onQueryTextSubmit, or when one of the items is clicked by the user.
     * @param searchString - The search string given by the search box
     */
    public void saveSearch(String searchString) {
        photoBuddyViewModel.insertNewSearch(searchString);
    }

    /**
     * Converts all of the results from the database into ImageElements.
     * @param searchResults - List of results sent from the database.
     */
    public List<ImageElement> getImageElements(List<MetaData> searchResults) {
        List<ImageElement> imageElementList= new ArrayList<>();
        for(MetaData metaData: searchResults) {
            ImageElement element = new ImageElement(metaData.getFileLocation());
            element.setFileLocation(metaData.getFileLocation());
            if (element.file.exists()) imageElementList.add(element);
        }
        return imageElementList;
    }
}
