package com.avner.lostfound.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.ImageUtils;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.messaging.MessagingActivity;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.R;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by avner on 28/04/2015.
 */
public class LostFoundListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

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
        final ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.list_row_lost_item, null);

            viewHolder = new ViewHolder();
            viewHolder.itemName = (TextView) view.findViewById(R.id.tv_item_name);
            viewHolder.description = (TextView) view.findViewById(R.id.tv_item_description);
            viewHolder.timeAdded = (TextView) view.findViewById(R.id.tv_number_of_days_ago);
            viewHolder.locationAdded = (TextView) view.findViewById(R.id.tv_near_address);
            viewHolder.itemImage = (ImageView) view.findViewById(R.id.iv_itemListingImage);
            viewHolder.editReport = (ImageButton) view.findViewById(R.id.ib_editReport);

            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        setViewHolderFields(position, viewHolder);

        return view;
    }

    private void setViewHolderFields(int position, final ViewHolder viewHolder) {
        final Item item = (Item)getItem(position);
        // Put the content in the view
        viewHolder.itemName.setText(item.getName());
        viewHolder.description.setText(item.getDescription());
        viewHolder.locationAdded.setText(item.getLocationString());

        viewHolder.timeAdded.setText("" + item.getDiff() + " ");
        if(item.getUserId().equals(ParseUser.getCurrentUser().getObjectId())){

            viewHolder.editReport.setVisibility(ImageButton.VISIBLE);
        }else{
            viewHolder.editReport.setVisibility(ImageButton.INVISIBLE);
        }

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                return ImageUtils.decodeRemoteUrl(item.getImageUrl());
            }

            @Override
            protected void onPostExecute(Object o) {
                viewHolder.itemImage.setImageBitmap((Bitmap)o);
            }
        };
        task.execute();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
        final Dialog dialog = new Dialog(rootView.getContext());
        dialog.setContentView(R.layout.dialog_item_details_layout);

        Item item = (Item) parent.getItemAtPosition(position);
        if (null == item) {
            Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
            return;
        }
        initMapButton(parent, position, dialog);
        initConversationButton(parent, position, dialog);
        setDialogContents(dialog, item);
        dialog.show();
    }

    private void initConversationButton (final AdapterView<?> parent, final int position, Dialog dialog) {

        ImageButton ib_startConversation = (ImageButton) dialog.findViewById(R.id.ib_sendMessage);

        final Item item = (Item) parent.getItemAtPosition(position);

        // can't message myself.
        if(item.getUserId().equals(ParseUser.getCurrentUser().getObjectId())){
            ib_startConversation.setEnabled(false);
            return;
        }
        ib_startConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootView.getContext(), MessagingActivity.class);
                final Item item = (Item) parent.getItemAtPosition(position);
                if (null == item) {
                    Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
                    return;
                }
                String userId = item.getUserId();
                intent.putExtra(Constants.Conversation.RECIPIENT_ID, userId);
                intent.putExtra(Constants.Conversation.ITEM_ID, item.getId());

                //only add conversation to parse database if it doesn't already exist there
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseConversations");

                query.whereEqualTo("conversationItemId", item.getId());
                query.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId());

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                        if (e == null) {
                            // check this conversation hasn't been added already.
                            if (messageList.size() == 0) {
                                ParseObject parseConversations = new ParseObject("ParseConversations");
                                parseConversations.put("userId", ParseUser.getCurrentUser().getObjectId());
                                parseConversations.put("conversationUserId", item.getUserId());
                                parseConversations.put("conversationUserName", item.getUserDisplayName());
                                parseConversations.put("conversationItem", item.getParseItem());
                                parseConversations.saveInBackground();
                            }
                        }
                    }
                });
                rootView.getContext().startActivity(intent);
            }
        });

    }

    private void initMapButton(final AdapterView<?> parent, final int position, Dialog dialog) {
        ImageButton ib_showMap = (ImageButton) dialog.findViewById(R.id.ib_showMap);
        ib_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootView.getContext(), ViewLocationActivity.class);
                Item item = (Item) parent.getItemAtPosition(position);
                if (null == item) {
                    Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
                    return;
                }
                double latitude = item.getLocation().getLatitude();
                double longitude = item.getLocation().getLongitude();
                intent.putExtra(Constants.LATITUDE, latitude);
                intent.putExtra(Constants.LONGITUDE, longitude);
                rootView.getContext().startActivity(intent);
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

        if(item.getImage() != 0){

            itemImage.setImageResource(item.getImage());
        }else{

            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    return ImageUtils.decodeRemoteUrl(item.getImageUrl());
                }

                @Override
                protected void onPostExecute(Object o) {
                    itemImage.setImageBitmap((Bitmap)o);
                }
            };
            task.execute();
        }

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Lost Item: " + item.getName());
    }

    private class ViewHolder {
        TextView itemName;
        TextView description;
        ImageView itemImage;
        TextView timeAdded;
        TextView locationAdded;
        ImageButton editReport;
    }

}
