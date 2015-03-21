package com.camera.sdi.sdi_camera;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.camera.sdi.sdi_camera.FileManager.FileManager;
import com.camera.sdi.sdi_camera.Instagram.InstagramPhotoShare;
import com.camera.sdi.sdi_camera.ListAdapters.ImageGalleryAdapter;
import com.camera.sdi.sdi_camera.Models.ImageGalleryItem;
import com.camera.sdi.sdi_camera.Models.ImageGalleryItemDirectory;
import com.camera.sdi.sdi_camera.Models.ImageGalleryItemImage;
import com.camera.sdi.sdi_camera.VK.VKLoginActivity;
import com.camera.sdi.sdi_camera.VK.VKManager;
import com.camera.sdi.sdi_camera.VK.VKWallPostDialogBox;
import com.camera.sdi.sdi_camera.VK.VkShareDialogBox;
import com.etsy.android.grid.StaggeredGridView;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKUIHelper;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by sdi on 18.07.14.
 */
public class GalleryScreen extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    final static int TYPE_IMAGE = 1;
    final static int TYPE_FOLDER = 2;

    final static int CONTEXT_MENU_VK_SHARE = 1;
    final static int CONTEXT_MENU_INSTAGRAM_SHARE = 2;
    final static int CONTEXT_MENU_FILE_DELETE = 3;

    StaggeredGridView mStaggeredGridView = null;
    ImageGalleryAdapter mImageGalleryAdapter = null;
    //TableLayout tableLayout = null;
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

        // init gallery
        mStaggeredGridView = (StaggeredGridView) findViewById(R.id.id_sgv_image_gallery);
        mImageGalleryAdapter = new ImageGalleryAdapter(this, 0);
        mStaggeredGridView.setAdapter(mImageGalleryAdapter);
        mStaggeredGridView.setOnItemClickListener(this);

        tvOnlineStatus = (TextView) findViewById(R.id.id_tv_online_status);
        Log.d("VK", "token: " + VKAccessToken
                        .tokenFromSharedPreferences(this, VKLoginActivity.getTokenKey())
                        .accessToken
        );

        // init buttons
        findViewById(R.id.id_btn_refresh_gallery).setOnClickListener(this);
        btnMoveToArchive = (ImageButton) findViewById(R.id.id_btn_move_photos_to_archive);
        btnMoveToArchive.setOnClickListener(this);

        // collect old photos to directory
        FileManager.moveOldFilesToArchive();

        tvCurrentDirectory = (TextView) findViewById(R.id.id_tv_current_directory);
        _refreshCurrentDirectory();

        SharedStaticAppData.isAlive_AsyncTaskOnlineStatusRefresher = true;
        internetStatusChecker = new RefreshInternetConnectionStatus();
        internetStatusChecker.execute();
    }

    private void _refreshCurrentDirectory() {
        File currentDir = FileManager.getCurrentDir();
        Log.d("Files", "current dir: " + currentDir.getName());

        showImagesInCurrentDirectory();
        if (currentDir.equals(FileManager.getBaseDir())){
            tvCurrentDirectory.setText("base directory");
            btnMoveToArchive.setBackgroundResource(R.drawable.icon_move_to_archive);
            showArchiveDirectories();
        } else{
            String name = currentDir.getName();
            tvCurrentDirectory.setText(name);
            btnMoveToArchive.setBackgroundResource(R.drawable.icon_return);
        }
    }

    private void loadFiles(final String format) {
        File baseDir = FileManager.getCurrentDir();
        Logger.Log("try to load data from " + baseDir.getAbsolutePath());
        files = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.length() > format.length() && filename.endsWith(format);
            }
        });
        Logger.Log(files.length + " files found");
    }

    private void showImagesInCurrentDirectory(){
        loadFiles(".jpg");
        mImageGalleryAdapter.clear();
        for (File f : files){
            ImageGalleryItem itemToAdd = new ImageGalleryItemImage(this, f);
            mImageGalleryAdapter.add(itemToAdd);
        }
        mImageGalleryAdapter.notifyDataSetChanged();
    }

    private void showArchiveDirectories(){
        File[] archiveDirectories = FileManager.getArchive();
        for (File dir : archiveDirectories){
            ImageGalleryItem itemToAdd = new ImageGalleryItemDirectory(this, dir);
            mImageGalleryAdapter.add(itemToAdd);
        }
        mImageGalleryAdapter.notifyDataSetChanged();
    }

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

                                    _refreshCurrentDirectory();
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
        int id = v.getId();
        switch (id){
            case R.id.id_btn_refresh_gallery:
                Logger.Log("back to camera");
                finish();
                break;

            case R.id.id_btn_move_photos_to_archive:
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!mImageGalleryAdapter.getItem(position).isDirectory()){
            // image was clicked
            VkShareDialogBox vkShareDialogBox = new VkShareDialogBox(this, files, position);
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
                        _refreshCurrentDirectory();
                    }
                }
            });
            vkShareDialogBox.show();
        } else {
            // directory was clicked
            FileManager.setCurrentDir(mImageGalleryAdapter.getItem(position).getFile());
            _refreshCurrentDirectory();
        }
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
