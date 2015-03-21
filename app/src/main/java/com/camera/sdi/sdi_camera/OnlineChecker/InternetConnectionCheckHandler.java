package com.camera.sdi.sdi_camera.OnlineChecker;

import android.os.Handler;
import android.os.Message;

/**
 * This handler used to update user interface when internet connection state changed.
 * In UI client must override onInternetConnectionStateChangeListener to define what to do
 * when user takes access to the internet or when he lose network connection.
 *
 * Created by sdi on 21.03.15.
 */
public class InternetConnectionCheckHandler extends Handler {
        public final static int NO_INTERNET_CONNECTION = 1;
        public final static int CONNECTED_TO_THE_INTERNET = 2;
        public final static int UNSET = 0;

        private int mLastState = UNSET;

        public interface OnInternetConnectionStateChangeListener{
            public void online();
            public void offline();
        }

        OnInternetConnectionStateChangeListener mOnInternetConnectionStateChangeListener
                = new OnInternetConnectionStateChangeListener() {
            @Override
            public void online() {
                // do nothing by default
            }

            @Override
            public void offline() {
                // do nothing by default
            }
        };

        public void setOnInternetConnectionStateChangeListener(
                OnInternetConnectionStateChangeListener listener){
            mOnInternetConnectionStateChangeListener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NO_INTERNET_CONNECTION:
                    if (mLastState != NO_INTERNET_CONNECTION){
                        mOnInternetConnectionStateChangeListener.offline();
                    }
                    break;

                case CONNECTED_TO_THE_INTERNET:
                    if (mLastState != CONNECTED_TO_THE_INTERNET){
                        mOnInternetConnectionStateChangeListener.online();
                    }
                    break;

                default:
                    break;
            }
        }
    }
