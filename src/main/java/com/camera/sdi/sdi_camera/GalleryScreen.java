package com.camera.sdi.sdi_camera;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by sdi on 18.07.14.
 */
public class GalleryScreen extends Activity {
    TableLayout tableLayout = null;
    File[] files = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discplay_galary);

        tableLayout = ((TableLayout) findViewById(R.id.id_tl_gallery_table));
        Button btn = ((Button) findViewById(R.id.id_btn_refresh_gallery));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFiles(".jpg");
                matchTableWithImageView(3);
            }
        });
    }

    private void loadFiles(final String format) {
        File baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        files = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.length() > 3 && filename.endsWith(format);
            }
        });
    }

    private void matchTableWithImageView(int column_count){
        // get image widthth
        int img_width = tableLayout.getWidth();
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
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new ViewGroup.LayoutParams(img_width, img_width));
                String path = files[i+j].getAbsolutePath();
                Bitmap bmp = BitmapFactory.decodeFile(path);
                iv.setImageBitmap(bmp);
                tr.addView(iv, new TableRow.LayoutParams(img_width+1, img_width));
            }
            tableLayout.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

}
