package com.adriangradinar.snap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.adriangradinar.snap.services.LocationService;
import com.adriangradinar.snap.utils.Utils;

import java.util.Arrays;

public class PermissionActivity extends AppCompatActivity {

    private static final String TAG = PermissionActivity.class.getSimpleName();

    // Identifier for the permission request
    private static final int REQUEST_ALL_PERMISSIONS = 1;
    private int typeOfClick = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        if (getIntent().hasExtra(LocationService.CLICK)) {
            typeOfClick = getIntent().getExtras().getInt(LocationService.CLICK);
            Log.e(TAG, "type " + typeOfClick);
        }

        //check them permissions again
        if (!Utils.hasPermissions(this, Utils.PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, Utils.PERMISSIONS, REQUEST_ALL_PERMISSIONS);
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        Log.e(TAG, Arrays.toString(permissions));
        Log.e(TAG, requestCode + "");
        Log.e(TAG, Arrays.toString(grantResults));

        if (requestCode == REQUEST_ALL_PERMISSIONS) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "The " + permissions[i] + " is required to run this application! You need to allow this permission to record your interaction.", Toast.LENGTH_LONG).show();
                    return;
                }
                finish();
            }

            //close down the activity (even if it is invisible)
            finish();

            //if all permissions were granted, move back to the our service
            startService(new Intent(PermissionActivity.this, LocationService.class).putExtra(LocationService.CLICK, typeOfClick));
        }
    }
}
