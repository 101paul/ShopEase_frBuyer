package com.example.shopease.cartInterface

interface CartInterface{
    fun showCartLayout(itemCount : Int)
    fun showImageincart(imageUrls: List<String>, productId: String)
    fun removeProductFromCart(imageUrls: List<String>, productId: String)
    fun savingCartItemCount(itemCount: Int)
    fun removeProductFromCartfromOrderPlacedActivity(imageUrl : String , productId : String)
}