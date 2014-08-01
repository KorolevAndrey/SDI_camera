package com.camera.sdi.sdi_camera.VK;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.camera.sdi.sdi_camera.R;
import com.camera.sdi.sdi_camera.SharedStaticAppData;

import java.io.File;

/**
 * Created by sdi on 29.07.14.
 */
public class VKWallPostDialogBox extends Dialog implements View.OnClickListener{
    Button btnWallShare       = null;
    Button btnCancel          = null;
    RadioButton rbFriendsOnly = null;
    RadioButton rbPublic      = null;
    EditText etDescription    = null;
    ProgressBar progressBar   = null;

    File sharedPhoto = null;
    SharePhotoTask wallPostTask = null;

    public VKWallPostDialogBox(Context context, File sharedPhoto) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialbog_vk_wall_share_params);

        this.sharedPhoto = sharedPhoto;

        // assign UI
        progressBar  = (ProgressBar) findViewById(R.id.id_pb_vk_wall_post_upload);
        btnCancel    = (Button) findViewById(R.id.id_dialog_vk_wall_post_params_cancel);
        btnWallShare = (Button) findViewById(R.id.id_btn_vk_wall_share);
        rbFriendsOnly= (RadioButton) findViewById(R.id.id_rb_vk_wall_post_privacy_friends_only);
        rbPublic     = (RadioButton) findViewById(R.id.id_rb_vk_wall_post_privacy_public);
        etDescription= (EditText) findViewById(R.id.id_et_wall_post_description);

        btnCancel.setOnClickListener(this);
        btnWallShare.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        wallPostTask = new SharePhotoTask();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.id_btn_vk_wall_share:
                wallPostTask = new SharePhotoTask();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    wallPostTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    wallPostTask.execute();

                break;
            case R.id.id_dialog_vk_wall_post_params_cancel:
                if (wallPostTask.isCancelled() == false)
                    wallPostTask.cancel(true);

                cancel();
                break;
        }
    }

    class SharePhotoTask extends AsyncTask<Void, Void, Boolean> {
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
                String description = etDescription.getText().toString();
                boolean friendsOnly = rbFriendsOnly.isChecked();

                Log.d("VK", "wall post to " + (friendsOnly ? "friends" : "all users"));
                Log.d("VK", "\t" + description);

                VKManager.WallPostPhoto(sharedPhoto, description, friendsOnly);

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
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();

            dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d("VK", "share task canceled");
        }
    }

}
