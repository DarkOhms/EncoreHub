package com.lukemartinrecords.encorehub.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/*
EXAMPLE SEARCH

Search for an artist Search for "Green Day".
curl -X GET "https://api.getsongbpm.com/search/?api_key=YOUR_API_KEY_HERE&type=artist&lookup=green+day"

EXAMPLE JSON RESULT

{
	"search":[
	{
		"id":"v9M",
		"name":"Green Day",
		"uri":"https:\/\/getsongbpm.com\/artist\/green-day\/v9M",
		"img":"https:\/\/lastfm-img2.akamaized.net\/i\/u\/2088cb779e7e4a828ddaea74fd0bacfa.png",
		"genres":[
			"pop",
			"punk",
			"rock"
		],
		"from":"US",
		"mbid":"084308bd-1654-436f-ba03-df6697104e19"
	}, ...
	]
}

 */
private const val SONGBPM_BASE_URL = "https://api.getsongbpm.com"

private val logging = HttpLoggingInterceptor().apply {
    this.level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(logging)
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofitMoshi = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(SONGBPM_BASE_URL)
    .client(client)
    .build()

interface GetSongBpmApi {
    @GET("/search/")
    suspend fun search(
        @Query("api_key") apiKey: String,
        @Query("type") type: String,
        @Query("lookup") lookup: String,
        @Query("limit") limit: Int)
    : Response<ResponseBody>

    @GET("/song/")
    suspend fun song(
        @Query("api_key") apiKey: String,
        @Query("id") id: String)
    : Response<ResponseBody>

}

object GetSongBpm {
    val api: GetSongBpmApi by lazy {
        retrofitMoshi.create(GetSongBpmApi::class.java)
    }
}