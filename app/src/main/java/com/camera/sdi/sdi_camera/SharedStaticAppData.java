package com.camera.sdi.sdi_camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.vk.sdk.VKAccessToken;

import java.io.File;

/**
 * Created by sdi on 20.07.14.
 */
public class SharedStaticAppData {
    // finals block
    public static final int     CAMERA_ID  = 0;
    public static final boolean FULLSCREEN = true;
    public static final int     IMAGE_LEAF_NEXT = 200;
    public static final int     IMAGE_LEAF_PREV = -200;
    // end of finals block
    //--------------------

    private static Context context = null;
    private static File baseDir = null;
    private static boolean VK_UPLOAD_TO_ALBUM = true; // true -- upload to album; false -- on wall

    // this variable show if alive asyncTask, that check your internet connection
    // asyncTask will be stoped when this variable will set to "false"
    // using in asynctask: while (isAlive){...}
    public static boolean isAlive_AsyncTaskOnlineStatusRefresher = false;
    public static boolean UPLOAD_TARGET_ALBUM = true;
    public static boolean UPLOAD_TARGET_WALL  = false;

    public static long VK_UploadAlbumId = -1;

    public  static boolean isVkInialized = false;
    private static SharedPreferences sharedPreferences = null;
    private static String sharedPreferencesTag   = "";
    private static String sp_key_VK_USER_ID      = ""; // shared preferences key for user id in vk.com
    private static String sp_key_VK_ACCESS_TOKEN = ""; // shared preferences access token in vk.com
    private static String sp_key_VK_ALBUM_ID     = ""; // shared preferences album to upload photo (album_id)
    private static String sp_key_VK_ALBUM_TITLE  = ""; // shared preferences album to upload photo (album_title)
    private static String sp_key_VK_UPLOAD_TARGET= ""; // shared preferences album to upload target (wall or album)

    public SharedStaticAppData(Context context){
        this.context = context;
        sharedPreferencesTag = context.getString(R.string.shared_preferences_tag);
        sharedPreferences    = context.getSharedPreferences(sharedPreferencesTag, Context.MODE_PRIVATE);
        sp_key_VK_USER_ID    = context.getString(R.string.sp_long_vk_user_id);
        sp_key_VK_ALBUM_ID   = context.getString(R.string.sp_long_vk_album_id);
        sp_key_VK_ALBUM_TITLE= context.getString(R.string.sp_long_vk_album_title);
        sp_key_VK_UPLOAD_TARGET= context.getString(R.string.sp_boolean_vk_upload_target);

        Log.d("VK", "user_id_spKey: " + sp_key_VK_USER_ID);
        Log.d("VK", "try to restore user_id: " + restore_VKUserId());
        Log.d("VK", "try to restore album_id: " + restore_VKAlbumId());
    }

    public static boolean isUploadToVKAlbum(){ return VK_UPLOAD_TO_ALBUM;}
    public static void saveUploadTarget(boolean isUploadToVKAlbum){
        VK_UPLOAD_TO_ALBUM = isUploadToVKAlbum;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(sp_key_VK_UPLOAD_TARGET, isUploadToVKAlbum);

        editor.commit();
    }

    public static boolean restore_VKUploadTarget(){
        VK_UPLOAD_TO_ALBUM = sharedPreferences.getBoolean(
                sp_key_VK_UPLOAD_TARGET,
                UPLOAD_TARGET_WALL
        );
        return VK_UPLOAD_TO_ALBUM;
    }

    public static long restore_VKUserId(){
        return sharedPreferences.getLong(sp_key_VK_USER_ID, -1);
    }

    public static long restore_VKAlbumId(){
        return sharedPreferences.getLong(sp_key_VK_ALBUM_ID, -1);
    }

    public static String restore_VKAlbumTitle(){
        return sharedPreferences.getString(
                sp_key_VK_ALBUM_TITLE,
                context.getString(R.string.vk_default_album_name)
        );
    }

    public static VKAccessToken restore_AccessToken(){
        VKAccessToken token =
                VKAccessToken.tokenFromSharedPreferences(context, sp_key_VK_ACCESS_TOKEN);
        Log.d("VK", "token "+(token != null ? token.accessToken : " [null] ")  + " restored");
        return token;
    }

    public static void save_VKAccessToken(VKAccessToken access_token){
        Log.d("VK", "token : " + access_token.accessToken + " stored");
        access_token.saveTokenToSharedPreferences(context, sp_key_VK_ACCESS_TOKEN);
    }

    public static void save_VKUserId(long nuser_id){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(sp_key_VK_USER_ID, nuser_id);

        editor.commit();
    }

    public static void save_VKAlbumName(String albumName){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(sp_key_VK_ALBUM_TITLE, albumName);

        editor.commit();
    }

    public static void save_VKAlbumId(long album_id){
        VK_UploadAlbumId = album_id;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(sp_key_VK_ALBUM_ID, album_id);

        editor.commit();
    }

    public static Bitmap rotateBitmap90Degrees(Bitmap bitmap){
        Matrix mtx = new Matrix();
        mtx.postRotate(90);

        Bitmap rotated = Bitmap.createBitmap(bitmap,
                0                 ,0                  ,
                bitmap.getWidth() , bitmap.getHeight(),
                mtx, true);

        //bitmap.recycle();
        return rotated;
    }

    public static boolean isOnline(){
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    public static void setBaseDir(File nBaseDir){
        baseDir = nBaseDir;
    }

    public static File getBaseDir(){return baseDir;}
}
