package com.avner.lostfound;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class MyWorldFragment extends Fragment implements View.OnClickListener {

    private ImageButton settingsButton;
    private ImageButton messagesButton;
    private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_new_my_world, container, false);

        messagesButton = (ImageButton)rootView.findViewById(R.id.b_messages);
        messagesButton.setOnClickListener(this);

        settingsButton = (ImageButton)rootView.findViewById(R.id.b_settings);
        settingsButton.setOnClickListener(this);


		return rootView;
	}


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.b_settings){

            Intent intent = new Intent(rootView.getContext(),SettingsActivity.class);

            startActivity(intent);

        }else if(v.getId() == R.id.b_messages){

        }
    }
}
