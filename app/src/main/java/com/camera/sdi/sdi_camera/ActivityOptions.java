package com.camera.sdi.sdi_camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.camera.sdi.sdi_camera.VK.VKAlbumSelect;
import com.vk.sdk.VKUIHelper;

/**
 * Created by sdi on 28.07.14.
 */
public class ActivityOptions extends Activity {

    Button btnSelectAlbum = null;
    TextView tvVKUploadAlbumName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_options);

        VKUIHelper.onCreate(this);

        // restore saved data
        boolean res = SharedStaticAppData.restore_VKUploadTarget();
        _setVKUploadTarget(res);

        tvVKUploadAlbumName = (TextView) findViewById(R.id.id_options_tvVKAlbum);

        btnSelectAlbum = (Button) findViewById(R.id.id_options_vk_albumName_change);
        btnSelectAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivityOptions.this, VKAlbumSelect.class);
                startActivity(i);
            }
        });
    }

    private void _setVKUploadTarget(boolean isToAlbum){
        if (tvVKUploadAlbumName == null) return;

        if (isToAlbum){
            // set album name
            String uploadAlbumTitle = SharedStaticAppData.restore_VKAlbumTitle();
            Log.d("VK", "photo must be saved to : " + uploadAlbumTitle);
            tvVKUploadAlbumName.setText(uploadAlbumTitle);
        } else{
            // upload to wall
            Log.d("VK", "upload to wall");
            tvVKUploadAlbumName.setText(getResources().getString(R.string.upload_target_wall));
        }
    }

    private void _setVKUploadTarget(){
        boolean isToAlbum = SharedStaticAppData.restore_VKUploadTarget();
        _setVKUploadTarget(isToAlbum);
    }

    @Override
    protected void onResume() {
        super.onResume();

        _setVKUploadTarget();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        VKUIHelper.onActivityResult(requestCode, resultCode, data);
    }
}
