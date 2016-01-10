package com.anthonyzhu.bikebuddy.ui.quote;

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
import com.anthonyzhu.bikebuddy.dummy.DummyContent;
import com.anthonyzhu.bikebuddy.ui.base.BaseActivity;
import com.anthonyzhu.bikebuddy.ui.base.BaseFragment;
import com.bumptech.glide.Glide;

import butterknife.Bind;

/**
 * Shows the quote detail page.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ArticleDetailFragment extends BaseFragment {

    /**
     * The argument represents the dummy item ID of this fragment.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content of this fragment.
     */
    private DummyContent.DummyItem dummyItem;

    @Bind(R.id.quote)
    TextView quote;

    @Bind(R.id.author)
    TextView author;

    @Bind(R.id.backdrop)
    ImageView backdropImg;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @Bind(R.id.rideInfo)
    TextView rideInfo;

    @Bind(R.id.quatInfo)
    TextView quatInfo;

    @Bind(R.id.ratBar)
    RatingBar ratBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // load dummy item by using the passed item ID.
            dummyItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateAndBind(inflater, container, R.layout.fragment_article_detail);

        if (!((BaseActivity) getActivity()).providesActivityToolbar()) {
            // No Toolbar present. Set include_toolbar:
            ((BaseActivity) getActivity()).setToolbar((Toolbar) rootView.findViewById(R.id.toolbar));
        }

        if (dummyItem != null) {
            loadBackdrop();
            collapsingToolbar.setTitle(dummyItem.title);
            author.setText("You rode on " + dummyItem.date);
            quote.setText("You rode " + dummyItem.content + " miles");
            rideInfo.setText("-Your ride lasted: "+ dummyItem.rideTime +". \n-Your average speed was " + dummyItem.avSpeed +" mph  \n-Your top speed was "+dummyItem.tSpeed +" mph  \n-Your largest elevation change was " + dummyItem.elev + " floors.");
            quatInfo.setText("Safe Brake percentages: "+ dummyItem.goodSt + "% \n-Number of Dangerous Stops:" +dummyItem.badSt );
            ratBar.setRating(dummyItem.rat);
        }

        return rootView;
    }

    private void loadBackdrop() {
        Glide.with(this).load(dummyItem.photoId).centerCrop().into(backdropImg);
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

    public static ArticleDetailFragment newInstance(String itemID) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putString(ArticleDetailFragment.ARG_ITEM_ID, itemID);
        fragment.setArguments(args);
        return fragment;
    }

    public ArticleDetailFragment() {}
}
