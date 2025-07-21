package com.example.shopease.retrofitInterface

import androidx.room.Index
import com.example.shopease.models.OrderRequest
import com.example.shopease.models.OrderResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RazorpayApi {
    @POST("/api/payment/createOrder")
    fun createOrder(@Body orderData : OrderRequest) : Call<OrderResponse>
    // @Body tell retrofit to convert this oject to JSON object
}