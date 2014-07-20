package com.camera.sdi.sdi_camera;

import android.content.Context;

import java.io.File;

/**
 * Created by sdi on 20.07.14.
 */
public class SharedStaticAppData {
    private Context context = null;
    private static File baseDir = null;

    public SharedStaticAppData(Context context){
        this.context = context;
    }

    public static void setBaseDir(File nBaseDir){
        baseDir = nBaseDir;
    }

    public static File getBaseDir(){return baseDir;}
}
