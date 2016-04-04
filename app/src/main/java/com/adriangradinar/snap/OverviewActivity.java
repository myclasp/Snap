package com.adriangradinar.snap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.adriangradinar.snap.utils.DatabaseHandler;
import com.adriangradinar.snap.utils.Utils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OverviewActivity extends AppCompatActivity {

    private static final String TAG = OverviewActivity.class.getSimpleName();
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        db = DatabaseHandler.getHelper(getApplicationContext());

        setMostActiveDay();
        setLeastActiveDay();
        setHappiestDay();
        setSaddestDay();
    }

    private void setMostActiveDay(){
        final String[] values = db.getMostActiveDay();
        TextView tv_mad_month = (TextView) findViewById(R.id.tv_mad_month);
        TextView tv_mad_day = (TextView) findViewById(R.id.tv_mad_day);
        TextView tv_mad_actions = (TextView) findViewById(R.id.tv_mad_actions);

        findViewById(R.id.relativeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTheNExtView(Long.parseLong(values[4]), Utils.convertStringToLongMonth(values[1]), values[2]);
            }
        });

        tv_mad_month.setText(values[1] + " " + values[0]);
        tv_mad_day.setText(String.valueOf(values[2]));
        values[3] += " actions.";
        tv_mad_actions.setText(values[3]);
    }

    private void setLeastActiveDay(){
        final String[] values = db.getLeastActiveDay();
        TextView tv_lad_month = (TextView) findViewById(R.id.tv_lad_month);
        TextView tv_lad_day = (TextView) findViewById(R.id.tv_lad_day);
        TextView tv_lad_actions = (TextView) findViewById(R.id.tv_lad_actions);

        findViewById(R.id.relativeLayout2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTheNExtView(Long.parseLong(values[4]), Utils.convertStringToLongMonth(values[1]), values[2]);
            }
        });

        tv_lad_month.setText(values[1] + " " + values[0]);
        tv_lad_day.setText(String.valueOf(values[2]));
        values[3] += " actions.";
        tv_lad_actions.setText(values[3]);
    }

    private void setHappiestDay(){
        final String[] values = db.getMostUpDay();
        TextView tv_hd_month = (TextView) findViewById(R.id.tv_hd_month);
        TextView tv_hd_day = (TextView) findViewById(R.id.tv_hd_day);
        TextView tv_hd_actions = (TextView) findViewById(R.id.tv_hd_actions);

        findViewById(R.id.relativeLayout3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTheNExtView(Long.parseLong(values[4]), Utils.convertStringToLongMonth(values[1]), values[2]);
            }
        });

        tv_hd_month.setText(values[1] + " " + values[0]);
        tv_hd_day.setText(String.valueOf(values[2]));
        values[3] += " actions.";
        tv_hd_actions.setText(values[3]);
    }

    private void setSaddestDay(){
        final String[] values = db.getMostDownDay();
        TextView tv_sd_month = (TextView) findViewById(R.id.tv_sd_month);
        TextView tv_sd_day = (TextView) findViewById(R.id.tv_sd_day);
        TextView tv_sd_actions = (TextView) findViewById(R.id.tv_sd_actions);

        findViewById(R.id.relativeLayout4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTheNExtView(Long.parseLong(values[4]), Utils.convertStringToLongMonth(values[1]), values[2]);
            }
        });

        tv_sd_month.setText(values[1] + " " + values[0]);
        tv_sd_day.setText(String.valueOf(values[2]));
        values[3] += " actions.";
        tv_sd_actions.setText(values[3]);
    }

    private void navigateToTheNExtView(long timestamp, String month, String day){
        Bundle bundle = new Bundle();
        bundle.putLong("timestamp", timestamp);
        bundle.putString("day", day);
        bundle.putString("month", month);

        //move to the newt view... with the bundle genius!
        startActivity(new Intent(OverviewActivity.this, DailyActivity.class).putExtra("values", bundle));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.month_view:
                startActivity(new Intent(OverviewActivity.this, MonthActivity.class));
                break;
            case R.id.week_view:
                startActivity(new Intent(OverviewActivity.this, WeekActivity.class));
                break;
            case R.id.refresh:
                Toast.makeText(OverviewActivity.this, getString(R.string.functionality_not_supported), Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
