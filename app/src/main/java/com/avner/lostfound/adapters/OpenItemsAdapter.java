package com.avner.lostfound.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.fragments.MyWorldFragment;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.utils.SignalSystem;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OpenItemsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private final MyWorldFragment myFragment;
    private List<Item> items;
    private View rootView;
    private int chosenItemPosition;

    public OpenItemsAdapter(List<Item> items, View rootView,MyWorldFragment worldFragment) {
        this.items = items;
        this.rootView = rootView;
        this.myFragment = worldFragment;
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
        final Item item = (Item) getItem(position);

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.list_row_open_listing_layout, null);

            viewHolder = new ViewHolder();
            viewHolder.itemName = (TextView) view.findViewById(R.id.tv_itemListingName);
            viewHolder.timeAdded = (TextView) view.findViewById(R.id.tv_itemListingAge);
            viewHolder.itemImage = (ImageView) view.findViewById(R.id.iv_itemListingImage);
            viewHolder.lostFoundImage = (ImageView) view.findViewById(R.id.iv_lost_or_found);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        // Put the content in the view
        viewHolder.itemName.setText(item.getName());
        viewHolder.timeAdded.setText(item.timeAgo());

        if (item.isLost()) {
            viewHolder.lostFoundImage.setImageResource(Constants.LOST_ITEM_IMAGE);
        } else {
            viewHolder.lostFoundImage.setImageResource(Constants.FOUND_ITEM_IMAGE);
        }

        Picasso.with(rootView.getContext()).load(item.getImageUrl()).placeholder(R.drawable.image_unavailable).into(viewHolder.itemImage);

        return view;
    }

    public void remove(final Item item) {

        //TODO check connectivity before removing.
        final ProgressDialog progressDialog = new ProgressDialog(rootView.getContext());
        progressDialog.setTitle("Deleting");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        ParseQuery<ParseObject> deleteFromLocalQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        deleteFromLocalQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, item.getId());
        deleteFromLocalQuery.fromLocalDatastore();
        deleteFromLocalQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseItem, ParseException e) {
                if (e != null) {
                    Log.e(Constants.LOST_FOUND_TAG, "item" + item.getName()
                            + " had already been removed from local data store. " + e.getLocalizedMessage());
                    progressDialog.dismiss();
                    Toast.makeText(rootView.getContext(), "Couldn't delete item from server, please check your connection.", Toast.LENGTH_SHORT);
                }
                parseItem.put(Constants.ParseReport.IS_ALIVE, false);
                parseItem.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(Constants.LOST_FOUND_TAG, "problem removing item" + item.getName()
                                    + " from local data store. " + e.getLocalizedMessage());
                            return;
                        }
                        SignalSystem.getInstance().fireUpdateChange(Constants.UIActions.uiaItemSaved);
                        progressDialog.dismiss();
                        deleteFromParse(item, parseItem);
                    }
                });
            }
        });
    }

    private void deleteFromParse(Item item, final ParseObject localParseItem) {
        ParseQuery<ParseObject> deleteQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        deleteQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, item.getId());
        deleteQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseItem, ParseException e) {
                if (parseItem != null) {
                    parseItem.put(Constants.ParseReport.IS_ALIVE, false);
                    parseItem.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            // couldn't remove from server.
                            if(e!= null){
                                restoreLocalParseItem(localParseItem);
                            }
                        }
                    });
                } else {
                    Log.e(Constants.LOST_FOUND_TAG, "item couldn't be retrieved for deletion from the parse data store.");
                    restoreLocalParseItem(localParseItem);
                }
            }
        });
    }

    private void restoreLocalParseItem(ParseObject localParseItem) {
        localParseItem.put(Constants.ParseReport.IS_ALIVE, true);
        localParseItem.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(rootView.getContext(), "Couldn't delete item from server, please check your connection.", Toast.LENGTH_SHORT).show();
                SignalSystem.getInstance().fireUpdateChange(Constants.UIActions.uiaItemSaved);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = (Item) getItem(position);

        if (!myFragment.setDisplayedItem(item)) {
            showItemInDialog(item, position);
            Log.d("BLA BLA BLA", "clicked item in PORTRAIT or non-large mode");
        }
        else {
            Log.d("BLA BLA BLA", "clicked item in LANDSCAPE & large mode");
        }
    }

    private void showItemInDialog(Item item, int position) {
        final Dialog dialog = new Dialog(rootView.getContext());
        dialog.setContentView(R.layout.dialog_item_details_layout);
        initMapButton(item, position, dialog);
        ImageButton ib_sendMessage = (ImageButton) dialog.findViewById(R.id.ib_sendMessage);
        ib_sendMessage.setVisibility(ImageButton.INVISIBLE);
        setDialogContents(dialog, item);
        dialog.show();
    }

    private void initMapButton(final Item item, final int position, Dialog dialog) {

        final Location location = item.getLocation();
        ImageButton ib_showMap = (ImageButton) dialog.findViewById(R.id.ib_showMap);

        // no location specified.
        if (location == null) {
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

        TextView itemTime = (TextView) dialog.findViewById(R.id.tv_lossTime);
        itemTime.setText(item.getTimeAsString());

        final ImageView itemImage = (ImageView) dialog.findViewById(R.id.iv_itemImage);
        Picasso.with(rootView.getContext()).load(item.getImageUrl()).placeholder(R.drawable.image_unavailable).into(itemImage);

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Item: " + item.getName());
    }


    public void edit(Item item) {
        Intent editItemIntent = new Intent(rootView.getContext(), ReportFormActivity.class);
        editItemIntent.putExtra(Constants.ReportForm.IS_LOST_FORM, item.isLost())
                      .putExtra(Constants.ReportForm.IS_EDIT_FORM, true)
                      .putExtra(Constants.ReportForm.ITEM, item);

        ((Activity) rootView.getContext()).startActivityForResult(editItemIntent, Constants.REQUEST_CODE_REPORT_FORM);
    }

    public int getChosenItemPosition() {
        return chosenItemPosition;
    }

    public void setChosenItemPosition(int position) {
        chosenItemPosition = position;
    }

    public Item getChosenItem() {
        return items.get(chosenItemPosition);
    }

    public class ViewHolder {
        public ImageView itemImage;
        public ImageButton deleteReport;
        TextView itemName;
        TextView timeAdded;
        ImageView lostFoundImage;
    }

}
