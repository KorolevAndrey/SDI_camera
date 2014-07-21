package com.camera.sdi.sdi_camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
    // end of finals block
    //--------------------

    private static Context context = null;
    private static File baseDir = null;

    public  static boolean isVkInialized = false;
    private static SharedPreferences sharedPreferences = null;
    private static String sharedPreferencesTag   = "";
    private static String sp_key_VK_USER_ID      = ""; // shared preferences key for user id in vk.com
    private static String sp_key_VK_ACCESS_TOKEN = ""; // shared preferences access token in vk.com

    public SharedStaticAppData(Context context){
        this.context = context;
        sharedPreferencesTag = context.getString(R.string.shared_preferences_tag);
        sharedPreferences = context.getSharedPreferences(sharedPreferencesTag, Context.MODE_PRIVATE);
        sp_key_VK_USER_ID = context.getString(R.string.sp_long_vk_user_id);
        Log.d("VK", "user_id_spKey: " + sp_key_VK_USER_ID);
        Log.d("VK", "try to restore user_id: " + restore_VKUserId());
    }

    public static long restore_VKUserId(){
        return sharedPreferences.getLong(sp_key_VK_USER_ID, -1);
    }

    public static VKAccessToken restore_AccessToken(){
        VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(context, sp_key_VK_ACCESS_TOKEN);
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

    public static Bitmap rotateBitmap90Degrees(Bitmap bitmap){
        Matrix mtx = new Matrix();
        mtx.postRotate(90);

        Bitmap rotated = Bitmap.createBitmap(bitmap,
                0                 ,0                  ,
                bitmap.getWidth() , bitmap.getHeight(),
                mtx, true);

        return rotated;
    }

    public static void setBaseDir(File nBaseDir){
        baseDir = nBaseDir;
    }

    public static File getBaseDir(){return baseDir;}
}