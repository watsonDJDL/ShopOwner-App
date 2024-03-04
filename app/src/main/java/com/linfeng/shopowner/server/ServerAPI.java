package com.linfeng.shopowner.server;

import com.linfeng.shopowner.GoodDetailInfo;
import com.linfeng.shopowner.GoodInfo;
import com.linfeng.shopowner.model.BaseResponse;
import com.linfeng.shopowner.model.GetUploadTokenResponse;
import com.linfeng.shopowner.model.UpdateInfoRequest;
import com.linfeng.shopowner.model.UpdateInfoResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerAPI {
    @GET("goods")
    Observable<List<GoodInfo>> getGoodsList();

    @GET("goodDetail")
    Observable<GoodDetailInfo> getGoodDetailInfo(@Query("goodId") int goodId);

    // 更新或创建新的good
    @POST("updateGoodInfo")
    Observable<UpdateInfoResponse> updateGoodInfo( @Body UpdateInfoRequest request);

    @GET("goodUploadToken")
    Observable<GetUploadTokenResponse> getGoodUploadToken();

    @POST("deleteGoodItem")
    Observable<BaseResponse> deleteGoodItem(@Body List<Integer> toDeleteList);
}
