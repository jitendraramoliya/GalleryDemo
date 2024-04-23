package com.gallerydemo.api

import com.gallerydemo.pojo.GallerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ImgApiService {

    @GET("content/misc/media-coverages")
    suspend fun getImageList(@Query("limit")  pageNo:Int): Response<List<GallerResponse>>

}