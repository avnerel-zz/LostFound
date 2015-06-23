package com.avner.lostfound.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.location.Location;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.ConversationListActivity;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.structs.Conversation;
import com.avner.lostfound.structs.Item;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by avner on 28/04/2015.
 * this class handles the conversation list.
 */
public class ConversationListAdapter extends BaseAdapter {

    private List<Conversation> conversations;
    private ConversationListActivity rootActivity;
    private SparseBooleanArray selectedItemIds;

    public ConversationListAdapter(List<Conversation> conversations, ConversationListActivity rootActivity) {
        this.conversations = conversations;
        this.rootActivity = rootActivity;
        selectedItemIds = new SparseBooleanArray();
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

        final Conversation conversation = conversations.get(position);

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) rootActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.list_row_conversation, null);

            viewHolder = new ViewHolder();
            viewHolder.userDisplayName = (TextView) view.findViewById(R.id.tv_user_list_item);
            viewHolder.itemImage = (ImageButton) view.findViewById(R.id.ib_conversation_item_image);
            viewHolder.unreadCount = (TextView) view.findViewById(R.id.tv_unread_count);
            viewHolder.itemName = (TextView) view.findViewById(R.id.tv_item_name);
            viewHolder.iv_item_done = (ImageView) view.findViewById(R.id.iv_item_done);

            viewHolder.itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Item item = conversation.getItem();

                    if (null == item) {
                        Log.d(Constants.LOST_FOUND_TAG, "Failed to retrieve item from adapter list, at position " + position);
                        return;
                    }

                    if (!rootActivity.setDisplayedItem(item)) {
                        showItemInDialog(item);
                        Log.d("BLA BLA BLA", "clicked item in PORTRAIT or non-large mode");
                    }
                    else {
                        Log.d("BLA BLA BLA", "clicked item in LANDSCAPE & large mode");
                    }
                }
            });

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        final Item item = conversation.getItem();
        String userName = conversation.getUserName();

        // Put the content in the view
        viewHolder.userDisplayName.setText(userName);
        viewHolder.itemName.setText(item.getName());
        if (!item.isAlive()) {
            viewHolder.iv_item_done.setVisibility(View.VISIBLE);
            viewHolder.itemName.setPaintFlags(viewHolder.itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            viewHolder.iv_item_done.setVisibility(View.INVISIBLE);
            viewHolder.itemName.setPaintFlags(viewHolder.itemName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }
        Picasso.with(rootActivity).load(item.getImageUrl()).placeholder(R.drawable.image_unavailable).into(viewHolder.itemImage);

        if (conversation.getUnreadCount() != 0) {
            viewHolder.unreadCount.setText(String.valueOf(conversation.getUnreadCount()));
            viewHolder.unreadCount.setVisibility(TextView.VISIBLE);
        } else {
            viewHolder.unreadCount.setVisibility(TextView.INVISIBLE);
        }

        return view;
    }

    private void showItemInDialog(Item item) {
        final Dialog dialog = new Dialog(rootActivity);
        dialog.setContentView(R.layout.dialog_item_details_layout);
        initMapButton(item, dialog);
        ImageButton ib_sendMessage = (ImageButton) dialog.findViewById(R.id.ib_sendMessage);
        ib_sendMessage.setVisibility(ImageButton.INVISIBLE);
        setDialogContents(dialog, item);
        dialog.show();
    }

    private void initMapButton(final Item item, Dialog dialog) {

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
                intent.putExtra(Constants.LATITUDE, location.getLatitude());
                intent.putExtra(Constants.LONGITUDE, location.getLongitude());
                rootActivity.startActivity(intent);
            }
        });
    }

    private void setDialogContents(Dialog dialog, final Item item) {
        TextView itemLocation = (TextView) dialog.findViewById(R.id.tv_location);
        itemLocation.setText(item.getLocationString());

        TextView itemTime = (TextView) dialog.findViewById(R.id.tv_lossTime);
        itemTime.setText(item.getTimeAsString());

        final ImageView itemImage = (ImageView) dialog.findViewById(R.id.iv_itemImage);
        Picasso.with(rootActivity).load(item.getImageUrl()).placeholder(R.drawable.image_unavailable).into(itemImage);

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Item: " + item.getName());
    }

    public void remove(Conversation selectedConversation) {

        conversations.remove(selectedConversation);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_CONVERSATION);
        query.whereEqualTo(Constants.ParseQuery.OBJECT_ID, selectedConversation.getId());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e== null){
                    parseObject.deleteInBackground();
                }
            }
        });

        ParseQuery<ParseObject> localQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_CONVERSATION);
        localQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, selectedConversation.getId());
        localQuery.fromLocalDatastore();
        localQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    parseObject.unpinInBackground();
                }
            }
        });
    }

    public void toggleSelection(int position) {
        selectView(position, !selectedItemIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            selectedItemIds.put(position, true);

        else
            selectedItemIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds(){
        return selectedItemIds;
    }

    public void removeSelection() {
        selectedItemIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView userDisplayName;
        TextView itemName;
        ImageButton itemImage;
        TextView unreadCount;
        ImageView iv_item_done;
    }

}
