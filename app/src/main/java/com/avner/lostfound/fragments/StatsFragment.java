package com.avner.lostfound.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.LostFoundApplication;
import com.avner.lostfound.R;
import com.avner.lostfound.structs.User;
import com.facebook.Profile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private ListView lv_topLosers;

    private ListView lv_topFinders;

    private ImageView userPicture;

    private View rootView;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        setUserDetails();

        updateLists();

        return rootView;
	}

    private void updateLists() {
        lv_topLosers = (ListView) rootView.findViewById(R.id.lv_topLosers);

        lv_topFinders = (ListView) rootView.findViewById(R.id.lv_topFinders);

        List<User> topLosers = new ArrayList<>();

        topLosers.add(new User("Itay",-10,R.drawable.profile4));

        List<User> topFinders = new ArrayList<>();

        topFinders.add(new User("Avner",10,R.drawable.profile1));

        topFinders.add(new User("Oded",10,R.drawable.profile1));

        TopUsersAdapter topFindersAdapter = new TopUsersAdapter(topFinders);

        TopUsersAdapter topLosersAdapter = new TopUsersAdapter(topLosers);

        lv_topLosers.setAdapter(topLosersAdapter);

        lv_topFinders.setAdapter(topFindersAdapter);
    }

    private void setUserDetails() {

        TextView userName = (TextView) rootView.findViewById(R.id.tv_userName);

        LostFoundApplication app = (LostFoundApplication) getActivity().getApplication();

        userName.setText(app.getUserDisplayName());

        userPicture = (ImageView) rootView.findViewById(R.id.iv_profileImage);

        File file = new File(Constants.USER_IMAGE_FILE_PATH);

        if(file.exists()){

            userPicture.setImageBitmap(BitmapFactory.decodeFile(Constants.USER_IMAGE_FILE_PATH));

        }

    }

        public class TopUsersAdapter extends BaseAdapter {

        private List<User> users;

        public TopUsersAdapter(List<User> users) {
            this.users = users;
        }


        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
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
                view = li.inflate(R.layout.list_row_statistics_layout, null);

                viewHolder = new ViewHolder();
                viewHolder.userName = (TextView) view.findViewById(R.id.tv_userName);
                viewHolder.userScore = (TextView) view.findViewById(R.id.tv_userScore);
                viewHolder.userImage = (ImageView) view.findViewById(R.id.iv_rankImage);

                view.setTag(viewHolder);
            }
            else {
                view = convertView;

                viewHolder = (ViewHolder) view.getTag();
            }

            User user = (User)getItem(position);
            // Put the content in the view
            viewHolder.userName.setText(user.getName());
            viewHolder.userScore.setText(""+user.getScore());
            viewHolder.userImage.setImageResource(user.getImageId());

            return view;
        }



        private class ViewHolder {
            TextView userName;
            TextView userScore;
            ImageView userImage;
        }

    }
}