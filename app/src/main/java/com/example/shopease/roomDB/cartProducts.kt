package com.example.shopease.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "cartProduct")
data class cartProducts(
    @PrimaryKey
    var productRandomId: String = "random",
    var productTitle: String = "",
    var productQuantity: Int = 0,
    var productUnit: String = "",
    var productPrice: String = "",
    var productStock: Int = 0,
    var productCategory: String = "",
    var itemCount: Int = 0,
    var adminUid: String = "",
    var productImageUrls: String = ""
)