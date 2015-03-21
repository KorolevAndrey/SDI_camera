package com.camera.sdi.sdi_camera.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.camera.sdi.sdi_camera.SharedStaticAppData;

import java.io.File;

/**
 * Created by sdi on 21.03.15.
 */
public class ImageGalleryItemImage extends ImageGalleryItem {

    public ImageGalleryItemImage(Context context, File imageFile) {
        super(context, imageFile);
    }

    @Override
    protected Bitmap getBitmap() {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 8;
        return SharedStaticAppData.rotateBitmap90Degrees(
                BitmapFactory.decodeFile(super.getFile().getAbsolutePath(), opt)
        );
    }
}
