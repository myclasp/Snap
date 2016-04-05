package com.adriangradinar.snap.classes;

/**
 * Created by adriangradinar on 13/03/2016.
 * The Click class - holds all details for a simple click
 */
public class Click {

    private int id;
    private int totalClicks;
    private double latitude;
    private double longitude;
    private double accuracy;
    private String address;
    private long timestamp;

    private String year;
    private String month;
    private String day;
    private int type;

    private String hour;
    private String minute;
    private String second;

    public Click(int totalClicks, double latitude, double longitude, double accuracy, String address, long timestamp) {
        this.totalClicks = totalClicks;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.address = address;
        this.timestamp = timestamp;
    }

    public Click(int id, int totalClicks, double latitude, double longitude, double accuracy, long timestamp) {
        this.id = id;
        this.totalClicks = totalClicks;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
    }

    public Click(String year, String month, String day, int type, int totalClicks, int timestamp) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.type = type;
        this.totalClicks = totalClicks;
        this.timestamp = timestamp;
    }

//    public Click(int id, String hour, String minute, int type) {
//        this.id = id;
//        this.hour = hour;
//        this.minute = minute;
//        this.type = type;
//    }

    public Click(String hour, int type, int totalClicks) {
        this.hour = hour;
        this.type = type;
        this.totalClicks = totalClicks;
    }

    public Click(int totalClicks, long timestamp) {
        this.totalClicks = totalClicks;
        this.timestamp = timestamp;
    }

    /**
     * This is used for getting the values for one day
     */
    public Click(int id, int totalClicks, double latitude, double longitude, double accuracy, String address, long timestamp, String hour, String minute, String second) {
        this.id = id;
        this.totalClicks = totalClicks;
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
        this.address = address;
        this.timestamp = timestamp;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    // this click is for updating the address of the click
    public Click(int id, double latitude, double longitude, String address) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(int totalClicks) {
        this.totalClicks = totalClicks;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }
}
