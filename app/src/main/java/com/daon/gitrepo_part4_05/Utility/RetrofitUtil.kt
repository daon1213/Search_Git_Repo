package com.daon.gitrepo_part4_05.Utility

import androidx.viewbinding.BuildConfig
import com.daon.gitrepo_part4_05.model.Url.GITHUB_BASE_URL
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

object RetrofitUtil {

    val authApiService: AuthApiService by lazy { getAuthApiRetrofit().create(AuthApiService::class.java) }

    private fun getAuthApiRetrofit() = Retrofit.Builder()
        .baseUrl(com.daon.gitrepo_part4_05.model.Url.GITHUB_BASE_URL)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
            )
        )
        .client(buildOkHttpClient())
        .build()

    val searchApiService: SearchApiService by lazy { getGitApiRetrofit().create(SearchApiService::class.java) }

    private fun getGitApiRetrofit() = Retrofit.Builder()
        .baseUrl(com.daon.gitrepo_part4_05.model.Url.GITHUB_API_URL)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
            )
        )
        .client(buildOkHttpClient())
        .build()

    private fun buildOkHttpClient() = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()
}