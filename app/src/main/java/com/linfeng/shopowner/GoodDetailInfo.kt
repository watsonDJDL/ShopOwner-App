package com.linfeng.shopowner

import android.net.Uri
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * 单个商品详细信息数据
 */
data class GoodDetailInfo(
        @SerializedName("id")
        var id: Int = -1,
        @SerializedName("name")
        var name: String = "",
        @SerializedName("des")
        var des: String = "",
        @SerializedName("img")
        var img: String = "",
        @SerializedName("imgs")
        var imgs: List<String> = ArrayList(),
        @SerializedName("type")
        var type: Int = -1,
        @SerializedName("tag")
        var tag: String = "",
        @SerializedName("num")
        var num: Int = 0,
        @SerializedName("price")
        var price: Float = 0F,

        ): Serializable {
        var locaImgs: List<Uri> = ArrayList()
}
