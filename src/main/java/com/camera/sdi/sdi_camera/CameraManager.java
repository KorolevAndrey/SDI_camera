package com.camera.sdi.sdi_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sdi on 17.07.14.
 */
public class CameraManager {
    File basePhotoDirectory;
    File outputFile;
    Date date;

    CameraManager(File BaseDir){
        basePhotoDirectory = BaseDir;
    }

    public String SavePhoto(byte[] data){
        //  get current datetime
        date = new Date();
        String now = new SimpleDateFormat("dd-MM-yyyy[HH:mm:ss.S]").format(date);
        Log.d("debug", "time:" + now.toString());

        // create output file in base dir
        outputFile = new File(basePhotoDirectory, now+".jpg");
        FileOutputStream fos = null;
        try {
            // write data to output file
            fos = new FileOutputStream(outputFile);
            fos.write(data);
            fos.close();
            return now; // GOOD

        } catch (Exception e) {
            // shit happens
            e.printStackTrace();
            Log.d("debug", "FILE ERROR !!! ---->>> " + e.getMessage());
        }

        return null; // FUUUCK
    }
}
