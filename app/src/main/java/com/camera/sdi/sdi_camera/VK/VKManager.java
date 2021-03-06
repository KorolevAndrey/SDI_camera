package com.camera.sdi.sdi_camera.VK;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;

import com.camera.sdi.sdi_camera.SharedStaticAppData;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdi on 21.07.14.
 */
public class VKManager {
    public static String strVKAppID = "4470227";
    public static int intVKAppID = 4470227;
    public static long user_id = -1;

    public static String token     = "VK Access token is undef";
    public static String[] scopes  = new String[]{VKScope.WALL, VKScope.PHOTOS};


    public interface getVKAlbumsCallback{
        void onGetAlbums( List< Pair<Long, String> >  albums);
    }

    public static getVKAlbumsCallback onGetVKAlbums = null;

    public static boolean WallPostPhoto(File photo, final String message, final boolean friendsOnly){
        /*
        * загрузка фото на стену. 0 если не на страницу группы
        * */
        //long uid = 0;//Long.parseLong();
        Log.d("VK", "try to share photo");
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
                makePost(new VKAttachments(photoModel), message, friendsOnly);
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

    public static boolean UploadPhotoToAlbum(File photo){
        if (SharedStaticAppData.isOnline() == false)
            return false;

        checkAndUploadToAlbum(photo);
        return true;
    }

    private static void _createNewAlbumAndUploadInIt(final File photo){
        // create new album
        VKParameters parameters = new VKParameters().from(
                "title", "SDI_camera",
                "description", "Для тестирования <<SDI_camera>>" ,
                "privacy", "3");

        VKRequest request = new VKRequest("photos.createAlbum", parameters);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                long album_id = 0;
                try {
                    // upload photo to new album and save it id
                    album_id = response.json.getJSONObject("response").getLong("id");
                    Log.d("VK", "album [" + album_id+"] created");
                    SharedStaticAppData.save_VKAlbumId(album_id);
                    _uploadPhotoToAlbum(album_id, photo);
                } catch (JSONException e) {
                    Log.d("VK", "json error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
    }

    private static void _uploadPhotoToAlbum(final long albumId, File photo){
        final Bitmap bmpPhoto = SharedStaticAppData.rotateBitmap90Degrees(BitmapFactory.decodeFile(photo.getAbsolutePath()));
        VKRequest request = VKApi.uploadAlbumPhotoRequest(
                new VKUploadImage(bmpPhoto, VKImageParameters.jpgImage(0.9f)),
                albumId,     // album id
                0            // group id
        );

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                bmpPhoto.recycle();
                Log.d("VK","uploaded to album["+albumId+"]");
                super.onComplete(response);
            }

            @Override
            public void onError(VKError error) {
                Log.d("VK", "upload error : " + error.errorMessage);
                bmpPhoto.recycle();
                super.onError(error);
            }
        });
    }

    private static void checkAndUploadToAlbum(final File photo){
        //final long oldAlbumId = SharedStaticAppData.VK_UploadAlbumId;
        final long newAlbumId = SharedStaticAppData.restore_VKAlbumId();
        Log.d("VK","restored album id: " + newAlbumId);
        if (newAlbumId != -1){
            // check if album with id==newAlbumId exists
            VKParameters parameters = VKParameters.from("album_ids", newAlbumId);
            VKRequest request = new VKRequest("photos.getAlbums", parameters);
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    // save new album id if this album exists
                    try {
                        if (response.json.getJSONObject("response").getInt("count") > 0) {
                            // album exists. upload to it
                            Log.d("VK", "album["+newAlbumId+"] exists");
                            SharedStaticAppData.save_VKAlbumId(newAlbumId);
                            _uploadPhotoToAlbum(newAlbumId, photo);
                        } else {
                            // create new album and upload
                            _createNewAlbumAndUploadInIt(photo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    super.onComplete(response);
                }
            });
        } else {
            // check if album named "SDI_camera" exists
            VKRequest request = new VKRequest("photos.getAlbums");
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    try {
                        JSONObject root = response.json.getJSONObject("response");
                        JSONArray items = root.getJSONArray("items");
                        int count = root.getInt("count");
                        boolean found = false;
                        for (int i=0;i<count; ++i){
                            JSONObject item = items.getJSONObject(i);
                            if (item.getString("title") == "SDI_camera"){
                                found = true;
                                long album_id = item.getLong("id");
                                SharedStaticAppData.save_VKAlbumId(album_id);
                                _uploadPhotoToAlbum(album_id, photo);
                            }
                        }
                        if ( !found ){
                            _createNewAlbumAndUploadInIt(photo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static void makePost(VKAttachments vkApiAttachments) {
        makePost(vkApiAttachments, null, false);
    }
    private static void makePost(VKAttachments vkApiAttachments, String strMessage, boolean friendsOnly) {
        String strOwnerId = Long.toString(user_id); // long to string
        VKRequest post = VKApi.wall().post(VKParameters.from(
                VKApiConst.OWNER_ID    , strOwnerId,        // user_id
                VKApiConst.FRIENDS_ONLY,(friendsOnly?1:0),  // privacy
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

    /*
    * get albums from vk.com and call getVKAlbum callback to process it
    *
    * */
    public static void getVKAlbums(){
        VKRequest request = new VKRequest("photos.getAlbums");
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    List<Pair<Long, String> > albums = new ArrayList<Pair<Long, String>>();

                    // parse result (user albums)
                    JSONObject root = response.json.getJSONObject("response");
                    JSONArray items = root.getJSONArray("items");
                    int count = root.getInt("count");
                    for (int i=0;i<count; ++i){
                        JSONObject item = items.getJSONObject(i);
                        long id = item.getLong("id");
                        String title = item.getString("title");

                        Log.d("VK", "album found: "+title+" ["+id+"]");

                        albums.add(new Pair(id, title));
                    }

                    // call callback if set
                    if (onGetVKAlbums != null)
                        onGetVKAlbums.onGetAlbums(albums);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    * check if vk.com was initialized.
    * */
    public static void InitVk(){
        if (SharedStaticAppData.isVkInialized == false){
            VKAccessToken at = SharedStaticAppData.restore_AccessToken();
            Log.d("VK", "vk init");
            // first creation. Vk sdk must be initialized.
            VKSdk.initialize(vksdkListener, VKManager.strVKAppID, at);
            SharedStaticAppData.isVkInialized = true;
        }
    }

    public static VKSdkListener vksdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            /*
            * Пришла капча
            * */

            Log.d("VK", "captch error");
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            /*
            * истек срок действия токена
            * */
            VKSdk.authorize(VKManager.scopes);
            Log.d("VK", "token expired");
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            /*
            * запрет доступа
            * */
            Log.d("VK", "access denied");

        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            /*
            * получен новый токен
            * */

            Log.d("VK", "new token was received");
            SharedStaticAppData.save_VKAccessToken(newToken);
            SharedStaticAppData.save_VKUserId(Long.parseLong(newToken.userId));

            //Intent i = new Intent(VKLoginActivity.this, VkShareDialogBox.class);
            //startActivity(i);
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.d("VK", "accept");
            VKManager.user_id = Long.parseLong(token.userId);
            SharedStaticAppData.save_VKUserId(VKManager.user_id);
            SharedStaticAppData.save_VKAccessToken(token);

//            Intent i = new Intent(VKLoginActivity.this, GalleryScreen.class);
            //          startActivity(i);
            //finish();
        }
    };


}
