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

    private int type;

    private String year;
    private String month;
    private String day;
    private int upDay;
    private int downDay;
    private int equalDay;
    private int totalUnmarked;
    private int totalMarked;

    private int totalUpUnmarked;
    private int totalDownUnmarked;

    private String hour;
    private String minute;
    private String second;

    private int marked;

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

    /**
     * This constructor is used for the Last 7 days view
     *
     * @param year
     * @param month
     * @param day
     * @param totalUpUnmarked
     * @param totalDownUnmarked
     * @param totalMarked
     * @param timestamp
     */
    public Click(String year, String month, String day, int totalUpUnmarked, int totalDownUnmarked, int totalMarked, long timestamp) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.totalUpUnmarked = totalUpUnmarked;
        this.totalDownUnmarked = totalDownUnmarked;
        this.totalMarked = totalMarked;
        this.timestamp = timestamp;
    }

    /**
     * This constructor is used for the MonthView
     *
     * @param year
     * @param month
     * @param day
     * @param upDay
     * @param downDay
     * @param equalDay
     * @param totalUnmarked
     * @param totalMarked
     * @param timestamp
     */
    public Click(String year, String month, String day, int upDay, int downDay, int equalDay, int totalUnmarked, int totalMarked, long timestamp) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.upDay = upDay;
        this.downDay = downDay;
        this.equalDay = equalDay;
        this.totalUnmarked = totalUnmarked;
        this.totalMarked = totalMarked;
        this.timestamp = timestamp;
    }

    public Click(String hour, int totalUpUnmarked, int totalDownUnmarked, int totalMarked, int totalClicks, long timestamp) {
        this.hour = hour;
        this.totalUpUnmarked = totalUpUnmarked;
        this.totalDownUnmarked = totalDownUnmarked;
        this.totalMarked = totalMarked;
        this.totalClicks = totalClicks;
        this.timestamp = timestamp;
    }

    //    public Click(String year, String month, String day, int type, int totalClicks, int timestamp) {
//        this.year = year;
//        this.month = month;
//        this.day = day;
//        this.type = type;
//        this.totalClicks = totalClicks;
//        this.timestamp = timestamp;
//    }

//    public Click(int id, String hour, String minute, int type) {
//        this.id = id;
//        this.hour = hour;
//        this.minute = minute;
//        this.type = type;
//    }

//    public Click(String hour, int type, int totalClicks) {
//        this.hour = hour;
//        this.type = type;
//        this.totalClicks = totalClicks;
//    }

    public Click(int totalClicks, long timestamp) {
        this.totalClicks = totalClicks;
        this.timestamp = timestamp;
    }

    /**
     * This is used for getting the values for one day
     */
    public Click(int id, int totalClicks, double latitude, double longitude, double accuracy, String address, long timestamp, String hour, String minute, String second, int marked) {
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
        this.marked = marked;
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

    public int getMarked() {
        return marked;
    }

    public void setMarked(int marked) {
        this.marked = marked;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUpDay() {
        return upDay;
    }

    public void setUpDay(int upDay) {
        this.upDay = upDay;
    }

    public int getDownDay() {
        return downDay;
    }

    public void setDownDay(int downDay) {
        this.downDay = downDay;
    }

    public int getEqualDay() {
        return equalDay;
    }

    public void setEqualDay(int equalDay) {
        this.equalDay = equalDay;
    }

    public int getTotalUnmarked() {
        return totalUnmarked;
    }

    public void setTotalUnmarked(int totalUnmarked) {
        this.totalUnmarked = totalUnmarked;
    }

    public int getTotalMarked() {
        return totalMarked;
    }

    public void setTotalMarked(int totalMarked) {
        this.totalMarked = totalMarked;
    }

    public int getTotalUpUnmarked() {
        return totalUpUnmarked;
    }

    public void setTotalUpUnmarked(int totalUpUnmarked) {
        this.totalUpUnmarked = totalUpUnmarked;
    }

    public int getTotalDownUnmarked() {
        return totalDownUnmarked;
    }

    public void setTotalDownUnmarked(int totalDownUnmarked) {
        this.totalDownUnmarked = totalDownUnmarked;
    }
}
