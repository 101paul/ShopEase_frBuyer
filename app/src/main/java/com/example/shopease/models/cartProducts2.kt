package com.example.shopease.models

data class cartProducts2(
    var productTitle: String? = null,
    var productPrice: String? = null,
    var productQuantity: Int? = null,
    var productImageUrls: String? = null,
    var productUnit: String? = null,
    var productCategory: String? = null,
    var itemCount: Int? = 0 // ✅ Must match exact field name in Firebase
)
