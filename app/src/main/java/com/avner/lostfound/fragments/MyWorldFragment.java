package com.avner.lostfound.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.avner.lostfound.activities.MainActivity;
import com.avner.lostfound.adapters.OpenItemsAdapter;
import com.avner.lostfound.structs.Item;
import com.avner.lostfound.utils.ImageUtils;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyWorldFragment extends Fragment {

    private View rootView;
    private TextView tv_openListingsNumber;
    private OpenItemsAdapter myOpenListingsAdapter;
    private List<Item> items;
    private ShareActionProvider shareActionProvider;
    private ActionMode actionMode;
    private ListView lv_openListings;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_my_world, container, false);
        tv_openListingsNumber = (TextView) rootView.findViewById(R.id.tv_openListingsNumber);
        initOpenListings();
        return rootView;
	}

    private void initOpenListings() {

        lv_openListings = (ListView) rootView.findViewById(R.id.lv_openListings);
        setContextualBar(lv_openListings);

        items = new ArrayList<>();
        myOpenListingsAdapter = new OpenItemsAdapter(items, rootView);
        updateMyItems(items);


        lv_openListings.setAdapter(myOpenListingsAdapter);
        lv_openListings.setOnItemClickListener(myOpenListingsAdapter);
    }

    private void updateMyItems(final List<Item> items) {
        // get all my lost items
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.ParseObject.PARSE_LOST);
        query.fromLocalDatastore();
        query.orderByDescending(Constants.ParseQuery.CREATED_AT);
        query.whereEqualTo(Constants.ParseReport.USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.whereEqualTo(Constants.ParseReport.IS_ALIVE, true);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> itemsList, com.parse.ParseException e) {
                if (e == null) {
                    items.clear();
                    for (int i = 0; i < itemsList.size(); i++) {
                        convertParseListToItemList(itemsList, items);
                    }
                    myOpenListingsAdapter.notifyDataSetChanged();
                    tv_openListingsNumber.setText(String.valueOf(itemsList.size()));
                }
            }
        });
    }

    private void convertParseListToItemList(List<ParseObject> itemsList, List<Item> items) {

        items.clear();
        Item item = null;
        for (ParseObject parseItem : itemsList){
            try {
                if (null != parseItem) {
                    item = new Item(parseItem);
                    if (null == item) {
                        Log.d(Constants.LOST_FOUND_TAG, "item created as NULL");
                    }
                    items.add(item);
                }
            } catch (NullPointerException e) {
                Log.d(Constants.LOST_FOUND_TAG, "caught NullPointerException when adding an item (item == null?" + (item == null) + ")");
                Log.d(Constants.LOST_FOUND_TAG, "based on parseItem: " + parseItem.toString());
            }
        }
    }

    private void setContextualBar(final ListView usersListView) {
        usersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        // Capture ListView item click
        usersListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectCount = usersListView.getCheckedItemCount();

                if(selectCount > 1){

                    SparseBooleanArray checkArr = usersListView.getCheckedItemPositions();
                    for(int i=0;i<usersListView.getCount();i++){

                        //check item is checked and not the last item
                        if(checkArr.get(i) && position != i){
                            usersListView.setItemChecked(i, false);
                            break;
                        }
                    }
                }
                myOpenListingsAdapter.setChosenItemPosition(position);
                initShareIntent(position);
            }
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                Item item_selected = (Item) myOpenListingsAdapter.getItem(myOpenListingsAdapter.getChosenItemPosition());
                switch (item.getItemId()) {
                    case R.id.delete:
                        myOpenListingsAdapter.remove(item_selected);
                        break;
                    case R.id.edit:
                        myOpenListingsAdapter.edit(item_selected);
                        break;
                    default:
                }
                // Close CAB
                mode.finish();
                return true;

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_my_world_contextual, menu);
                ((MainActivity) rootView.getContext()).setActionMode(mode);
                setActionMode(mode);
                initContextMenu(menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                ((MainActivity)rootView.getContext()).setActionMode(null);
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

    private void initShareIntent(int position) {

        // save image of item to file.
//        ImageView imageView = ((OpenItemsAdapter.ViewHolder) lv_openListings.getChildAt(position).getTag()).itemImage;
//        Bitmap image = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//        ImageUtils.saveImageToFile(image, "tempImage.png");

        // create intent for sharing, if user chooses to share.
        Item chosenItem = myOpenListingsAdapter.getChosenItem();
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        File tempImageFile = new File(Environment.getExternalStorageDirectory() + Constants.APP_IMAGE_DIRECTORY_NAME + "/tempImage.png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempImageFile));
        shareIntent.putExtra(Intent.EXTRA_TEXT, chosenItem.getShareDescription());
        shareActionProvider.setShareIntent(shareIntent);

    }

    private void initContextMenu(Menu menu) {

        shareActionProvider = (ShareActionProvider) menu.findItem(R.id.share).getActionProvider();
        shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {

            @Override
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                final String packageName = intent.getComponent().getPackageName();
                Item chosenItem = myOpenListingsAdapter.getChosenItem();
                int chosenItemPosition = myOpenListingsAdapter.getChosenItemPosition();
                // save image of item to file.
                ImageView imageView = ((OpenItemsAdapter.ViewHolder) lv_openListings.getChildAt(chosenItemPosition).getTag()).itemImage;
                Bitmap image = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                ImageUtils.saveImageToFile(image, "tempImage.png");

                if (packageName.equals("com.facebook.katana")) {

                    // save description to clipboard because facebook don't let you upload a status.
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) rootView.getContext()
                            .getSystemService(rootView.getContext().CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData
                            .newPlainText("share description", chosenItem.getShareDescription());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(rootView.getContext(), "Share description copied to clipboard, paste it in facebook status.", Toast.LENGTH_LONG).show();
                }
                actionMode.finish();
                return false;
            }

        });
    }

    public void updateData() {
        if(items == null){
            return;
        }
        updateMyItems(items);
    }

    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }
}
