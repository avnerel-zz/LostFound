package com.avner.lostfound.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.avner.lostfound.R;
import com.avner.lostfound.activities.MainActivity;
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.activities.ViewLocationActivity;
import com.avner.lostfound.structs.Item;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OpenItemsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{

    private List<Item> items;

    private View rootView;
    private int chosenItemPosition;

    public OpenItemsAdapter(List<Item> items, View rootView) {
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
            view = li.inflate(R.layout.list_row_open_listing_layout, null);

            viewHolder = new ViewHolder();
            viewHolder.itemName = (TextView) view.findViewById(R.id.tv_itemListingName);
            viewHolder.timeAdded = (TextView) view.findViewById(R.id.tv_itemListingAge);
            viewHolder.itemImage = (ImageView) view.findViewById(R.id.iv_itemListingImage);
            viewHolder.lostFound = (TextView) view.findViewById(R.id.tv_lost_found);

            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        // Put the content in the view
        viewHolder.itemName.setText(item.getName());
        viewHolder.timeAdded.setText(item.timeAgo());
        if(item.isLost()){
            viewHolder.lostFound.setText(Constants.LOST_SHORTCUT);
            viewHolder.lostFound.setTextColor(Color.RED);
        }else{
            viewHolder.lostFound.setText(Constants.FOUND_SHORTCUT);
            viewHolder.lostFound.setTextColor(Color.BLUE);
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
            public void done(ParseObject parseItem, ParseException e) {
                if(e!=null){
                    Log.e(Constants.LOST_FOUND_TAG, "item" + item.getName()
                            + " had already been removed from local data store. " + e.getLocalizedMessage());
                }
                parseItem.put(Constants.ParseReport.ALIVE, false);
                parseItem.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(Constants.LOST_FOUND_TAG, "problem removing item" + item.getName()
                                    + " from local data store. " + e.getLocalizedMessage());
                            return;
                        }
                        ((MainActivity) rootView.getContext()).updateLocalDataInFragments();
                        progressDialog.dismiss();
                        deleteFromParse(item);
                    }
                });
            }
        });

    }

    private void deleteFromParse(Item item) {
        ParseQuery<ParseObject> deleteQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        deleteQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, item.getId());
        deleteQuery.getFirstInBackground(new GetCallback<ParseObject>(){
            @Override
            public void done(ParseObject parseItem, ParseException e) {
                if(parseItem!= null){
                    parseItem.put(Constants.ParseReport.ALIVE, false);
                    parseItem.saveInBackground();
                }else{
                    Log.e(Constants.LOST_FOUND_TAG, "item isn't in the parse data store. WTF???");
                }
            }
        });
    }


//    public void share(final Item item) {
//
//            final Intent shareIntent = new Intent();
//            shareIntent.setAction(Intent.ACTION_SEND);
//
//            //TODO save image bitmap so don't need to get it from server.
//            ImageUtils.saveImageToFile(item.getImageUrl(), "tempImage.png");
//            File tempImageFile = new File(Environment.getExternalStorageDirectory() + Constants.APP_IMAGE_DIRECTORY_NAME + "/tempImage.png");
//            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempImageFile));
//            shareIntent.putExtra(Intent.EXTRA_TEXT, item.getShareDescription());
//            shareIntent.setType("*/*");
//            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                final List<ResolveInfo> activities = rootView.getContext().getPackageManager().queryIntentActivities(shareIntent, 0);
//
//                List<String> appNames = new ArrayList<String>();
//                for (ResolveInfo info : activities) {
//                    appNames.add(info.loadLabel(rootView.getContext().getPackageManager()).toString());
//                }
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
//                builder.setTitle("Complete Action using...");
//                builder.setItems(appNames.toArray(new CharSequence[appNames.size()]), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int activity) {
//                        ResolveInfo info = activities.get(activity);
//                        if (info.activityInfo.packageName.toLowerCase().contains("facebook")) {
//       //                Facebook was chosen
//                            ShareLinkContent content = new ShareLinkContent.Builder().setContentTitle("LostFound")
//                                                                    .setImageUrl(Uri.parse(item.getImageUrl()))
//                                                                    .setContentDescription(item.getDescription()).build();
////                            SharePhoto photo = new SharePhoto.Builder().setBitmap(((BitmapDrawable) image.getDrawable()).getBitmap()).build();
////                            SharePhotoContent content = new SharePhotoContent.Builder().setRef("avner")
////                                    .oContent content = new SharePhotoContent.Builder().setRef("avner")
////                                    .addPhoto(photo).build();
//                            ShareDialog shareDialog = new ShareDialog((android.app.Activity) rootView.getContext());
//                            shareDialog.show(content);
//                        } else  {
//                            // start the selected activity
//                            shareIntent.setPackage(info.activityInfo.packageName);
//                            rootView.getContext().startActivity(shareIntent);
//                        }
//
//                    }
//                });
//                AlertDialog alert = builder.create();
//                alert.show();
////
////                rootView.getContext().startActivity(Intent.createChooser(shareIntent, "Share to..."));
////            }
////        });
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Item item = (Item) getItem(position);
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
        itemTime.setText(item.getTimeAsString());

        final ImageView itemImage = (ImageView) dialog.findViewById(R.id.iv_itemImage);
        Picasso.with(rootView.getContext()).load(item.getImageUrl()).placeholder(R.drawable.image_unavailable).into(itemImage);

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Item: " + item.getName());
    }


    public void edit(Item item){

        Intent editItemIntent = new Intent(rootView.getContext(), ReportFormActivity.class);
        editItemIntent.putExtra(Constants.ReportForm.IS_LOST_FORM, item.isLost());
        editItemIntent.putExtra(Constants.ReportForm.IS_EDIT_FORM, true);
        editItemIntent.putExtra(Constants.ReportForm.ITEM, item);

        ((Activity)rootView.getContext()).startActivityForResult(editItemIntent, Constants.REQUEST_CODE_REPORT_FORM);
    }

    public void setChosenItemPosition(int position) {
        chosenItemPosition = position;
    }

    public int getChosenItemPosition() {
        return chosenItemPosition;
    }

    public Item getChosenItem() {
        return items.get(chosenItemPosition);
    }

//    public void remove(Item selectedItem) {
//
//        //TODO spinner while trying to remove.
//        items.remove(selectedItem);
//        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
//        query.whereEqualTo(Constants.ParseQuery.OBJECT_ID, selectedItem.getId());
//        query.getFirstInBackground(new GetCallback<ParseObject>() {
//            @Override
//            public void done(ParseObject parseObject, ParseException e) {
//                if(e == null){
//                    parseObject.deleteInBackground();
//                }
//            }
//        });
//
//        ParseQuery<ParseObject> localQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_CONVERSATION);
//        localQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, selectedItem.getId());
//        localQuery.fromLocalDatastore();
//        try {
//            ParseObject parseItem = localQuery.getFirst();
//            parseItem.unpin();
//            notifyDataSetChanged();
//        } catch (ParseException e) {
//            e.printStackTrace();
//            Log.e(Constants.LOST_FOUND_TAG, "didn't find item to delete or couldn't delete.");
//        }
//    }

    public class ViewHolder {
        TextView itemName;
        public ImageView itemImage;
        TextView timeAdded;
        TextView lostFound;
        public ImageButton deleteReport;
    }

}
