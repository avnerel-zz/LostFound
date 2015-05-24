package com.avner.lostfound.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.avner.lostfound.Constants;
import com.avner.lostfound.utils.ImageUtils;
import com.avner.lostfound.LostFoundApplication;
import com.avner.lostfound.R;

import java.io.File;
import java.io.FileNotFoundException;

public class SettingsActivity extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final String INFINITY = "\u221E";
    Spinner messageHistory;

    ImageButton userPhotoImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        messageHistory = (Spinner)findViewById(R.id.sp_history_length);

        String[] items = new String[]{"30", "50", "100", INFINITY};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.message_history_spinner_item, items);

        messageHistory.setAdapter(adapter);

        messageHistory.setOnItemSelectedListener(this);

        TextView userName = (TextView) findViewById(R.id.tv_userName);

        LostFoundApplication app = (LostFoundApplication) getApplication();

        userName.setText(app.getUserEmail());

        userPhotoImageButton = (ImageButton) findViewById(R.id.ib_user_image);

        File file = new File(Constants.USER_IMAGE_FILE_PATH);

        if(file.exists()){

            userPhotoImageButton.setImageBitmap(BitmapFactory.decodeFile(Constants.USER_IMAGE_FILE_PATH));

        }

        userPhotoImageButton.setOnClickListener(this);


    }

//    private void selectImage() {
//        final CharSequence[] items = { "Take Photo", "Choose from Library",
//                "Cancel" };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Add Photo!");
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int item) {
//                if (items[item].equals("Take Photo")) {
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(intent, Constants.REQUEST_CODE_CAMERA);
//                } else if (items[item].equals("Choose from Library")) {
//                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    intent.setType("image/*");
//                    startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.REQUEST_CODE_SELECT_FILE);
//                } else if (items[item].equals("Cancel")) {
//                    dialog.dismiss();
//                }
//            }
//        });
//        builder.show();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == Constants.REQUEST_CODE_CAMERA) {
            Bitmap image_from_camera = (Bitmap) data.getExtras().get("data");
            ImageUtils.saveImageToFile(image_from_camera, Constants.USER_IMAGE_FILE_NAME);
            userPhotoImageButton.setImageBitmap(image_from_camera);

        } else if (requestCode == Constants.REQUEST_CODE_SELECT_FILE) {

            try{
                Bitmap imageFromGallery = ImageUtils.decodeUri(getContentResolver(),data.getData());
                ImageUtils.saveImageToFile(imageFromGallery, Constants.USER_IMAGE_FILE_NAME);
                userPhotoImageButton.setImageBitmap(imageFromGallery);

            } catch (FileNotFoundException e) {
                Log.e(Constants.LOST_FOUND_TAG, "user image file from gallery not found. WTF???");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        ImageUtils.selectItemImage(this);
    }
}
