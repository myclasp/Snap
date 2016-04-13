package com.adriangradinar.snap.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.adriangradinar.snap.R;
import com.adriangradinar.snap.classes.Click;

import java.util.ArrayList;

/**
 * Created by adriangradinar on 21/03/2016.
 *
 */
public class ClickAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private ArrayList<Click> allClicks;

    public ClickAdapter(Activity activity, ArrayList<Click> allClicks){
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.allClicks = allClicks;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        final ViewHolder holder;

//        if (convertView == null) {
            if (allClicks.get(position).getTotalClicks() == 1) {
                vi = inflater.inflate(R.layout.up_click_item_layout, parent, false);
            } else {
                vi = inflater.inflate(R.layout.down_click_item_layout, parent, false);
            }

            holder = new ViewHolder();
            holder.hour_minute_second_tv = (TextView) vi.findViewById(R.id.hour_min_sec_tv);
            holder.location_tv = (TextView) vi.findViewById(R.id.location_tv);

            vi.setTag(holder);
//        } else {
//            //the current view is being recycled
//            if (allClicks.get(position).getTotalClicks() == 1) {
//                vi = inflater.inflate(R.layout.up_click_item_layout, parent, false);
//            } else {
//                vi = inflater.inflate(R.layout.down_click_item_layout, parent, false);
//            }
//            holder = (ViewHolder) vi.getTag();
//        }

        final Click click = allClicks.get(position);

        //set the values
        String text = click.getHour() + ":" + click.getMinute();
        holder.hour_minute_second_tv.setText(text);
        holder.location_tv.setText(click.getAddress());
        return vi;
    }

    public static class ViewHolder {
        public TextView hour_minute_second_tv;
        public TextView location_tv;
    }
}
