package com.avner.lostfound;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class FoundListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView lv_myList;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_found_list, container, false);

        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> imageResourceNumbers = new ArrayList<>();

        lv_myList = (ListView) rootView.findViewById(R.id.lv_myList);

        List<Item> items = new ArrayList<>();

        Item item = new Item("Bottle", "water bottle", new GregorianCalendar(), new Location("stam"));

        items.add(item);

        //ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        LostListAdapter adapter = new LostListAdapter(items);

        lv_myList.setClickable(true);

        lv_myList.setAdapter(adapter);

        lv_myList.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_item_details_layout);

        Item item = (Item) parent.getItemAtPosition(position);
        if (null == item) {
            Log.d("DEBUG", "Failed to retrieve item from adapter list, at position " + position);
            return;
        }

        setDialogContents(dialog, item);

        dialog.show();
    }

    private void setDialogContents(Dialog dialog, Item item) {
        TextView itemLocation = (TextView) dialog.findViewById(R.id.tv_location);
        itemLocation.setText(item.getLocationString());
        itemLocation.setMaxLines(2);

        TextView itemTime = (TextView) dialog.findViewById(R.id.tv_lossTime);
        itemTime.setText(item.timeAgo());

        ImageView itemImage = (ImageView) dialog.findViewById(R.id.iv_itemImage);
        itemImage.setImageResource(item.getImage());

        TextView itemDescription = (TextView) dialog.findViewById(R.id.tv_descriptionContent);
        itemDescription.setText(item.getDescription());

        dialog.setTitle("Found Item: " + item.getName());
    }


    public class LostListAdapter extends BaseAdapter {

        private List<Item> items;

        public LostListAdapter(List<Item> items) {
            this.items = items;
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
            ViewHolder viewHolder;

            Log.d("MY_TAG", "Position: " + position);

            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.list_row_lost_item, null);

                viewHolder = new ViewHolder();
                viewHolder.itemName = (TextView) view.findViewById(R.id.tv_item_name);
                viewHolder.description = (TextView) view.findViewById(R.id.tv_item_description);
                viewHolder.timeAdded = (TextView) view.findViewById(R.id.tv_number_of_days_ago);
                viewHolder.locationAdded = (TextView) view.findViewById(R.id.tv_near_address);
                viewHolder.itemImage = (ImageView) view.findViewById(R.id.iv_itemListingImage);

                view.setTag(viewHolder);
            }
            else {
                view = convertView;

                viewHolder = (ViewHolder) view.getTag();
            }

            Item item = (Item)getItem(position);
            // Put the content in the view
            viewHolder.itemName.setText(item.getName());
            viewHolder.description.setText(item.getDescription());

            viewHolder.timeAdded.setText("" + item.getDiff() + " ");
//            viewHolder.locationAdded.setText(item.getLocation().describeContents());
            viewHolder.itemImage.setImageResource(item.getImage());

            return view;
        }



        private class ViewHolder {
            TextView itemName;
            TextView description;
            ImageView itemImage;
            TextView timeAdded;
            TextView locationAdded;
        }

    }

}


/*        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> imageResourceNumbers = new ArrayList<>();

        names.add(getResources().getString(R.string.pizza));

        imageResourceNumbers.add(R.drawable.pizza);
//        imageResourceNumbers.add(R.mipmap.ic_pizza);

        names.add(getResources().getString(R.string.hamburger));

        imageResourceNumbers.add(R.drawable.hamburger);
//        imageResourceNumbers.add(R.mipmap.ic_hamburger);

        names.add(getResources().getString(R.string.french_fries));

        imageResourceNumbers.add(R.drawable.french_fries);
//        imageResourceNumbers.add(R.mipmap.ic_french_fries);

        names.add(getResources().getString(R.string.spaghetti));

        imageResourceNumbers.add(R.drawable.spaghetti);
//        imageResourceNumbers.add(R.mipmap.ic_spaghetti);

        names.add(getResources().getString(R.string.grass));

        imageResourceNumbers.add(R.drawable.grass);
//        imageResourceNumbers.add(R.mipmap.ic_grass);

        lv_myList = (ListView) findViewById(R.id.lv_myList);

        //ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        SheepListAdapter adapter = new SheepListAdapter(names, imageResourceNumbers);

        lv_myList.setClickable(true);

        lv_myList.setAdapter(adapter);

        lv_myList.setOnItemClickListener(adapter);

    }

*/

