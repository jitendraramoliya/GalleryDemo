package com.gallerydemo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gallerydemo.adapter.GalleryAdapter
import com.gallerydemo.databinding.ActivityMovieListBinding
import com.gallerydemo.utils.NetworkResult
import com.gallerydemo.utils.GridSpacingItemDecoration
import com.gallerydemo.viewmodel.GalleryViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GalleryListActivity : AppCompatActivity() {

    lateinit var adapterMovie: GalleryAdapter
    private lateinit var binding: ActivityMovieListBinding
    private val galleryViewModel: GalleryViewModel by viewModels()
    private var pageNo = 300
    private var isLastPage = false
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        handleObserver()
    }


    private fun initViews() {

        // adding movie adapter
        binding.rvMovie.layoutManager = getLayoutManager()
        adapterMovie = GalleryAdapter(applicationContext)
        binding.rvMovie.adapter = adapterMovie
//        binding.rvMovie.addOnScrollListener(recyclerViewOnScrollListener)

        binding.tvBack.setOnClickListener {
            finish()
        }

        galleryViewModel.getPhotoList(pageNo)

    }

    private fun handleObserver() { // Getting movie list
        galleryViewModel.photoMutableList.observe(this) {
            when (it) {
                is NetworkResult.Error -> {
                    isLoading = false
                }

                is NetworkResult.Loading -> {
                }

                is NetworkResult.Success -> {
                    val galleryResponse = it.data!!
                    isLoading = false
                    isLastPage = false
                    print(galleryResponse.toString())
                    adapterMovie.submitList(galleryResponse)
                }
            }
        }
    }

    private fun getLayoutManager(): RecyclerView.LayoutManager {
        val orientation = resources.configuration.orientation
        val spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
        binding.rvMovie.addItemDecoration(GridSpacingItemDecoration(spanCount))
        return GridLayoutManager(this, spanCount)
    }

}