package com.camera.sdi.sdi_camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKScopes;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import java.io.File;

/**
 * Created by sdi on 21.07.14.
 */
public class VKManager {
    public static String strVKAppID = "4470227";
    public static int intVKAppID = 4470227;
    public static long user_id = -1;

    public static String token     = "VK Access token is undef";
    public static String[] scopes  = new String[]{VKScope.WALL, VKScope.PHOTOS};

    public static boolean WallPostPhoto(File photo){
        /*
        * загрузка фото на стену. 0 если не на страницу группы
        * */
        //long uid = 0;//Long.parseLong();
        user_id = SharedStaticAppData.restore_VKUserId();
        if (user_id == -1) return false;

        VKUploadImage img = new VKUploadImage(BitmapFactory.decodeFile(photo.getAbsolutePath()), VKImageParameters.jpgImage(0.9f));

        VKRequest request = VKApi.uploadWallPhotoRequest(photo, user_id, 0);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                Log.d("VK", "photo upload complete");

                // фото на серве. Todo: wallpost [completed]
                VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                makePost(new VKAttachments(photoModel));
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                Log.d("VK", "attempts: " + attemptNumber + " total: " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                Log.d("VK", "error while uploading photo: " + error.errorMessage);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                Log.d("VK", "progress [ " + bytesLoaded + " / " + bytesTotal + " ]");
            }
        });
        return true;
    }

    private static void makePost(VKAttachments vkApiAttachments) {
        makePost(vkApiAttachments, null);
    }
    private static void makePost(VKAttachments vkApiAttachments, String strMessage) {
        String strOwnerId = Long.toString(user_id); // long to string
        VKRequest post = VKApi.wall().post(VKParameters.from(
                VKApiConst.OWNER_ID    , strOwnerId,        // user_id
                VKApiConst.ATTACHMENTS , vkApiAttachments,  // params
                VKApiConst.MESSAGE     , strMessage));      // message if not null
        post.setModelClass(VKWallPostResult.class);         // WTF ? O_o
        post.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                Log.d("VK", "wall post complete");
                //Intent i = new Intent( Intent.ACTION_VIEW, Uri.parse() );
                Log.d("VK", "url : " +
                        String.format("https://vk.com//wall86410922_%s", ((VKWallPostResult) response.parsedModel).post_id));
            }
        });
    }
}
