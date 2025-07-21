package com.example.shopease.models

import com.example.shopease.utilites.Utils

data class OrderedItems(
    var productTitle: String = "",
    var productQuantity: Int = 0,
    var productUnit: String = "",
    var productPrice: String = "",
    var productCategory: String = "",
    var totalitemCount: Int = 0,
    var date : String="",
    val userId :String = "" ,
    var OrderId : String = "N/A",
    var OrderStatus : Int = 0 ,
    var ImageUrl : List<String> = arrayListOf<String>()
)
