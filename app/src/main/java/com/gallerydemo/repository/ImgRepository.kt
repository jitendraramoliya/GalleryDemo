package com.gallerydemo.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gallerydemo.api.ImgApiService
import com.gallerydemo.pojo.GallerResponse
import com.gallerydemo.utils.NetworkResult
import javax.inject.Inject

open class ImgRepository @Inject constructor(private val imgApiService: ImgApiService) {

    private var _photoMutableList = MutableLiveData<NetworkResult<List<GallerResponse>>>()
    public val photoMutableList: LiveData<NetworkResult<List<GallerResponse>>>
        get() = _photoMutableList

    suspend fun getImgList(pageNo: Int) {
        val response = imgApiService.getImageList(pageNo)
        if (response.isSuccessful && response.body() != null) {
            println(response.body().toString())
            _photoMutableList.postValue(NetworkResult.Success(response.body()!!))
        }else{
            _photoMutableList.postValue(NetworkResult.Error("Something went wrong"))
        }
    }

}