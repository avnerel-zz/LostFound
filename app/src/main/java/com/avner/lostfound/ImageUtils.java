package com.avner.lostfound;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

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
     * @param thumbnail
     * @param fileName
     */
    public static void saveImageToFile(Bitmap thumbnail, String fileName) {

        saveImageToFile(thumbnail, Environment.getExternalStorageDirectory() + Constants.APP_IMAGE_DIRECTORY_NAME, fileName);

    }

    public static void saveImageToFile(Bitmap image, String directoryPath, String fileName) {

        // make dir for the app if it isn't already created.
        boolean success = (new File(directoryPath)).mkdir();
        if (!success)
        {
            Log.d("my_tag", "directory " + directoryPath + " already created");
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

//            FileOutputStream stream = new FileOutputStream(Constants.USER_IMAGE_FILE_PATH);
//
//            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
//            byte[] byteArray = outStream.toByteArray();
//
//            stream.write(byteArray);
//            stream.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;


    }
}
