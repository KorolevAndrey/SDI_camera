package com.camera.sdi.sdi_camera.VK;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.camera.sdi.sdi_camera.DeleteFileDialogBox;
import com.camera.sdi.sdi_camera.Instagram.InstagramPhotoShare;
import com.camera.sdi.sdi_camera.R;
import com.camera.sdi.sdi_camera.ScrollPowerView;
import com.camera.sdi.sdi_camera.SharedStaticAppData;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.api.VKError;

import java.io.File;

/**
 * Created by sdi on 21.07.14.
 */
public class VkShareDialogBox extends Dialog implements View.OnClickListener{
    float fLastImageTouch_x;
    float fLastImageTouch_y;
    float fImageTouch_dx =0;
    float fImageTouch_dy =0;

    Context context              = null;
    TextView tvImageIndex        = null;
    Button btnShareVK            = null;
    Button btnCancel             = null;
    Button btnDelete             = null;
    Button btnShareInstagram     = null;
    ScrollPowerView spvPower     = null;

    File[] sharedPhotos   = null;
    int currentSharedPhotosInd = 0;

    Dialog parent    = null;

    ProgressBar progressBar  = null;
    SharePhotoTask shareTask = null;

    public boolean isFileExists(){
        boolean ret = false;
        try {
            ret = sharedPhotos[currentSharedPhotosInd].exists();
            Log.d("Delete File", ret ? sharedPhotos[currentSharedPhotosInd].getName() + " exists" : sharedPhotos[currentSharedPhotosInd].getName() + " not exists");
        }catch (Exception e){
            Log.e("Touch", e.getMessage());
        }
        return ret;
    }

    /*
    * this method called when sets actual photo in imageView.
    * */
    private void _refreshImageView(){
        // set photo
        try {
            Bitmap bmp = BitmapFactory.decodeFile(sharedPhotos[currentSharedPhotosInd].getAbsolutePath());
            Bitmap rotated = SharedStaticAppData.rotateBitmap90Degrees(bmp);
            ((ImageView) findViewById(R.id.id_img_uploaded_to_vk)).setImageBitmap(rotated);
        } catch (Exception e){}

        // refresh index info in UI
        tvImageIndex.setText((currentSharedPhotosInd+1) + " / " + sharedPhotos.length);

        // called to delete "old" bitmap
        System.gc();
    }

    public VkShareDialogBox(Context context, File[] bmpPhotos, int ind) {
        super(context);

        this.context = context;
        sharedPhotos = bmpPhotos;
        parent = this;
        currentSharedPhotosInd = ind;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_vk_share);

        // assign UI
        btnDelete    = (Button) findViewById(R.id.id_btn_delete_photo);
        btnShareVK     = (Button) findViewById(R.id.id_btn_share_photo_vk);
        btnCancel    = (Button) findViewById(R.id.id_btn_share_cancel);
        progressBar  = (ProgressBar) findViewById(R.id.id_pb_vk_upload);
        tvImageIndex = (TextView) findViewById(R.id.id_tv_image_index);
        spvPower     = (ScrollPowerView) findViewById(R.id.id_spv_scroll_power);
        btnShareInstagram = (Button) findViewById(R.id.id_btn_share_instagram);

        // set click_listeners
        btnCancel.setOnClickListener(this);
        btnShareVK.setOnClickListener(this);
        btnShareInstagram.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        // set edges
        spvPower.setBothVal(SharedStaticAppData.IMAGE_LEAF_PREV, SharedStaticAppData.IMAGE_LEAF_NEXT);

