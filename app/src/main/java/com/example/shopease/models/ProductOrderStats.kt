package com.example.shopease.models

data class ProductOrderStats(
    val productId: String,
    var totalQuantity: Int = 0,
    var lastOrderedDate: String = ""
)
