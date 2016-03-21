package com.adriangradinar.snap;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SnapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/BebasNeue Bold.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build());
    }
}
