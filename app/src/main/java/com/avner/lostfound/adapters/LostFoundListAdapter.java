package com.avner.lostfound.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.messaging.MessagingActivity;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.R;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by avner on 28/04/2015.
 */
public class LostFoundListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{

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
        final Item item = (Item)getItem(position);

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.list_row_lost_item, null);

            viewHolder = new ViewHolder();
            viewHolder.itemName = (TextView) view.findViewById(R.id.tv_item_name);
            viewHolder.description = (TextView) view.findViewById(R.id.tv_item_description);
            viewHolder.timeAdded = (TextView) view.findViewById(R.id.tv_number_of_days_ago);
            viewHolder.locationAdded = (TextView) view.findViewById(R.id.tv_near_address);
            viewHolder.itemImage = (ImageView) view.findViewById(R.id.iv_itemListingImage);
            initEditReport(view, viewHolder,item);

            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        setViewHolderFields(position, viewHolder,item);

        return view;
    }

    private void initEditReport(View view, ViewHolder viewHolder, final Item item) {
        viewHolder.editReport = (ImageButton) view.findViewById(R.id.ib_editReport);
        viewHolder.editReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(rootView.getContext(),ReportFormActivity.class);
                intent.putExtra(Constants.ReportForm.IS_LOST_FORM, item.isLost());
                intent.putExtra(Constants.ReportForm.IS_EDIT_FORM, true);
                intent.putExtra(Constants.ReportForm.ITEM, item);
                rootView.getContext().startActivity(intent);

            }
        });
    }


    private void setViewHolderFields(int position, final ViewHolder viewHolder,Item item) {
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

        Picasso.with(rootView.getContext()).load(item.getImageUrl()).into(viewHolder.itemImage);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
        final Dialog dialog = new Dialog(rootView.getContext());
        dialog.setContentView(R.layout.dialog_item_details_layout);

        Item item = (Item) parent.getItemAtPosition(position);
        if (null == item) {
            Log.d(Constants.LOST_FOUND_TAG, "Failed to retrieve item from adapter list, at position " + position);
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
                final Item item = (Item) parent.getItemAtPosition(position);
                if (null == item) {
                    Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
                    return;
                }
                saveConversationToParse(item);

                Intent intent = new Intent(rootView.getContext(), MessagingActivity.class);
                intent.putExtra(Constants.Conversation.RECIPIENT_ID, item.getUserId());
                intent.putExtra(Constants.Conversation.RECIPIENT_NAME, item.getUserDisplayName());
                intent.putExtra(Constants.Conversation.ITEM_ID, item.getId());
                rootView.getContext().startActivity(intent);
            }
        });

    }

    private void saveConversationToParse(final Item item) {

        //only add conversation to parse database if it doesn't already exist there
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_CONVERSATION);

        query.whereEqualTo(Constants.ParseConversation.ITEM, item.getParseItem());
        query.whereEqualTo(Constants.ParseConversation.MY_USER_ID, ParseUser.getCurrentUser().getObjectId());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                if (e == null) {
                    // this conversation hasn't been added already, so add it.
                    if (messageList.size() == 0) {
                        ParseObject parseConversations = new ParseObject(Constants.ParseObject.PARSE_CONVERSATION);
                        parseConversations.put(Constants.ParseConversation.MY_USER_ID,
                                                ParseUser.getCurrentUser().getObjectId());
                        parseConversations.put(Constants.ParseConversation.RECIPIENT_USER_ID, item.getUserId());
                        parseConversations.put(Constants.ParseConversation.RECIPIENT_USER_NAME, item.getUserDisplayName());
                        parseConversations.put(Constants.ParseConversation.ITEM, item.getParseItem());
                        parseConversations.put(Constants.ParseConversation.UNREAD_COUNT, 0);
                        parseConversations.saveInBackground();
                    }
                }
            }
        });
    }

    private void initMapButton(final AdapterView<?> parent, final int position, Dialog dialog) {

        final Item item = (Item) parent.getItemAtPosition(position);
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
                Intent intent = new Intent(rootView.getContext(), ViewLocationActivity.class);
                if (null == item) {
                    Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
                    return;
                }
                intent.putExtra(Constants.LATITUDE, location.getLatitude());
                intent.putExtra(Constants.LONGITUDE, location.getLongitude());
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

        // get item image from url.
        Picasso.with(rootView.getContext()).load(item.getImageUrl()).into(itemImage);

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Item: " + item.getName());
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
