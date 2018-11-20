package com.example.mauro.glovoclient.Interfaces

import com.example.mauro.glovoclient.Model.Country
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface  ICountry {

    @get:GET("countries/")
    val countryList: Call<ArrayList<Country>>
}