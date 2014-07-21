package com.camera.sdi.sdi_camera;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by sdi on 18.07.14.
 */
public class GalleryScreen extends Activity implements View.OnClickListener{
    TableLayout tableLayout = null;
    File[] files = null;
    DebugLogger Logger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // NOTE: if(){..} must be called before setContent
        if (SharedStaticAppData.FULLSCREEN) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_discplay_galary);

        // use already created log to save data
        Logger = new DebugLogger(false);

        tableLayout = ((TableLayout) findViewById(R.id.id_tl_gallery_table));
        Button btn_vk_auth = (Button) findViewById(R.id.id_btn_vk_auth);
        btn_vk_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GalleryScreen.this, VKLoginActivity.class);
                startActivity(i);
            }
        });
        //if (VKAccessToken.tokenFromSharedPreferences(this,VKLoginActivity.sTokenKey).isExpired())
        Log.d("VK", "token: " + VKAccessToken.tokenFromSharedPreferences(this,VKLoginActivity.sTokenKey).accessToken);

        Button btn = ((Button) findViewById(R.id.id_btn_refresh_gallery));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.Log("back to camera");
                Intent i = new Intent(GalleryScreen.this, MainScreen.class);
                startActivity(i);
                /*loadFiles(".jpg");
                matchTableWithImageView(3);*/
            }
        });

        loadFiles(".jpg");
        matchTableWithImageView(3);
    }

    private void loadFiles(final String format) {
        File baseDir = SharedStaticAppData.getBaseDir();
        Logger.Log("try to load data from " + baseDir.getAbsolutePath());
        files = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.length() > 3 && filename.endsWith(format);
            }
        });
        Logger.Log(files.length + " files found");
    }

    private void matchTableWithImageView(int column_count){
        // get image widthth
        int img_width = getWindowManager().getDefaultDisplay().getWidth(); //tableLayout.getWidth();
        Log.d("debug", "window width: " + img_width);
        img_width /= column_count;
        Log.d("debug", "img_width: "+ img_width);
        if (img_width < 50) {
            img_width = 50;
            //column_count = tableLayout.getWidth() / img_width;
        }

        Log.d("debug", "column_count: " + column_count);
        int n = files.length;
        for (int i=0; i<n && column_count > 0; i+=column_count){
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new ViewGroup.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, img_width));
            tr.setBackgroundColor(getResources().getColor(R.color.gallery_table_row_background));
            for (int j=0; j<column_count && i+j<n; ++j){
                String path = files[i+j].getAbsolutePath();
                Bitmap bmp = SharedStaticAppData.rotateBitmap90Degrees(BitmapFactory.decodeFile(path));

                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new ViewGroup.LayoutParams(img_width, img_width));
                iv.setImageBitmap(bmp);
                iv.setTag(files[i+j]);
                iv.setOnClickListener(this);

                tr.addView(iv, new TableRow.LayoutParams(img_width+1, img_width));
            }
            tableLayout.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    public void onClick(View v) {
        File f = (File)v.getTag();
        //VKManager.WallPostPhoto(f);
        VkShareDialogBox vkShareDialogBox = new VkShareDialogBox(this, f);
        vkShareDialogBox.show();
        //Toast.makeText(this, f.getName(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        VKUIHelper.onDestroy(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
        Toast.makeText( this,
                VKAccessToken.tokenFromSharedPreferences(this,VKLoginActivity.sTokenKey).accessToken,
                Toast.LENGTH_SHORT).show();
    }
}