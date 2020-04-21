package com.example.w3d3_rxjavaretrofitexample.network

import com.example.w3d3_rxjavaretrofitexample.network.model.DataModel
import io.reactivex.Single
import retrofit2.http.GET

interface ApiService {

    @GET("photos")
    fun photos(): Single<MutableList<DataModel>>

}