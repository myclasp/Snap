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

    public String[] getHappiestDay(){
        String[] values = new String[4];
        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch')) AS year, strftime('%m', date(t.timestamp, 'unixepoch')) AS month, strftime('%d', date(t.timestamp, 'unixepoch')) AS day, t.results FROM (SELECT "+TIMESTAMP+" AS timestamp, COUNT("+ID+") AS results FROM " + TBL_CLICKS + " WHERE " + NUMBER + " = 1 GROUP BY date("+TIMESTAMP+", 'unixepoch') ORDER BY results DESC LIMIT 1) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = cursor.getString(0).substring(2); //year
                        values[1] = Utils.convertIntToMonth(cursor.getInt(1)); //month
                        values[2] = cursor.getString(2); //day
                        values[3] = cursor.getString(3); //clicks
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

    public String[] getSaddestDay(){
        String[] values = new String[4];
        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch')) AS year, strftime('%m', date(t.timestamp, 'unixepoch')) AS month, strftime('%d', date(t.timestamp, 'unixepoch')) AS day, t.results FROM (SELECT "+TIMESTAMP+" AS timestamp, COUNT("+ID+") AS results FROM " + TBL_CLICKS + " WHERE " + NUMBER + " = 2 GROUP BY date("+TIMESTAMP+", 'unixepoch') ORDER BY results DESC LIMIT 1) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = cursor.getString(0).substring(2); //year
                        values[1] = Utils.convertIntToMonth(cursor.getInt(1)); //month
                        values[2] = cursor.getString(2); //day
                        values[3] = cursor.getString(3); //clicks
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
        String[] values = new String[4];
        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch')) AS year, strftime('%m', date(t.timestamp, 'unixepoch')) AS month, strftime('%d', date(t.timestamp, 'unixepoch')) AS day, t.results FROM (SELECT "+TIMESTAMP+" AS timestamp, COUNT("+ID+") AS results FROM " + TBL_CLICKS + " GROUP BY date("+TIMESTAMP+", 'unixepoch') ORDER BY results DESC LIMIT 1) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = cursor.getString(0).substring(2); //year
                        values[1] = Utils.convertIntToMonth(cursor.getInt(1)); //month
                        values[2] = cursor.getString(2); //day
                        values[3] = cursor.getString(3); //clicks
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
        String[] values = new String[4];
        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch')) AS year, strftime('%m', date(t.timestamp, 'unixepoch')) AS month, strftime('%d', date(t.timestamp, 'unixepoch')) AS day, t.results FROM (SELECT "+TIMESTAMP+" AS timestamp, COUNT("+ID+") AS results FROM " + TBL_CLICKS + " GROUP BY date("+TIMESTAMP+", 'unixepoch') ORDER BY results ASC LIMIT 1) AS t";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(sql, null);

            try {
                if (cursor.moveToFirst()) {
                    do {
                        values[0] = cursor.getString(0).substring(2); //year
                        values[1] = Utils.convertIntToMonth(cursor.getInt(1)); //month
                        values[2] = cursor.getString(2); //day
                        values[3] = cursor.getString(3); //clicks
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
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch')) AS year, strftime('%m', date(t.timestamp, 'unixepoch')) AS month, strftime('%d', date(t.timestamp, 'unixepoch')) AS day, t.action, t.clicks, t.timestamp FROM (SELECT "+TIMESTAMP+" as timestamp, "+NUMBER+" AS action, COUNT("+NUMBER+") AS clicks FROM "+TBL_CLICKS+" WHERE date("+TIMESTAMP+", 'unixepoch') BETWEEN date('now', '-7 days') AND date('now', '-1 day') GROUP BY "+NUMBER+", date("+TIMESTAMP+", 'unixepoch') ORDER BY "+TIMESTAMP+" ASC) AS t";
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

    public ArrayList<Click> getCurrentMonth(){ //this does not include the current day's data

        ArrayList<Click> ups = new ArrayList<>();
        ArrayList<Click> downs = new ArrayList<>();

        try{
            String sql = "SELECT strftime('%Y', date(t.timestamp, 'unixepoch')) AS year, strftime('%m', date(t.timestamp, 'unixepoch')) AS month, strftime('%d', date(t.timestamp, 'unixepoch')) AS day, t.action, t.clicks, t.timestamp FROM (SELECT "+TIMESTAMP+" as timestamp, "+NUMBER+" AS action, COUNT("+NUMBER+") AS clicks FROM "+TBL_CLICKS+" WHERE date("+TIMESTAMP+", 'unixepoch') BETWEEN date('now', 'start of month') AND date('now', 'start of day') GROUP BY "+NUMBER+", date("+TIMESTAMP+", 'unixepoch') ORDER BY "+TIMESTAMP+" ASC) AS t";
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

    public void logClicks() {

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

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}