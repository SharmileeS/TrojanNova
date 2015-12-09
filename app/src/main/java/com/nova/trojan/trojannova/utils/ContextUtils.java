package com.nova.trojan.trojannova.utils;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by xy on 6/12/15.
 */
public class ContextUtils {
    static ContextUtils instance = null;
    private final Context context;

    public static void setupUtils(Context context) {
        if (instance == null) {
            instance = new ContextUtils(context);
        }
    }
    private ContextUtils(Context context){
        this.context = context;
    }

    public static String getRealPathFromUri(Uri contentUri) {
        return instance.getRealPath(contentUri);
    }

    public static void displayExceptionDialog(String message){
        instance.displayDialog(message,instance.context, null);
    }
    public static void displayExceptionDialog(String message, Context context, Callable<Void> callable){
        instance.displayDialog(message, context, callable);
    }
    public static void setWallpaper( int resId){
        instance.setWallpaper(instance.context, resId);
    }
    public static void setWallpaper(String path){
        instance.setWallpaper(instance.context, path);
    }

    private String getRealPath(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void setWallpaper(Context context, int resId){
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(context.getApplicationContext());
        try {
            myWallpaperManager.setResource(resId);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void setWallpaper(Context context, String path){
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(context.getApplicationContext());
        try {
            // use if resizing the image
            BitmapFactory.Options options = new BitmapFactory.Options();

            // set to true to set image bounds
            options.inScaled = true;

            // set to 2, 4, 6, etc to create a progressively smaller image
            options.inSampleSize = 2;

            myWallpaperManager.setBitmap(BitmapFactory.decodeFile(path, options));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static Drawable getSmallDrawable(String path){
        return new BitmapDrawable(instance.context.getResources(), getScaledBitmap(path, 10));
    }


    public static Bitmap getScaledBitmap(String path, int sampleSize){
            // use if resizing the image
            BitmapFactory.Options options = new BitmapFactory.Options();

            // set to true to set image bounds
            options.inScaled = true;

            // set to 2, 4, 6, etc to create a progressively smaller image
            options.inSampleSize = sampleSize;

            return BitmapFactory.decodeFile(path,options);
    }

    private void displayDialog(String message, Context context, final Callable<Void> callable) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Oops..!!");
        builder.setMessage(message);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                try {
                    callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.show();
    }
}
