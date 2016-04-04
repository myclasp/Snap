package com.adriangradinar.snap.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adriangradinar.snap.services.LocationService;

public class SnapReceiver extends BroadcastReceiver {

    private static final String TAG = SnapReceiver.class.getSimpleName();
    public SnapReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            String action = intent.getAction();

            //log single click
            if(action.equals("snap.UpReceiver")) {
                Log.d(TAG, "one click");
                context.startService(new Intent(context.getApplicationContext(), LocationService.class).putExtra(LocationService.CLICK, LocationService.UP_CLICK));
            }

            //log double click
            if(action.equals("snap.DownReceiver")) {
                Log.d(TAG, "two clicks");
                context.startService(new Intent(context.getApplicationContext(), LocationService.class).putExtra(LocationService.CLICK, LocationService.DOWN_CLICK));
            }
        }
    }
}
