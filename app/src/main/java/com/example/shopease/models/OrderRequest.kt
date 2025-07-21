package com.example.shopease.models

data class OrderRequest(
    val amount : Int  ,
    val currency : String ,
    val receipt : String
)
