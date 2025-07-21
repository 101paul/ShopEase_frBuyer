package com.example.shopease.models

data class bestseller(
    var productId: String = "",
    var name: String = "",
    var price: Int = 0,
    var discountPercent: Int = 0,
    var imageUrl: ArrayList<String> = ArrayList<String>(),
    var isBestSeller: Boolean = false ,
    var itemCount : Int = 0 ,
    var itemUnit : String = "" ,
    var productstock : Int = 0 ,
    var productcategory : String = "" ,
    var userId : String = ""
)
