package com.avner.lostfound.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.MessagingActivity;
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.fragments.ListingFragment;
import com.avner.lostfound.structs.Item;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LostFoundListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{

    private final ListingFragment myFragment;
    private List<Item> items;
    private View rootView;
    private int clickedPosition = -1;

    public LostFoundListAdapter(List<Item> items, View rootView, ListingFragment myFragment) {
        this.items = items;
        this.rootView = rootView;
        this.myFragment = myFragment;
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
    public View getView(int position, final View convertView, final ViewGroup parent) {

        final View view;
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
            Log.d(Constants.LOST_FOUND_TAG, "setting edit adapter for item " + item.getName() + " at position " + position);

            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        setEditReportButton(view, viewHolder, item);
        setViewHolderFields(viewHolder, item);

        return view;
    }

    private void setEditReportButton(View view, ViewHolder viewHolder, final Item item) {
        viewHolder.editReport = (ImageButton) view.findViewById(R.id.ib_editReport);
        viewHolder.editReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(Constants.LOST_FOUND_TAG, "pressed to edit item " + item.getName());
                Intent intent = new Intent(rootView.getContext(),ReportFormActivity.class);
                intent.putExtra(Constants.ReportForm.IS_LOST_FORM, item.isLost());
                intent.putExtra(Constants.ReportForm.IS_EDIT_FORM, true);
                intent.putExtra(Constants.ReportForm.ITEM, item);
                ((Activity)rootView.getContext()).startActivityForResult(intent,Constants.REQUEST_CODE_REPORT_FORM);

            }
        });
    }


    private void setViewHolderFields(final ViewHolder viewHolder, Item item) {
        // Put the content in the view
        viewHolder.itemName.setText(item.getName());
        viewHolder.description.setText(item.getDescription());
        viewHolder.locationAdded.setText(item.getLocationString());
        viewHolder.timeAdded.setText("" + item.getDiff() + " ");

        // show edit image button only if the user reported it
        if (item.getUserId().equals(ParseUser.getCurrentUser().getObjectId())) {
            viewHolder.editReport.setVisibility(ImageButton.VISIBLE);
        } else {
            viewHolder.editReport.setVisibility(ImageButton.INVISIBLE);
        }

        Picasso.with(rootView.getContext()).load(item.getImageUrl()).placeholder(R.drawable.image_unavailable).into(viewHolder.itemImage);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
        final Item item = (Item) parent.getItemAtPosition(position);
        Log.d(Constants.LOST_FOUND_TAG, "clicked item " + item.getName() + ", at position " + position);

        setClickedPosition(position);
        if (!myFragment.setDisplayedItem(item)) {
            showItemInDialog(parent, item, position);
            Log.d("BLA BLA BLA", "clicked item in PORTRAIT or non-large mode");
        } else {
            Log.d("BLA BLA BLA", "clicked item in LANDSCAPE & large mode");
        }
    }

    private void showItemInDialog(AdapterView<?> parent, Item item, int position) {
        final Dialog dialog = new Dialog(myFragment.getActivity());
        myFragment.setClickedDialog(dialog);
        dialog.setContentView(R.layout.dialog_item_details_layout);
        initMapButton(parent, position, dialog);
        initConversationButton(parent, position, dialog);
        setDialogContents(dialog, item);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(Constants.LOST_FOUND_TAG, "dismissed dialog in listing adapter.");
                setClickedPosition(-1);
                myFragment.setClickedDialog(null);
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(Constants.LOST_FOUND_TAG, "cancelled dialog in listing adapter.");
                setClickedPosition(-1);
                myFragment.setClickedDialog(null);
            }
        });

        dialog.show();
    }


    private void initConversationButton (final AdapterView<?> parent, final int position, Dialog dialog) {

        ImageButton ib_startConversation = (ImageButton) dialog.findViewById(R.id.ib_sendMessage);
        final Item item = (Item) parent.getItemAtPosition(position);

        // can't message myself.
        if(item.getUserId().equals(ParseUser.getCurrentUser().getObjectId())){
            ib_startConversation.setVisibility(Button.INVISIBLE);
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

                Intent intent = new Intent(rootView.getContext(), MessagingActivity.class);
                intent.putExtra(Constants.Conversation.RECIPIENT_ID, item.getUserId());
                intent.putExtra(Constants.Conversation.RECIPIENT_NAME, item.getUserDisplayName());
                intent.putExtra(Constants.Conversation.ITEM_ID, item.getId());
                rootView.getContext().startActivity(intent);
            }
        });

    }


    private void initMapButton(final AdapterView<?> parent, final int position, Dialog dialog) {
        final Item item = (Item) parent.getItemAtPosition(position);
        final Location location = item.getLocation();
        final ImageButton ib_showMap = (ImageButton) dialog.findViewById(R.id.ib_showMap);

        // no location specified.
        if (location == null) {
            ib_showMap.setVisibility(ImageButton.INVISIBLE);
            return;
        }

        ib_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootView.getContext(), ViewLocationActivity.class);
                intent.putExtra(Constants.LATITUDE, location.getLatitude());
                intent.putExtra(Constants.LONGITUDE, location.getLongitude());
                rootView.getContext().startActivity(intent);
            }
        });
    }

    private void setDialogContents(Dialog dialog, final Item item) {
        final TextView itemLocation = (TextView) dialog.findViewById(R.id.tv_location);
        itemLocation.setText(item.getLocationString());

        final TextView itemTime = (TextView) dialog.findViewById(R.id.tv_lossTime);
        itemTime.setText(item.getTimeAsString());

        // get item image from url.
        final ImageView itemImage = (ImageView) dialog.findViewById(R.id.iv_itemImage);
        Picasso.with(rootView.getContext()).load(item.getImageUrl()).placeholder(R.drawable.image_unavailable).into(itemImage);

        final TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());
        itemDescription.setMovementMethod(new ScrollingMovementMethod());

        dialog.setTitle("Item: " + item.getName());
    }

    public void setList(List<Item> list) {
        this.items = list;
    }

    public int getClickedPosition() {
        return clickedPosition;
    }

    public void setClickedPosition(int clickedPosition) {
        this.clickedPosition = clickedPosition;
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
