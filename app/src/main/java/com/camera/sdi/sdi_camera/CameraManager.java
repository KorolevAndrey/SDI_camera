package com.camera.sdi.sdi_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sdi on 17.07.14.
 */
public class CameraManager {
    File basePhotoDirectory;
    File outputFile;
    Date date;

    String filesFormat = ".jpg";
    File[] filesInBaseDir = null;

    CameraManager(File BaseDir){
        basePhotoDirectory = BaseDir;
    }

    public File[] getFilesInBaseDir(){
        filesInBaseDir = basePhotoDirectory.listFiles( new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.length() > 4 && filename.endsWith(".jpg");
            }
        });
        return filesInBaseDir;
    }

    public List<String> getFileNamesInBaseDir(){
        // check new files in baseDir
        getFilesInBaseDir();

        List<String> lFileNamesInBaseDir = new ArrayList<String>();
        for (File f : filesInBaseDir){
            lFileNamesInBaseDir.add(f.getName());
        }
        return lFileNamesInBaseDir;
    }

    public String SavePhoto(byte[] data){
        //  get current datetime
        date = new Date();
        String now = new SimpleDateFormat("HH_mm_ss_S").format(date);
        //String now = "tmp";
        Log.d("debug", "time:" + now.toString());

        // create output file in base dir
        if (!basePhotoDirectory.exists()) {
            Log.d("debug", "try to create : " + basePhotoDirectory.getAbsolutePath());
            basePhotoDirectory.mkdirs();
            Log.d("debug", basePhotoDirectory.getAbsolutePath() + " created");
        }

        outputFile = new File(basePhotoDirectory, now + filesFormat); // example: 25-11-2013[12:12:12.11].jpg
        FileOutputStream fos = null;
        try {
            // write data to output file

            if (!outputFile.exists()){
                Log.d("debug", "try to craete: " + outputFile.getAbsolutePath());
                if (outputFile.createNewFile())
                    Log.d("debug", "created : " + outputFile.getAbsolutePath());
                else
                    Log.d("debug", "created : " + outputFile.getAbsolutePath());
            }
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
