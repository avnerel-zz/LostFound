package com.avner.lostfound.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.structs.Item;

import java.util.List;

/**
 * Created by avner on 28/04/2015.
 */
public class ConversationListAdapter extends BaseAdapter {

    private List<Item> items;
    private List<String> userNames;

    private Activity rootView;

    public ConversationListAdapter(List<Item> items, List<String> userNames,Activity rootView) {
        this.items = items;
        this.userNames = userNames;
        this.rootView=rootView;
    }


    @Override
    public int getCount() {
        return userNames.size();
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
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final View view;
        ViewHolder viewHolder;

//        Log.d("MY_TAG", "Position: " + position);

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) rootView.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.list_row_conversation, null);

            viewHolder = new ViewHolder();
            viewHolder.userDisplayName = (TextView) view.findViewById(R.id.tv_user_list_item);
            viewHolder.itemImage = (ImageButton) view.findViewById(R.id.ib_conversation_item_image);
            viewHolder.itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Item item = items.get(position);
                    final Dialog dialog = new Dialog(rootView);
                    dialog.setContentView(R.layout.dialog_item_details_layout);
                    initMapButton(item, position, dialog);
                    ImageButton ib_sendMessage = (ImageButton) dialog.findViewById(R.id.ib_sendMessage);
                    ib_sendMessage.setEnabled(false);
                    setDialogContents(dialog, item);
                    dialog.show();
                }
            });
            view.setTag(viewHolder);
        }
        else {
            view = convertView;

            viewHolder = (ViewHolder) view.getTag();
        }
        Item item = (Item)getItem(position);
        String userName = userNames.get(position);

        // Put the content in the view
        viewHolder.userDisplayName.setText(userName);
        viewHolder.itemImage.setImageResource(item.getImage());

        return view;
    }

    private void initMapButton(final Item item, final int position, Dialog dialog) {
        ImageButton ib_showMap = (ImageButton) dialog.findViewById(R.id.ib_showMap);
        ib_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootView, ViewLocationActivity.class);
                if (null == item) {
                    Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
                    return;
                }
                double latitude = item.getLocation().getLatitude();
                double longitude = item.getLocation().getLongitude();
                intent.putExtra(Constants.LATITUDE, latitude);
                intent.putExtra(Constants.LONGITUDE, longitude);
                rootView.startActivity(intent);
            }
        });
    }

    private void setDialogContents(Dialog dialog, Item item) {
        TextView itemLocation = (TextView) dialog.findViewById(R.id.tv_location);
        itemLocation.setText(item.getLocationString());
        itemLocation.setMaxLines(2);

        TextView itemTime = (TextView) dialog.findViewById(R.id.tv_lossTime);
        itemTime.setText(item.timeAgo());

        ImageView itemImage = (ImageView) dialog.findViewById(R.id.iv_itemImage);
        itemImage.setImageResource(item.getImage());

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Item: " + item.getName());
    }



    private class ViewHolder {
        TextView userDisplayName;
        ImageButton itemImage;
    }

}
