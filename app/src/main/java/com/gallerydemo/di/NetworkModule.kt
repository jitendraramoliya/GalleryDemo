package com.gallerydemo.di


import com.gallerydemo.api.ImgApiService
import com.gallerydemo.constant.Constant.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// Network module for creating retrofit object
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit{
        return Retrofit.Builder().baseUrl(BASE_URL)
            .client(httpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideAPI(retrofit: Retrofit) : ImgApiService {
        return retrofit.create(ImgApiService::class.java)
    }

    private fun httpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
        return builder.build()
    }
}



