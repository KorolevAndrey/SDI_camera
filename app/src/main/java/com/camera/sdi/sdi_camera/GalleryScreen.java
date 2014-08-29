package com.camera.sdi.sdi_camera;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.camera.sdi.sdi_camera.FileManager.FileManager;
import com.camera.sdi.sdi_camera.Instagram.InstagramPhotoShare;
import com.camera.sdi.sdi_camera.VK.VKLoginActivity;
import com.camera.sdi.sdi_camera.VK.VKManager;
import com.camera.sdi.sdi_camera.VK.VKWallPostDialogBox;
import com.camera.sdi.sdi_camera.VK.VkShareDialogBox;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by sdi on 18.07.14.
 */
public class GalleryScreen extends Activity implements View.OnClickListener{

    final static int TYPE_IMAGE = 1;
    final static int TYPE_FOLDER = 2;

    final static int CONTEXT_MENU_VK_SHARE = 1;
    final static int CONTEXT_MENU_INSTAGRAM_SHARE = 2;
    final static int CONTEXT_MENU_FILE_DELETE = 3;

    TableLayout tableLayout = null;
    File[] files = null;
    DebugLogger Logger = null;

    ImageButton btnMoveToArchive = null;

    TextView tvOnlineStatus = null;
    TextView tvCurrentDirectory = null;

    int currentFileIndex = 0;

    SharePhotoTask shareTask = null;

    RefreshInternetConnectionStatus internetStatusChecker = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            Intent i = new Intent(this, ActivityOptions.class);
            startActivity(i);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

        tvOnlineStatus = (TextView) findViewById(R.id.id_tv_online_status);
        tableLayout = ((TableLayout) findViewById(R.id.id_tl_gallery_table));
        //if (VKAccessToken.tokenFromSharedPreferences(this,VKLoginActivity.sTokenKey).isExpired())
        Log.d("VK", "token: " + VKAccessToken
                        .tokenFromSharedPreferences(this, VKLoginActivity.getTokenKey())
                        .accessToken
        );


