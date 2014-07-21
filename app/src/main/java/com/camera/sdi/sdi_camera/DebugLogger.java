package com.camera.sdi.sdi_camera;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Created by sdi on 19.07.14.
 */
public class DebugLogger {
    private String fileName = "/SDI_camera";
    private final File base = Environment.getExternalStorageDirectory();
    private File fLog = null;

    public DebugLogger(String fileName){
        this.fileName = fileName;

        /* init fLog
        * ...........Note: must be called before this.saveLog(/../)
        */
        fLog = new File(base.getAbsolutePath()+"/SDI_camera.log");

        // clear log
        saveToLog("", true);

    }

    public DebugLogger(){
        /* init fLog
        * ...........Note: must be called before this.saveLog(/../)
        */
        fLog = new File(base.getAbsolutePath()+"/SDI_camera.log");

        //clear log
        saveToLog("", true);
    }

    public DebugLogger(boolean mustBeCleaned){
        /* init fLog
        * ...........Note: must be called before this.saveLog(/../)
        */
        fLog = new File(base.getAbsolutePath()+"/SDI_camera.log");

        saveToLog("", mustBeCleaned);
    }

    public DebugLogger(String fileName, boolean mustBeCleaned){
        this.fileName = fileName;

        /* init fLog
        * ...........Note: must be called before this.saveLog(/../)
        */
        fLog = new File(base.getAbsolutePath()+"/SDI_camera.log");

        saveToLog("", mustBeCleaned);
    }

    public void Log(String data){
        saveToLog(data, false);
    }

    public String getFullFileName(){
        return fLog.getAbsolutePath();
    }

    private void saveToLog(String strData, boolean mustBeCleaned){
        /*
        * save strData to .../fileName
        * used to save Log
        * */
        try {
            if (!fLog.exists()) {
                fLog.createNewFile();
                Log.d("debug", "file " + fLog.getAbsolutePath() + "created");
            } else if (mustBeCleaned){
                fLog.delete();
                fLog.createNewFile();
            }

            try{
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fLog, true)));
                pw.println(strData);
                pw.close();
            }
            catch (Exception e){}
        } catch (FileNotFoundException e) {
        //    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e){
            Log.d("debug", e.getMessage());
        }
    }

}
