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
import com.adriangradinar.snap.utils.DatabaseHandler;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DailyActivity extends AppCompatActivity {

    private static final String TAG = DailyActivity.class.getSimpleName();
    private DatabaseHandler db;
    private Activity activity;

    private ArrayList<Click> all;
    private ArrayList<Click> ups;
    private ArrayList<Click> downs;
    private ClickAdapter clickAdapter;

    private int upsCount = 0;
    private int downsCount = 0;
    private int totalCount = 0 ;

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
        for(Click click : all){
            if(click.getTotalClicks() == 1){
                ups.add(click);
                upsCount++;
            }
            else{
                downs.add(click);
                downsCount++;
            }
        }
        totalCount = upsCount + downsCount;

        clickAdapter = new ClickAdapter(activity, all);
        listview.setAdapter(clickAdapter);

        //set the title to the selected day
        setTitle(b.getString("day") + " " + b.getString("month"));

        //set the values and the listeners
        TextView upsTV = ((TextView) findViewById(R.id.ups_tv));
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
}

//class MyValueFormatter implements ValueFormatter {
//
//    private DecimalFormat mFormat;
//
//    public MyValueFormatter() {
//        mFormat = new DecimalFormat("###,###,##0"); // use no decimal
//    }
//
//    @Override
//    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//        // write your logic here
//        return mFormat.format(value); // e.g. append a dollar-sign
//    }
//}
