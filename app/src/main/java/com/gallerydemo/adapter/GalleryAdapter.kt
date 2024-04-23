package com.gallerydemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gallerydemo.R
import com.gallerydemo.databinding.ItemMovieBinding
import com.gallerydemo.imageloading.ImageLoader
import com.gallerydemo.pojo.GallerResponse


class GalleryAdapter(private val appContext: Context) :
    RecyclerView.Adapter<GalleryAdapter.MovieViewHolder>() {

    private val list: MutableList<GallerResponse> = mutableListOf()
    private val listFiltered: MutableList<GallerResponse> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listFiltered.size
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.setItem(appContext, listFiltered.get(position))
    }

    fun submitList(movieList: List<GallerResponse>) {
        // Adding movie in list
        list.addAll(movieList)
        listFiltered.addAll(movieList)
        notifyDataSetChanged()
    }

    fun setSearchText(searchText: String) {
        // filter movie base on text
        listFiltered.clear()
        if (searchText.isNullOrEmpty()) {
            listFiltered.addAll(list)
        } else {
            listFiltered.addAll(list.filter {
                it.title!!.startsWith(
                    searchText,
                    ignoreCase = true
                )
            })
        }
        notifyDataSetChanged()
    }

    class MovieViewHolder(val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setItem(appContext: Context, movieItem: GallerResponse) {

            movieItem.thumbnail?.let {// Movie Image
                val imageUrl = it.domain + "/" + it.basePath + "/0/" + it.key
                ImageLoader(itemView.context).loadImage(
                    imageUrl,
                    binding.ivMovie,
                    R.drawable.placeholder_for_missing_posters
                )
//                Glide.with(itemView.context).load(imageUrl)
//                    .placeholder(R.drawable.placeholder_for_missing_posters)
//                    .error(R.drawable.placeholder_for_missing_posters)
//                    .into(binding.ivMovie)
            }

        }

    }
}