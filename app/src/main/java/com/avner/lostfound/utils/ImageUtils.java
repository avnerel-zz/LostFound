package com.avner.lostfound.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.avner.lostfound.Constants;
import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by avner on 20/05/2015.
 */
public class ImageUtils {


    /**
     * saves to default directory - "lostfound"
     * @param image
     * @param fileName
     */
    public static void saveImageToFile(Bitmap image, String fileName) {

        saveImageToFile(image, Environment.getExternalStorageDirectory() + Constants.APP_IMAGE_DIRECTORY_NAME, fileName);

    }

    public static void saveImageToFile(String imageUri, String fileName){
        Bitmap image = decodeRemoteUrl(imageUri);
        saveImageToFile(image,fileName);
    }

    public static void saveImageToFile(Bitmap image, String directoryPath, String fileName) {

        // make dir for the app if it isn't already created.
        boolean success = (new File(directoryPath)).mkdir();
        if (!success)
        {
            Log.d(Constants.LOST_FOUND_TAG, "directory " + directoryPath + " already created");
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 90, bytes);
        File destination = new File(directoryPath + "/" + fileName);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ParseFile getImageAsParseFile(String imageName, Bitmap image){

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 90, bytes);

        return new ParseFile(imageName, bytes.toByteArray());
    }

    public static Bitmap decodeUri(ContentResolver resolver, Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(resolver.openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(resolver.openInputStream(selectedImage), null, o2);

    }

    public static Bitmap decodeRemoteUrl(final String imageUrl) {

        URL imageURL;
        Bitmap bitmap = null;
        // fetch photo and save it to dir.
        try {
            imageURL = new URL(imageUrl);
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void selectItemImage(final Activity activity) {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activity.startActivityForResult(intent, Constants.REQUEST_CODE_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    activity.startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.REQUEST_CODE_SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

}
