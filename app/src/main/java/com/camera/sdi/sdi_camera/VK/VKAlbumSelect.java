package com.camera.sdi.sdi_camera.VK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.camera.sdi.sdi_camera.R;
import com.camera.sdi.sdi_camera.SharedStaticAppData;
import com.camera.sdi.sdi_camera.VK.VKManager;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.camera.sdi.sdi_camera.R;

import java.util.List;

/**
 * Created by sdi on 28.07.14.
 */
public class VKAlbumSelect extends Activity implements AdapterView.OnItemClickListener{

    ListView lvAlbums = null;
    VKAlbumsListAdapter adapter = null;
    private List<Pair<Long, String>> itemsInList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vk_albums_list);

        // set list view
        lvAlbums = (ListView) findViewById(R.id.id_lv_vk_albums);
        lvAlbums.setOnItemClickListener(this);

        // see documentation from vk.com
        VKUIHelper.onCreate(this);

        // set callback. it will be removed to listActivity that shows user albums
        VKManager.onGetVKAlbums = new VKManager.getVKAlbumsCallback() {
            @Override
            public void onGetAlbums(List<Pair<Long, String>> albums) {
                Log.d("VK", "found " + albums.size() + " albums");

                // display albums
                itemsInList = albums;
                adapter = new VKAlbumsListAdapter(
                        getBaseContext(),
                        R.layout.list_view_row_vk_album, albums
                );

                lvAlbums.setAdapter(adapter);
            }
        };

        VKAccessToken at = SharedStaticAppData.restore_AccessToken();
        if (SharedStaticAppData.isVkInialized == false){
            Log.d("VK", "vk init");
            // first creation. Vk sdk must be initialized.
            VKSdk.initialize(vksdkListener, VKManager.strVKAppID, at);
            SharedStaticAppData.isVkInialized = true;
        }

        ((Button) findViewById(R.id.id_btn_load_vk_albums)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKManager.getVKAlbums();
            }
        });

        ((Button) findViewById(R.id.id_btn_post_on_wall)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // share via wall-post
                SharedStaticAppData.saveUploadTarget( SharedStaticAppData.UPLOAD_TARGET_WALL );
                finish();
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

    private VKSdkListener vksdkListener = new VKSdkListener() {
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
            new AlertDialog.Builder(getBaseContext())
                    .setMessage(authorizationError.errorMessage)
                    .show();

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        long nAlbumId = itemsInList.get(position).first;
        String nAlbumName = itemsInList.get(position).second;

        Log.d("VK", "new album id" + nAlbumId);
        Log.d("VK", "new album name: " + nAlbumName);

        SharedStaticAppData.save_VKAlbumId(nAlbumId);
        SharedStaticAppData.save_VKAlbumName(nAlbumName);

        SharedStaticAppData.saveUploadTarget(SharedStaticAppData.UPLOAD_TARGET_ALBUM);

        finish();
    }

    private class VKAlbumsListAdapter extends ArrayAdapter<Pair<Long, String > >{
        private Context context = null;
        private List<Pair<Long, String>> items = null;

        public VKAlbumsListAdapter(Context context, int resource, List<Pair<Long, String>> objects) {
            super(context, resource, objects);

            this.context = context;
            this.items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View row = li.inflate(R.layout.list_view_row_vk_album,parent, false);

            TextView tvAlbumTitle = (TextView) row.findViewById(R.id.id_lvr_vk_album_title);
            TextView tvAlbumId    = (TextView) row.findViewById(R.id.id_lvr_vk_album_id);

            if (tvAlbumId != null && tvAlbumTitle != null){
                long id = items.get(position).first;
                String title = items.get(position).second;

                tvAlbumId.setText(Long.toString(id));
                tvAlbumTitle.setText(title);
            }

            return row;
        }
    }

}
