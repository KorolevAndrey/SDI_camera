package com.camera.sdi.sdi_camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class MainScreen extends Activity implements View.OnClickListener{

    // locals block
    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera = null;
    CameraManager cameraManager = null;
    // end of locals block
    // --------------------

    // finals block
    final int CAMERA_ID = 0;
    final boolean FULLSCREEN = true;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.id_sv_camera:
                Log.d("debug", "surface click");
                forceCamera();
                break;

            case R.id.id_btn_gallery:
                List<String> lFileNames = cameraManager.getFileNamesInBaseDir();
                for (String s : lFileNames)
                    Log.d("debug", s);
                Log.d("debug", "-------------");

                Intent i = new Intent(this, GalleryScreen.class);
                camera.stopPreview();
                camera.release();
                camera = null;
                startActivity(i);
                break;
        }
    }
    // end of finals block
    //--------------------

    private void forceCamera(){
        if (camera == null){
            Log.d("debug", "camera: null");
            try {
                camera = Camera.open(CAMERA_ID);
                camera.startPreview();
            } catch (Exception e) {
                Log.d("debug", e.getMessage());
            }
        } else {
            Log.d("debug", "trying to restart preview");
            holderCallback.reset();
        }
    }

    class HolderCallback implements SurfaceHolder.Callback{

        public void reset(){
            create();
            change();
        }

        private void create(){
            try {
                Log.d("debug", "surfaceCreated");
                if (camera == null)
                    camera = Camera.open(CAMERA_ID);
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void change(){
            Log.d("debug","surfaceChanged");
            if (camera != null){
                camera.stopPreview();
            }else{
                camera = Camera.open(CAMERA_ID);
                camera.stopPreview();
            }

            setCameraDisplayOrientation(CAMERA_ID);
            try{
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            create();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            change();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d("debug", "surfaceDestroyed");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (    keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("debug", "pressed");
            makeShot();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void makeShot(){
        Camera.Parameters cam_params = camera.getParameters();
        List<String> focusModes = cam_params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            // called when user try to make shot
            // and device has autofocus
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // this function called wher camera finished auto focusing
                    Log.d("debug", "autofocus");
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            // save picture
                            Toast.makeText(getBaseContext(), cameraManager.SavePhoto(data) + " autofocus enabled", Toast.LENGTH_LONG).show();
                        }
                    });
                    forceCamera();
                }// end of autofocus
            });
        } else{
            Log.d("debug", "no autofocus");
            // called whed device has no autofocus
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    // save picture
                    File pictures_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    CameraManager cm = new CameraManager(pictures_dir);
                    Toast.makeText(getBaseContext(), cm.SavePhoto(data)+" autofocus disabled", Toast.LENGTH_LONG).show();
                    forceCamera();
                }
            });
        }
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

        File pictures_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        cameraManager = new CameraManager(pictures_dir);

        sv = (SurfaceView) findViewById(R.id.id_sv_camera);
        holder = sv.getHolder();

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);
        if (camera == null) camera = Camera.open(CAMERA_ID);

        ((SurfaceView) findViewById(R.id.id_sv_camera)).setOnClickListener(this);
        ((Button) findViewById(R.id.id_btn_gallery)).setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("debug", "onresume");
        if (camera == null) camera = Camera.open(CAMERA_ID);
        Log.d("debug", "camera:" + (camera == null ? " OK" : "NULL"));
        setPreviewSize(FULLSCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debug", "onpause");
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
