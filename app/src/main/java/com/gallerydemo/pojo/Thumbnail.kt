package com.gallerydemo.pojo

import com.google.gson.annotations.SerializedName


data class Thumbnail(

    @SerializedName("id") var id: String? = null,
    @SerializedName("version") var version: Int? = null,
    @SerializedName("domain") var domain: String? = null,
    @SerializedName("basePath") var basePath: String? = null,
    @SerializedName("key") var key: String? = null,
)