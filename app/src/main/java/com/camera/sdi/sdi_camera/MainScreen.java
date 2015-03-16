package com.camera.sdi.sdi_camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.camera.sdi.sdi_camera.FileManager.FileManager;
import com.camera.sdi.sdi_camera.VK.VKLoginActivity;
import com.camera.sdi.sdi_camera.VK.VKManager;
import com.capricorn.RayMenu;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKUIHelper;

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
    DebugLogger Logger = null;
    SharedStaticAppData appData = null;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock = null;
    // end of locals block
    // --------------------

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

                camera.stopPreview();
                camera.release();
                camera = null;

                Intent i = new Intent(this, GalleryScreen.class);
                startActivity(i);

                break;

            case R.id.id_btn_make_photo:
                autoFocusMakeShot();
                break;

            case R.id.id_btn_options:
                // start options activity
                Intent intent_options = new Intent(this, ActivityOptions.class);
                startActivity(intent_options);
                break;
        }
    }

    private void forceCamera(){
        if (camera == null){
            Log.d("debug", "camera: null");
            try {
                camera = Camera.open(SharedStaticAppData.CAMERA_ID);
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
                    camera = Camera.open(SharedStaticAppData.CAMERA_ID);

                // get max possible resolution
                List<Camera.Size> possibleSize = camera.getParameters().getSupportedPictureSizes();
                Camera.Size maxSize = possibleSize.get(0);
                for (Camera.Size s : possibleSize){
                    if (s.height * s.width > maxSize.height * maxSize.width)
                        maxSize = s;
                }

                // set max possible resolution
                Camera.Parameters params = camera.getParameters();
                params.setPictureSize(maxSize.width, maxSize.height);
                camera.setParameters(params);

                Log.d("camera", "size : w = " + maxSize.width + " h = " + maxSize.height);

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
                camera = Camera.open(SharedStaticAppData.CAMERA_ID);
                camera.stopPreview();
            }

            setCameraDisplayOrientation(SharedStaticAppData.CAMERA_ID);
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
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("debug", "pressed");
            try {
                Logger.Log("try to make shot");
                autoFocusMakeShot();
                Logger.Log( "shot maked");
                //Toast.makeText(this, Environment.getDataDirectory().getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception e){
                String text = e.getMessage();
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                Logger.Log( "error : " + text);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ){
            // todo: add possibility to change photo count in current series
            new MakePhotoSeriesTask(5).execute();
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    private class MakePhotoSeriesTask extends AsyncTask<Void, Void, Void>{
        private int mPhotoCount = 1;

        private void makePhotoSeries(){
            mPhotosInCurrentSerie = 0;
            for (int i=0; i<mPhotoCount; ++i){
                // wait until last photo has saved
                while (mPhotosInCurrentSerie < i){
                    try {Thread.sleep(100, 0);} catch (InterruptedException e) {}
                };

                // make next photo
                publishProgress();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            try{
                autoFocusMakeShot();
            } catch(Exception e){
                Toast.makeText(getBaseContext(), "Error !!!", Toast.LENGTH_LONG).show();
            }
            Log.d("debug camera", "photo made");
        }

        public MakePhotoSeriesTask(int photoCount){
            mPhotoCount = photoCount;
        }

        @Override
        protected Void doInBackground(Void... params) {
            makePhotoSeries();
            return null;
        }
    }


    private volatile static int mPhotosInCurrentSerie = 0;

    private void autoFocusMakeShot(){
        if (camera == null) {
            Log.d("Camera", "camera is null (from autoFocusMakeShot)");
            return;
        }

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Log.d("Camera", success ? " autofocus success" : "autofocus fail");
                if (success) {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            // save picture
                            //Logger.Log("[no autofocus] try to save photo");
                            File pictures_dir = FileManager.getBaseDir();
                            Log.d("debug", "pic dir : " + pictures_dir.getAbsolutePath());
                            CameraManager cm = new CameraManager(pictures_dir);
                            String savedPhotoName = cm.SavePhoto(data);
                            //Toast.makeText(getBaseContext(), cm.SavePhoto(data)+" autofocus disabled", Toast.LENGTH_LONG).show();
                            forceCamera();

                            // the photo was made
                            mPhotosInCurrentSerie += 1;
                            Log.d("debug camera", savedPhotoName);
                        }
                    });
                } else {
                    Log.d("debug camera", "fail");
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedStaticAppData.FULLSCREEN) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_main_screen);

        File pictures_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        cameraManager = new CameraManager(pictures_dir);

        sv = (SurfaceView) findViewById(R.id.id_sv_camera);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);
        if (camera == null) camera = Camera.open(SharedStaticAppData.CAMERA_ID);

        ((SurfaceView) findViewById(R.id.id_sv_camera)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.id_btn_gallery)).setOnClickListener(this);
        ((Button) findViewById(R.id.id_btn_make_photo)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.id_btn_options)).setOnClickListener(this);

        // init menu
        initRaymenu();

        // clear log on create
        Logger = new DebugLogger();

        // shared data container
        appData = new SharedStaticAppData(this);

        // set base file directory
        File base = getExternalFilesDir(null);
        FileManager.setBaseDir(base);
        FileManager.setCurrentDir(base);

        // get power manager to keep screen on
        powerManager  = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MTag");

        // restore saved shared preferences
        VKManager.user_id = SharedStaticAppData.restore_VKUserId();
        Log.d("VK", "onload token: " +
                VKAccessToken
                        .tokenFromSharedPreferences(this, VKLoginActivity.getTokenKey())
                        .accessToken
        );
    }

    /*
    * get raymenu and set buttons to it
    * */
    private void initRaymenu(){
        RayMenu rayMenu = (RayMenu) findViewById(R.id.id_raymenu);

        // save context to use it in OnClickListener methods
        final Context context = this;

        // create imageView button
        ImageView settingImageView = new ImageView(this);
        settingImageView.setImageResource(R.drawable.icon_options);
        rayMenu.addItem(settingImageView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start options activity
                Intent intent_options = new Intent(context, ActivityOptions.class);
                startActivity(intent_options);
            }
        });

        // create imageView button
        ImageView galleryImageView = new ImageView(this);
        galleryImageView.setImageResource(R.drawable.icon_gallery);
        rayMenu.addItem(galleryImageView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> lFileNames = cameraManager.getFileNamesInBaseDir();
                for (String s : lFileNames)
                Log.d("debug", s);
                Log.d("debug", "-------------");

                camera.stopPreview();
                camera.release();
                camera = null;

                Intent i = new Intent(context, GalleryScreen.class);
                startActivity(i);
            }
        });

        // create imageView button
        ImageView makePhotoImageView = new ImageView(this);
        makePhotoImageView.setImageResource(R.drawable.button_photo);
        rayMenu.addItem(makePhotoImageView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFocusMakeShot();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
        Log.d("debug", "onresume");
        if (camera == null)
            camera = Camera.open(SharedStaticAppData.CAMERA_ID);
        Log.d("debug", "camera:" + (camera == null ? " OK" : "NULL"));
        setPreviewSize(SharedStaticAppData.FULLSCREEN);
        forceCamera();

        // lock screen
        this.wakeLock.acquire();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debug", "onpause");
        if (camera != null){
            camera.release();
        }
        camera = null;

        // release screen blocker
        this.wakeLock.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            Intent i = new Intent(this, ActivityOptions.class);
            startActivity(i);

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

        Log.d("debug", "degrees: " + degrees);

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
