package com.example.shopease.models

import com.example.shopease.roomDB.cartProducts

data class Orders(
    val OrderId : String ?= null,
    val OrderList: List<cartProducts>? = null,
    val userId : String ?= null ,
    val userAddress : String ?= null,
    val orderStatus : Int ?= 0,
    val OrderDate : String ?= null,
    val orderingUserid : String ?= null,
    val totalItemcount : Int ?= 0
)
