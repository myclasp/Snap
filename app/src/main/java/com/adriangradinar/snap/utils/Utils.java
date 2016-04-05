package com.adriangradinar.snap.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.adriangradinar.snap.MainActivity;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by adriangradinar on 12/03/2016.
 * This class holds a bunch of static methods to be accessed from wherever within the app
 */
public class Utils {

    public static long getTimestamp(){
        return System.currentTimeMillis() / 1000;
    }

    public static int randInt(Random random, int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    public static void checkRunningThread(String TAG) {
        Log.e(TAG, "Running in thread: " + Thread.currentThread().toString());
    }

    public static boolean isWifiON(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && Utils.isOnline();
    }

    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String convertIntToMonth(int month){
        switch (month){
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sept";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
        }
        return null;
    }

    public static String convertIntToLongMonth(int month){
        switch (month){
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
        }
        return null;
    }

    public static String convertStringToLongMonth(String month){
        switch (month) {
            case "Jan":
                return "January";
            case "Feb":
                return "February";
            case "Mar":
                return "March";
            case "Apr":
                return "April";
            case "May":
                return "May";
            case "Jun":
                return "June";
            case "Jul":
                return "July";
            case "Aug":
                return "August";
            case "Sept":
                return "September";
            case "Oct":
                return "October";
            case "Nov":
                return "November";
            case "Dec":
                return "December";
            default:
                return "conversion error!";
        }
    }

    public static void createNotification(Context context, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(context)
                .setAutoCancel(true)
                .setContentTitle("Snap")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, noti);
    }

    public static boolean checkIfGpsIsOn(Context context) {
        return ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String returnAddress(Geocoder geocoder, double latitude, double longitude) {
        String address = String.valueOf(latitude) + " / " + String.valueOf(longitude);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                address = "";
                for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                    if (i != addresses.get(0).getMaxAddressLineIndex() - 1)
                        address += addresses.get(0).getAddressLine(i) + "\n";
                    else
                        address += addresses.get(0).getAddressLine(i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }
}
