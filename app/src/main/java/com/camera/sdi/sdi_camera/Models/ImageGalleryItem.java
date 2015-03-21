package com.camera.sdi.sdi_camera.Models;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by sdi on 21.03.15.
 */
public abstract class ImageGalleryItem {
    private File mFile = null;
    private Bitmap mImage = null;
    private boolean mIsChecked = false;
    private Context mContext = null;

    public ImageGalleryItem(Context context, File imageFile){
        mFile = imageFile;
        mIsChecked = false;
        mContext = context;
        mImage = getBitmap();
    }

    protected abstract Bitmap getBitmap();

    public boolean isDirectory(){
        return mFile.isDirectory();
    }

    public Bitmap getImage(){return mImage;}

    public boolean isChecked(){ return mIsChecked;}

    public void setCheckedState(boolean isChecked){
        mIsChecked = isChecked;
    }

    public File getFile(){ return mFile;}

    public Context getContext(){return mContext;}
}
