package com.camera.sdi.sdi_camera.Instagram;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.camera.sdi.sdi_camera.R;
import com.camera.sdi.sdi_camera.SharedStaticAppData;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sdi on 30.07.14.
 */
public class InstagramPhotoShare extends Dialog{

    IntagramGetAccessTokenTask getAccessTokenTask = null;

    File sharedFile = null;

    public InstagramPhotoShare(Context context, File sharedFile) {
        super(context);

        this.sharedFile = sharedFile;
    }

    private String _createAuthString (){
        String authString = SharedStaticAppData.INSTAGRAM_AUTHURL +
                "?client_id=" + SharedStaticAppData.INSTAGRAM_CLIENT_ID +
                "&redirect_uri=" + SharedStaticAppData.INSTAGRAM_CALLBACKURL +
                "&response_type=code";
        Log.d("Instagram", "auth string : " + authString);

        return authString;
    }

    private String _streamToString(InputStream is){
        try{
            BufferedReader br;
            StringBuffer sb = new StringBuffer();
            br = new BufferedReader(new InputStreamReader(is));
            String buf =br.readLine();
            while (buf != null){
                sb.append(buf);
                buf = br.readLine();
            }
            return sb.toString();
        }catch (Exception e){
            Log.d("Instagram", "error: "+ e.getMessage());
        }
        return  "";
    }

    private String _createTokenString (){
        String tokenString = SharedStaticAppData.INSTAGRAM_TOKENURL +
                "?client_id=" + SharedStaticAppData.INSTAGRAM_CLIENT_ID +
                "&client_secret=" + SharedStaticAppData.INSTAGRAM_CLIENT_SECRET +
                "&redirect_uri=" + SharedStaticAppData.INSTAGRAM_CALLBACKURL +
                "&grant_type=authorization_code";
        Log.d("Instagram", "token string : " + tokenString);

        return tokenString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_instagram_registration_form);

        //((Button) findViewById(R.id.id_instagram_registration_btn_register)).setOnClickListener(this);
        //((Button) findViewById(R.id.id_instagram_registration_btn_cancel)).setOnClickListener(this);

        Intent i = getContext()
                .getPackageManager()
                .getLaunchIntentForPackage("com.instagram.android");

        if (i == null){
            // ask user to download instagramm app
            i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse("market://details?id=com.instagram.android"));
            getContext().startActivity(i);
        } else {

            ((LinearLayout)(findViewById(R.id.id_tv_instagram_loading)).getParent()).removeAllViews();

            WebView webView = new WebView(getContext());
            webView.setVerticalScrollBarEnabled(true);
            webView.setHorizontalScrollBarEnabled(true);
            webView.setWebViewClient(new InstagramWebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(_createAuthString());

            this.addContentView(webView, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );
        }
    }

    class InstagramWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(SharedStaticAppData.INSTAGRAM_CALLBACKURL) ){
                Log.d("Instagram", "current url: " + url);
                SharedStaticAppData.save_InstagramAccessToken(url.split("=") [ 1 ]);
                getAccessTokenTask = new IntagramGetAccessTokenTask();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    getAccessTokenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                } else {
                    getAccessTokenTask.execute();
                }

                return true;
            }

            return false;
        }
    }

    class IntagramGetAccessTokenTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //(new InstagramPostPhoto()).execute();

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage("com.instagram.android");

            try {
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(
                                        getContext().getContentResolver(), sharedFile.getAbsolutePath(),
                                        "name", "description")
                        )
                );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            shareIntent.setType("image/jpeg");

            getContext().startActivity(shareIntent);

            dismiss();

        }

        @Override
        protected Void doInBackground(Void... params) {

            try{
                Log.d("Instagram", "get access token task");

                URL url = new URL(_createTokenString());
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(true);

                OutputStreamWriter osw = new OutputStreamWriter(
                        httpsURLConnection.getOutputStream()
                );

                String code = SharedStaticAppData.restore_InstagramAccessToken();
                osw.write("client_id="+SharedStaticAppData.INSTAGRAM_CLIENT_ID +
                                "&client_secret="+SharedStaticAppData.INSTAGRAM_CLIENT_SECRET +
                                "&grant_type=authorization_code" +
                                "&redirect_uri=" + SharedStaticAppData.INSTAGRAM_REDIRECT_URL+
                                "&code="+code

                );

                osw.flush();

                int responseCode = httpsURLConnection.getResponseCode();

                InputStream is = httpsURLConnection.getInputStream();
                String response = _streamToString(is);
                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                String access_token = jsonObject.getString("access_token");
                SharedStaticAppData.save_InstagramAccessToken(access_token);

                String id = jsonObject.getJSONObject("user").getString("id");
                String userName = jsonObject.getJSONObject("user").getString("username");
                Log.d("Instagram", "access_token: " + access_token);
                Log.d("Instagram", "id: " + id);
                Log.d("Instagram", "username: " + userName);
            }catch (Exception e) {
                Log.d("Instagram", "error: "+ e.getMessage());
            }

            return null;
        }
    }

    class InstagramPostPhoto extends AsyncTask<Void, Void, Void>{
        private void _postPhoto(){
            try {
                //URL url = new URL("http://instagr.am/api/v1/media/upload/?access_token=" +
                //        SharedStaticAppData.restore_InstagramAccessToken());
                URL url = new URL("http://instagr.am/api/v1/media/upload/");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStreamWriter osw = new OutputStreamWriter(
                        httpURLConnection.getOutputStream()
                );

                Long tsLong = System.currentTimeMillis()/1000;
                Double lat = 40 + 2*(new Random()).nextDouble();
                Double lng = 2 + (new Random()).nextDouble();
                osw.write("device_timestamp=" + tsLong.toString()+
                                "lat=" + lat.toString() +
                                "lng=" + lng.toString() +
                                "photo="
                );


                osw.write((new FileReader(sharedFile)).read());

                osw.flush();

                int responseCode = httpURLConnection.getResponseCode();

                InputStream is = httpURLConnection.getInputStream();
                String response = _streamToString(is);
            }catch (Exception e){
                Log.d("Instagram", "error: " + e.getMessage());
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            _postPhoto();
            return null;
        }
    }
}
