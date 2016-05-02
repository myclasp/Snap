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

    private TextView upsTV, downsTV, totalTV;

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
                if (click.getMarked() == 0)
                    upsCount++;
            } else {
                downs.add(click);
                if (click.getMarked() == 0)
                    downsCount++;
            }
        }
        int totalCount = upsCount + downsCount;

        //setup the textViews
        upsTV = ((TextView) findViewById(R.id.ups_tv));
        downsTV = ((TextView) findViewById(R.id.downs_tv));
        totalTV = ((TextView) findViewById(R.id.all_tv));

        clickAdapter = new ClickAdapter(activity, all, upsTV, downsTV, totalTV);
        assert listview != null;
        listview.setAdapter(clickAdapter);

        //set the title
        Utils.setActionBarTextAndFont(getSupportActionBar(), new SimpleDateFormat("EE dd.MM.yyyy", Locale.getDefault()), new Date(b.getLong("timestamp") * 1000), new TypefaceSpan(getApplicationContext(), "BebasNeue Bold.ttf"));

        //set the values and the listeners
        assert upsTV != null;
        upsTV.setText(String.valueOf(upsCount));
        upsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAdapter.notifyDataSetInvalidated();
                clickAdapter = new ClickAdapter(activity, ups, upsTV, downsTV, totalTV);
                listview.setAdapter(clickAdapter);
            }
        });

        assert downsTV != null;
        downsTV.setText(String.valueOf(downsCount));
        downsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAdapter.notifyDataSetInvalidated();
                clickAdapter = new ClickAdapter(activity, downs, upsTV, downsTV, totalTV);
                listview.setAdapter(clickAdapter);
            }
        });

        assert totalTV != null;
        totalTV.setText(String.valueOf(totalCount));
        totalTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAdapter.notifyDataSetInvalidated();
                clickAdapter = new ClickAdapter(activity, all, upsTV, downsTV, totalTV);
                listview.setAdapter(clickAdapter);
            }
        });
    }

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
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
