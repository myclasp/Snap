package com.adriangradinar.snap.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adriangradinar.snap.PermissionActivity;
import com.adriangradinar.snap.services.LocationService;
import com.adriangradinar.snap.utils.Utils;

public class SnapReceiver extends BroadcastReceiver {

    private static final String TAG = SnapReceiver.class.getSimpleName();
    public SnapReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            String action = intent.getAction();

            //log single click
            if(action.equals("snap.UpReceiver")) {
                if (!Utils.hasPermissions(context.getApplicationContext(), Utils.PERMISSIONS)) {
                    context.startActivity(new Intent(context.getApplicationContext(), PermissionActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(LocationService.CLICK, LocationService.UP_CLICK));
                } else {
                    context.startService(new Intent(context.getApplicationContext(), LocationService.class).putExtra(LocationService.CLICK, LocationService.UP_CLICK));
                }
            }

            //log double click
            if(action.equals("snap.DownReceiver")) {
                if (!Utils.hasPermissions(context.getApplicationContext(), Utils.PERMISSIONS)) {
                    context.startActivity(new Intent(context.getApplicationContext(), PermissionActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(LocationService.CLICK, LocationService.DOWN_CLICK));
                } else {
                    context.startService(new Intent(context.getApplicationContext(), LocationService.class).putExtra(LocationService.CLICK, LocationService.DOWN_CLICK));
                }
            }
        }
    }
}
