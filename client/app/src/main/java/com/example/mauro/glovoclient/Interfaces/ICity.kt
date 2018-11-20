package com.example.mauro.glovoclient.Interfaces

import com.example.mauro.glovoclient.Model.Cities
import com.example.mauro.glovoclient.Model.City
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ICity {

    @get:GET("cities")
    val cityList: Call<ArrayList<Cities>>

    @GET("cities/{city_code}")
    fun getCityDetail(@Path("city_code") cityCode: String): Call<City>
}