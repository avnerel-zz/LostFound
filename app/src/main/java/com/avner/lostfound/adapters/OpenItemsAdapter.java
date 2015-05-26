package com.avner.lostfound.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.ShareActionProvider;
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
import com.avner.lostfound.utils.ImageUtils;
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.R;
import com.facebook.FacebookSdk;
import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by avner on 28/04/2015.
 */
public class OpenItemsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private List<Item> items;

    private View rootView;

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

            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        initShareButton(view, item, viewHolder.itemImage);
        initDeleteButton(view,item, viewHolder);
        // Put the content in the view
        viewHolder.itemName.setText(item.getName());
        viewHolder.timeAdded.setText(item.timeAgo());
        Picasso.with(rootView.getContext()).load(item.getImageUrl()).into(viewHolder.itemImage);

        return view;
    }

    private void initDeleteButton(View view, final Item item, ViewHolder viewHolder) {
        viewHolder.deleteReport = (ImageButton) view.findViewById(R.id.ib_deleteReport);
        viewHolder.deleteReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.LOST_FOUND_TAG, "pressed to delete item " + item.getName());
                ParseQuery<ParseObject> deleteQuery = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
                deleteQuery.whereEqualTo(Constants.ParseQuery.OBJECT_ID, item.getId());
                deleteQuery.getFirstInBackground(new GetCallback<ParseObject>(){
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        parseObject.deleteInBackground();
                    }
                });

            }
        });
    }

    private void initShareButton(View view, final Item item, final ImageView image) {
        final ImageButton shareButton = (ImageButton) view.findViewById(R.id.iv_shareImage);


        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);

                ImageUtils.saveImageToFile(((BitmapDrawable)image.getDrawable()).getBitmap(), "tempImage.png");
                File tempImageFile = new File(Environment.getExternalStorageDirectory() + Constants.APP_IMAGE_DIRECTORY_NAME + "/tempImage.png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempImageFile));
                shareIntent.putExtra(Intent.EXTRA_TEXT, item.getDescription());
                shareIntent.setType("*/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

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
                            // Facebook was chosen
//                            ShareLinkContent content = new ShareLinkContent.Builder().setContentTitle("LostFound")
//                                                                    .setImageUrl(Uri.parse(item.getImageUrl()))
//                                                                    .setContentDescription(item.getDescription()).build();
//                            SharePhoto photo = new SharePhoto.Builder().setBitmap(((BitmapDrawable) image.getDrawable()).getBitmap()).build();
//                            SharePhotoContent content = new SharePhotoContent.Builder().setRef("avner")
//                                    .addPhoto(photo).build();
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

                rootView.getContext().startActivity(Intent.createChooser(shareIntent, "Share to..."));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent editItemIntent = new Intent(rootView.getContext(), ReportFormActivity.class);
        Item item = items.get(position);
        editItemIntent.putExtra(Constants.ReportForm.IS_LOST_FORM, item.isLost());
        editItemIntent.putExtra(Constants.ReportForm.IS_EDIT_FORM, true);
        editItemIntent.putExtra(Constants.ReportForm.ITEM, item);

        rootView.getContext().startActivity(editItemIntent);

    }

    private class ViewHolder {
        TextView itemName;
        ImageView itemImage;
        TextView timeAdded;
        public ImageButton deleteReport;
    }

}
