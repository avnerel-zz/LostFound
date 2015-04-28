package com.avner.lostfound;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.software.shell.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class FoundListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView lv_myList;

    private View rootView;
    private FloatingActionButton button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_found_list, container, false);

        lv_myList = (ListView) rootView.findViewById(R.id.lv_myList);

        button = (FloatingActionButton) rootView.findViewById(R.id.b_add_item);
        button.setOnClickListener(this);

        List<Item> items = new ArrayList<>();

        items.add(new Item("Ring", "very nice ring", new GregorianCalendar(), new Location("stam"),R.drawable.ring1));

        items.add(new Item("Necklace", "very nice necklace", new GregorianCalendar(), new Location("stam"),R.drawable.necklace1));

        items.add(new Item("Car keys", "my beautiful car keys", new GregorianCalendar(), new Location("stam"),R.drawable.car_keys1));

        items.add(new Item("Earrings", "very nice earrings", new GregorianCalendar(), new Location("stam"),R.drawable.earings1));

        items.add(new Item("Headphones", "lost my beats", new GregorianCalendar(), new Location("stam"),R.drawable.headphones2));


        //ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        LostFoundListAdapter adapter = new LostFoundListAdapter(items,rootView);

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

        ImageButton ib_showMap = (ImageButton) dialog.findViewById(R.id.ib_showMap);
        ib_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootView.getContext(), MapsActivity.class);
                intent.putExtra("Location_LAT", 0); // TODO get current location LAT
                intent.putExtra("Location_LONG", 0); // TODO get current location LONG
                startActivity(intent);
            }
        });

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

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.b_add_item){

            Intent intent = new Intent(rootView.getContext(),ReportForm.class);

            startActivity(intent);
        }
    }

}



