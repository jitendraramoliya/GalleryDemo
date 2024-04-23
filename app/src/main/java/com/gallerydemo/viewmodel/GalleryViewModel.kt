package com.gallerydemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gallerydemo.pojo.GallerResponse
import com.gallerydemo.repository.ImgRepository
import com.gallerydemo.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(private val imgRepository: ImgRepository) :
    ViewModel() {

    public val photoMutableList: LiveData<NetworkResult<List<GallerResponse>>>
        get() = imgRepository.photoMutableList

    fun getPhotoList(pageNo: Int) { // Fetching movie list
        viewModelScope.launch {
            imgRepository.getImgList(pageNo)
        }
    }

}