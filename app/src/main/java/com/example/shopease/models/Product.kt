package com.example.shopease.models

import java.util.UUID

data class Product(
   var productRandomId : String ?= null ,
   var productTitle : String ?= null ,
   var productQuantity : Int ? = null ,
   var productUnit : String ? = null ,
   var productPrice : Int ? = null ,
   var productStock : Int ? = null ,
   var productCategory  : String ? = null ,
   var itemCount : Int ?= null ,
   var adminUid : String ? = null ,
   var productImageUrls : ArrayList<String>? = null
)