        // set image in image view and bind "leaf" listener
        _refreshImageView();
        ((ImageView) findViewById(R.id.id_img_uploaded_to_vk)).setOnTouchListener(new View.OnTouchListener() {
            /*
            * this method used for leaf images
            * */
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        fImageTouch_dx = 0;
                        fImageTouch_dy = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        fImageTouch_dx += fLastImageTouch_x - x;
                        fImageTouch_dy += fLastImageTouch_y - y;

                        float alpha = 1;
                        if (fImageTouch_dx > 0){
                            Log.d("Touch", "fImageTouch_dx: " + fImageTouch_dx + "next: " + SharedStaticAppData.IMAGE_LEAF_NEXT);
                            alpha -= fImageTouch_dx / SharedStaticAppData.IMAGE_LEAF_NEXT;
                        }else {
                            Log.d("Touch", "fImageTouch_dx: " + fImageTouch_dx + "prev: " + SharedStaticAppData.IMAGE_LEAF_PREV);
                            alpha -= fImageTouch_dx / SharedStaticAppData.IMAGE_LEAF_PREV;
                        }

                        Log.d("Touch", "alpha: " + alpha);
                        v.setAlpha(alpha);

                        break;
                    case MotionEvent.ACTION_UP:
                        if (fImageTouch_dx > SharedStaticAppData.IMAGE_LEAF_NEXT) {
                            // user want to leaf to the next photo
                            // n --> 0
                            currentSharedPhotosInd = ++currentSharedPhotosInd % sharedPhotos.length;
                        }else if (fImageTouch_dx < SharedStaticAppData.IMAGE_LEAF_PREV){
                            // user want to leaf to the previous photo
                            // 0 --> n-1
                            currentSharedPhotosInd = (--currentSharedPhotosInd + sharedPhotos.length) % sharedPhotos.length;
                        }
                        Log.d("Touch", "dx: " + fImageTouch_dx + " dy: "+fImageTouch_dy);
                        Log.d("Touch", "new ind: " + currentSharedPhotosInd);

                        // set new image
                        _refreshImageView();

                        // set image alpha
                        v.setAlpha(1);

                        // reset animation
                        fImageTouch_dx = 0;
                        fImageTouch_dy = 0;

                        break;
                }

                spvPower.setCurrentVal((int)fImageTouch_dx);

                fLastImageTouch_x = x;
                fLastImageTouch_y = y;
                return true;
            }
        });

        shareTask = new SharePhotoTask();

        VKAccessToken at = SharedStaticAppData.restore_AccessToken();
        if (SharedStaticAppData.isVkInialized == false){
            Log.d("VK", "vk init");
            // first creation. Vk sdk must be initialized.
            VKSdk.initialize(vksdkListener, VKManager.strVKAppID, at);
            SharedStaticAppData.isVkInialized = true;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.id_btn_share_photo_vk:
                //VKManager.WallPostPhoto(sharedPhoto);
                if (SharedStaticAppData.isUploadToVKAlbum()) {
                    shareTask = new SharePhotoTask();
                    shareTask.execute();
                } else {
                    // call wall post dialog
                    Dialog dialogWallPostParams = new VKWallPostDialogBox(this.getContext(),
                            sharedPhotos[currentSharedPhotosInd]
                    );
                    dialogWallPostParams.setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dismiss();
                        }
                    });
                    dialogWallPostParams.show();
                }
                //VKManager.UploadPhotoToAlbum(this.sharedPhoto);
                break;

            case R.id.id_btn_share_cancel:
                if (shareTask.isCancelled() == false)
                    shareTask.cancel(true); // true == may interrupt if running

                dismiss();
                break;

            case R.id.id_btn_share_instagram:
                Dialog instagramDialogRegistraion = new InstagramPhotoShare(
                        getContext(),
                        sharedPhotos[currentSharedPhotosInd]
                );

                instagramDialogRegistraion.show();
                break;

            case R.id.id_btn_delete_photo:
                DeleteFileDialogBox deleteFileDialogBox = new DeleteFileDialogBox(context, sharedPhotos[currentSharedPhotosInd]);
                deleteFileDialogBox.show();
                deleteFileDialogBox.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // it means that user don't want to delete file
                        Log.d("Delete File","user answer is no");
                    }
                });
                deleteFileDialogBox.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // file was deleted by user
                        Log.d("Delete File","user answer is yes");
                        parent.dismiss();
                    }
                });
                break;
        }
    }

    private VKSdkListener vksdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            /*
            * Пришла капча
            * */

            Log.d("VK", "captch error");
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            /*
            * истек срок действия токена
            * */
            VKSdk.authorize(VKManager.scopes);
            Log.d("VK", "token expired");
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            /*
            * запрет доступа
            * */
            Log.d("VK", "access denied");
            new AlertDialog.Builder(VkShareDialogBox.this.context)
                    .setMessage(authorizationError.errorMessage)
                    .show();

        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            /*
            * получен новый токен
            * */

            Log.d("VK", "new token was received");
            SharedStaticAppData.save_VKAccessToken(newToken);
            SharedStaticAppData.save_VKUserId(Long.parseLong(newToken.userId));

            //Intent i = new Intent(VKLoginActivity.this, VkShareDialogBox.class);
            //startActivity(i);
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.d("VK", "accept");
            VKManager.user_id = Long.parseLong(token.userId);
            SharedStaticAppData.save_VKUserId(VKManager.user_id);
            SharedStaticAppData.save_VKAccessToken(token);

//            Intent i = new Intent(VKLoginActivity.this, GalleryScreen.class);
  //          startActivity(i);
            //finish();
        }
    };


    class SharePhotoTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            Log.d("VK", "share task start");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d("VK", "Background upload");
            if (SharedStaticAppData.isOnline()){
                if (SharedStaticAppData.isUploadToVKAlbum())
                    VKManager.UploadPhotoToAlbum(sharedPhotos[currentSharedPhotosInd]);
               //VKManager.WallPostPhoto(sharedPhotos[currentSharedPhotosInd]);

                return true;
            }

            return  false;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            Log.d("VK", "share task finished");
            progressBar.setVisibility(View.INVISIBLE);

            String text = aVoid ? "upload finished" : "check your internet connection";
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

            dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d("VK", "share task canceled");
        }
    }
}
