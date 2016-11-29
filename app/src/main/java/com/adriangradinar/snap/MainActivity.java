package com.adriangradinar.snap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.classes.ClickAddress;
import com.adriangradinar.snap.utils.DatabaseHandler;
import com.adriangradinar.snap.utils.ThreadManager;
import com.adriangradinar.snap.utils.Utils;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.adriangradinar.snap.R.id.login;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LIMIT = 20;
    private static String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snap";
    private long analytics_timestamp = 0;
    private DatabaseHandler db;
    private long currentTimestamp;
    private Button button;
    private Button uploadButton;
    private Button loginButton;
    private ProgressBar spinner;
    private transient Geocoder geocoder;
    private String user_ID = "unset";
    private String serverURL= "http://148.88.227.177:8000";


    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (db.countTotalClicks() != 0) {

                //clean up the duplicates
                db.deleteDuplicates();

                if (Utils.isWifiON(getApplicationContext()) && db.countTotalLocationsWithoutAnAddress() > 0) {

                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    Toast.makeText(getApplicationContext(), "Please wait while we convert your locations into meaningful addresses. Thank you!", Toast.LENGTH_LONG).show();
                    spinner.setVisibility(View.VISIBLE);
                    button.setVisibility(View.INVISIBLE);

                    ThreadManager.runInBackgroundThenUi(new Runnable() {
                        @Override
                        public void run() {
                            //this is in background
                            convertAddresses();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            spinner.setVisibility(View.INVISIBLE);
                            button.setVisibility(View.VISIBLE);
                            startActivity(new Intent(MainActivity.this, OverviewActivity.class));
                        }
                    });
                } else {
                    startActivity(new Intent(MainActivity.this, OverviewActivity.class));
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please record more clicks!", Toast.LENGTH_LONG).show();
            }
        }
    };

    //login button listener
    View.OnClickListener loginButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            
            // Create Object of Dialog class
            final Dialog login = new Dialog(MainActivity.this);
            // Set GUI of login screen
            login.setContentView(R.layout.login_dialog);
            login.setTitle("Login to your Snap account");

            // Init button of login GUI
            Button btnLogin = (Button) login.findViewById(R.id.btnLogin);
            Button btnCancel = (Button) login.findViewById(R.id.btnCancel);
            final EditText txtUsername = (EditText)login.findViewById(R.id.txtUsername);
            final EditText txtPassword = (EditText)login.findViewById(R.id.txtPassword);
            txtPassword.setTypeface(Typeface.SANS_SERIF);
            txtUsername.setTypeface(Typeface.SANS_SERIF);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            //String strSavedMem1 = sharedPreferences.getString("username","d");
            txtUsername.setText( sharedPreferences.getString("username","test@test.comA") );
            txtPassword.setText( sharedPreferences.getString("password","passwurd"));



            // Attached listener for login GUI button
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(txtUsername.getText().toString().trim().length() > 0 && txtPassword.getText().toString().trim().length() > 0)
                    {
                        // Validate Your login credential here than display message
                        checkCredentials(txtUsername.getText().toString().trim(),txtPassword.getText().toString().trim());
                        login.dismiss();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                "Please enter Username and Password", Toast.LENGTH_LONG).show();
                    }
                }
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login.dismiss();
                }
            });

            // Make dialog box visible.
            login.show();

        }
    };



    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    uploadData();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };






    //upload button listener

    // new method db.getClicksToUpload()
    // returns json of clicks to upload
    // upload those clicks
    // once they have been successfull
    // new method db.markUploaded([ids])
    // marks those IDS as uploaded
    // repeat until db.getClicksToUpload returns false
