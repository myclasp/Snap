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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private static final String ADDRESS = "click_address";
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
                + ADDRESS + " TEXT, " + TIMESTAMP + " TEXT" + ")";
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
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.action, t.clicks, t.timestamp FROM (SELECT " + TIMESTAMP + " as timestamp, " + NUMBER + " AS action, COUNT(" + NUMBER + ") AS clicks FROM " + TBL_CLICKS + " WHERE date(" + TIMESTAMP + ", 'unixepoch', 'localtime') BETWEEN date('now', '-7 days', 'localtime') AND date('now', '-1 day', 'localtime') GROUP BY " + NUMBER + ", date(" + TIMESTAMP + ", 'unixepoch', 'localtime') ORDER BY " + TIMESTAMP + " ASC) AS t ORDER BY year, month, day, action";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        allClicks.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
//                        Log.e(TAG, cursor.getString(0) + " - " + cursor.getString(1) + " - " + cursor.getString(2) + " - " + cursor.getInt(3) + " - " + cursor.getInt(4) + " - " + cursor.getInt(5));
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

    /**
     * Function which searches and fetches all the results for the current months
     * @return An ArrayList of ups and downs
     */
    public ArrayList<Click> getCurrentMonth_old() { //this does not include the current day's data
        ArrayList<Click> clicks = new ArrayList<>();

        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.action, t.clicks, t.timestamp FROM (SELECT "+TIMESTAMP+" as timestamp, "+NUMBER+" AS action, COUNT("+NUMBER+") AS clicks FROM "+TBL_CLICKS+" WHERE date("+TIMESTAMP+", 'unixepoch', 'localtime') BETWEEN date('now', 'start of month', 'localtime') AND date('now', 'start of day', 'localtime') GROUP BY "+NUMBER+", date("+TIMESTAMP+", 'unixepoch', 'localtime') ORDER BY "+TIMESTAMP+" ASC) AS t";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
//                        clicks.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
                        Log.e("db", cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getInt(3) + " " + cursor.getInt(4) + " " + cursor.getInt(5));
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

    public ArrayList<Click> getCurrentMonth() { //this does not include the current day's data
        ArrayList<Click> clicks = new ArrayList<>();
        try {
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch', 'localtime')) AS year, strftime('%m', date(t.timestamp, 'unixepoch', 'localtime')) AS month, strftime('%d', date(t.timestamp, 'unixepoch', 'localtime')) AS day, t.action AS actions, t.clicks AS clicks, t.timestamp AS timestamp FROM (SELECT " + TIMESTAMP + " as timestamp, " + NUMBER + " AS action, COUNT(" + NUMBER + ") AS clicks FROM " + TBL_CLICKS + " WHERE date(" + TIMESTAMP + ", 'unixepoch', 'localtime') BETWEEN date('now', 'start of month', 'localtime') AND date('now', 'start of day', '+1 day', 'localtime') GROUP BY " + NUMBER + ", date(" + TIMESTAMP + ", 'unixepoch', 'localtime') ORDER BY " + TIMESTAMP + " ASC) AS t ORDER BY day, actions";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        clicks.add(new Click(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
//                        Log.e("db", cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getInt(3)+" "+cursor.getInt(4)+" "+cursor.getInt(5));
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
//        ArrayList<Click> ups = new ArrayList<>();
//        ArrayList<Click> downs = new ArrayList<>();

        ArrayList<Click> values = new ArrayList<>();

        try{
            String sql = "SELECT t.id, t.action, t.lat, t.lon, t.acc, t.address, t.timestamp, strftime('%H', datetime(t.timestamp, 'unixepoch', 'localtime')) AS hour, strftime('%M', datetime(t.timestamp, 'unixepoch', 'localtime')) AS minute, strftime('%S', datetime(t.timestamp, 'unixepoch', 'localtime')) AS second FROM (SELECT " + ID + " AS id, " + NUMBER + " AS action, " + LAT + " AS lat, " + LON + " AS lon, " + ACC + " AS acc, " + ADDRESS + " AS address, " + TIMESTAMP + " as timestamp FROM " + TBL_CLICKS + " WHERE date(" + TIMESTAMP + ", 'unixepoch', 'localtime') == date(" + timestamp + ", 'unixepoch', 'localtime') ORDER BY " + TIMESTAMP + ") as t";
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
                        values.add(new Click(cursor.getInt(0), cursor.getInt(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5), cursor.getLong(6), cursor.getString(7), cursor.getString(8), cursor.getString(9)));
                        //int id, int totalClicks, double latitude, double longitude, double accuracy, long timestamp, String hour, String minute, String second
//                        Log.e("db", cursor.getInt(0) + " " + cursor.getString(1)+":"+cursor.getString(2)+" "+cursor.getInt(3) + " " + cursor.getString(4));
                    }
                    while (cursor.moveToNext());
                }
//                ups.addAll(downs);
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
                if (cursor != null)
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
                if (cursor != null)
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
                        Log.e("db", cursor.getInt(0) + " - " + cursor.getInt(1) + " - " + cursor.getDouble(2) + " - " + cursor.getDouble(3) + " - " + cursor.getString(4) + " - " + new Date(cursor.getLong(5) * 1000));
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

    public void downloadCSV(String fullPath) {
        try {
            File dir = new File(fullPath);

            boolean isDirectoryCreated = dir.exists();
            if (!isDirectoryCreated) {
                isDirectoryCreated = dir.mkdir();
            }

            if (isDirectoryCreated) {
                File myFile = new File(fullPath, "database_" + Utils.getTimestamp() + ".txt");

                boolean isFileCreated = myFile.exists();
                if (!isFileCreated) {
                    isFileCreated = myFile.createNewFile();
                }

                if (isFileCreated) {
                    PrintWriter printWriter = new PrintWriter(new FileWriter(myFile));
                    printWriter.println(TBL_CLICKS + "@" + Utils.getTimestamp());
                    printWriter.println(ID + "," + NUMBER + "," + LAT + "," + LON + "," + ACC + "," + ADDRESS + "," + TIMESTAMP);

                    try {
                        String sql = "SELECT * FROM " + TBL_CLICKS + " ORDER BY " + ID;
                        SQLiteDatabase db = this.getWritableDatabase();
                        Cursor cursor = db.rawQuery(sql, null);

                        try {
                            if (cursor.moveToFirst()) {
                                do {
                                    printWriter.println(cursor.getInt(0) + "," + cursor.getInt(1) + "," + cursor.getDouble(2) + "," + cursor.getDouble(3) + "," + cursor.getDouble(4) + "," + cursor.getString(5).replaceAll("\\n", " ") + "," + cursor.getLong(6));
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

        Log.d(TAG, "Database download completed!");
    }

    public void readCSV(Context context, int file) {
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

                        //the damn split on the address comma :)
                        if (row.length == 8) {
                            values.put(ADDRESS, row[5] + ", " + row[6]);
                            values.put(TIMESTAMP, Long.parseLong(row[7]));
                        } else {
                            values.put(ADDRESS, row[5]);
                            values.put(TIMESTAMP, Long.parseLong(row[6]));
                        }

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
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
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