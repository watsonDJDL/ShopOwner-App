package com.linfeng.shopowner;


import android.app.Application;
import android.content.Context;

public class ShopOwner extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    public static Context getContext() {
        return context;
    }
}