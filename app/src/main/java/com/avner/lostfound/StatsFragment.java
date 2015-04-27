package com.avner.lostfound;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        TextView userName = (TextView) rootView.findViewById(R.id.tv_userName);

        LostFoundApplication app = (LostFoundApplication) getActivity().getApplication();

        userName.setText(app.getUserName());

		return rootView;
	}
}
