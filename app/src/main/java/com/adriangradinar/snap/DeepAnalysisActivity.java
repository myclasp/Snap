package com.adriangradinar.snap;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.utils.DatabaseHandler;
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

public class DeepAnalysisActivity extends AppCompatActivity {

    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_deep_analysis);
        setTitle("Hourly view");

        mChart = (LineChart) findViewById(R.id.chart1);
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

        mChart.setData(generateLineData());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    protected LineData generateLineData() {

        //get the data
        ArrayList[] allClicks = DatabaseHandler.getHelper(getApplicationContext()).getTotalEventsPerHour();
        Set<String> labelSet = new HashSet<>();

        //arrange the data
        ArrayList<Entry> ups = new ArrayList<>();
        ArrayList<Entry> downs = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        Click up, down;
        for(int i = 0; i < allClicks[0].size(); i++){
            up = (Click) allClicks[0].get(i);
            ups.add(new Entry(up.getTotalClicks(), Integer.parseInt(up.getHour())));
            labelSet.add(up.getHour());
        }

        for(int i = 0; i < allClicks[1].size(); i++){
            down = (Click) allClicks[1].get(i);
            downs.add(new Entry(down.getTotalClicks(), Integer.parseInt(down.getHour())));
            labelSet.add(down.getHour());
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

        LineData d = new LineData(labels,  sets);
        return d;
    }
}
