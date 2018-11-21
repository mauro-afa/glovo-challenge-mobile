package com.example.mauro.glovoclient.interfaces

import com.example.mauro.glovoclient.model.SimpleCity
import com.example.mauro.glovoclient.model.City
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Name: ICity
 *
 * Purpose: retrofit interface for City and SimpleCity Object
 */
interface ICity {

    @get:GET("cities")
    val cityList: Call<ArrayList<SimpleCity>>

    @GET("cities/{city_code}")
    fun getCityDetail(@Path("city_code") cityCode: String): Call<City>
}