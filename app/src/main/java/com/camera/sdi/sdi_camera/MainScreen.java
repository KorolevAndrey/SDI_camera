package com.camera.sdi.sdi_camera;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class MainScreen extends Activity {

    // locals block
    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera = null;
    // end of locals block
    // --------------------

    // finals block
    final int CAMERA_ID = 0;
    final boolean FULLSCREEN = true;
    // end of finals block
    //--------------------

    class HolderCallback implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (camera == null || holder == null)
                    return;
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (camera!=null) camera.stopPreview();
            setCameraDisplayOrientation(CAMERA_ID);
            try{
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (    keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("debug", "pressed");
            makeShot();
        }
        return true;
    }

    private void makeShot(){

        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictures_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                CameraManager cm = new CameraManager(pictures_dir);
                Toast.makeText(getBaseContext(), cm.SavePhoto(data), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FULLSCREEN) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_main_screen);

        sv = (SurfaceView) findViewById(R.id.id_sv_camera);
        holder = sv.getHolder();

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);
        if (camera == null) camera = Camera.open(CAMERA_ID);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) camera = Camera.open(CAMERA_ID);
        Log.d("debug", "camera:" + (camera == null ? " OK" : "NULL"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (camera != null){
            camera.release();
        }
        camera = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setPreviewSize(boolean isFullScreen){
        // get display size
        Display display = getWindowManager().getDefaultDisplay();
        boolean isWidthBigger = display.getWidth() > display.getHeight();

        // get size of camera preview
        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF preview
        if (isWidthBigger){
            // Horizontal orientation
            rectPreview.set(0,0, size.width, size.height);
        }else{
            // vertical orientation
            rectPreview.set(0,0, size.height, size.width);
        }

        Matrix mtx = new Matrix();
        // prepare matrix
        if (!isFullScreen){
            // preview must be placed in screen
            mtx.setRectToRect(rectPreview, rectDisplay, Matrix.ScaleToFit.START);
        }else{
            // screen must be placed in preview
            mtx.setRectToRect(rectDisplay, rectPreview, Matrix.ScaleToFit.START);
            mtx.invert(mtx);
        }
        mtx.mapRect(rectPreview);

        // setting surface size from last transformation
        sv.getLayoutParams().width  = (int) (rectPreview.right);
        sv.getLayoutParams().height = (int) (rectPreview.bottom);
    }

    void setCameraDisplayOrientation(int cameraId){
        // defined orientation
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch(rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // get camera info
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
            // background camera
            result = (360-degrees) + info.orientation;
        }else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            // frontground camera
            result = (360-degrees) + info.orientation;
            result += 360;
        }
        result %= 360;

        camera.setDisplayOrientation(result);
    }
}
