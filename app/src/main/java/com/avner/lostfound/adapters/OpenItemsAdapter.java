package com.avner.lostfound.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.R;

import java.io.File;
import java.util.List;

/**
 * Created by avner on 28/04/2015.
 */
public class OpenItemsAdapter extends BaseAdapter {

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

//        Log.d("MY_TAG", "Position: " + position);

        final Item item = (Item)getItem(position);

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.list_row_open_listing_layout, null);

            viewHolder = new ViewHolder();
            viewHolder.itemName = (TextView) view.findViewById(R.id.tv_itemListingName);
            viewHolder.timeAdded = (TextView) view.findViewById(R.id.tv_itemListingAge);
            viewHolder.itemImage = (ImageView) view.findViewById(R.id.iv_itemListingImage);

            initShareButton(view, item);

            view.setTag(viewHolder);
        }
        else {
            view = convertView;

            viewHolder = (ViewHolder) view.getTag();
        }

        // Put the content in the view
        viewHolder.itemName.setText(item.getName());

        viewHolder.timeAdded.setText("" + item.getDiff() + " days ago");
        viewHolder.itemImage.setImageResource(item.getImage());

        return view;
    }

    private void initShareButton(View view, final Item item) {
        ImageButton shareButton = (ImageButton) view.findViewById(R.id.iv_shareImage);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
//                    Uri uriToImage = ResourceToUri(rootView.getContext(),item.getImage());
                Uri uriToImage = Uri.fromFile(new File(Constants.USER_IMAGE_FILE_PATH));
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
                shareIntent.putExtra(Intent.EXTRA_TEXT, item.getDescription());
                shareIntent.setType("*/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                rootView.getContext().startActivity(Intent.createChooser(shareIntent, "Share to..."));
            }

            public Uri ResourceToUri (Context context,int resID) {
                return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        context.getResources().getResourcePackageName(resID) + '/' +
                        context.getResources().getResourceTypeName(resID) + '/' +
                        context.getResources().getResourceEntryName(resID) );
            }
        });
    }

    private class ViewHolder {
        TextView itemName;
        ImageView itemImage;
        TextView timeAdded;
    }

}
