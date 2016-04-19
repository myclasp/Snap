package com.adriangradinar.snap;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.classes.ClickAddress;
import com.adriangradinar.snap.utils.DatabaseHandler;
import com.adriangradinar.snap.utils.ThreadManager;
import com.adriangradinar.snap.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends ActivityManagePermission {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LIMIT = 20;
    private static String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snap";
    private long analytics_timestamp = 0;
    private DatabaseHandler db;
    private long currentTimestamp;
    private Button button;
    private ProgressBar spinner;
    private Geocoder geocoder;
    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Utils.isWifiON(getApplicationContext()) && db.countTotalLocationsWithoutAnAddress() > 0) {

                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                Toast.makeText(getApplicationContext(), "Please wait while we convert your locations into addresses. Thank you!", Toast.LENGTH_LONG).show();
                spinner.setVisibility(View.VISIBLE);
                button.setVisibility(View.INVISIBLE);

                ThreadManager.runInBackgroundThenUi(new Runnable() {
                    @Override
                    public void run() {
                        //this is in background
                        convertAddresses();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        spinner.setVisibility(View.INVISIBLE);
                        button.setVisibility(View.VISIBLE);
                        startActivity(new Intent(MainActivity.this, OverviewActivity.class));
                    }
                });
            } else {
                startActivity(new Intent(MainActivity.this, OverviewActivity.class));
            }
//            if(db.countTotalClicks() == 0){
//                //need to generate some random data
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        generateClicks(2500);
//                        startActivity(new Intent(MainActivity.this, OverviewActivity.class));
//                    }
//                }).start();
//            }
//            else{
////                db.logClicks();
////                startActivity(new Intent(MainActivity.this, OverviewActivity.class));
//
//            }


//            db.getTotalEventsPerHour();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get a ref to the database
        db = DatabaseHandler.getHelper(getApplicationContext());
        spinner = (ProgressBar) findViewById(R.id.progressBar);

        button = (Button) findViewById(R.id.button);
        assert button != null;
        button.setOnClickListener(buttonListener);

        TextView snapTV = (TextView) findViewById(R.id.snapTV);
        assert snapTV != null;
        snapTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                db.readCSV(getApplicationContext(), R.raw.stats);
//                startActivity(new Intent(MainActivity.this, DeepAnalysisActivity.class));
//                db.getCurrentMonth();
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        assert imageView != null;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                generateClicks(2500);
                spinner.setVisibility(View.VISIBLE);
                button.setVisibility(View.INVISIBLE);
                ThreadManager.runInBackgroundThenUi(new Runnable() {
                    @Override
                    public void run() {
                        db.downloadClicks(fullPath);
                        db.downloadAnalytics(fullPath);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Download finished!", Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.INVISIBLE);
                        button.setVisibility(View.VISIBLE);
                    }
                });

            }
        });
    }

//    View.OnClickListener buttonListenerLocation = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            askCompactPermissions(new String[]{PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE}, new PermissionResult() {
//                @Override
//                public void permissionGranted() {
//                    startService(new Intent(MainActivity.this, LocationService.class).putExtra(LocationService.CLICK, LocationService.UP_CLICK));
//                }
//
//                @Override
//                public void permissionNotGranted() {
//                    Log.e(TAG, "no permissions given");
//                }
//            });
//        }
//    };

    private void convertAddresses() {
        int total = db.countTotalLocationsWithoutAnAddress();
        if (total > LIMIT) {
            total = LIMIT;
        }

        // we might need to add a limit here in case we hit a memory issue
        ArrayList<ClickAddress> clickAddresses = new ArrayList<>();
        ArrayList<Click> clicksToBeConverted = db.getClicksWithoutAddress(total);
        if (clicksToBeConverted.size() != 0) {
            for (Click click : clicksToBeConverted) {
                clickAddresses.add(new ClickAddress(click.getId(), Utils.returnAddress(geocoder, click.getLatitude(), click.getLongitude())));
            }
            db.updateAddresses(clickAddresses);
            convertAddresses();
        } else {
            Log.d(TAG, "conversion finished!");
        }
    }

    private void generateClicks(int total) {
        currentTimestamp = Utils.getTimestamp() - (604800 * 3); //take 3 weeks off
        final Random random = new Random();

        for (int i = 0; i < total; i++) {
            //increment the timestamp
            currentTimestamp += Utils.randInt(random, 60 * 5, 60 * 30); //between 5min and 30min
            db.addClick(new Click(Utils.randInt(random, 1, 2), 54.048775, -2.806450, 10.0, "dummy address", currentTimestamp));
        }
        Log.e(TAG, "Generation finished!");
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
