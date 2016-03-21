package com.adriangradinar.snap.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.utils.CustomExceptionHandler;
import com.adriangradinar.snap.utils.DatabaseHandler;
import com.adriangradinar.snap.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;

public class LocationService extends Service implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    public final static String CLICK = "click";
    public final static int UP_CLICK = 1;
    public final static int DOWN_CLICK = 2;

    private final static int FASTEST_TIME_INTERVAL = 500;
    private final static int UPDATE_TIME_INTERVAL = 4000;
    private final static int SLEEP_TIME_INTERVAL = 2000;
    private final static double DESIRED_ACCURACY = 30; //in meters
    private final static int NUMBER_OF_TRIES = 15;

    //    private static boolean isGpsListenerSet = false;
    public final static String TAG = LocationService.class.getSimpleName();
    private String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Clasp/Snap";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation = null;
    private LocationRequest locationRequest;
    private DatabaseHandler db;

    private JobManager jobManager;

    public LocationService() {}

    @Override
    public void onCreate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "no write permissions have been given!");
            return;
        }

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(fullPath));
        }

//        if (locationManager == null) {
//            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//            if (!isGpsListenerSet) {
//                locationManager.addGpsStatusListener(new GpsStatus.Listener() {
//                    @Override
//                    public void onGpsStatusChanged(int event) {
//                        if (event == GpsStatus.GPS_EVENT_STOPPED) {
//                            if (!((LocationManager) LocationService.this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                                Utils.createNotification(LocationService.this.getApplicationContext(), "Location was deactivated!");
//                                sendBroadcast(new Intent(NOTIFICATION).putExtra("result", GPS_OFF));
//                            }
//                        }
//                    }
//                });
//                isGpsListenerSet = true;
//            }
//        }

        buildGoogleApiClient();
        if (!Utils.checkIfGpsIsOn(this)) {
            Utils.createNotification(LocationService.this.getApplicationContext(), "Location is deactivated!");
        }

        locationRequest = new LocationRequest();
        db = DatabaseHandler.getHelper(LocationService.this.getApplicationContext());
    }

    //---- Location data
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed!");
    }

    protected void createLocationRequest() {
        locationRequest.setFastestInterval(FASTEST_TIME_INTERVAL);
        locationRequest.setInterval(UPDATE_TIME_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //start the update process
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "no permissions have been given!");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Log.d(TAG, "location changed " + location.getLatitude() + " - " + location.getLongitude() + " - " + location.getAccuracy());

        //when we get access to the jobManager, we close the location listener
        if(jobManager != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(jobManager.getActiveConsumerCount() == 0){
                        Log.e(TAG, "done!");
                        stopLocationService();
                    }
                }
            }).start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //connect the google client in case it was disconnected
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        if (intent != null && intent.hasExtra(LocationService.CLICK)) {
            if(jobManager == null){
                jobManager = new JobManager(getApplicationContext());
            }
            jobManager.addJobInBackground(new HandleRequests(new Click(intent.getExtras().getInt(LocationService.CLICK), Utils.getTimestamp())));
        }

        return START_NOT_STICKY;
    }

    private void stopLocationService(){
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            stopSelf();

//            db.logClicks();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class HandleRequests extends Job{

        public static final int PRIORITY = 1;
        private int iterations;
        private Click click;

        public HandleRequests(Click click) {
            super(new Params(PRIORITY).persist());
            this.click = click;
            this.iterations = 0;
        }

        @Override
        public void onAdded() {
            Log.d(TAG, "added");
            while(mLastLocation == null ||  mLastLocation.getAccuracy() > DESIRED_ACCURACY){
                try {
                    Thread.sleep(SLEEP_TIME_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(this.iterations > NUMBER_OF_TRIES) {
                    break;
                }else{
                    this.iterations++;
                }
            }

            //let's save whatever location we have to the database
            if(mLastLocation != null){
                Log.e(TAG, "save current location to db!");
//                Log.e(TAG, "acc: " + mLastLocation.getAccuracy());
                db.addClick(new Click(click.getTotalClicks(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAccuracy(), click.getTimestamp()));
            }
            else{
                Log.e(TAG, "save dummy location to db!");
                db.addClick(new Click(click.getTotalClicks(), 0d, 0d, 0d, click.getTimestamp()));
//                Log.e(TAG, "acc: " + mLastLocation.getAccuracy());
            }
        }

        @Override
        public void onRun() throws Throwable {
            Log.e(TAG, "job running!");
        }

        @Override
        protected void onCancel(int cancelReason) {
            Log.e(TAG, "cancelled " + cancelReason);
            super.onCancel(cancelReason);
        }
    }
}