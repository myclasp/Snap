package com.adriangradinar.snap.classes;

import java.util.ArrayList;

/**
 * Created by adriangradinar on 01/04/2016.
 */
public class ClickList {

    private String title;
    private ArrayList<Click> clicks;

    public ClickList(String title, ArrayList<Click> clicks) {
        this.title = title;
        this.clicks = clicks;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Click> getClicks() {
        return clicks;
    }

    public void setClicks(ArrayList<Click> clicks) {
        this.clicks = clicks;
    }
}
