package com.adriangradinar.snap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.adriangradinar.snap.classes.Click;
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
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WeekActivity extends AppCompatActivity {

    private final static String TAG = WeekActivity.class.getSimpleName();
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week);

        //open the database
        db = DatabaseHandler.getHelper(getApplicationContext());
        setBarWeekView();
    }

    private void setBarWeekView(){
        BarChart barChart = (BarChart) findViewById(R.id.chartUp);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDescription("");
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
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

        ArrayList<Click> all = db.selectLast7Days();
        final List ups = all.subList(0, all.size()/2);
        final List downs = all.subList(all.size()/2, all.size());
        ArrayList<BarEntry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        for(int i = 0; i < ups.size(); i++){
            Click upCLick = (Click) ups.get(i);
            Click downClick = (Click) downs.get(i);

            if(upCLick.getTotalClicks() > downClick.getTotalClicks()){
                colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_secondary));
                colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_primary));
            }
            else if(upCLick.getTotalClicks() < downClick.getTotalClicks()){
                colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_primary));
                colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_secondary));
            }
            else{
                colors.add(ContextCompat.getColor(getApplicationContext(), R.color.down_primary));
                colors.add(ContextCompat.getColor(getApplicationContext(), R.color.up_primary));
            }

            yVals.add(new BarEntry(new float[]{((-1) * downClick.getTotalClicks()), upCLick.getTotalClicks()}, i));
            xVals.add(upCLick.getDay() + " "+ Utils.convertIntToMonth(Integer.parseInt(((Click)ups.get(i)).getMonth())));
        }

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                //create a bundle to add all data we can about this day
//                Bundle bundle = new Bundle();
//                bundle.putLong("timestamp", ((Click)ups.get(e.getXIndex())).getTimestamp());
//                bundle.putString("day", ((Click)ups.get(e.getXIndex())).getDay());
//                bundle.putString("month", Utils.convertIntToLongMonth(Integer.parseInt(((Click)ups.get(e.getXIndex())).getMonth())));
//                bundle.putInt("ups", ((Click)ups.get(e.getXIndex())).getTotalClicks());
//                bundle.putInt("downs", ((Click)downs.get(e.getXIndex())).getTotalClicks());
//
//                //move to the newt view... with the bundle genius!
//                startActivity(new Intent(WeekActivity.this, DailyActivity.class).putExtra("values", bundle));

//                bundle.putLong("timestamp", ((Click)ups.get(e.getXIndex())).getTimestamp());
//                bundle.putString("day", ((Click)ups.get(e.getXIndex())).getDay());
//                bundle.putString("day", Utils.convertIntToMonth(Integer.parseInt(((Click)ups.get(e.getXIndex())).getMonth())));

//                db.getDay(((Click)ups.get(e.getXIndex())).getTimestamp());

//                db.getDayPowerRelationship(((Click)ups.get(e.getXIndex())).getTimestamp());
//                db.getDayPowerRelationship(((Click)ups.get(e.getXIndex())).getTimestamp());
//                Log.e(TAG, "- "+ ((Click)ups.get(e.getXIndex())).getTotalClicks());
//                Log.e(TAG, "- "+ ((Click)downs.get(e.getXIndex())).getTotalClicks());
            }

            @Override
            public void onNothingSelected() {}
        });

        BarDataSet barDataSet = new BarDataSet(yVals, "blah-blah");
        barDataSet.setColors(colors);
        barDataSet.setDrawValues(false);

        BarData data = new BarData(xVals, barDataSet);
        barChart.setData(data);
        barChart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_week, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.month_view:
                startActivity(new Intent(WeekActivity.this, MonthActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
