package com.avner.lostfound;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private static final String INFINITY = "\u221E";
    Spinner messageHistory;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        messageHistory = (Spinner)rootView.findViewById(R.id.sp_history_length);
        String[] items = new String[]{"30", "50", "100", INFINITY};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, items);
        messageHistory.setAdapter(adapter);
        messageHistory.setOnItemSelectedListener(this);
		
		return rootView;
	}

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
