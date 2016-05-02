package com.adriangradinar.snap;

import android.content.Context;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WeekActivity extends AppCompatActivity {

    private final static String TAG = WeekActivity.class.getSimpleName();
    private long analytics_timestamp = 0;

    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week);

        //set the title
        Utils.setActionBarTextAndFont(getSupportActionBar(), new TypefaceSpan(this, "BebasNeue Bold.ttf"), "Last 7 Days");

        //open the database
        db = DatabaseHandler.getHelper(getApplicationContext());
    }

    private void setBarWeekView(final BarChart barChart) {
        if (barChart != null) {

            barChart.invalidate();

            barChart.setDrawGridBackground(false);
            barChart.getLegend().setEnabled(false);
            barChart.setDescription("");
            barChart.setDoubleTapToZoomEnabled(false);
            barChart.setPinchZoom(false);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
            xAxis.setSpaceBetweenLabels(2);

            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setDrawAxisLine(false);
            leftAxis.setDrawGridLines(false);
            leftAxis.setDrawLabels(false);

            YAxis rightAxis = barChart.getAxisRight();
            rightAxis.setDrawAxisLine(false);
            rightAxis.setDrawGridLines(false);
            rightAxis.setDrawLabels(false);

            ArrayList<Integer> colors = new ArrayList<>();
            ArrayList<BarEntry> yVals = new ArrayList<>();
            ArrayList<String> xVals = new ArrayList<>();

            Click click;
            int ups, downs;

            final ArrayList<Click> clicks = db.selectLast7Days();
            for (int i = 0; i < clicks.size(); i++) {

                click = clicks.get(i);
                ups = click.getTotalUpUnmarked();
                downs = click.getTotalDownUnmarked();

                //set the colours
                if (ups > downs) {
                    if (downs != 0) {
                        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_secondary));
                        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_primary));
                    } else {
                        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_primary));
                    }
                } else if (ups < downs) {
                    if (ups != 0) {
                        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_primary));
                        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_secondary));
                    } else {
                        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_primary));
                    }
                } else {
                    //equal day
                    colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_primary));
                    colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_primary));
                }

                //set the data
                if (click.getTotalUpUnmarked() == 0) {
                    yVals.add(new BarEntry(new float[]{((-1) * click.getTotalDownUnmarked())}, i));
                } else if (click.getTotalDownUnmarked() == 0) {
                    yVals.add(new BarEntry(new float[]{click.getTotalUpUnmarked()}, i));
                } else {
                    yVals.add(new BarEntry(new float[]{((-1) * click.getTotalDownUnmarked()), click.getTotalUpUnmarked()}, i));
                }

                //set the labels
                if (click.getTotalMarked() == 0) {
                    xVals.add(Utils.getShortDayName(click.getTimestamp()));
                } else {
                    xVals.add(Utils.getShortDayName(click.getTimestamp()) + " !");
                }
            }

            barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                    //create a bundle to add all data we can about this day
                    Bundle bundle = new Bundle();
                    bundle.putLong("timestamp", (clicks.get(e.getXIndex())).getTimestamp());
                    bundle.putString("day", (clicks.get(e.getXIndex())).getDay());
                    bundle.putString("month", Utils.convertIntToLongMonth(Integer.parseInt((clicks.get(e.getXIndex())).getMonth())));

                    //move to the newt view... with the bundle genius!
                    startActivity(new Intent(WeekActivity.this, DailyActivity.class).putExtra("values", bundle));

                    //deactivate the highlighter
                    barChart.highlightValue(null);
                }

                @Override
                public void onNothingSelected() {
                }
            });

            BarDataSet barDataSet = new BarDataSet(yVals, "Our data set");
            barDataSet.setColors(colors);
            barDataSet.setDrawValues(false);

            BarData data = new BarData(xVals, barDataSet);
            barChart.setData(data);
            barChart.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_week, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.month_view:
                startActivity(new Intent(WeekActivity.this, MonthActivity.class));
                break;
            case R.id.hourly:
                startActivity(new Intent(WeekActivity.this, HourlyActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.addActivityAnalytic(TAG, analytics_timestamp, (Utils.getTimestamp() - analytics_timestamp));
    }

    @Override
    protected void onResume() {
        super.onResume();

        setBarWeekView((BarChart) findViewById(R.id.chartUp));
        analytics_timestamp = Utils.getTimestamp();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
