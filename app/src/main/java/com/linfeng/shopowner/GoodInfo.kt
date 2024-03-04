package com.linfeng.shopowner

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 商品信息数据
 */
data class GoodInfo (
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("des")
        val des: String,
        @SerializedName("img")
        val img: String,
        @SerializedName("type")
        val type: Int,
        @SerializedName("tag")
        val tag: String,
        @SerializedName("num")
        val num: Int,
        @SerializedName("price")
        val price: Float): Serializable {
}