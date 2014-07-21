package com.camera.sdi.sdi_camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;

/**
 * Created by sdi on 21.07.14.
 */
public class VKLoginActivity extends Activity {
    final static String sTokenKey = "VK_ACCESS_TOKEN_KEY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("VK", "created");
        VKAccessToken at = SharedStaticAppData.restore_AccessToken();
        VKSdk.initialize(vksdkListener, VKManager.strVKAppID, at);

        if (VKSdk.isLoggedIn()) {
            Log.d("VK", "inited");
        }else {
            VKSdk.authorize(VKManager.scopes, false, false);
            // NOTE: if(){..} must be called before setContent
            if (SharedStaticAppData.FULLSCREEN) {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            setContentView(R.layout.activity_vk_login);

            Log.d("VK", "auth");
            ((Button) findViewById(R.id.id_btn_vk_auth_finished)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
        Log.d("VK", "onresume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
        Log.d("VK", "destroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
        Log.d("VK", "activity result");
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
            new AlertDialog.Builder(VKLoginActivity.this)
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

            Intent i = new Intent(VKLoginActivity.this, GalleryScreen.class);
            startActivity(i);
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.d("VK", "accept");
            VKManager.user_id = Long.parseLong(token.userId);
            SharedStaticAppData.save_VKUserId(VKManager.user_id);
            SharedStaticAppData.save_VKAccessToken(token);

            Intent i = new Intent(VKLoginActivity.this, GalleryScreen.class);
            startActivity(i);
            //finish();
        }
    };

}
