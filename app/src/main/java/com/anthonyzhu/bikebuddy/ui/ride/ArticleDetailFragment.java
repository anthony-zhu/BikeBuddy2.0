package com.anthonyzhu.bikebuddy.ui.ride;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.anthonyzhu.bikebuddy.R;
import com.anthonyzhu.bikebuddy.database.Ride;
import com.anthonyzhu.bikebuddy.database.RideHandler;
import com.anthonyzhu.bikebuddy.ui.base.BaseActivity;
import com.anthonyzhu.bikebuddy.ui.base.BaseFragment;
import com.bumptech.glide.Glide;

import butterknife.Bind;

/**
 * Shows the distance detail page.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ArticleDetailFragment extends BaseFragment {

    /**
     * The argument represents the dummy item ID of this fragment.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The ride content of this fragment.
     */
    private Ride ride;

    /**
     * Image IDs to cycle through
     */
    public final static Integer[] imageResIds = new Integer[] {
            R.drawable.p1, R.drawable.p2, R.drawable.p3,
            R.drawable.p4, R.drawable.p5};

    @Bind(R.id.distance)
    TextView distance;

    @Bind(R.id.dateTime)
    TextView dateTime;

    @Bind(R.id.backdrop)
    ImageView backdropImg;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @Bind(R.id.rideInfo)
    TextView rideInfo;

    @Bind(R.id.extraStats)
    TextView extraStats;

    @Bind(R.id.ratBar)
    RatingBar ratBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // load ride item by using the passed item ID.
            ride = RideHandler.getInstance(getActivity()).getRide(getArguments().getLong(ARG_ITEM_ID));
//                    DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateAndBind(inflater, container, R.layout.fragment_ride_detail);

        if (!((BaseActivity) getActivity()).providesActivityToolbar()) {
            // No Toolbar present. Set include_toolbar:
            ((BaseActivity) getActivity()).setToolbar((Toolbar) rootView.findViewById(R.id.toolbar));
        }

        if (ride != null) {
            loadBackdrop();
            collapsingToolbar.setTitle("Ride #" + ride.id);
            String dateTimeText = "You rode on " + ride.date + " at " + ride.time;
            String distanceText = "You rode " + ride.distance + " miles";
            String rideInfoText = "-Your ride lasted: "+ ride.rideTime +". \n-Your average speed was " + ride.averageSpeed +" mph.";
            String extraStatsText = "Safe Brake percentages: "+ ride.goodStops + "% \n-Number of Dangerous Stops:" + ride.badStops;
            dateTime.setText(dateTimeText);
            distance.setText(distanceText);
            rideInfo.setText(rideInfoText);
            extraStats.setText(extraStatsText);
            ratBar.setRating(ride.rating);
        }

        return rootView;
    }

    private void loadBackdrop() {
        Glide.with(this).load(imageResIds[(int) ride.id % 5]).centerCrop().into(backdropImg);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sample_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // your logic
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static ArticleDetailFragment newInstance(long itemID) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ArticleDetailFragment.ARG_ITEM_ID, itemID);
        fragment.setArguments(args);
        return fragment;
    }

    public ArticleDetailFragment() {}
}
