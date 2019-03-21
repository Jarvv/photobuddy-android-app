package uk.ac.shef.oak.com4510.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A custom PagerAdapter which is used to display pages to introduce users to the app that the user
 * is able to swipe through.
 */
public class WelcomePagerAdapter extends PagerAdapter {
    private Activity activity;
    private int[] pages;

    /**
     * The WelcomePagerAdapter constructor.
     * @param activity - The WelcomeSplash activity.
     * @param pages - The layouts for the pages the adapter is using.
     */
    public WelcomePagerAdapter(Activity activity, int[] pages){
        super();
        this.activity = activity;
        this.pages = pages;
    }

    @Override
    public Object instantiateItem(ViewGroup parent, int position) {
        // Inflate the layout of the current page the user is on, and add this view to the parent
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(pages[position], parent, false);
        parent.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return pages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }
}
