package com.adriangradinar.snap.adapters;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.adriangradinar.snap.R;
import com.adriangradinar.snap.classes.Click;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by adriangradinar on 21/03/2016.
 */
public class ClickAdapter extends BaseAdapter {

    private ArrayList<Click> allClicks;
    private static LayoutInflater inflater = null;
    private Geocoder geocoder;
//    private ImageLoader imageLoader;

    public ClickAdapter(Activity activity, ArrayList<Click> allClicks){
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.allClicks = allClicks;
        geocoder = new Geocoder(activity.getApplicationContext(), Locale.getDefault());
//        imageLoader = new ImageLoader(activity.getApplicationContext(), "iplayer");
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

    public static class ViewHolder{
//        public ImageView imageView;
        public TextView hour_minute_second_tv;
        public TextView location_tv;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View vi=convertView;
        final ViewHolder holder;

        if(convertView==null){
            if(allClicks.get(position).getTotalClicks() == 1){
                vi = inflater.inflate(R.layout.up_click_item_layout, null);
            }
            else{
                vi = inflater.inflate(R.layout.down_click_item_layout, null);
            }

            holder=new ViewHolder();
            holder.hour_minute_second_tv = (TextView) vi.findViewById(R.id.hour_min_sec_tv);
            holder.location_tv = (TextView) vi.findViewById(R.id.location_tv);

            vi.setTag(holder);
        }
        else{
            //the current view is being recycled
            holder=(ViewHolder)vi.getTag();
        }

        final Click click = allClicks.get(position);

        //set the values
//        holder.hour_minute_second_tv.setText(click.getHour() + ":" + click.getMinute() + "." + click.getSecond());
        holder.hour_minute_second_tv.setText(click.getHour() + ":" + click.getMinute());
        holder.location_tv.setText(click.getLatitude() + " - " + click.getLongitude() + " - " + click.getAccuracy());
//        holder.location_tv.setText(returnAddress(click.getLatitude(), click.getLongitude()).toString());

//        imageLoader.DisplayImage(track.getTrackImage(), holder.trackImageView);

        return vi;
    }

    private String returnAddress(double latitude, double longitude){
        String address = String.valueOf(latitude) + " / " + String.valueOf(longitude);
        try {
            List <Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses.size() > 0){
                address = "";
                for(int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++){
                    if(i != addresses.get(0).getMaxAddressLineIndex() - 1)
                        address += addresses.get(0).getAddressLine(i) + "\n";
                    else
                        address += addresses.get(0).getAddressLine(i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }
}
