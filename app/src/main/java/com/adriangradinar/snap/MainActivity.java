package com.adriangradinar.snap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.services.LocationService;
import com.adriangradinar.snap.utils.DatabaseHandler;
import com.adriangradinar.snap.utils.Utils;
import com.path.android.jobqueue.AsyncAddCallback;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;

import java.util.Random;

import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends ActivityManagePermission {

    private DatabaseHandler db;
    private long currentTimestamp;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get a ref to the database
        db = DatabaseHandler.getHelper(getApplicationContext());

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(buttonListener);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            startActivity(new Intent(MainActivity.this, OverviewActivity.class));
            if(db.countTotalClicks() == 0){
                //need to generate some random data
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        generateClicks(2500);
                        startActivity(new Intent(MainActivity.this, OverviewActivity.class));
                    }
                }).start();
            }
            else{
//                db.logClicks();
                startActivity(new Intent(MainActivity.this, OverviewActivity.class));
            }
        }
    };

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

    private void generateClicks(int total) {
        currentTimestamp = Utils.getTimestamp() - (604800 * 3); //take 3 weeks off
        final Random random = new Random();

        for(int i = 0; i < total; i++){
            //increment the timestamp
            currentTimestamp += Utils.randInt(random, 60*5, 60*30); //between 5min and 30min
            db.addClick(new Click(Utils.randInt(random, 1,2), 54.048775, -2.806450, 10.0, currentTimestamp));
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
