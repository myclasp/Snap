package com.adriangradinar.snap.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adriangradinar.snap.classes.Click;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHandler extends SQLiteOpenHelper {

    //the TAG
    private static final String TAG = DatabaseHandler.class.getSimpleName();

    //database version
    private static final int DATABASE_VERSION = 1;
    //database name
    private static final String DATABASE_NAME = "snapino_database";

    //declaring the variable names for the table of transaction
    private static final String TBL_CLICKS = "tbl_clicks";
    private static final String ID = "click_id";
    private static final String NUMBER = "click_total";
    private static final String LAT = "click_latitude";
    private static final String LON = "click_longitude";
    private static final String ACC = "click_accuracy";
    private static final String TIMESTAMP = "click_timestamp";

    private static DatabaseHandler instance;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHandler getHelper(Context context) {
        if (instance == null)
            instance = new DatabaseHandler(context.getApplicationContext());
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //declare and create the transaction table
        String CREATE_LOCATIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + TBL_CLICKS + "("
                + ID + " INTEGER PRIMARY KEY, " + NUMBER + " INTEGER, "
                + LAT + " TEXT, " + LON + " TEXT, " + ACC + " REAL, "
                + TIMESTAMP + " TEXT" + ")";
        db.setLocale(Locale.UK);
        db.execSQL(CREATE_LOCATIONS_TABLE);
        db.execSQL("CREATE INDEX DATE ON " + TBL_CLICKS + " ("+TIMESTAMP+")");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.setLocale(Locale.getDefault());
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void addClick(Click click) {
        //insert the data into the database
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            //allow the database to create the values to be insert
            ContentValues values = new ContentValues();
            values.put(NUMBER, click.getTotalClicks());
            values.put(LAT, click.getLatitude());
            values.put(LON, click.getLongitude());
            values.put(ACC, click.getAccuracy());
            values.put(TIMESTAMP, click.getTimestamp());

            db.insert(TBL_CLICKS, null, values);
            db.close();
//            Log.w(TAG, "CLick inserted into the database!");
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }
    }

    public int countTotalClicks() {
        int total = 0;
        try {
            String sql = "SELECT COUNT(*) FROM " + TBL_CLICKS;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            //save every event to the events list array
            try {
                if (cursor.moveToFirst()) {
                    do {
                        total = cursor.getInt(0);
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
                db.close();
            }
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }

        return total;
    }

    public String[] getMostUpDay(){
        String[] values = new String[5];
        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.results, t.timestamp FROM (SELECT "+TIMESTAMP+" AS timestamp, COUNT("+ID+") AS results FROM " + TBL_CLICKS + " WHERE " + NUMBER + " = 1 GROUP BY date("+TIMESTAMP+", 'unixepoch', 'localtime') ORDER BY results DESC LIMIT 1) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = cursor.getString(0).substring(2); //year
                        values[1] = Utils.convertIntToMonth(cursor.getInt(1)); //month
                        values[2] = cursor.getString(2); //day
                        values[3] = cursor.getString(3); //clicks
                        values[4] = cursor.getString(4); //timestamp
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    public String[] getMostDownDay(){
        String[] values = new String[5];
        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.results, t.timestamp FROM (SELECT "+TIMESTAMP+" AS timestamp, COUNT("+ID+") AS results FROM " + TBL_CLICKS + " WHERE " + NUMBER + " = 2 GROUP BY date("+TIMESTAMP+", 'unixepoch', 'localtime') ORDER BY results DESC LIMIT 1) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = cursor.getString(0).substring(2); //year
                        values[1] = Utils.convertIntToMonth(cursor.getInt(1)); //month
                        values[2] = cursor.getString(2); //day
                        values[3] = cursor.getString(3); //clicks
                        values[4] = cursor.getString(4); //timestamp
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    public String[] getMostActiveDay(){
        String[] values = new String[5];
        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.results, t.timestamp FROM (SELECT "+TIMESTAMP+" AS timestamp, COUNT("+ID+") AS results FROM " + TBL_CLICKS + " GROUP BY date("+TIMESTAMP+", 'unixepoch', 'localtime') ORDER BY results DESC LIMIT 1) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = cursor.getString(0).substring(2); //year
                        values[1] = Utils.convertIntToMonth(cursor.getInt(1)); //month
                        values[2] = cursor.getString(2); //day
                        values[3] = cursor.getString(3); //clicks
                        values[4] = cursor.getString(4); //timestamp
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    public String[] getLeastActiveDay(){
        String[] values = new String[5];
        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.results, t.timestamp FROM (SELECT "+TIMESTAMP+" AS timestamp, COUNT("+ID+") AS results FROM " + TBL_CLICKS + " GROUP BY date("+TIMESTAMP+", 'unixepoch', 'localtime') ORDER BY results ASC LIMIT 1) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = cursor.getString(0).substring(2); //year
                        values[1] = Utils.convertIntToMonth(cursor.getInt(1)); //month
                        values[2] = cursor.getString(2); //day
                        values[3] = cursor.getString(3); //clicks
                        values[4] = cursor.getString(4); //timestamp
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    public ArrayList<Click> selectLast7Days(){ //this does not include the current day's data

        ArrayList<Click> ups = new ArrayList<>();
        ArrayList<Click> downs = new ArrayList<>();

        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.action, t.clicks, t.timestamp FROM (SELECT "+TIMESTAMP+" as timestamp, "+NUMBER+" AS action, COUNT("+NUMBER+") AS clicks FROM "+TBL_CLICKS+" WHERE date("+TIMESTAMP+", 'unixepoch', 'localtime') BETWEEN date('now', '-7 days', 'localtime') AND date('now', '-1 day', 'localtime') GROUP BY "+NUMBER+", date("+TIMESTAMP+", 'unixepoch', 'localtime') ORDER BY "+TIMESTAMP+" ASC) AS t";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        if(cursor.getInt(3) == 1){
                            ups.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
                        }
                        else{
                            downs.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
                        }
                    }
                    while (cursor.moveToNext());
                }
                ups.addAll(downs);
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return ups;
    }

    /**
     * Function which searches and fetches all the results for the current months
     * @return An ArrayList of ups and downs
     */
    public ArrayList<Click> getCurrentMonth(){ //this does not include the current day's data
        ArrayList<Click> ups = new ArrayList<>();
        ArrayList<Click> downs = new ArrayList<>();

        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.action, t.clicks, t.timestamp FROM (SELECT "+TIMESTAMP+" as timestamp, "+NUMBER+" AS action, COUNT("+NUMBER+") AS clicks FROM "+TBL_CLICKS+" WHERE date("+TIMESTAMP+", 'unixepoch', 'localtime') BETWEEN date('now', 'start of month', 'localtime') AND date('now', 'start of day', 'localtime') GROUP BY "+NUMBER+", date("+TIMESTAMP+", 'unixepoch', 'localtime') ORDER BY "+TIMESTAMP+" ASC) AS t";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        if(cursor.getInt(3) == 1){
                            ups.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
                        }
                        else{
                            downs.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
                        }
//                        Log.e("db", cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getInt(3)+" "+cursor.getInt(4)+" "+cursor.getInt(5));
                    }
                    while (cursor.moveToNext());
                }
                ups.addAll(downs);
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }

        Log.e(TAG, "Total: " + ups.size());
        return ups;
    }

    public ArrayList<Click> getDayBasedOnTimestamp(long timestamp){
//        ArrayList<Click> ups = new ArrayList<>();
//        ArrayList<Click> downs = new ArrayList<>();

        ArrayList<Click> values = new ArrayList<>();

        try{
            String sql = "SELECT t.id, t.action, t.lat, t.lon, t.acc, t.timestamp, strftime('%H', datetime(t.timestamp, 'unixepoch', 'localtime')) AS hour, strftime('%M', datetime(t.timestamp, 'unixepoch', 'localtime')) AS minute, strftime('%S', datetime(t.timestamp, 'unixepoch', 'localtime')) AS second FROM (SELECT "+ID+" AS id, "+NUMBER+" AS action, "+LAT+" AS lat, "+LON+" AS lon, "+ACC+" AS acc, "+TIMESTAMP+" as timestamp FROM "+TBL_CLICKS+" WHERE date("+TIMESTAMP+", 'unixepoch', 'localtime') == date("+timestamp+", 'unixepoch', 'localtime') ORDER BY "+TIMESTAMP+") as t";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
//                        if(cursor.getInt(3) == 1){
//                            ups.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
//                        }
//                        else{
//                            downs.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
//                        }
//                        Log.e("db", cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getInt(3)+" "+cursor.getInt(4)+" "+cursor.getInt(5));
                        values.add(new Click(cursor.getInt(0), cursor.getInt(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getDouble(4), cursor.getLong(5), cursor.getString(6), cursor.getString(7), cursor.getString(8)));
                        //int id, int totalClicks, double latitude, double longitude, double accuracy, long timestamp, String hour, String minute, String second
//                        Log.e("db", cursor.getInt(0) + " " + cursor.getString(1)+":"+cursor.getString(2)+" "+cursor.getInt(3) + " " + cursor.getString(4));
                    }
                    while (cursor.moveToNext());
                }
//                ups.addAll(downs);
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    /**
     * Simple method to count the events in one day - might be useful to something else later on :)
     * @param timestamp
     * @return total!
     */
    public int countEventsInDayBasedOnTimestamp(long timestamp){
        int total = 0;
        try{
            String sql = "SELECT COUNT("+ID+") AS total, "+TIMESTAMP+" as timestamp FROM "+TBL_CLICKS+" WHERE date("+TIMESTAMP+", 'unixepoch', 'localtime') == date("+timestamp+", 'unixepoch', 'localtime')";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        total = cursor.getInt(0);
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return total;
    }









    public ArrayList[] getTotalEventsPerHour(){
        ArrayList[] list = new ArrayList[2];
        ArrayList<Click> ups = new ArrayList<>();
        ArrayList<Click> downs = new ArrayList<>();
        try{
            String sql = "SELECT strftime('%H', datetime(t.timestamp, 'unixepoch', 'localtime')) AS hour, t.click, t.clicks FROM (SELECT "+TIMESTAMP+" AS timestamp, "+NUMBER+" AS click, COUNT("+NUMBER+") AS clicks FROM "+TBL_CLICKS+" GROUP BY "+NUMBER+", strftime('%H', datetime("+TIMESTAMP+", 'unixepoch', 'localtime')) ORDER BY "+TIMESTAMP+" ASC) as t ORDER BY hour, t.click";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        //hour, click, clicks
                        if(cursor.getInt(1) == 1){
                            //up click
                            ups.add(new Click(cursor.getString(0), cursor.getInt(1), cursor.getInt(2)));
                        }
                        else{
                            //down click
                            downs.add(new Click(cursor.getString(0), cursor.getInt(1), cursor.getInt(2)));
                        }
                        Log.e("db", cursor.getString(0)+" "+cursor.getInt(1)+" "+cursor.getInt(2));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }

        list[0] = ups;
        list[1] = downs;

        return list;
    }

    /**
     * This is a helper function to list all the clicks in the database
     * Extremely useful for debugging
     */
    public void logClicks() {
        try{
            String sql = "SELECT * FROM " + TBL_CLICKS + " ORDER BY " + ID;
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery(sql, null);

            //save every event to the events list array
            try {
                if (cursor.moveToFirst()) {
                    do {
                        Log.e("db", cursor.getInt(0) + " - " + cursor.getInt(1) + " - " + cursor.getDouble(2) + " - " + cursor.getDouble(3) + " - " + cursor.getString(4) + " - " + new Date(cursor.getLong(5) * 1000));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}