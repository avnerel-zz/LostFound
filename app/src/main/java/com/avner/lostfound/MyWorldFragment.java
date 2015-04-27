package com.avner.lostfound;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseUser;

public class MyWorldFragment extends Fragment implements View.OnClickListener {

    private ImageButton settingsButton;
    private ImageButton messagesButton;
    private ImageButton logOutButton;
    private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_my_world, container, false);

        messagesButton = (ImageButton)rootView.findViewById(R.id.b_messages);
        messagesButton.setOnClickListener(this);

        settingsButton = (ImageButton)rootView.findViewById(R.id.b_settings);
        settingsButton.setOnClickListener(this);

        logOutButton = (ImageButton)rootView.findViewById(R.id.b_log_out);
        logOutButton.setOnClickListener(this);


		return rootView;
	}


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.b_settings){

            Intent intent = new Intent(rootView.getContext(),SettingsActivity.class);

            startActivity(intent);

        }else if(v.getId() == R.id.b_messages){

            Intent intent = new Intent(rootView.getContext(),UsersListActivity.class);

            startActivity(intent);
        }

        if(v.getId() == R.id.b_log_out){

            ParseUser.logOut();

            Intent intent = new Intent(rootView.getContext(),LoginActivity.class);

            startActivity(intent);

        }
    }
}
