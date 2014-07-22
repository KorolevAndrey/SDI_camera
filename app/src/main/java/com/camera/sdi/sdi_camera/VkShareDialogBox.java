package com.camera.sdi.sdi_camera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    Context context  = null;
    Button btnShare  = null;
    Button btnCancel = null;
    File sharedPhoto = null;

    ProgressBar progressBar  = null;
    SharePhotoTask shareTask = null;

    public VkShareDialogBox(Context context, File bmpPhoto) {
        super(context);

        this.context = context;
        sharedPhoto = bmpPhoto;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_vk_share);

        // assign UI
        btnShare    = (Button) findViewById(R.id.id_btn_share_photo_vk);
        btnCancel   = (Button) findViewById(R.id.id_btn_share_cancel);
        progressBar = (ProgressBar) findViewById(R.id.id_pb_vk_upload);

        // set click_listeners
        btnCancel.setOnClickListener(this);
        btnShare.setOnClickListener(this);

        // set photo
        Bitmap bmp = BitmapFactory.decodeFile(sharedPhoto.getAbsolutePath());
        Bitmap rotated = SharedStaticAppData.rotateBitmap90Degrees(bmp);
        ((ImageView) findViewById(R.id.id_img_uploaded_to_vk)).setImageBitmap(rotated);

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
                //shareTask = new SharePhotoTask();
                shareTask.execute();
                break;

            case R.id.id_btn_share_cancel:
                if (shareTask.isCancelled() == false)
                    shareTask.cancel(true); // true == may interrupt if running

                dismiss();
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
                VKManager.WallPostPhoto(sharedPhoto);
                return true;}

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
