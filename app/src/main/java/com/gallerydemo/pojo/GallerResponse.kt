package com.gallerydemo.pojo

import com.google.gson.annotations.SerializedName


data class GallerResponse(

    @SerializedName("id") var id: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("thumbnail") var thumbnail: Thumbnail? = Thumbnail(),
)