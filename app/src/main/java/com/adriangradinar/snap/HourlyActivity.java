package com.adriangradinar.snap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.classes.TypefaceSpan;
import com.adriangradinar.snap.utils.DatabaseHandler;
import com.adriangradinar.snap.utils.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HourlyActivity extends AppCompatActivity {

    private static final String TAG = HourlyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_analysis);

        //set the title
        Utils.setActionBarTextAndFont(getSupportActionBar(), new TypefaceSpan(this, "BebasNeue Bold.ttf"), "Hourly Analysis");
    }

    private void setGraph(LineChart mChart) {
        if (mChart != null) {
            mChart.setDrawGridBackground(false);
            mChart.getLegend().setEnabled(false);
            mChart.setDescription("");
            mChart.setDoubleTapToZoomEnabled(false);
            mChart.setPinchZoom(false);
            mChart.setTouchEnabled(false);

            XAxis xAxis = mChart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setSpaceBetweenLabels(2);

            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setDrawAxisLine(false);
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawLabels(false);

            YAxis rightAxis = mChart.getAxisRight();
            rightAxis.setDrawAxisLine(false);
            rightAxis.setDrawGridLines(false);
            rightAxis.setDrawLabels(false);

            //get the data
            ArrayList<Click> allClicks = DatabaseHandler.getHelper(getApplicationContext()).getTotalEventsPerHour();
            Set<String> labelSet = new HashSet<>();

            //arrange the data
            ArrayList<Entry> ups = new ArrayList<>();
            ArrayList<Entry> downs = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            for (int i = 0; i < allClicks.size(); i++) {
                ups.add(new Entry(allClicks.get(i).getTotalUpUnmarked(), i));
                downs.add(new Entry(allClicks.get(i).getTotalDownUnmarked(), i));
                if (allClicks.get(i).getTotalMarked() != 0) {
                    labelSet.add(allClicks.get(i).getHour() + "!");
                } else {
                    labelSet.add(allClicks.get(i).getHour());
                }
            }

            labels.addAll(labelSet);
            Collections.sort(labels);

            ArrayList<ILineDataSet> sets = new ArrayList<>();
            LineDataSet upsLineDataSet = new LineDataSet(ups, "ups");
            LineDataSet downsLineDataSet = new LineDataSet(downs, "downs");

            //set some line widths
//        upsLineDataSet.setLineWidth(2f);
//        downsLineDataSet.setLineWidth(2f);
            upsLineDataSet.enableDashedLine(16f, 10f, 10f);
            downsLineDataSet.enableDashedLine(16f, 10f, 10f);

            //don't draw circles
            upsLineDataSet.setDrawCircles(false);
            upsLineDataSet.setDrawValues(false);
            downsLineDataSet.setDrawCircles(false);
            downsLineDataSet.setDrawValues(false);

            //set the colours
            upsLineDataSet.setColor(ContextCompat.getColor(getApplicationContext(), R.color.up_primary));
            downsLineDataSet.setColor(ContextCompat.getColor(getApplicationContext(), R.color.down_primary));

            //add the sets
            sets.add(upsLineDataSet);
            sets.add(downsLineDataSet);
//        downsLineDataSet.setDrawCubic(true);

            mChart.setData(new LineData(labels, sets));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setGraph((LineChart) findViewById(R.id.chart1));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hourly, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.month_view:
                startActivity(new Intent(HourlyActivity.this, MonthActivity.class));
                break;
            case R.id.week_view:
                startActivity(new Intent(HourlyActivity.this, WeekActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