private void uploadData(){

        // Log.v("data to upload", db.getClicksToUpload().toString() );
        JSONObject moments = db.getClicksToUpload();

//
//                /// upload data
//
//
//                //get json to post up
//                JSONObject moments = new JSONObject();
//                JSONObject moment = new JSONObject();
//
//                //{"moments":[
//                //   {"identifier":1, "timestamp":"2016-09-27 12:09:28 +0000", "state":0, "latitude":12999999.0, "longitude":-99999.0}
//                //   ]}
//
//                try {
//                    //obj.put("moments","h");
//                    moment.put("identifier", 83449);
//                    moment.put("timestamp", "2016-09-27 12:09:28 +0000");
//                    moment.put("state", 0);
//                    moment.put("latitude", 99);
//                    moment.put("longitude", 99);
//                    JSONArray jsonArray = new JSONArray();
//                    jsonArray.put(moment);
//                    moments.put("moments", jsonArray);
//
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
        Log.v("data for upload", moments.toString() );


        String urla = serverURL+"/v1/users/"+user_ID+"/moments";

        Log.v("moments length", Integer.toString(moments.length()) );

        try {
            if(moments != null && moments.getJSONArray("moments").length() > 0 ) {


                JsonObjectRequest jsObjRequesta = new JsonObjectRequest
                        (Request.Method.POST, urla, moments, new Response.Listener<JSONObject>() {


                            @Override
                            public void onResponse(JSONObject response) {
                                //mTxtDisplay.setText("Response: " + response.toString());
                                Toast.makeText(getApplicationContext(), "Data Donation Successful!!", Toast.LENGTH_LONG).show();
                                Log.v("uploadresponse", response.toString());
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Toast.makeText(getApplicationContext(), "Upload Failed: Server Unreachable", Toast.LENGTH_LONG).show();
                                Log.v("tag", "request fail");
                                error.printStackTrace();

                            }
                        });

                // Add the request to the RequestQueue.
                SnapApplication.getInstance().getRequestQueue().add(jsObjRequesta);
                //super.queue.add(jsObjRequest);
            }
            else {
                Toast.makeText(getApplicationContext(), "No Data to Upload", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }





}

    View.OnClickListener uploadButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!user_ID.equals("unset")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Are you sure you want to share your data for research purposes?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Please login before donating data", Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get a ref to the database
        db = DatabaseHandler.getHelper(getApplicationContext());
        spinner = (ProgressBar) findViewById(R.id.progressBar);

        button = (Button) findViewById(R.id.button);
        assert button != null;
        button.setOnClickListener(buttonListener);

        //add upload button
        uploadButton = (Button) findViewById(R.id.upload);
        assert uploadButton != null;
        uploadButton.setOnClickListener(uploadButtonListener);

        //add login button
        loginButton = (Button) findViewById(login);
        assert loginButton != null;
       loginButton.setOnClickListener(loginButtonListener);

        if (Utils.getTimestamp() < 1462924800)
            button.setVisibility(View.INVISIBLE);

        TextView snapTV = (TextView) findViewById(R.id.snapTV);
        assert snapTV != null;
        snapTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                db.readCSV(getApplicationContext(), R.raw.stats_6);
//                db.deleteDuplicates();
//                db.logClicks();

//                startActivity(new Intent(MainActivity.this, HourlyActivity.class));
//                db.getCurrentMonth();
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        assert imageView != null;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                generateClicks(2500);
                spinner.setVisibility(View.VISIBLE);
                button.setVisibility(View.INVISIBLE);
                ThreadManager.runInBackgroundThenUi(new Runnable() {
                    @Override
                    public void run() {
                        db.downloadClicks(fullPath);
                        db.downloadAnalytics(fullPath);
                        db.downloadMarkedAnalytics(fullPath);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Download finished!", Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.INVISIBLE);
                        button.setVisibility(View.VISIBLE);
                    }
                });

            }
        });

       // Log.v("saved login","creat");
        loadLogin();

    }


    private void saveLogin(String username, String password, String id){
        //Log.v("saved login","hello");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("id", id);
        editor.commit();


        //Log.v("saved login", sharedPreferences.getString("username","d"));

    }

    private void loadLogin(){
        //done in login dialogue instead
//        Log.v("saved login","dgdgdfg");
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String strSavedMem1 = sharedPreferences.getString("username","d");
//        String strSavedMem2 = sharedPreferences.getString("MEM2", "");
//        Log.v("saved login",strSavedMem1);
//
//        user_ID =
//
//
//        final EditText txtUsername = (EditText)login.findViewById(R.id.txtUsername);
//        final EditText txtPassword = (EditText)login.findViewById(R.id.txtPassword);

          //  textSavedMem1.setText(strSavedMem1);
          //  textSavedMem2.setText(strSavedMem2);
    }


    private void checkCredentials(final String username, final String password){
        //auth user


        //get json to post up
        JSONObject obj = new JSONObject();

        try {
            //obj.put("moments","h");
            //obj.put("email", "test@test.com");
            //obj.put("password", "passwurd");
            obj.put("email", username);
            obj.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("uploadresponse",obj.toString());
        String url = serverURL+"/v1/auth_user";

        user_ID = "unset";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {


                    @Override
                    public void onResponse(JSONObject response) {
                        //mTxtDisplay.setText("Response: " + response.toString());
                        //
                        Log.v("uploadresponse", response.toString());

                        try {
                            if (response.getBoolean("success") == true){
                                Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_LONG).show();

                                user_ID = response.getString("id");

                                saveLogin(username,password,user_ID);



                                //save ID here

                            }else{

                                Toast.makeText(getApplicationContext(), "Login Fail", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Login Fail", Toast.LENGTH_LONG).show();
                        }



                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.v("tag", "request fail");
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Login Request Fail", Toast.LENGTH_LONG).show();

                    }
                });

        // Add the request to the RequestQueue.
        SnapApplication.getInstance().getRequestQueue().add(jsObjRequest);
        //super.queue.add(jsObjRequest);

        saveLogin(username,password,user_ID);



    }

    private void convertAddresses() {
        int total = db.countTotalLocationsWithoutAnAddress();
        if (total > LIMIT) {
            total = LIMIT;
        }

        // we might need to add a limit here in case we hit a memory issue
        ArrayList<ClickAddress> clickAddresses = new ArrayList<>();
        ArrayList<Click> clicksToBeConverted = db.getClicksWithoutAddress(total);
        if (clicksToBeConverted.size() != 0) {
            for (Click click : clicksToBeConverted) {
                clickAddresses.add(new ClickAddress(click.getId(), Utils.returnAddress(geocoder, click.getLatitude(), click.getLongitude())));
            }
            db.updateAddresses(clickAddresses);
            convertAddresses();
        } else {
            Log.d(TAG, "conversion finished!");
        }
    }

    private void generateClicks(int total) {
        currentTimestamp = Utils.getTimestamp() - (604800 * 3); //take 3 weeks off
        final Random random = new Random();

        for (int i = 0; i < total; i++) {
            //increment the timestamp
            currentTimestamp += Utils.randInt(random, 60 * 5, 60 * 30); //between 5min and 30min
            db.addClick(new Click(Utils.randInt(random, 1, 2), 54.048775, -2.806450, 10.0, "dummy address", currentTimestamp));
        }
        Log.e(TAG, "Generation finished!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.addActivityAnalytic(TAG, analytics_timestamp, (Utils.getTimestamp() - analytics_timestamp));
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics_timestamp = Utils.getTimestamp();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
