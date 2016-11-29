package com.adriangradinar.snap.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.classes.ClickAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHandler extends SQLiteOpenHelper implements Serializable {

    //the TAG
    private static final String TAG = DatabaseHandler.class.getSimpleName();

    //database version
    private static final int DATABASE_VERSION = 9;
    //database name
    private static final String DATABASE_NAME = "snapino_database";

    //declaring the variable names for the table of transaction
    private static final String TBL_CLICKS = "tbl_clicks";
    private static final String ID = "click_id";
    private static final String NUMBER = "click_total"; //1 or 2
    private static final String LAT = "click_latitude";
    private static final String LON = "click_longitude";
    private static final String ACC = "click_accuracy";
    private static final String ADDRESS = "click_address";
    private static final String TIMESTAMP = "click_timestamp";
    private static final String MARKED = "marked_for_deletion"; // 0 or 1

    private static final String TBL_ACTIVITY_ANALYTICS = "tbl_activity_analytics";
    private static final String ACTIVITY_NAME = "activity_name";
    private static final String START_TIMESTAMP = "start_timestamp"; //the start of the interaction
    private static final String DURATION = "duration"; //the duration of the interaction

    private static final String TBL_MARKED_ANALYTICS = "tbl_marked_analytics";
    private static final String ID_MARKED = "marked_id";

    private static final String CREATE_LOCATIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + TBL_CLICKS + "("
            + ID + " INTEGER PRIMARY KEY, " + NUMBER + " INTEGER, "
            + LAT + " TEXT, " + LON + " TEXT, " + ACC + " REAL, "
            + ADDRESS + " TEXT, " + TIMESTAMP + " TEXT, "
            + MARKED + " INTEGER DEFAULT 0" + ")";

    private static final String CREATE_ACTIVITY_ANALYTICS_TABLE = "CREATE TABLE IF NOT EXISTS " + TBL_ACTIVITY_ANALYTICS + "("
            + ID + " INTEGER PRIMARY KEY, " + ACTIVITY_NAME + " TEXT, "
            + START_TIMESTAMP + " TEXT, " + DURATION + " INTEGER" + ")";

    private static final String CREATE_MARKED_ANALYTICS_TABLE = "CREATE TABLE IF NOT EXISTS " + TBL_MARKED_ANALYTICS + "("
            + ID_MARKED + " INTEGER PRIMARY KEY, " + ID + " INTEGER, " + MARKED + " INTEGER, "
            + TIMESTAMP + " TEXT)";

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
        db.setLocale(Locale.UK);
        db.execSQL(CREATE_LOCATIONS_TABLE);
        db.execSQL(CREATE_ACTIVITY_ANALYTICS_TABLE);
        db.execSQL(CREATE_MARKED_ANALYTICS_TABLE);
        db.execSQL("CREATE INDEX DATE ON " + TBL_CLICKS + " ("+TIMESTAMP+")");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.setLocale(Locale.getDefault());
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //for sanity, try and create the missing tables
        db.execSQL(CREATE_MARKED_ANALYTICS_TABLE);
        db.execSQL(CREATE_ACTIVITY_ANALYTICS_TABLE);

        //just add the missing column
        if (oldVersion <= 6) {
            //add the extra column
            db.execSQL("ALTER TABLE " + TBL_CLICKS + " ADD COLUMN " + MARKED + " INTEGER DEFAULT 0");
        }
    }

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
            values.put(ADDRESS, click.getAddress());
            values.put(TIMESTAMP, click.getTimestamp());

            db.insert(TBL_CLICKS, null, values);
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
                if (cursor != null)
                    cursor.close();
            }
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }

        return total;
    }

    public int countTotalLocationsWithoutAnAddress() {
        int total = 0;
        try {
            String sql = "SELECT COUNT(*) FROM " + TBL_CLICKS + " WHERE " + ADDRESS + " = 'not yet converted'";
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
                if (cursor != null)
                    cursor.close();
            }
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }

        return total;
    }

    public String[] getMostUpDay(){
        String[] values = new String[6];
        try{
            String sql = "SELECT strftime('%Y', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS year, strftime('%m', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS month, strftime('%d', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS day, " +
                    "datetime(" + TIMESTAMP + ",'unixepoch', 'localtime') as click_date, COUNT(*) AS total_clicks, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' THEN 1 ELSE 0 END) total_unmarked, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' THEN 1 ELSE 0 END) total_marked, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' and " + NUMBER + " = '1' THEN 1 ELSE 0 END) up_unmarked_count, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' and " + NUMBER + " = '1' THEN 1 ELSE 0 END) up_marked_count, " + TIMESTAMP +
                    " FROM " + TBL_CLICKS + " GROUP BY DATE(click_date) ORDER BY up_unmarked_count DESC, click_date DESC LIMIT 1";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = (cursor.getString(0)).substring(2, 4); //year
                        values[1] = cursor.getString(1); //month
                        values[2] = cursor.getString(2); //day

                        //the clicks count
                        values[3] = cursor.getString(7); //total_unmarked
                        values[4] = cursor.getString(9); //timestamp
                        values[5] = cursor.getString(8); //up_marked_clicks

                        //Log.e(TAG, cursor.getString(0) + " - " + cursor.getString(1) + " - " + cursor.getString(2) + " - " + cursor.getString(3) + " - " + cursor.getInt(5));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    public String[] getMostDownDay(){
        String[] values = new String[6];
        try{
            String sql = "SELECT strftime('%Y', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS year, strftime('%m', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS month, strftime('%d', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS day, " +
                    "datetime(" + TIMESTAMP + ",'unixepoch', 'localtime') as click_date, COUNT(*) AS total_clicks, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' THEN 1 ELSE 0 END) total_unmarked, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' THEN 1 ELSE 0 END) total_marked, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' and " + NUMBER + " = '2' THEN 1 ELSE 0 END) down_unmarked_count, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' and " + NUMBER + " = '2' THEN 1 ELSE 0 END) down_marked_count, " + TIMESTAMP +
                    " FROM " + TBL_CLICKS + " GROUP BY DATE(click_date) ORDER BY down_unmarked_count DESC, click_date DESC LIMIT 1";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = (cursor.getString(0)).substring(2, 4); //year
                        values[1] = cursor.getString(1); //month
                        values[2] = cursor.getString(2); //day

                        //the clicks count
                        values[3] = cursor.getString(7); //total_unmarked
                        values[4] = cursor.getString(9); //timestamp
                        values[5] = cursor.getString(8); //down_marked_clicks
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    public String[] getMostActiveDay(){
        String[] values = new String[6];
        try{
            String sql = "SELECT strftime('%Y', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS year, strftime('%m', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS month, strftime('%d', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS day, " +
                    "datetime(" + TIMESTAMP + ",'unixepoch', 'localtime') as click_date, COUNT(*) AS total_clicks, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' THEN 1 ELSE 0 END) total_unmarked, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' THEN 1 ELSE 0 END) total_marked, " + TIMESTAMP +
                    " FROM " + TBL_CLICKS + " GROUP BY DATE(click_date) ORDER BY total_unmarked DESC, click_date DESC LIMIT 1";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = (cursor.getString(0)).substring(2, 4); //year
                        values[1] = cursor.getString(1); //month
                        values[2] = cursor.getString(2); //day

                        //the clicks count
                        values[3] = cursor.getString(5); //total_unmarked
                        values[4] = cursor.getString(7); //timestamp
                        values[5] = cursor.getString(6); //total_marked
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    public String[] getLeastActiveDay(){
        String[] values = new String[6];
        try{
            String sql = "SELECT strftime('%Y', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS year, strftime('%m', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS month, strftime('%d', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS day, " +
                    "datetime(" + TIMESTAMP + ",'unixepoch', 'localtime') as click_date, COUNT(*) AS total_clicks, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' THEN 1 ELSE 0 END) total_unmarked, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' THEN 1 ELSE 0 END) total_marked, " + TIMESTAMP +
                    " FROM " + TBL_CLICKS + " GROUP BY DATE(click_date) ORDER BY total_unmarked ASC, click_date DESC LIMIT 1";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = (cursor.getString(0)).substring(2, 4); //year
                        values[1] = cursor.getString(1); //month
                        values[2] = cursor.getString(2); //day

                        //the clicks count
                        values[3] = cursor.getString(5); //total_unmarked
                        values[4] = cursor.getString(7); //timestamp
                        values[5] = cursor.getString(6); //total_marked
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return values;
    }

    public ArrayList<Click> selectLast7Days(){ //this does not include the current day's data
        ArrayList<Click> allClicks = new ArrayList<>();
        try{
            String sql = "SELECT strftime('%Y', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS year, strftime('%m', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS month, strftime('%d', date(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS day, " +
                    "datetime(" + TIMESTAMP + ",'unixepoch', 'localtime') as click_date, COUNT(*) AS total_clicks, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' AND " + NUMBER + " = '1' THEN 1 ELSE 0 END) total_up_unmarked, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' AND " + NUMBER + " = '2' THEN 1 ELSE 0 END) total_down_unmarked, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' THEN 1 ELSE 0 END) total_marked, " + TIMESTAMP + " " +
                    "FROM tbl_clicks WHERE " +
                    "date(" + TIMESTAMP + ", 'unixepoch', 'localtime') BETWEEN date('now', '-7 days', 'localtime') AND date('now', '-1 day', 'localtime') " +
                    "GROUP BY DATE(click_date) ORDER BY click_date ASC";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        allClicks.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(5), cursor.getInt(6), cursor.getInt(7), cursor.getLong(8)));
//                        Log.e(TAG, cursor.getString(0)+" - "+cursor.getString(1)+" - "+cursor.getString(2)+" - "+cursor.getInt(5)+" - "+cursor.getInt(6)+" - "+cursor.getInt(7)+" - "+cursor.getLong(8));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return allClicks;
    }

    public ArrayList<Click> getCurrentMonth() { //this does not include the current day's data
        ArrayList<Click> clicks = new ArrayList<>();
        try {
            String sql = "SELECT t.year, t.month, t.day," +
                    "CASE WHEN t.total_up_unmarked > t.total_down_unmarked THEN 1 ELSE 0 END up_day, " +
                    "CASE WHEN t.total_up_unmarked < t.total_down_unmarked THEN 1 ELSE 0 END down_day, " +
                    "CASE WHEN t.total_up_unmarked = t.total_down_unmarked THEN 1 ELSE 0 END equal_day, t.total_unmarked, t.total_marked, t.click_timestamp " +
                    "FROM (SELECT strftime('%Y', date(click_timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(click_timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(click_timestamp, 'unixepoch', 'localtime')) AS day, " +
                    "datetime(" + TIMESTAMP + ",'unixepoch', 'localtime') as click_date, COUNT(*) AS total_clicks, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' THEN 1 ELSE 0 END) total_unmarked, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' THEN 1 ELSE 0 END) total_marked, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' AND " + NUMBER + " = '1' THEN 1 ELSE 0 END) total_up_unmarked, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' AND " + NUMBER + " = '2' THEN 1 ELSE 0 END) total_down_unmarked, " + TIMESTAMP +
                    " FROM tbl_clicks GROUP BY DATE(click_date) ORDER BY click_date ASC) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
//                        Log.e(TAG, cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getInt(3)+" "+cursor.getInt(4)+" "+cursor.getInt(5)+" "+cursor.getInt(6)+" "+cursor.getInt(7) +" " + cursor.getLong(8));
                        clicks.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6), cursor.getInt(7), cursor.getLong(8)));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }
        return clicks;
    }

    public ArrayList<Click> getDayBasedOnTimestamp(long timestamp){

        ArrayList<Click> values = new ArrayList<>();

        try{
            String sql = "SELECT t.id, t.action, t.lat, t.lon, t.acc, t.address, t.timestamp, strftime('%H', datetime(t.timestamp, 'unixepoch', 'localtime')) AS hour, strftime('%M', datetime(t.timestamp, 'unixepoch', 'localtime')) AS minute, strftime('%S', datetime(t.timestamp, 'unixepoch', 'localtime')) AS second, t.marked FROM (SELECT " + TIMESTAMP + " as timestamp, " + ID + " AS id, " + NUMBER + " AS action, " + LAT + " AS lat, " + LON + " AS lon, " + ACC + " AS acc, " + ADDRESS + " AS address, " + MARKED + " AS marked FROM " + TBL_CLICKS + " WHERE date(" + TIMESTAMP + ", 'unixepoch', 'localtime') == date(" + timestamp + ", 'unixepoch', 'localtime') ORDER BY " + TIMESTAMP + ") as t";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
//                        Log.e("db", cursor.getInt(0) + " " + cursor.getInt(1) + " " + cursor.getDouble(2) + " " + cursor.getDouble(3) + " " + cursor.getDouble(4) + " " + cursor.getString(5) + " " + cursor.getLong(6) + " " + cursor.getString(7) + " " + cursor.getString(8) + " " + cursor.getString(9) + " " + cursor.getInt(10));
                        values.add(new Click(cursor.getInt(0), cursor.getInt(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5), cursor.getLong(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getInt(10)));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
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
     */
    public int countEventsInDayBasedOnTimestamp(long timestamp){
        int total = 0;
        try{
            String sql = "SELECT COUNT(" + TIMESTAMP + ") AS total, " + TIMESTAMP + " as timestamp FROM " + TBL_CLICKS + " WHERE date(" + TIMESTAMP + ", 'unixepoch', 'localtime') == date(" + timestamp + ", 'unixepoch', 'localtime')";
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
                if (cursor != null)
                    cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return total;
    }


    public ArrayList<Click> getTotalEventsPerHour() {
        ArrayList<Click> clicks = new ArrayList<>();
        try{
            String sql = "SELECT strftime('%H', datetime(" + TIMESTAMP + ", 'unixepoch', 'localtime')) AS hour, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' AND " + NUMBER + " = '1' THEN 1 ELSE 0 END) up_count, " +
                    "SUM(CASE WHEN " + MARKED + " = '0' AND " + NUMBER + " = '2' THEN 1 ELSE 0 END) down_count, " +
                    "SUM(CASE WHEN " + MARKED + " = '1' THEN 1 ELSE 0 END) total_marked, " +
                    "COUNT(" + NUMBER + ") AS total_clicks, " + TIMESTAMP + " AS timestamp " +
                    "FROM " + TBL_CLICKS + " GROUP BY hour ORDER BY hour";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        clicks.add(new Click(cursor.getString(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getLong(5)));
//                        Log.e(TAG, cursor.getString(0)+" - "+cursor.getInt(1)+" - "+cursor.getInt(2)+" - "+cursor.getInt(3)+" - "+cursor.getInt(4)+" - "+cursor.getLong(5));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
        return clicks;
    }

    public ArrayList<Click> getClicksWithoutAddress(int limit) {
        ArrayList<Click> clicks = new ArrayList<>();
        try {
            String sql = "SELECT " + ID + " AS id, " + LAT + " AS lat, " + LON + " AS lon, " + ADDRESS + " AS address FROM " + TBL_CLICKS + " WHERE " + ADDRESS + " = 'not yet converted' LIMIT " + limit;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            //save every event to the events list array
            try {
                if (cursor.moveToFirst()) {
                    do {
                        clicks.add(new Click(cursor.getInt(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getString(3)));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }
        return clicks;
    }

    public void updateAddresses(ArrayList<ClickAddress> addresses) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int total = addresses.size();
            for (int i = 0; i < total; i++) {
                ContentValues values = new ContentValues();
                values.put(ADDRESS, addresses.get(i).getAddress());
                db.update(TBL_CLICKS, values, ID + "='" + addresses.get(i).getId() + "'", null);
            }
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }
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
                        Log.e("db", cursor.getInt(0) + " - " + cursor.getInt(1) + " - " + cursor.getDouble(2) + " - " + cursor.getDouble(3) + " - " + cursor.getString(4) + " - " + new Date(cursor.getLong(6) * 1000) + " - " + cursor.getInt(7));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        catch (SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
    }

    public void downloadClicks(String fullPath) {
        try {
            File dir = new File(fullPath);

            boolean isDirectoryCreated = dir.exists();
            if (!isDirectoryCreated) {
                isDirectoryCreated = dir.mkdir();
            }

            if (isDirectoryCreated) {
                File myFile = new File(fullPath, "clicks_" + Utils.getTimestamp() + ".txt");

                boolean isFileCreated = myFile.exists();
                if (!isFileCreated) {
                    isFileCreated = myFile.createNewFile();
                }

                if (isFileCreated) {
                    PrintWriter printWriter = new PrintWriter(new FileWriter(myFile));
                    printWriter.println(TBL_CLICKS + "@" + Utils.getTimestamp());
                    printWriter.println(ID + "," + NUMBER + "," + LAT + "," + LON + "," + ACC + "," + ADDRESS + "," + TIMESTAMP + "," + MARKED);

                    try {
                        String sql = "SELECT * FROM " + TBL_CLICKS + " ORDER BY " + ID;
                        SQLiteDatabase db = this.getWritableDatabase();
                        Cursor cursor = db.rawQuery(sql, null);

                        try {
                            if (cursor.moveToFirst()) {
                                int count = cursor.getColumnCount();
                                do {
                                    if (count == 7)
                                        printWriter.println(cursor.getInt(0) + "," + cursor.getInt(1) + "," + cursor.getDouble(2) + "," + cursor.getDouble(3) + "," + cursor.getDouble(4) + "," + cursor.getString(5).replaceAll("\\n", " ") + "," + cursor.getLong(6));
                                    else
                                        printWriter.println(cursor.getInt(0) + "," + cursor.getInt(1) + "," + cursor.getDouble(2) + "," + cursor.getDouble(3) + "," + cursor.getDouble(4) + "," + cursor.getString(5).replaceAll("\\n", " ") + "," + cursor.getLong(6) + "," + cursor.getInt(7));
                                }
                                while (cursor.moveToNext());
                            }
                        } finally {
                            if (cursor != null)
                                cursor.close();
                        }
                    } catch (SQLiteDatabaseLockedException e) {
                        e.printStackTrace();
                    } finally {
                        printWriter.close();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find the file");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "An I/O Error occurred");
            e.printStackTrace();
        }

        Log.d(TAG, "Clicks download completed!");
    }

    public void downloadAnalytics(String fullPath) {
        try {
            File dir = new File(fullPath);

            boolean isDirectoryCreated = dir.exists();
            if (!isDirectoryCreated) {
                isDirectoryCreated = dir.mkdir();
            }

            if (isDirectoryCreated) {
                File myFile = new File(fullPath, "analytics_" + Utils.getTimestamp() + ".txt");

                boolean isFileCreated = myFile.exists();
                if (!isFileCreated) {
                    isFileCreated = myFile.createNewFile();
                }

                if (isFileCreated) {
                    PrintWriter printWriter = new PrintWriter(new FileWriter(myFile));
                    printWriter.println(TBL_ACTIVITY_ANALYTICS + "@" + Utils.getTimestamp());
                    printWriter.println(ID + "," + ACTIVITY_NAME + "," + START_TIMESTAMP + "," + DURATION);

                    try {
                        String sql = "SELECT * FROM " + TBL_ACTIVITY_ANALYTICS + " ORDER BY " + ID;
                        SQLiteDatabase db = this.getWritableDatabase();
                        Cursor cursor = db.rawQuery(sql, null);

                        try {
                            if (cursor.moveToFirst()) {
                                do {
                                    printWriter.println(cursor.getInt(0) + "," + cursor.getString(1) + "," + cursor.getLong(2) + "," + cursor.getLong(3));
                                }
                                while (cursor.moveToNext());
                            }
                        } finally {
                            if (cursor != null)
                                cursor.close();
                        }
                    } catch (SQLiteDatabaseLockedException e) {
                        e.printStackTrace();
                    } finally {
                        printWriter.close();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find the file");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "An I/O Error occurred");
            e.printStackTrace();
        }

        Log.d(TAG, "Activity analytics download completed!");
    }

    public void downloadMarkedAnalytics(String fullPath) {
        try {
            File dir = new File(fullPath);

            boolean isDirectoryCreated = dir.exists();
            if (!isDirectoryCreated) {
                isDirectoryCreated = dir.mkdir();
            }

            if (isDirectoryCreated) {
                File myFile = new File(fullPath, "marked_analytics_" + Utils.getTimestamp() + ".txt");

                boolean isFileCreated = myFile.exists();
                if (!isFileCreated) {
                    isFileCreated = myFile.createNewFile();
                }

                if (isFileCreated) {
                    PrintWriter printWriter = new PrintWriter(new FileWriter(myFile));
                    printWriter.println(TBL_MARKED_ANALYTICS + "@" + Utils.getTimestamp());
                    printWriter.println(ID_MARKED + "," + ID + "," + MARKED + "," + TIMESTAMP);

                    try {
                        String sql = "SELECT * FROM " + TBL_MARKED_ANALYTICS + " ORDER BY " + ID_MARKED;
                        SQLiteDatabase db = this.getWritableDatabase();
                        Cursor cursor = db.rawQuery(sql, null);

                        try {
                            if (cursor.moveToFirst()) {
                                do {
                                    printWriter.println(cursor.getInt(0) + "," + cursor.getInt(1) + "," + cursor.getInt(2) + "," + cursor.getLong(3));
                                }
                                while (cursor.moveToNext());
                            }
                        } finally {
                            if (cursor != null)
                                cursor.close();
                        }
                    } catch (SQLiteDatabaseLockedException e) {
                        e.printStackTrace();
                    } finally {
                        printWriter.close();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find the file");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "An I/O Error occurred");
            e.printStackTrace();
        }

        Log.d(TAG, "Marked analytics download completed!");
    }




    //WAS
    //makes up the moments json object for upload
    public JSONObject getClicksToUpload() {
        //printWriter.println(ID + "," + NUMBER + "," + LAT + "," + LON + "," + ACC + "," + ADDRESS + "," + TIMESTAMP + "," + MARKED);


            String sql = "SELECT * FROM " + TBL_CLICKS + " WHERE marked_for_deletion < 1  ORDER BY " + ID;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);
            JSONArray resultSet = new JSONArray();


            cursor.moveToFirst();
            //Log.v("count",  Integer.toString(cursor.getCount()));

            if (cursor.getCount() > 0) {

                while (cursor.isAfterLast() == false) {

                    int totalColumn = cursor.getColumnCount();
                    JSONObject rowObject = new JSONObject();

                    for (int i = 0; i < totalColumn; i++) {
                        if (cursor.getColumnName(i) != null) {
                            try {
                                if (cursor.getString(i) != null) {
                                    //Log.d("TAG_NAME", cursor.getString(i));

                                    String colName = cursor.getColumnName(i);
                                    String data = cursor.getString(i);

                                    if (colName.equals("click_id")) {
                                        colName = "identifier";
                                    } else if (colName.equals("click_total")) {
                                        colName = "state";
                                        //up click =1
                                        if (data.equals("1")){
                                            data = "1";
                                        }
                                        // down click =2
                                        else if (data.equals("2")){
                                            data = "0";
                                        }
                                    } else if (colName.equals("click_latitude")) {
                                        colName = "latitude";
                                    } else if (colName.equals("click_longitude")) {
                                        colName = "longitude";
                                    } else if (colName.equals("click_accuracy")) {
                                        colName = "accuracy";
                                    } else if (colName.equals("click_timestamp")) {
                                        colName = "timestamp";
                                        Date date = new Date ();
                                        date.setTime(Long.parseLong(cursor.getString(i))*1000);
                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");
                                        data = dateFormat.format(date);
                                        //data = date.toString();
                                    }



//
//                                    private static final String TBL_CLICKS = "tbl_clicks";
//                                    private static final String ID = "click_id";
//                                    private static final String NUMBER = "click_total"; //1 or 2
//                                    private static final String LAT = "click_latitude";
//                                    private static final String LON = "click_longitude";
//                                    private static final String ACC = "click_accuracy";
//                                    private static final String ADDRESS = "click_address";
//                                    private static final String TIMESTAMP = "click_timestamp";
//                                    private static final String MARKED = "marked_for_deletion"; // 0 or 1




                                    if (!colName.equals("click_address") && !colName.equals("marked_for_deletion")) {
                                        rowObject.put(colName, data);
                                    }

                                } else {
                                    rowObject.put(cursor.getColumnName(i), "");
                                }
                            } catch (Exception e) {
                                Log.d("TAG_NAME", e.getMessage());
                            }
                        }
                    }
                    resultSet.put(rowObject);
                    cursor.moveToNext();
                }



            }
        else{

            }
        cursor.close();

        JSONObject moments = new JSONObject();
        try {
            moments.put("moments", resultSet);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("data", moments.toString());
        return moments;

    }













    public void readCSV(Context context, int file) {
        String address;
        deleteAllRecords();
        InputStream inputStream = context.getResources().openRawResource(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            int i = 0;
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                if (++i > 2) {
                    String[] row = csvLine.split(",");

                    //insert the data into the database
                    try {
                        SQLiteDatabase db = this.getWritableDatabase();

                        //allow the database to create the values to be insert
                        ContentValues values = new ContentValues();
                        values.put(ID, Integer.parseInt(row[0]));
                        values.put(NUMBER, Integer.parseInt(row[1]));
                        values.put(LAT, Double.parseDouble(row[2]));
                        values.put(LON, Double.parseDouble(row[3]));
                        values.put(ACC, Double.parseDouble(row[4]));

                        address = "";

                        //the damn split on the address comma :)
                        if (row.length == 8) {
                            values.put(ADDRESS, row[5]);
                            values.put(TIMESTAMP, Long.parseLong(row[6]));
                            values.put(MARKED, Integer.parseInt(row[7]));
                        } else {
                            int j = 5;
                            for (; j < row.length - 3; j++) {
                                address += row[j] + ", ";
                            }
                            address += row[j];
                            values.put(ADDRESS, address);
                            values.put(TIMESTAMP, Long.parseLong(row[j + 1]));
                            values.put(MARKED, Integer.parseInt(row[j + 2]));
                        }

                        //insert the values
                        db.insert(TBL_CLICKS, null, values);
                    } catch (SQLiteDatabaseLockedException e) {
                        e.printStackTrace();
                    }

//                    if(row.length == 8)
//                        Log.e(TAG, Integer.parseInt(row[0]) +" - " + Integer.parseInt(row[1]) +" - " + Double.parseDouble(row[2]) +" - " + Double.parseDouble(row[3]) +" - " + Double.parseDouble(row[4]) +" - " + row[5] + row[6] + row[7]);// +" - " + Long.parseLong(row[6]));
//                    else
//                        Log.e(TAG, Integer.parseInt(row[0]) +" - " + Integer.parseInt(row[1]) +" - " + Double.parseDouble(row[2]) +" - " + Double.parseDouble(row[3]) +" - " + Double.parseDouble(row[4]) +" - " + row[5] + row[6]);// +" - " + Long.parseLong(row[6]));
                }
            }
            Log.d(TAG, "finished!");
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addActivityAnalytic(String activityName, long startTime, long duration) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            //allow the database to create the values to be insert
            ContentValues values = new ContentValues();
            values.put(ACTIVITY_NAME, activityName);
            values.put(START_TIMESTAMP, startTime);
            values.put(DURATION, duration);

            db.insert(TBL_ACTIVITY_ANALYTICS, null, values);
//            Log.w(TAG, "Analytic inserted into the database!");
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }
    }

    public void addMarkedAnalytic(int id, int marked) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            //allow the database to create the values to be insert
            ContentValues values = new ContentValues();
            values.put(ID, id);
            values.put(MARKED, marked);
            values.put(TIMESTAMP, Utils.getTimestamp());

            db.insert(TBL_MARKED_ANALYTICS, null, values);
            Log.w(TAG, "Marked into the database!");
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }
    }

    public void logAnalytics() {
        try {
            String sql = "SELECT * FROM " + TBL_ACTIVITY_ANALYTICS + " ORDER BY " + ID;
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery(sql, null);

            //save every event to the events list array
            try {
                if (cursor.moveToFirst()) {
                    do {
                        Log.e("analytics", cursor.getInt(0) + " - " + cursor.getString(1) + " - " + new Date(cursor.getLong(2) * 1000) + " - " + cursor.getLong(3));
                    }
                    while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }
    }

    public void setMarked(int id, int value) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MARKED, value);
            db.update(TBL_CLICKS, values, ID + "='" + id + "'", null);
        } catch (SQLiteDatabaseLockedException e) {
            e.printStackTrace();
        }
    }

    public void deleteDuplicates() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_CLICKS, ID + " NOT IN (SELECT MAX(" + ID + ") FROM " + TBL_CLICKS + " GROUP BY " + TIMESTAMP + ", " + NUMBER + ")", null);
        db.close();
    }

    public void deleteAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_CLICKS, null, null);
        db.close();
    }

    @Override
    public void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}