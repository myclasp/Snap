package com.adriangradinar.snap;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;




public class SnapApplication extends Application {
    // Instantiate the RequestQueue.
//    RequestQueue queue = Volley.newRequestQueue(this);
    private static SnapApplication sInstance;
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        //some nicer fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/BebasNeue Bold.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build());

        // initialize the singleton
        sInstance = this;
    }



    public static synchronized SnapApplication getInstance() {
        return sInstance;
    }
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }


}
