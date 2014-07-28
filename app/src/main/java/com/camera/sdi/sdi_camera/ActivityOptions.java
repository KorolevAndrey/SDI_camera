package com.camera.sdi.sdi_camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camera.sdi.sdi_camera.VK.VKAlbumSelect;
import com.camera.sdi.sdi_camera.VK.VKManager;
import com.vk.sdk.VKUIHelper;

import java.util.List;

/**
 * Created by sdi on 28.07.14.
 */
public class ActivityOptions extends Activity {

    Button btnSelectAlbum = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_options);

        VKUIHelper.onCreate(this);

        btnSelectAlbum = (Button) findViewById(R.id.id_options_vk_albumName_change);
        btnSelectAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivityOptions.this, VKAlbumSelect.class);
                startActivity(i);
            }
        });
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
