package com.avner.lostfound.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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
import com.avner.lostfound.structs.Conversation;
import com.avner.lostfound.structs.Item;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by avner on 28/04/2015.
 * this class handles the conversation list.
 */
public class ConversationListAdapter extends BaseAdapter {

    private List<Conversation> conversations;

    private Activity rootActivity;

    public ConversationListAdapter(List<Conversation> conversations, Activity rootActivity) {
        this.conversations = conversations;
        this.rootActivity =rootActivity;
    }


    @Override
    public int getCount() {
        return conversations.size();
    }

    @Override
    public Object getItem(int position) {
        return conversations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final View view;
        final ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) rootActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.list_row_conversation, null);

            viewHolder = new ViewHolder();
            viewHolder.userDisplayName = (TextView) view.findViewById(R.id.tv_user_list_item);
            viewHolder.itemImage = (ImageButton) view.findViewById(R.id.ib_conversation_item_image);
            viewHolder.itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Item item = conversations.get(position).getItem();
                    final Dialog dialog = new Dialog(rootActivity);
                    dialog.setContentView(R.layout.dialog_item_details_layout);
                    initMapButton(item, position, dialog);
                    ImageButton ib_sendMessage = (ImageButton) dialog.findViewById(R.id.ib_sendMessage);
                    ib_sendMessage.setVisibility(ImageButton.INVISIBLE);
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
        final Item item = conversations.get(position).getItem();
        String userName = conversations.get(position).getUserName();

        // Put the content in the view
        viewHolder.userDisplayName.setText(userName);
        Picasso.with(rootActivity).load(item.getImageUrl()).into(viewHolder.itemImage);

//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                return ImageUtils.decodeRemoteUrl(item.getImageUrl());
//            }
//
//            @Override
//            protected void onPostExecute(Object o) {
//                viewHolder.itemImage.setImageBitmap((Bitmap)o);
//            }
//        };
//        task.execute();

        return view;
    }

    private void initMapButton(final Item item, final int position, Dialog dialog) {

        final Location location = item.getLocation();
        ImageButton ib_showMap = (ImageButton) dialog.findViewById(R.id.ib_showMap);

        // no location specified.
        if(location == null){
            ib_showMap.setVisibility(ImageButton.INVISIBLE);
            return;
        }
        ib_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootActivity, ViewLocationActivity.class);
                if (null == item) {
                    Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
                    return;
                }
                intent.putExtra(Constants.LATITUDE, location.getLatitude());
                intent.putExtra(Constants.LONGITUDE, location.getLongitude());
                rootActivity.startActivity(intent);
            }
        });
    }

    private void setDialogContents(Dialog dialog, final Item item) {
        TextView itemLocation = (TextView) dialog.findViewById(R.id.tv_location);
        itemLocation.setText(item.getLocationString());
        itemLocation.setMaxLines(2);

        TextView itemTime = (TextView) dialog.findViewById(R.id.tv_lossTime);
        itemTime.setText(item.timeAgo());

        final ImageView itemImage = (ImageView) dialog.findViewById(R.id.iv_itemImage);
        Picasso.with(rootActivity).load(item.getImageUrl()).into(itemImage);


//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                return ImageUtils.decodeRemoteUrl(item.getImageUrl());
//            }
//
//            @Override
//            protected void onPostExecute(Object o) {
//                itemImage.setImageBitmap((Bitmap)o);
//            }
//        };
//        task.execute();

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Item: " + item.getName());
    }



    private class ViewHolder {
        TextView userDisplayName;
        ImageButton itemImage;
    }

}
