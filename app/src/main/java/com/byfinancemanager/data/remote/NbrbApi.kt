package com.byfinancemanager.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NbrbApi {
    // https://api.nbrb.by/exrates/rates/USD?ondate=2023-06-15&parammode=2
    @GET("exrates/rates/USD")
    suspend fun getUsdRateByDate(
        @Query("ondate") onDate: String,
        @Query("parammode") paramMode: Int = 2
    ): NbrbRate

    // Текущий курс
    @GET("exrates/rates/USD")
    suspend fun getCurrentUsdRate(
        @Query("parammode") paramMode: Int = 2
    ): NbrbRate

    // Альтернативный вариант по ID валюты 431 = USD
    @GET("exrates/rates/431")
    suspend fun getUsdRateByIdAndDate(
        @Query("ondate") onDate: String,
        @Query("parammode") paramMode: Int = 2
    ): NbrbRate
}
