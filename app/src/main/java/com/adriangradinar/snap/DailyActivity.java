package com.adriangradinar.snap;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DailyActivity extends AppCompatActivity {

    private static final String TAG = DailyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_view);

        //get the bundle
        Bundle b = getIntent().getBundleExtra("values");

        //set the title to the selected day
        setTitle(b.getString("day") + " " + b.getString("month"));

        //set the pie chart data
        setWeekView(b.getInt("ups"), b.getInt("downs"));

        //set the events
        setEvents(b.getLong("timestamp"));
    }

    private void setWeekView(int ups, int downs){
        PieChart pieChart = (PieChart) findViewById(R.id.day_pie_chart);
        pieChart.setDescription("");
        pieChart.getLegend().setEnabled(false);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onNothingSelected() {}
        });
//        mChart.getLegend().setPosition(Legend.LegendPosition.LEFT_OF_CHART);
//        mChart.setUsePercentValues(true);

        //?!?
        pieChart.setHoleRadius(44f);
        pieChart.setTransparentCircleRadius(50f);
//        mChart.setRotationAngle(0);


        //add some data
        ArrayList<Entry> yVals = new ArrayList<>();
        yVals.add(new Entry(ups, 0));
        yVals.add(new Entry(downs, 1));

        //and some colours
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_primary));
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_primary));

        //and some labels
        ArrayList<String> xVals = new ArrayList<>();
        xVals.add("Ups");
        xVals.add("Downs");

        //set the data
        PieDataSet pieDataSet = new PieDataSet(yVals, "");
        pieDataSet.setSliceSpace(3);
        pieDataSet.setColors(colors);

        PieData pieData = new PieData(xVals, pieDataSet);
        pieData.setValueFormatter(new MyValueFormatter());
//        pieData.setDrawValues(false);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);

        // undo all highlights
        pieChart.highlightValues(null);

        // update pie chart
        pieChart.invalidate();
    }

    private void setEvents(long timestamp){

    }
}

class MyValueFormatter implements ValueFormatter {

    private DecimalFormat mFormat;

    public MyValueFormatter() {
        mFormat = new DecimalFormat("###,###,##0"); // use no decimal
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        // write your logic here
        return mFormat.format(value); // e.g. append a dollar-sign
    }
}
