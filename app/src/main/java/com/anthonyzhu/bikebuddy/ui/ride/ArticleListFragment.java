package com.anthonyzhu.bikebuddy.ui.ride;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.anthonyzhu.bikebuddy.R;
import com.anthonyzhu.bikebuddy.database.Ride;
import com.anthonyzhu.bikebuddy.database.RideHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

/**
 * Shows a list of all available quotes.
 * <p/>
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ArticleListFragment extends ListFragment {

    private Callback callback = dummyCallback;
    public final static Integer[] imageResIds = new Integer[] {
            R.drawable.p1, R.drawable.p2, R.drawable.p3,
            R.drawable.p4, R.drawable.p5};
    private ArrayList<Ride> rideList;

    /**
     * A callback interface. Called whenever a item has been selected.
     */
    public interface Callback {
        void onItemSelected(long id);
    }

    /**
     * A dummy no-op implementation of the Callback interface. Only used when no active Activity is present.
     */
    private static final Callback dummyCallback = new Callback() {
        @Override
        public void onItemSelected(long id) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideList = new ArrayList<>();
        buildList();
        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // notify callback about the selected list item
        callback.onItemSelected(rideList.get(position).id);
    }

    private void buildList() {
        rideList.clear();
        String query = "SELECT * FROM " + Ride.TABLE_NAME;
        Cursor c1 = RideHandler.getInstance(getActivity()).selectQuery(query);
        if (c1 != null && c1.getCount() != 0) {
            if (c1.moveToFirst()) {
                do {
                    Ride ride = new Ride();
                    ride.id = c1.getLong(0);
                    ride.date = c1.getString(1);
                    ride.time = c1.getString(2);
                    ride.distance = c1.getFloat(3);
                    ride.averageSpeed = c1.getFloat(4);
                    ride.rideTime = c1.getString(5);
                    ride.badStops = c1.getInt(6);
                    ride.rating = c1.getInt(7);
                    // List goes from newest to oldest
                    rideList.add(0, ride);
                } while (c1.moveToNext());
            }
            c1.close();
        }

        setListAdapter(new MyListAdapter(rideList));
    }

    /**
     * onAttach(Context) is not called on pre API 23 versions of Android.
     * onAttach(Activity) is deprecated but still necessary on older devices.
     */
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    /**
     * Deprecated on API 23 but still necessary for pre API 23 devices.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /**
     * Called when the fragment attaches to the context
     */
    protected void onAttachToContext(Context context) {
        if (!(context instanceof Callback)) {
            throw new IllegalStateException("Activity must implement callback interface.");
        }

        callback = (Callback) context;
    }

    private class MyListAdapter extends BaseAdapter {
        ArrayList<Ride> rideList;

        public MyListAdapter(ArrayList<Ride> list) {
            rideList = list;
        }

        @Override
        public int getCount() {
            return rideList.size();
        }

        @Override
        public Object getItem(int position) {
            return rideList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return rideList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_ride, container, false);
            }

            final Ride ride = (Ride) getItem(position);
            ((TextView) convertView.findViewById(R.id.ride_date)).setText(ride.date);
            ((TextView) convertView.findViewById(R.id.ride_time)).setText(ride.time);
            final ImageView img = (ImageView) convertView.findViewById(R.id.thumbnail);
            Glide.with(getActivity()).load(imageResIds[position % 5]).asBitmap().fitCenter().into(new BitmapImageViewTarget(img) {
//            Glide.with(getActivity()).load(item.photoId).asBitmap().fitCenter().into(new BitmapImageViewTarget(img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    img.setImageDrawable(circularBitmapDrawable);
                }
            });

            return convertView;
        }
    }

    public ArticleListFragment() {
    }
}
