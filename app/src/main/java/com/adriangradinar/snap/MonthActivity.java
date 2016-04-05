package com.adriangradinar.snap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.utils.DatabaseHandler;
import com.adriangradinar.snap.utils.ThreadManager;
import com.adriangradinar.snap.utils.Utils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MonthActivity extends AppCompatActivity {

    private final static String TAG = MonthActivity.class.getSimpleName();
    private MaterialCalendarView materialCalendarView;
    private DatabaseHandler db;
    private ArrayList<Click> monthClicks;

    private List<CalendarDay> upDecorators = new ArrayList<>();
    private List<CalendarDay> downDecorators = new ArrayList<>();
    private List<CalendarDay> equalDecorators = new ArrayList<>();
//    private Click up, down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setView();
            }
        }).start();
    }

    public void setView(){
        ThreadManager.runInBackgroundThenUi(new Runnable() {
            @Override
            public void run() {
                db = DatabaseHandler.getHelper(getApplicationContext());
                monthClicks = db.getCurrentMonth();

                for (int i = 0; i < monthClicks.size() - 1; i++) {
                    Click now = monthClicks.get(i);
                    Click next = monthClicks.get(i + 1);

//                    Log.e(TAG, now.getType() + " - " + next.getType());

                    if (now.getType() == 1) {
                        //we have ups
                        if (now.getDay().equals(next.getDay())) {
                            i++;
//                            Log.e(TAG, "we should be here");
                            //we have both type of events in the same day
                            if (now.getTotalClicks() > next.getTotalClicks()) {
                                //ups win
                                upDecorators.add(CalendarDay.from(new Date(now.getTimestamp() * 1000)));
                            } else if (now.getTotalClicks() == next.getTotalClicks()) {
                                //same value
                                equalDecorators.add(CalendarDay.from(new Date(next.getTimestamp() * 1000)));
                            } else if (now.getTotalClicks() < next.getTotalClicks()) {
                                //downs win
                                downDecorators.add(CalendarDay.from(new Date(now.getTimestamp() * 1000)));
                            } else {
                                Log.e(TAG, "logic error");
                            }
                        } else {
                            //we only have ups
                            upDecorators.add(CalendarDay.from(new Date(now.getTimestamp() * 1000)));
                        }
                    }
                    else{
                        //we only have downs
                        downDecorators.add(CalendarDay.from(new Date(now.getTimestamp() * 1000)));
                    }
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
                materialCalendarView.setSelectedDate(Calendar.getInstance());
                materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                        long timestamp = materialCalendarView.getSelectedDate().getDate().getTime()/1000 + 3600;
                        if(db.countEventsInDayBasedOnTimestamp(timestamp) == 0){
                            Toast.makeText(getApplicationContext(), "Seems like is was an ordinary day with no activity!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Bundle bundle = new Bundle();
                            bundle.putLong("timestamp", timestamp);
                            bundle.putString("day", String.valueOf(materialCalendarView.getSelectedDate().getCalendar().get(Calendar.DAY_OF_MONTH)));
                            bundle.putString("month", Utils.convertIntToLongMonth(materialCalendarView.getSelectedDate().getCalendar().get(Calendar.MONTH)+1));

                            //move to the newt view... with the bundle genius!
                            startActivity(new Intent(MonthActivity.this, DailyActivity.class).putExtra("values", bundle));
                        }
                    }
                });

                try{
                    materialCalendarView.addDecorator(new EventDecorator(R.drawable.up_day_selector, upDecorators));
                    materialCalendarView.addDecorator(new EventDecorator(R.drawable.down_day_selector, downDecorators));
                    materialCalendarView.addDecorator(new EventDecorator(R.drawable.equal_day_selector, equalDecorators));
                }
                catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_month, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.week_view:
                startActivity(new Intent(MonthActivity.this, WeekActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public class EventDecorator implements DayViewDecorator {
        private final int drawable;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int drawable, Collection<CalendarDay> dates) {
            this.drawable = drawable;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
            if (ContextCompat.getDrawable(getApplicationContext(), drawable) != null)
                view.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), drawable));
        }
    }
}