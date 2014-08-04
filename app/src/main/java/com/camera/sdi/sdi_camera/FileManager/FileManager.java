package com.camera.sdi.sdi_camera.FileManager;

import android.util.Log;

import com.camera.sdi.sdi_camera.SharedStaticAppData;

import org.apache.http.impl.cookie.DateUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sdi on 04.08.14.
 */
public class FileManager {

    private final static String DEBUG_TAG = "File manager";

    private static File baseDir = null;

    private static File currentDir = null;
    
    /*
    * Archive is placed in base directory.
    * Every subdirectory in base directory is named as date when it was created.
    *
    * !!! NOTE: save files to archive only with move to archive function
    *
    * @return directories with old files
    * */
    public static File[] getArchive(){
        if (baseDir == null) {
            return null;
        } else{
            // get all subdirectories in current folder
            File[] directories = baseDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return new File(dir, filename).isDirectory();
                }
            });

            return directories;
        }
    }

    /*
    * this method called to move files from @param[0] to archive dir
    * that named as <<year_month_day>>
    *
    * @return true if files successfully moved and false otherwise
    * */
    public static boolean moveToArchive(File[] files){
        // get current date
        String currentDate = new SimpleDateFormat("yyyy_MM_dd").format(new Date());

        // create file in base directory named as <<year_month_day>>
        File archiveDir = new File(baseDir, currentDate);

        if (archiveDir.exists() == false){
            // archive folder not exists
            Log.d(DEBUG_TAG, "try to create directory: " + archiveDir.getAbsolutePath());
            try {
                // create archive directory
                archiveDir.mkdirs();
            } catch (Exception e) {
                Log.d(DEBUG_TAG, "error: " + e.getMessage());
                return false;
            }
        }

        // move files to new directory
        for (File f : files){
            File nFile = new File(archiveDir, f.getName());
            Log.d(DEBUG_TAG, "old: " + f.getAbsolutePath() +
                    "\n\tnew :" + nFile.getAbsolutePath());

            f.renameTo(nFile);
        }

        return true; // files was moved
    }

    public static boolean moveOldFilesToArchive(){
        final Date today = new Date();
        File[] oldFiles = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File currentFile = new File(dir, filename);
                long lastModified = currentFile.lastModified();
                long dayDiff = (today.getTime() - lastModified) / (1000 * 60 * 60 * 24);
                Log.d(DEBUG_TAG, filename + " modified " + dayDiff + " days ago");

                return dayDiff >= SharedStaticAppData.MAX_DAYS_IN_BASE_FOLDER;
            }
        });

        Log.d(DEBUG_TAG, "old files count: " + oldFiles.length);
        return moveToArchive(oldFiles);
    }

    /*
    * moves all files from base directory to archive directory
    * @ return true if success, false otherwise
    * */
    public static boolean moveToArchive(){
        if (baseDir == null) {
            Log.d(DEBUG_TAG, "base dir is unset");
            return false;
        }

        File[] files = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".jpg") || filename.endsWith(".jpeg");
            }
        });

        return moveToArchive(files);
    };

    public static void setBaseDir(File nBaseDir){
        Log.d(DEBUG_TAG, "SetBaseDir (" + nBaseDir.getPath() + ") called");

        if (currentDir == null)
            currentDir = nBaseDir;

        baseDir = nBaseDir;
    }

    public static File getBaseDir(){return baseDir;}

    public static void setCurrentDir(File nCurrentDir){
        Log.d(DEBUG_TAG, "SetCurrentDir (" + nCurrentDir.getPath() + ") called");

        if (baseDir == null)
            baseDir = nCurrentDir;

        currentDir = nCurrentDir;
    }

    public static File getCurrentDir(){
        return currentDir;
    }
}
