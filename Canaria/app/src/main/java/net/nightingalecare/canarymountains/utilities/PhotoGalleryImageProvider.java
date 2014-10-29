/*
 * Copyright (c) 2014 Rex St. John on behalf of AirPair.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.nightingalecare.canarymountains.utilities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.nightingalecare.canarymountains.adapter.items.PhotoItem;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This is a helper utility which automatically fetches paths to full size and thumbnail sized gallery images.
 *
 * Created by Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class PhotoGalleryImageProvider {

    // Consts
    public static final int IMAGE_RESOLUTION = 5;

    // Buckets where we are fetching images from.
    public static final String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID =
            getBucketId(CAMERA_IMAGE_BUCKET_NAME);

    public static List<PhotoItem> getAlbumThumbnails(Context context, int limit){

        final String[] projection = {MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID};
        Cursor thumbnailsCursor = context.getContentResolver().query( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, // Which columns to return
                null,       // Return all rows
                null,
                null);

        // Extract the proper column thumbnails
        int thumbnailColumnIndex = thumbnailsCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
        ArrayList<PhotoItem> result = new ArrayList<PhotoItem>(thumbnailsCursor.getCount());
        int i = 0;

        if (thumbnailsCursor.moveToLast()) {
            do {
                // Generate a tiny thumbnail version.
                int thumbnailImageID = thumbnailsCursor.getInt(thumbnailColumnIndex);
                String thumbnailPath = thumbnailsCursor.getString(thumbnailImageID);
                Uri thumbnailUri = Uri.parse(thumbnailPath);

                HashMap<String,String> attributes = new HashMap<String,String>();
                // Create the list item.
                if(getImageAttributes(attributes,thumbnailsCursor,context)) {
                    String fullImageUri = (String) attributes.get("uri");
                    String dateTaken = (String) attributes.get("date_taken");
                    String longitude = (String) attributes.get("longitude");
                    String latitude = (String) attributes.get("latitude");
                    PhotoItem newItem = new PhotoItem(thumbnailUri, Uri.parse(fullImageUri), dateTaken, longitude + "," + latitude);
                    result.add(newItem);
                    Log.d("Test", "thumnailURI" + thumbnailUri.toString());
                    i++;
                    if (i == (limit)) {
                        thumbnailsCursor.close();
                        return result;
                    }
                }
            } while (thumbnailsCursor.moveToPrevious());
        }
        thumbnailsCursor.close();
        return result;
    }


    public static List<PhotoItem> getImagesForDay(Context context, int limit, Date filterDate){

        final String[] projection = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.LATITUDE};
        Cursor imagesCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, // Which columns to return
                null,       // Return all rows
                null,
                null);

        ArrayList<PhotoItem> result = new ArrayList<PhotoItem>(imagesCursor.getCount());
        int i = 0;

        if (imagesCursor.moveToLast()) {
            do {

                int columnIndex = imagesCursor.getColumnIndex(projection[0]);
                String fullImageUri = imagesCursor.getString(columnIndex);
                columnIndex = imagesCursor.getColumnIndex(projection[2]);
                String dateTakenStr = imagesCursor.getString(columnIndex);
                columnIndex = imagesCursor.getColumnIndex(projection[3]);
                String longitude = (String) imagesCursor.getString(columnIndex);
                columnIndex = imagesCursor.getColumnIndex(projection[4]);
                String latitude = (String) imagesCursor.getString(columnIndex);


                Date dateTaken = new Date(Long.parseLong(dateTakenStr));

                //formatting date in Java using SimpleDateFormat
                SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
                String filterDateFormat = DATE_FORMAT.format(filterDate);
                String dateTakenFormat = DATE_FORMAT.format(dateTaken);

                if(filterDateFormat.equals(dateTakenFormat)) {
                    PhotoItem newItem = new PhotoItem(Uri.parse(fullImageUri), Uri.parse(fullImageUri), dateTakenStr, longitude + "," + latitude);
                    result.add(newItem);
                }

                Log.d("Test", "imagesURI" + fullImageUri.toString());
                i++;
                if (i == (limit)) {
                        imagesCursor.close();
                        return result;
                }
            } while (imagesCursor.moveToPrevious());
        }
        imagesCursor.close();
        return result;
    }


    public static List<PhotoItem> getAlbumThumbnailsForToday(Context context, int limit){

        final String[] projection = {MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID};
        Cursor thumbnailsCursor = context.getContentResolver().query( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, // Which columns to return
                null,       // Return all rows
                null,
                null);

        // Extract the proper column thumbnails
        int thumbnailColumnIndex = thumbnailsCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
        ArrayList<PhotoItem> result = new ArrayList<PhotoItem>(thumbnailsCursor.getCount());
        int i = 0;


        if (thumbnailsCursor.moveToLast()) {
            do {
                // Generate a tiny thumbnail version.
                int thumbnailImageID = thumbnailsCursor.getInt(thumbnailColumnIndex);
                String thumbnailPath = thumbnailsCursor.getString(thumbnailImageID);
                Uri thumbnailUri = Uri.parse(thumbnailPath);
                Uri fullImageUri = getUriToFullImageForToday(thumbnailsCursor, context);

                // Create the list item.
                if(!fullImageUri.getPath().equals(""))
                {
                    PhotoItem newItem = new PhotoItem(thumbnailUri, fullImageUri);
                    result.add(newItem);
                }
                i++;
                if(i == (limit))
                {
                    break;
                }
            } while (thumbnailsCursor.moveToPrevious());
        }
        thumbnailsCursor.close();
        return result;
    }

    /**
     * Get the path to the full image for a given thumbnail.
     */
    private static Uri getUriToFullImageForToday(Cursor thumbnailsCursor, Context context) {
        Date date = new Date();
        return getUriToFullImageForDay(thumbnailsCursor, context, date);
    }


    /**
     * Get the path to the full image for a given thumbnail.
     */
    private static Uri getUriToFullImageForDay(Cursor thumbnailsCursor, Context context, Date filterDate){
        String imageId = thumbnailsCursor.getString(thumbnailsCursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID));

        // Request image related to this thumbnail
        String[] filterColumn = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN };
        Cursor imagesCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filterColumn, MediaStore.Images.Media._ID + "=?", new String[]{imageId}, null);

        if (imagesCursor != null && imagesCursor.moveToFirst()) {
            int columnIndex = imagesCursor.getColumnIndex(filterColumn[0]);
            String filePath = imagesCursor.getString(columnIndex);
            columnIndex = imagesCursor.getColumnIndex(filterColumn[1]);
            String dateTakenStr = imagesCursor.getString(columnIndex);
            imagesCursor.close();

            Date dateTaken = new Date(Long.parseLong(dateTakenStr));

            //formatting date in Java using SimpleDateFormat
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
            String filterDateFormat = DATE_FORMAT.format(filterDate);
            String dateTakenFormat = DATE_FORMAT.format(dateTaken);

            if(filterDateFormat.equals(dateTakenFormat)) {
                return Uri.parse(filePath);
            } else {
                return Uri.parse("");
            }
        } else {
            imagesCursor.close();
            return Uri.parse("");
        }
    }

    /**
     * Get the path to the full image for a given thumbnail.
     */
    private static Uri uriToFullImage(Cursor thumbnailsCursor, Context context){
        String imageId = thumbnailsCursor.getString(thumbnailsCursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID));

        // Request image related to this thumbnail
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor imagesCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, MediaStore.Images.Media._ID + "=?", new String[]{imageId}, null);

        if (imagesCursor != null && imagesCursor.moveToFirst()) {
            int columnIndex = imagesCursor.getColumnIndex(filePathColumn[0]);
            String filePath = imagesCursor.getString(columnIndex);
            imagesCursor.close();
            return Uri.parse(filePath);
        } else {
            imagesCursor.close();
            return Uri.parse("");
        }
    }

    /**
     * Get the path to the full image for a given thumbnail.
     */
    private static boolean getImageAttributes(HashMap <String, String> attributes, Cursor thumbnailsCursor, Context context){

        String imageId = thumbnailsCursor.getString(thumbnailsCursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID));

        attributes.clear();
        // Request image related to this thumbnail
        String[] filePathColumn = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.LATITUDE};
        Cursor imagesCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, MediaStore.Images.Media._ID + "=?", new String[]{imageId}, null);

        if (imagesCursor != null && imagesCursor.moveToFirst()) {
            int columnIndex = imagesCursor.getColumnIndex(filePathColumn[0]);
            attributes.put("uri", imagesCursor.getString(columnIndex));
            columnIndex = imagesCursor.getColumnIndex(filePathColumn[1]);
            attributes.put("date_taken", imagesCursor.getString(columnIndex));
            columnIndex = imagesCursor.getColumnIndex(filePathColumn[2]);
            attributes.put("longitude", imagesCursor.getString(columnIndex));
            columnIndex = imagesCursor.getColumnIndex(filePathColumn[3]);
            attributes.put("latitude", imagesCursor.getString(columnIndex));
            imagesCursor.close();
            return true;
        } else {
            imagesCursor.close();
            return false;
        }
    }
    /**
     * Render a thumbnail photo and scale it down to a smaller size.
     * @param path
     * @return
     */
    public static Bitmap bitmapFromPath(String path){
        File imgFile = new File(path);
        Bitmap imageBitmap = null;

        if(imgFile.exists()){

            //BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = IMAGE_RESOLUTION;
            //imageBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
            imageBitmap = ImageUtil.getThumbnailBitmap(imgFile.getAbsolutePath(), 400);
        }
        return imageBitmap;
    }

    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }


}
