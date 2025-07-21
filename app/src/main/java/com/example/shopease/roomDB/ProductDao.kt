package com.example.shopease.roomDB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(products : cartProducts)
//    @Update
//    fun updateProduct(product : cartProducts)
    @Query("DELETE FROM cartProduct WHERE productRandomId = :productId")
    suspend fun deleteCartProduct(productId : String)
    @Query("DELETE FROM cartProduct")
    suspend fun deleteAllCartProducts()
    @Query("SELECT * FROM cartProduct")
    suspend fun getallproducts() : List<cartProducts>
    @Query("SELECT  itemCount FROM cartProduct WHERE productRandomId = :productId")
    suspend fun getproudctQuantity(productId : String) : Int
    @Query("UPDATE cartProduct SET itemCount = 0")
    suspend fun resetAllItemCounts()
}