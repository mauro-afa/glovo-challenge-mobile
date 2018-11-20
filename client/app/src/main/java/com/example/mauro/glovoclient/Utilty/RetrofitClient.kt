package com.example.mauro.glovoclient.Utilty

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var ourInstance: Retrofit?=null

    val instance: Retrofit
        get() {
            if(ourInstance == null) {
                ourInstance = Retrofit.Builder()
                    .baseUrl("http://192.168.0.148:3000/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            }
            return ourInstance!!
        }

}