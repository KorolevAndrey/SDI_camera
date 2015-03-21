package com.camera.sdi.sdi_camera.OnlineChecker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;

/**
 * Every second this internet connection checked here.
 * Message with internet connection status sends to InternetConnectionHandler, that has
 * access to the UI thread.
 *
 * Created by sdi on 21.03.15.
 */
public class InternetConnectionCheckerThread extends Thread{

    private InternetConnectionCheckHandler mHandler = null;
    private final static int MS_TIMEOUT = 1000; // update every second
    private Context mContext = null;

    private boolean mIsAlive = true;

    private ConnectivityManager mConnectivityManager = null;

    public InternetConnectionCheckerThread(Context context, InternetConnectionCheckHandler handler){
        mHandler = handler;
        mContext = context;
        mConnectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * @return true if device has access to the internet and false otherwise
     * */
    private boolean checkOnline(){
        try {
            NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Exception e){
            return false;
        }
    }

    /**
     * this method sends data to the main thread.
     * */
    private void sendMessageToUI(){
        boolean online = checkOnline();
        Message msgToUI = new Message();
        msgToUI.what = online ? mHandler.CONNECTED_TO_THE_INTERNET : mHandler.NO_INTERNET_CONNECTION;
        mHandler.sendMessage(msgToUI);
    }

    public void stopThread(){
        mIsAlive = false;
    }

    @Override
    public void run() {
        while (mIsAlive){
            sendMessageToUI();

            // wait for 1 second
            try { sleep(MS_TIMEOUT); } catch (InterruptedException e) {};
        }
    }
}
