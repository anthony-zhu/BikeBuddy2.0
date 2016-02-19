package com.anthonyzhu.bikebuddy.dummy;

import com.anthonyzhu.bikebuddy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Just dummy content. Nothing special.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class DummyContent {

    /**
     * An array of sample items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample items. Key: sample ID; Value: Item.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<>(5);

    static {
        addItem(new DummyItem("1", R.drawable.p1, "Ride #1", "10/12/15", "10.0", "23:14", "28", "38","31",
                "89" , "1" , 4 ));
        addItem(new DummyItem("2", R.drawable.p2, "Ride #2", "10/30/15","6.1", "31:55", "20", "29","40",
                "75" , "4", 3));
        addItem(new DummyItem("3", R.drawable.p3, "Ride #3", "11/27/15", "4.5", "58:08", "36", "50", "24",
                "100", "0", 5));
        addItem(new DummyItem("4", R.drawable.p4, "Ride #4", "12/23/15","3.1", "20:35", "30", "35","8",
                "85", "2", 4));
        addItem(new DummyItem("5", R.drawable.p5, "Ride #5", "1/2/16","8.5", "41:19", "24", "33", "12",
                "66", "6", 2));
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class DummyItem {
        public final String id;
        public final int photoId;
        public final String title;
        public final String date;
        public final String content;
        public final String tSpeed;
        public final String avSpeed;
        public final String rideTime;
        public final String elev;
        public final String goodSt;
        public final String badSt;
        public final int rat;

        public DummyItem(String id, int photoId, String title, String date, String content, String rideTime,
                         String avSpeed, String tSpeed, String elev, String goodSt, String badSt, int rat ) {
            this.id = id;
            this.photoId = photoId;
            this.title = title;
            this.date = date;
            this.content = content;
            this.rideTime = rideTime;
            this.avSpeed = avSpeed;
            this.tSpeed = tSpeed;
            this.elev = elev;
            this.goodSt = goodSt;
            this.badSt = badSt;
            this.rat = rat;
        }
    }
}
