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
import com.adriangradinar.snap.classes.TypefaceSpan;
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
    private long analytics_timestamp = 0;

    private MaterialCalendarView materialCalendarView;
    private DatabaseHandler db;
    private ArrayList<Click> monthClicks;

    private List<CalendarDay> upDecorators = new ArrayList<>();
    private List<CalendarDay> upMarkedDecorators = new ArrayList<>();
    private List<CalendarDay> downDecorators = new ArrayList<>();
    private List<CalendarDay> downMarkedDecorators = new ArrayList<>();
    private List<CalendarDay> equalDecorators = new ArrayList<>();
    private List<CalendarDay> equalMarkedDecorators = new ArrayList<>();
    private List<CalendarDay> emptyDecorators = new ArrayList<>();
    private List<CalendarDay> emptyMarkedDecorators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month);

        //set the title
        Utils.setActionBarTextAndFont(getSupportActionBar(), new TypefaceSpan(this, "BebasNeue Bold.ttf"), "Month View");
        db = DatabaseHandler.getHelper(getApplicationContext());

        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
    }

    public void setView(){
        ThreadManager.runInBackgroundThenUi(new Runnable() {
            @Override
            public void run() {
                monthClicks = db.getCurrentMonth();

                //clear the decorators
                upDecorators.clear();
                upMarkedDecorators.clear();
                downDecorators.clear();
                downMarkedDecorators.clear();
                equalDecorators.clear();
                equalMarkedDecorators.clear();
                emptyDecorators.clear();
                emptyMarkedDecorators.clear();

                db.logClicks();

                for (Click click : monthClicks) {
                    if (click.getUpDay() == 1) {
                        //up day
                        if (click.getTotalMarked() != 0) {
                            //marked day
                            upMarkedDecorators.add(CalendarDay.from(new Date(click.getTimestamp() * 1000)));
                        } else {
                            //unmarked day
                            upDecorators.add(CalendarDay.from(new Date(click.getTimestamp() * 1000)));
                        }
                    } else if (click.getDownDay() == 1) {
                        //down day
                        if (click.getTotalMarked() != 0) {
                            //marked day
                            downMarkedDecorators.add(CalendarDay.from(new Date(click.getTimestamp() * 1000)));
                        } else {
                            //unmarked day
                            downDecorators.add(CalendarDay.from(new Date(click.getTimestamp() * 1000)));
                        }
                    }
                    else{
                        //equal day
                        if (click.getTotalUnmarked() != 0) {
                            //with data
                            if (click.getTotalMarked() != 0) {
                                //marked day
                                equalMarkedDecorators.add(CalendarDay.from(new Date(click.getTimestamp() * 1000)));
                            } else {
                                //unmarked day
                                equalDecorators.add(CalendarDay.from(new Date(click.getTimestamp() * 1000)));
                            }
                        } else {
                            //with no data
                            if (click.getTotalMarked() != 0) {
                                //marked day
                                emptyMarkedDecorators.add(CalendarDay.from(new Date(click.getTimestamp() * 1000)));
                            } else {
                                //unmarked day
                                emptyDecorators.add(CalendarDay.from(new Date(click.getTimestamp() * 1000)));
                            }
                        }
                    }
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                materialCalendarView.invalidateDecorators();
                materialCalendarView.addDecorator(new EventDecorator(R.drawable.up_day, upDecorators));
                materialCalendarView.addDecorator(new EventDecorator(R.drawable.up_marked_day, upMarkedDecorators));
                materialCalendarView.addDecorator(new EventDecorator(R.drawable.down_day, downDecorators));
                materialCalendarView.addDecorator(new EventDecorator(R.drawable.down_marked_day, downMarkedDecorators));
                materialCalendarView.addDecorator(new EventDecorator(R.drawable.equal_day, equalDecorators));
                materialCalendarView.addDecorator(new EventDecorator(R.drawable.equal_day_marked, equalMarkedDecorators));
                materialCalendarView.addDecorator(new EventDecorator(R.drawable.empty_day, emptyDecorators));
                materialCalendarView.addDecorator(new EventDecorator(R.drawable.empty_marked_day, emptyMarkedDecorators));

                materialCalendarView.setSelectedDate(Calendar.getInstance());
                materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                        Log.e(TAG, date.toString());

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
        switch (item.getItemId()) {
            case R.id.hourly:
                startActivity(new Intent(MonthActivity.this, HourlyActivity.class));
                break;
            case R.id.week_view:
                startActivity(new Intent(MonthActivity.this, WeekActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.addActivityAnalytic(TAG, analytics_timestamp, (Utils.getTimestamp() - analytics_timestamp));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setView();
        analytics_timestamp = Utils.getTimestamp();
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
            if (ContextCompat.getDrawable(getApplicationContext(), drawable) != null) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), drawable));
            }
        }
    }
}