package com.example.mauro.glovoclient.interfaces

import com.example.mauro.glovoclient.model.Country
import retrofit2.Call
import retrofit2.http.GET

/**
 * Name: ICountry
 *
 * Purpose: retrofit interface for Country
 */
interface  ICountry {

    @get:GET("countries/")
    val countryList: Call<ArrayList<Country>>
}