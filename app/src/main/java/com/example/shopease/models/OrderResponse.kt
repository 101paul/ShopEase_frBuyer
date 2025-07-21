package com.example.shopease.models

data class OrderResponse(
    val id : String , // Razorpay order_id
    val amount : Int ,
    val currency : String ,
    val receipt: String
)