        Button btn = ((Button) findViewById(R.id.id_btn_refresh_gallery));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.Log("back to camera");
                finish();
                //Intent i = new Intent(GalleryScreen.this, MainScreen.class);
                //startActivity(i);
                /*loadFiles(".jpg");
                matchTableWithImageView(3);*/
            }
        });

        btnMoveToArchive = (ImageButton) findViewById(R.id.id_btn_move_photos_to_archive);
        btnMoveToArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File currentDir = FileManager.getCurrentDir();
                if (currentDir == FileManager.getBaseDir()){
                    // in base directory. move files to archive
                    FileManager.moveToArchive();
                    _refreshCurrentDirectory();
                } else{
                    // back to base dir
                    FileManager.setCurrentDir(FileManager.getBaseDir());
                    _refreshCurrentDirectory();
                }
            }
        });

        tvCurrentDirectory = (TextView) findViewById(R.id.id_tv_current_directory);
        _refreshCurrentDirectory();

        FileManager.moveOldFilesToArchive();
        _refreshTableLayout();

        SharedStaticAppData.isAlive_AsyncTaskOnlineStatusRefresher = true;
        internetStatusChecker = new RefreshInternetConnectionStatus();
        internetStatusChecker.execute();
    }

    private void _refreshCurrentDirectory() {
        File currentDir = FileManager.getCurrentDir();
        Log.d("Files", "current dir: " + currentDir.getName());

        if (currentDir == FileManager.getBaseDir()){
            tvCurrentDirectory.setText("base directory");
            btnMoveToArchive.setBackgroundResource(R.drawable.icon_move_to_archive);
        } else{
            String name = currentDir.getName();
            tvCurrentDirectory.setText(name);
            btnMoveToArchive.setBackgroundResource(R.drawable.icon_return);
        }

        _refreshTableLayout();
    }

    private void loadFiles(final String format) {
        File baseDir = FileManager.getCurrentDir();
        Logger.Log("try to load data from " + baseDir.getAbsolutePath());
        files = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.length() > 3 && filename.endsWith(format);
            }
        });
        Logger.Log(files.length + " files found");
    }

    private void _refreshTableLayout(){
        tableLayout.removeAllViews();

        loadFiles(".jpg");
        matchTableWithImageView(3);
        matchTableWithArchiveDirs(3);
    }

    private void matchTableWithImageView(int column_count){
        //if (1 == 1) return;
        // clear layout
        Log.d("debug", "tableLayout.removeAllViewsInLayout");
        tableLayout.removeAllViewsInLayout();

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
            tr.setLayoutParams(new ViewGroup.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, img_width)
            );

            tr.setBackgroundColor(getResources().getColor(R.color.gallery_table_row_background));
            //if (1 == 1) return;
            BitmapFactory.Options bmp_options = new BitmapFactory.Options();
            bmp_options.inSampleSize = 8;
            for (int j=0; j<column_count && i+j<n; ++j){
                String path = files[i+j].getAbsolutePath();

                Bitmap bmp = SharedStaticAppData.rotateBitmap90Degrees(
                        BitmapFactory.decodeFile(path,bmp_options)
                );

                ImageView iv = new ImageView(this);
                iv.setLayoutParams(new ViewGroup.LayoutParams(img_width, img_width));
                iv.setImageBitmap(bmp);
                iv.setTag(R.string.view_tag_key_files_index, i + j); // save index
                iv.setTag(R.string.view_tag_key_type, TYPE_IMAGE);   // save type
                iv.setOnClickListener(this);

                tr.addView(iv, new TableRow.LayoutParams(img_width+1, img_width));

                registerForContextMenu(iv);
                iv.setOnCreateContextMenuListener(this);
                //bmp.recycle();
            }

            tableLayout.addView( tr,
                    new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT)
            );
        }

        //TableRow tr = new TableRow(this);
        //tr.setLayoutParams(new ViewGroup.LayoutParams(
        //                TableRow.LayoutParams.MATCH_PARENT, img_width)
        //);

    }

    private void matchTableWithArchiveDirs( int column_count){
        Log.d("File manager", "current dir: " + FileManager.getCurrentDir().getAbsolutePath());
        Log.d("File manager", "base dir: " + FileManager.getBaseDir().getAbsolutePath());
        if ( !FileManager.getCurrentDir().getAbsolutePath()
                .equals(FileManager.getBaseDir().getAbsolutePath())){
            // don't show archive if user not in base directory
            Log.d("Files", "don't show archive");
            return;
        }

        int img_width = getWindowManager().getDefaultDisplay().getWidth(); //tableLayout.getWidth();
        Log.d("debug", "window width: " + img_width);
        img_width /= column_count;
        Log.d("debug", "img_width: "+ img_width);
        if (img_width < 50) {
            img_width = 50;
            //column_count = tableLayout.getWidth() / img_width;
        }

        Log.d("debug", "column_count: " + column_count);

        // init table row
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new ViewGroup.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, img_width)
        );

        // get files to add (show) in container
        File[] archive = FileManager.getArchive();
        int n = archive.length;
        for (int i=0, current_collumn = 0; i<n && column_count>0 ; ++i, ++current_collumn){

            tr.setBackgroundColor(getResources().getColor(R.color.gallery_table_row_background));
            //if (1 == 1) return;
            View v = getLayoutInflater().inflate(R.layout.view_directory, null, false);
            v.setLayoutParams(new ViewGroup.LayoutParams(
                            img_width,
                            img_width)
            );
            v.setTag(R.string.view_tag_key_file ,archive[i]); // save file
            v.setTag(R.string.view_tag_key_type ,TYPE_FOLDER);  // save type
            ((TextView)v.findViewById(R.id.id_tv_gallery_dir_name))
                    .setText(archive[i].getName());

            tr.addView(v, new TableRow.LayoutParams(img_width + 1, img_width));

            registerForContextMenu(v);
            v.setOnCreateContextMenuListener(this);
            v.setOnClickListener(this);

            if ((current_collumn %= column_count) == column_count-1) {
                // first, second, third ... column_count-1    elements in row
                // row must be added to container
                tableLayout.addView(tr,
                        new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT)
                );

                // create new row
                tr = new TableRow(this);
                tr.setLayoutParams(new ViewGroup.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT, img_width)
                );
            }
        }

        if (tr.getChildCount() > 0){
            // not completed row must be added to container
            tableLayout.addView(tr,
                    new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT)
            );
        }

    } // end of function matchTableWithArchiveDirs(...)

    @Override
    public void onCreateContextMenu(ContextMenu menu, final View v, ContextMenu.ContextMenuInfo menuInfo) {

        try{
            this.currentFileIndex = (Integer) v.getTag(R.string.view_tag_key_files_index);
        } catch (Exception e ){};

        menu.add(0, CONTEXT_MENU_VK_SHARE,
                0, getBaseContext().getString(R.string.str_context_menu_field_vk_share)
        );
        menu.add(0, CONTEXT_MENU_INSTAGRAM_SHARE,
                 0, getBaseContext().getString(R.string.str_context_menu_field_instagram_share)
        );
        menu.add(1, CONTEXT_MENU_FILE_DELETE,
                 0, getBaseContext().getString(R.string.str_context_menu_field_delete_file)
        );

        for (int i=0; i<3; ++i)
            menu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case CONTEXT_MENU_VK_SHARE:
                            if (SharedStaticAppData.isOnline() == false) {
                                Toast.makeText(GalleryScreen.this,
                                        "check your internet connection",
                                        Toast.LENGTH_LONG)
                                        .show();
                                return true;
                            }

                            // init vk.com before first use.
                            VKManager.InitVk();

                            if (SharedStaticAppData.isUploadToVKAlbum()) {
                                shareTask = new SharePhotoTask();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                    shareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                else
                                    shareTask.execute();

                            } else {
                                // call wall post dialog
                                int index = (Integer) v.getTag(R.string.view_tag_key_files_index);
                                Log.d("File manager", "index : " + index);

                                Dialog dialogWallPostParams = new VKWallPostDialogBox(
                                        GalleryScreen.this,
                                        files[currentFileIndex]
                                );
                                dialogWallPostParams.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        Toast.makeText(GalleryScreen.this, "posted to wall",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                dialogWallPostParams.show();
                            }

                            return true;

                        case CONTEXT_MENU_INSTAGRAM_SHARE:
                            if (SharedStaticAppData.isOnline() == false) {
                                Toast.makeText(GalleryScreen.this,
                                        "check your internet connection",
                                        Toast.LENGTH_LONG)
                                        .show();
                                return true;
                            }

                            Dialog instagramDialogRegistraion = new InstagramPhotoShare(
                                    GalleryScreen.this,
                                    files[currentFileIndex]
                            );

                            instagramDialogRegistraion.show();
                            return true;

                        case CONTEXT_MENU_FILE_DELETE:
                            DeleteFileDialogBox deleteFileDialogBox =
                                    new DeleteFileDialogBox(GalleryScreen.this,
                                            files[currentFileIndex]);

                            deleteFileDialogBox.show();
                            deleteFileDialogBox.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    // it means that user don't want to delete file
                                    Log.d("Delete File","user answer is no");
                                }
                            });
                            deleteFileDialogBox.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    // file was deleted by user. remove it
                                    Log.d("Delete File", "user answer is yes");

                                    _refreshTableLayout();
                                }
                            });
                            return true;
                    }
                    return false;
                }
            });
    }

    @Override
    public void onClick(View v) {
        int type = (Integer) v.getTag(R.string.view_tag_key_type); // image of folder
        switch (type){
            case TYPE_IMAGE:
                // restore index
                int ind = (Integer) v.getTag(R.string.view_tag_key_files_index);

                VkShareDialogBox vkShareDialogBox = new VkShareDialogBox(this, files, ind);
                vkShareDialogBox.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        SharedStaticAppData.isAlive_AsyncTaskOnlineStatusRefresher = false;
                        internetStatusChecker.cancel(true);
                    }
                });
                vkShareDialogBox.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        SharedStaticAppData.isAlive_AsyncTaskOnlineStatusRefresher = true;
                        internetStatusChecker = new RefreshInternetConnectionStatus();
                        internetStatusChecker.execute();

                        if (((VkShareDialogBox) dialog).isFileExists() == false) {
                            // file was deleted by user
                            _refreshTableLayout();
                        }
                    }
                });
                vkShareDialogBox.show();
                //Toast.makeText(this, f.getName(), Toast.LENGTH_LONG).show();
            break;

            case TYPE_FOLDER:
                // restore folder
                File dir = (File) v.getTag(R.string.view_tag_key_file);
                FileManager.setCurrentDir(dir);
                _refreshCurrentDirectory();
                //Toast.makeText(this, dir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("Internet status", "onPause");
        SharedStaticAppData.isAlive_AsyncTaskOnlineStatusRefresher = false;
        internetStatusChecker.cancel(true);
    }

    @Override
    protected void onDestroy() {
        System.gc();
        VKUIHelper.onDestroy(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);

        Log.d("Internet status", "onResume");
        SharedStaticAppData.isAlive_AsyncTaskOnlineStatusRefresher = true;
        internetStatusChecker = new RefreshInternetConnectionStatus();
        internetStatusChecker.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
        Toast.makeText( this,
                VKAccessToken.tokenFromSharedPreferences(this, VKLoginActivity.getTokenKey()).accessToken,
                Toast.LENGTH_SHORT).show();
    }

    class RefreshInternetConnectionStatus extends AsyncTask<Void, Void, Void>{
        boolean isOnline = false;
        long lastCall = System.currentTimeMillis();
        long deltaTime = 1000; // equal to 1 second

        @Override
        protected Void doInBackground(Void... params) {
            boolean nStatus = SharedStaticAppData.isOnline();
            publishProgress();
            while (SharedStaticAppData.isAlive_AsyncTaskOnlineStatusRefresher){
                if (System.currentTimeMillis() - lastCall < deltaTime) {
                    try {
                        Thread.sleep(deltaTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //Log.d("Internet status", "called");
                lastCall = System.currentTimeMillis();
                nStatus = SharedStaticAppData.isOnline();
                if (nStatus != isOnline){
                    // online status changed
                    // UI must be refreshed
                    isOnline = nStatus;
                    publishProgress();
                    Log.d("Internet status", "is online: " + (isOnline ? "true" : "false"));
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.d("Internet status", "onProgress");
            if (isOnline) {
                // set online message
                tvOnlineStatus.setTextColor(getResources().getColor(R.color.color_status_online));
                tvOnlineStatus.setText(getResources().getString(R.string.internet_status_online));
            }else{
                // set offline message
                tvOnlineStatus.setTextColor(getResources().getColor(R.color.color_status_offline));
                tvOnlineStatus.setText(getResources().getString(R.string.internet_status_offline));
            }
        }
    }

    class SharePhotoTask extends AsyncTask<Void, Void, Boolean>{
        ProgressDialog progressDialog = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(GalleryScreen.this, "",
                    "Uploading to vk.com. Please wait...", true);
            //progressBar.setVisibility(View.VISIBLE);
            Log.d("VK", "share task start");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d("VK", "Background upload");
            if (SharedStaticAppData.isOnline()){
                if (SharedStaticAppData.isUploadToVKAlbum())
                    VKManager.UploadPhotoToAlbum(files[currentFileIndex]);
                //VKManager.WallPostPhoto(sharedPhotos[currentSharedPhotosInd]);

                return true;
            }

            return  false;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            Log.d("VK", "share task finished");
            progressDialog.cancel();

            String text = aVoid ? "upload finished" : "check your internet connection";
            Toast.makeText(GalleryScreen.this, text, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d("VK", "share task canceled");
        }
    }
}
