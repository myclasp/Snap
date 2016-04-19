package com.adriangradinar.snap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.adriangradinar.snap.adapters.ClickAdapter;
import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.classes.TypefaceSpan;
import com.adriangradinar.snap.utils.DatabaseHandler;
import com.adriangradinar.snap.utils.Utils;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DailyActivity extends AppCompatActivity {

    private static final String TAG = DailyActivity.class.getSimpleName();
    private long analytics_timestamp = 0;

    private DatabaseHandler db;
    private Activity activity;

    private ArrayList<Click> all;
    private ArrayList<Click> ups;
    private ArrayList<Click> downs;
    private ClickAdapter clickAdapter;

    private int upsCount = 0;
    private int downsCount = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_view);

        activity = this;
        final ListView listview = (ListView) findViewById(R.id.listView);
        db = DatabaseHandler.getHelper(getApplicationContext());

        //get the bundle
        Bundle b = getIntent().getBundleExtra("values");

        //get the clicks
        all = db.getDayBasedOnTimestamp(b.getLong("timestamp"));

        ups = new ArrayList<>();
        downs = new ArrayList<>();

        //let's parse the clicks
        for (Click click : all) {
            if (click.getTotalClicks() == 1) {
                ups.add(click);
                upsCount++;
            } else {
                downs.add(click);
                downsCount++;
            }
        }
        int totalCount = upsCount + downsCount;

        clickAdapter = new ClickAdapter(activity, all);
        assert listview != null;
        listview.setAdapter(clickAdapter);

        //set the title
        Utils.setActionBarTextAndFont(getSupportActionBar(), new SimpleDateFormat("EE dd.MM.yyyy", Locale.getDefault()), new Date(b.getLong("timestamp") * 1000), new TypefaceSpan(getApplicationContext(), "BebasNeue Bold.ttf"));

        //set the values and the listeners
        TextView upsTV = ((TextView) findViewById(R.id.ups_tv));
        assert upsTV != null;
        upsTV.setText(String.valueOf(upsCount));
        upsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAdapter.notifyDataSetInvalidated();
                clickAdapter = new ClickAdapter(activity, ups);
                listview.setAdapter(clickAdapter);
            }
        });

        final TextView downsTV = ((TextView) findViewById(R.id.downs_tv));
        assert downsTV != null;
        downsTV.setText(String.valueOf(downsCount));
        downsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAdapter.notifyDataSetInvalidated();
                clickAdapter = new ClickAdapter(activity, downs);
                listview.setAdapter(clickAdapter);
            }
        });

        TextView totalTV = ((TextView) findViewById(R.id.all_tv));
        assert totalTV != null;
        totalTV.setText(String.valueOf(totalCount));
        totalTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAdapter.notifyDataSetInvalidated();
                clickAdapter = new ClickAdapter(activity, all);
                listview.setAdapter(clickAdapter);
            }
        });

        //set the pie chart data
//        setWeekView(b.getInt("ups"), b.getInt("downs"));

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //    private void setWeekView(int ups, int downs){
//        PieChart pieChart = (PieChart) findViewById(R.id.day_pie_chart);
//        pieChart.setDescription("");
//        pieChart.getLegend().setEnabled(false);
//        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
//            @Override
//            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
//                Log.e(TAG, e.toString());
//            }
//
//            @Override
//            public void onNothingSelected() {}
//        });
////        mChart.getLegend().setPosition(Legend.LegendPosition.LEFT_OF_CHART);
////        mChart.setUsePercentValues(true);
//
//        //?!?
//        pieChart.setHoleRadius(44f);
//        pieChart.setTransparentCircleRadius(50f);
////        mChart.setRotationAngle(0);
//
//
//        //add some data
//        ArrayList<Entry> yVals = new ArrayList<>();
//        yVals.add(new Entry(ups, 0));
//        yVals.add(new Entry(downs, 1));
//
//        //and some colours
//        ArrayList<Integer> colors = new ArrayList<>();
//        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_primary));
//        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_primary));
//
//        //and some labels
//        ArrayList<String> xVals = new ArrayList<>();
//        xVals.add("Ups");
//        xVals.add("Downs");
//
//        //set the data
//        PieDataSet pieDataSet = new PieDataSet(yVals, "");
//        pieDataSet.setSliceSpace(3);
//        pieDataSet.setColors(colors);
//
//        PieData pieData = new PieData(xVals, pieDataSet);
//        pieData.setValueFormatter(new MyValueFormatter());
////        pieData.setDrawValues(false);
//        pieData.setValueTextSize(14f);
//        pieData.setValueTextColor(Color.WHITE);
//
//        pieChart.setData(pieData);
//
//        // undo all highlights
//        pieChart.highlightValues(null);
//
//        // update pie chart
//        pieChart.invalidate();
//    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.addActivityAnalytic(TAG, analytics_timestamp, (Utils.getTimestamp() - analytics_timestamp));
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics_timestamp = Utils.getTimestamp();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Daily Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.adriangradinar.snap/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Daily Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.adriangradinar.snap/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
