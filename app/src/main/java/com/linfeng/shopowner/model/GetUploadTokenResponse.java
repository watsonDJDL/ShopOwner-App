package com.linfeng.shopowner.model;

import com.google.gson.annotations.SerializedName;

public class GetUploadTokenResponse {
    @SerializedName("token")
    public String mToken;

    @SerializedName("result")
    public int result;
}
