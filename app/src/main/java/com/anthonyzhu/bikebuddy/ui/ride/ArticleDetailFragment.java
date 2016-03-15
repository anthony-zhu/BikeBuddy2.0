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

    @Bind(R.id.ivTEST1)
    ImageView ivT1;

    @Bind(R.id.ivTEST2)
    ImageView ivT2;

    @Bind(R.id.ivTEST3)
    ImageView ivT3;

    @Bind(R.id.tvBot1)
    TextView q1;

    @Bind(R.id.tvBot2)
    TextView q2;

    @Bind(R.id.tvBot3)
    TextView q3;

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
            String extraStatsText = "Number of Dangerous Stops:" + ride.badStops;
            dateTime.setText(dateTimeText);
            distance.setText(distanceText);
            rideInfo.setText(rideInfoText);
            extraStats.setText(extraStatsText);
            ratBar.setRating(ride.rating);

            int attCount = 0;

            if (ride.badStops > (ride.distance / 2.5) || ride.badStops < (ride.distance / 4) ){
                String note1= null;
                ViewGroup.MarginLayoutParams ivT1LayoutParams = (ViewGroup.MarginLayoutParams) ivT1.getLayoutParams();
                ivT1LayoutParams.leftMargin = 10;
                if (ride.badStops < (ride.distance / 4)){
                    ivT1.setImageDrawable(getResources().getDrawable(R.drawable.helmeticon));
                    note1 = "Thanks for riding safely; keep it up!";
                }
                else{
                    ivT1.setImageDrawable(getResources().getDrawable(R.drawable.dangericon));
                    note1 = "DANGER: Please be more careful on the road";
                }
                q1.setText(note1);
                ivT1.setVisibility(View.VISIBLE);
                attCount = attCount + 1;
            }
            if (ride.distance > 10){
                String note2 = null;
                int pix1 = ( attCount * 100) + 10;
                ViewGroup.MarginLayoutParams ivT2LayoutParams = (ViewGroup.MarginLayoutParams) ivT2.getLayoutParams();
                ivT2LayoutParams.leftMargin = pix1;
                if (ride.distance > 26.2){
                    ivT2.setImageDrawable(getResources().getDrawable(R.drawable.marathonicon));
                    note2= "We're impressed with your distance, you're an endurance champ!";

                }
                else{
                    ivT2.setImageDrawable(getResources().getDrawable(R.drawable.rulericon));
                    note2="Good Distance! But do you have what it takes to be a marathoner?";
                }
                if (attCount==1){
                    q2.setText(note2);
                } else {
                    q1.setText(note2);
                }
                ivT2.setVisibility(View.VISIBLE);
                ivT2.setLayoutParams(ivT2LayoutParams);
                attCount = attCount +1;
            }
            if (ride.averageSpeed > 14){
                String note3 = null;
                int pix2 = (attCount * 100)+10;
                ViewGroup.MarginLayoutParams ivT3LayoutParams = (ViewGroup.MarginLayoutParams) ivT3.getLayoutParams();
                ivT3LayoutParams.leftMargin = pix2;
                if (ride.averageSpeed > 23){
                    ivT3.setImageDrawable(getResources().getDrawable(R.drawable.fastbikingicon));
                    note3 = "Wow!! That was fast! Are you a professional?";
                }
                else if (ride.averageSpeed > 18){
                    ivT3.setImageDrawable(getResources().getDrawable(R.drawable.semifastbikingicon));
                    note3 = "Great Speed! We're impressed";
                }
                else{
                    ivT3.setImageDrawable(getResources().getDrawable(R.drawable.bikingicon));
                    note3= "Good Speed; next ride let's notch it up a little";
                }

                if (attCount==2){
                    q3.setText(note3);
                } else if(attCount==1){
                    q2.setText(note3);
                } else {
                    q1.setText(note3);
                }

                ivT3.setVisibility(View.VISIBLE);
                ivT3.setLayoutParams(ivT3LayoutParams);
                //attCount = attCount +1;
            }
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
