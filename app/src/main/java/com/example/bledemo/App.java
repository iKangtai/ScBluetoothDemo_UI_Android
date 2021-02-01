package com.example.bledemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class App extends Application {
    private static App _instance;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        registerAppLifecycle();
    }

    public static App getInstance() {
        return _instance;
    }


    private int isForeground;

    public boolean isForeground() {
        return isForeground > 0;
    }

    private void registerAppLifecycle() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                isForeground++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                isForeground--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }
}
