package com.example.mauro.glovoclient.utility

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var ourInstance: Retrofit?=null

    val instance: Retrofit
        get() {
            if(ourInstance == null) {
                ourInstance = Retrofit.Builder()
                    .baseUrl("http://127.0.0.1:3000/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            }
            return ourInstance!!
        }

}