package com.avner.lostfound.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avner.lostfound.structs.Item;
import com.avner.lostfound.R;

import java.util.List;

/**
 * Created by avner on 28/04/2015.
 */
public class LostFoundListAdapter extends BaseAdapter {

    private List<Item> items;

    private View rootView;

    public LostFoundListAdapter(List<Item> items, View rootView) {
        this.items = items;
        this.rootView=rootView;
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        ViewHolder viewHolder;

        Log.d("MY_TAG", "Position: " + position);

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.list_row_lost_item, null);

            viewHolder = new ViewHolder();
            viewHolder.itemName = (TextView) view.findViewById(R.id.tv_item_name);
            viewHolder.description = (TextView) view.findViewById(R.id.tv_item_description);
            viewHolder.timeAdded = (TextView) view.findViewById(R.id.tv_number_of_days_ago);
            viewHolder.locationAdded = (TextView) view.findViewById(R.id.tv_near_address);
            viewHolder.itemImage = (ImageView) view.findViewById(R.id.iv_itemListingImage);

            view.setTag(viewHolder);
        }
        else {
            view = convertView;

            viewHolder = (ViewHolder) view.getTag();
        }

        Item item = (Item)getItem(position);
        // Put the content in the view
        viewHolder.itemName.setText(item.getName());
        viewHolder.description.setText(item.getDescription());

        viewHolder.timeAdded.setText("" + item.getDiff() + " ");
//            viewHolder.locationAdded.setText(item.getLocation().describeContents());
        viewHolder.itemImage.setImageResource(item.getImage());

        return view;
    }



    private class ViewHolder {
        TextView itemName;
        TextView description;
        ImageView itemImage;
        TextView timeAdded;
        TextView locationAdded;
    }

}
