package com.linfeng.shopowner.server;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiServiceManager {
    private static final String BASE_URL = "https://yulan.work:8000";

    private ApiServiceManager() {}

    private static class ApiServiceManagerHolder {
        private static final ApiServiceManager sInstance = new ApiServiceManager();
    }

    public static ApiServiceManager getInstance() {
        return ApiServiceManagerHolder.sInstance;
    }


    public ServerAPI getAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // 设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) // 设置数据解析器
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        return retrofit.create(ServerAPI.class);
    }

}
