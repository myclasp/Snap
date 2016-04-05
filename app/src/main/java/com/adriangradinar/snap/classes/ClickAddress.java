package com.adriangradinar.snap.classes;

/**
 * Created by adriangradinar on 04/04/2016.
 * Simple class to hold and later display the address of where the click was recorded
 */
public class ClickAddress {

    private int id;
    private String address;

    public ClickAddress(int id, String address) {
        this.id = id;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
