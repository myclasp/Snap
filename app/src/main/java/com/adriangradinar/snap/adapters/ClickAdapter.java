package com.adriangradinar.snap.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adriangradinar.snap.R;
import com.adriangradinar.snap.classes.Click;
import com.adriangradinar.snap.utils.DatabaseHandler;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.ArrayList;

/**
 * Created by adriangradinar on 21/03/2016.
 * The click adapter class allow one to define the interactions and looks of the click when displayed in a list
 */
public class ClickAdapter extends BaseSwipeAdapter {

    private static final String TAG = ClickAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<Click> allClicks;
    private DatabaseHandler databaseHandler;
    private TextView upsTV, downsTV, totalTV;

    public ClickAdapter(Context context, ArrayList<Click> allClicks, TextView upsTV, TextView downsTV, TextView totalTV) {
        databaseHandler = DatabaseHandler.getHelper(context);
        this.allClicks = allClicks;
        this.context = context;

        this.upsTV = upsTV;
        this.downsTV = downsTV;
        this.totalTV = totalTV;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        return allClicks.size();
    }

    @Override
    public Object getItem(int position) {
        return allClicks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        return ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.default_click_item_layout, parent, false);
    }

    @Override
    public void fillValues(int position, final View convertView) {
        final Click click = allClicks.get(position);

        final ViewHolder holder;
        holder = new ViewHolder();
        holder.click = click;
        holder.hour_minute_second_tv = (TextView) convertView.findViewById(R.id.hour_min_sec_tv);
        holder.location_tv = (TextView) convertView.findViewById(R.id.location_tv);
        holder.swipeLayout = (SwipeLayout) convertView.findViewById(getSwipeLayoutResourceId(position));

        //configure our data holder
        holder.setTime();
        holder.setLocation();
        holder.setTimeBackgroundColor(context);

        //set the delete/restore layout
        holder.action_layout = (LinearLayout) convertView.findViewById(R.id.action_layout);
        holder.action_icon = (ImageView) convertView.findViewById(R.id.action_icon);
        holder.action_text = (TextView) convertView.findViewById(R.id.action_text);
        holder.action_button = (Button) convertView.findViewById(R.id.action_button);
        holder.setSwipeLayout(context);

        convertView.findViewById(R.id.action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //let's also close the item
                holder.swipeLayout.close();

                //update the database
                databaseHandler.setMarked(click.getId(), holder.returnInverseOfMarked());

                //update the data set, otherwise we'll not get the correct data
                changeClickMarked(click.getId());

                //add the action to the database
                databaseHandler.addMarkedAnalytic(click.getId(), click.getMarked());

                //since the user decided to mark/un-mark the current item, we need to change the colour of the item
                holder.setTimeBackgroundColor(context);

                //update the layout
                holder.setSwipeLayout(context);

                //update the header
                updateHeaderValues(click);
            }
        });

        //finally, set the holder to the view
        convertView.setTag(holder);
    }

    private void updateHeaderValues(Click click) {
        int total, downs, ups;
        if (click.getMarked() == 0) {
            if (click.getTotalClicks() == 1) {
                ups = Integer.parseInt(upsTV.getText().toString());
                ups++;
                upsTV.setText(String.valueOf(ups));
            } else {
                downs = Integer.parseInt(downsTV.getText().toString());
                downs++;
                downsTV.setText(String.valueOf(downs));
            }
            total = Integer.parseInt(totalTV.getText().toString());
            total++;
            totalTV.setText(String.valueOf(total));
        } else {
            if (click.getTotalClicks() == 1) {
                ups = Integer.parseInt(upsTV.getText().toString());
                ups--;
                upsTV.setText(String.valueOf(ups));
            } else {
                downs = Integer.parseInt(downsTV.getText().toString());
                downs--;
                downsTV.setText(String.valueOf(downs));
            }
            total = Integer.parseInt(totalTV.getText().toString());
            total--;
            totalTV.setText(String.valueOf(total));
        }
    }

    private void changeClickMarked(int id) {
        for (Click click : allClicks) {
            if (click.getId() == id) {
                if (click.getMarked() == 0)
                    click.setMarked(1);
                else
                    click.setMarked(0);
            }
        }
    }

    public static class ViewHolder {
        public Click click;
        public TextView hour_minute_second_tv;
        public TextView location_tv;
        public SwipeLayout swipeLayout;

        public LinearLayout action_layout;
        public ImageView action_icon;
        public TextView action_text;
        public Button action_button;

        public void setTime() {
            String time = click.getHour() + ":" + click.getMinute();
            this.hour_minute_second_tv.setText(time);
        }

        public void setLocation() {
            this.location_tv.setText(click.getAddress());
        }

        public void setTimeBackgroundColor(Context context) {
            if (click.getMarked() == 1) {
                this.hour_minute_second_tv.setBackgroundColor(ContextCompat.getColor(context, R.color.action_disabled));
            } else {
                if (click.getTotalClicks() == 1) {
                    this.hour_minute_second_tv.setBackgroundColor(ContextCompat.getColor(context, R.color.up_primary));
                } else {
                    this.hour_minute_second_tv.setBackgroundColor(ContextCompat.getColor(context, R.color.down_primary));
                }
            }
        }

        public int returnInverseOfMarked() {
            if (click.getMarked() == 0)
                return 1;
            else
                return 0;
        }

        public void setSwipeLayout(Context context) {
            if (click.getMarked() == 0) {
                this.action_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_gray_press));
                this.action_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.hidden_icon));
                this.action_text.setText(context.getString(R.string.action_delete_text));
                this.action_button.setText(context.getString(R.string.action_button_delete_text));
                this.action_button.setTextColor(ContextCompat.getColor(context, R.color.dark_gray_press));
            } else {
                if (click.getTotalClicks() == 1) {
                    this.action_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.up_primary));
                    this.action_button.setTextColor(ContextCompat.getColor(context, R.color.up_primary));
                } else {
                    this.action_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.down_primary));
                    this.action_button.setTextColor(ContextCompat.getColor(context, R.color.down_primary));
                }
                this.action_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.restore_icon));
                this.action_text.setText(context.getString(R.string.action_restore_text));
                this.action_button.setText(context.getString(R.string.action_button_restore_text));
            }
        }
    }
}