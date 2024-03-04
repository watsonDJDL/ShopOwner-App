package com.linfeng.shopowner.model;

import com.google.gson.annotations.SerializedName;
import com.linfeng.shopowner.GoodDetailInfo;

import java.util.List;

public class UpdateInfoRequest {
    @SerializedName("goodDetailInfo")
    public GoodDetailInfo goodDetailInfo;


    @SerializedName("toDeleteImgList")
    public List<String> toDeleteImgList;
}
