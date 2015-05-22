package com.avner.lostfound.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
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
import com.avner.lostfound.activities.ReportFormActivity;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.R;
import com.squareup.picasso.Picasso;

import java.io.File;
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

            initShareButton(view, item, viewHolder.itemImage);
            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        // Put the content in the view
        viewHolder.itemName.setText(item.getName());
        viewHolder.timeAdded.setText(item.timeAgo());
        Picasso.with(rootView.getContext()).load(item.getImageUrl()).into(viewHolder.itemImage);

        return view;
    }

    private void initShareButton(View view, final Item item, final ImageView image) {
        ImageButton shareButton = (ImageButton) view.findViewById(R.id.iv_shareImage);


        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);

                ImageUtils.saveImageToFile(((BitmapDrawable)image.getDrawable()).getBitmap(), "tempImage.png");
                File tempImageFile = new File(Environment.getExternalStorageDirectory() + Constants.APP_IMAGE_DIRECTORY_NAME + "/tempImage.png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempImageFile));
                shareIntent.putExtra(Intent.EXTRA_TEXT, item.getDescription());
                shareIntent.setType("*/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
    }

}
