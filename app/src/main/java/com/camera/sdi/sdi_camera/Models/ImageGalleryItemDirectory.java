package com.camera.sdi.sdi_camera.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.camera.sdi.sdi_camera.R;

import java.io.File;

/**
 * Created by sdi on 21.03.15.
 */
public class ImageGalleryItemDirectory extends ImageGalleryItem {

    public ImageGalleryItemDirectory(Context context, File directory){
        super(context, directory);
    }

    @Override
    protected Bitmap getBitmap() {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 8;
        return BitmapFactory.decodeResource(super.getContext().getResources(), R.drawable.icon_folder, opt);
    }
}
