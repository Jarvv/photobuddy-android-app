package uk.ac.shef.oak.com4510.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.ac.shef.oak.com4510.BaseActivity;
import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.adapters.WelcomePagerAdapter;
import uk.ac.shef.oak.com4510.util.Permissions;

/**
 * The activity that the app starts with on launch. Tis activity is only displayed once when the
 * app is launched for the first time, or again when the user selects it in the Drawer Menu. It contains
 * a PagerAdapter with layouts related to the use of the app.
 */
public class WelcomeSplash extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private int[] pages;
    private Button btnSkip, btnNext;
    private Activity activity;
    private Permissions permissions;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        activity = this;

        // Check that we have external storage permissions
        permissions = new Permissions(this);
        permissions.requestReadExternalStorage();

        // Check if it is the first time the activity (and therefore the app) is run.
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("firstRun", true);

        // If the app has already been run and the user is launching the app from the launcher,
        // load the BaseActivity.
        if(!isFirstRun && getIntent().getAction() == Intent.ACTION_MAIN){
            homeScreen();
        }

        setContentView(R.layout.activity_welcome);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        // The layouts for the slides that will be used.
        pages = new int[]{
                R.layout.splash_slide1,
                R.layout.splash_slide2,
                R.layout.splash_slide3};

        // Set up the dots along the bottom of the layout
        pageDots(0);

        // Set up the pager adapter
        WelcomePagerAdapter welcomePagerAdapter = new WelcomePagerAdapter(activity, pages);
        viewPager.setAdapter(welcomePagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        // If the 'SKIP' button is pressed, load the BaseActivity
        btnSkip.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        homeScreen();
                    }
                }
        );

        // If the 'Next' button is pressed
        btnNext.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the current page number
                        int current = viewPager.getCurrentItem() + 1;

                        // If it's not the end then set the new layout, else if on the final page then
                        // launch the BaseActivity.
                        if (current < pages.length) {
                            viewPager.setCurrentItem(current);
                        } else {
                            homeScreen();
                        }
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissionList[], @NonNull int[] grantResults){
        if(requestCode == permissions.REQUEST_READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            permissions.requestWriteExternalStorage();
        } else if(requestCode == permissions.REQUEST_READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            permissions.displayRationale("Reading external storage permission is necessary.", new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, permissions.REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Create 'dots' to give a visual representation to the user what page they are currently on.
     * @param currentPage - The current page that the user is on, to highlight the specific dot.
     */
    private void pageDots(int currentPage) {
        TextView[] dots = new TextView[pages.length];

        // Remove any existing dots.
        dotsLayout.removeAllViews();

        // For each dot, create this dot and colour it.
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(40);
            dots[i].setTextColor(ContextCompat.getColor(activity,R.color.colorPrimaryLight));
            dotsLayout.addView(dots[i]);
        }

        // Highlight the current page dot.
        dots[currentPage].setTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDarker));

    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener =
            new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    // Update the dots to highlight the current page
                    pageDots(position);

                    // If on the final page, change the button text to reflect this.
                    if (position == pages.length - 1) {
                        btnNext.setText(getString(R.string.start));
                        btnSkip.setVisibility(View.INVISIBLE);
                    } else {
                        btnNext.setText(getString(R.string.next));
                        btnSkip.setVisibility(View.VISIBLE);
                    }
                }

                // Methods not needed
                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {}

                @Override
                public void onPageScrollStateChanged(int arg0) {}
    };

    /**
     * Notify the shared preferences that this activity has already been run and start the
     * BaseActivity.
     */
    private void homeScreen(){
        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .edit().putBoolean("firstRun", false).apply();

        startActivity(new Intent(WelcomeSplash.this, BaseActivity.class));
        finish();
    }

    public Activity getActivity() {
        return activity;
    }
}
