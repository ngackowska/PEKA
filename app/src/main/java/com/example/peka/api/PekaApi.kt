package com.example.peka.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST


interface PekaApi {
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @FormUrlEncoded
    @POST("vm/method.vm")
    suspend fun getStopPoints(
        @Field("method") method: String = "getTimes",
        @Field("p0") p0: String = "{\"symbol\":\"TRAU43\"}"
    ): PekaResponse<StopsData>
}


val retrofit = Retrofit.Builder()
    .baseUrl("https://www.peka.poznan.pl/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val pekaApiService: PekaApi = retrofit.create(PekaApi::class.java)