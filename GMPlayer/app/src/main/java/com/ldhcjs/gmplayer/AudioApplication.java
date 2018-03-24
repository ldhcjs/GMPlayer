package com.ldhcjs.gmplayer;

import android.app.Application;

/**
 * Created by tony.lee on 2018-02-27.
 */

public class AudioApplication extends Application {
    private static AudioApplication mInstance;
    private AudioServiceInterface mInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mInterface = new AudioServiceInterface(getApplicationContext());
    }

    public static AudioApplication getInstance() {
        return mInstance;
    }

    public AudioServiceInterface getServiceInterface() {
        return mInterface;
    }
}
