package com.example.shopease.models

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep //@IgnoreExtraProperties

data class OrdersRetrieveFromFB(
    var OrderId : String ?= null,
    var OrderList: List<cartProducts2>? = null,
    var userAddress : String ?= null,
    var orderStatus : Int ?= 0,
    var OrderDate : String ?= null,
    var orderingUserid : String ?= null,
    var totalItemcount : Int ?= 0
)
