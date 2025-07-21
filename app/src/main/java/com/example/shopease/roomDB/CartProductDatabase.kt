package com.example.shopease.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shopease.models.Product

@Database(entities = [cartProducts::class] , version = 3 , exportSchema = false)
abstract class CartProductDatabase : RoomDatabase() {
    abstract fun getproductdao() : ProductDao

    companion object{
        @Volatile
        var Instance : CartProductDatabase ?= null
        fun getDataBase(context : Context) : CartProductDatabase{
            return Instance ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext ,
                    CartProductDatabase::class.java,
                    "cart_db"
                ).fallbackToDestructiveMigration()
                .build()
                Instance = instance
                return instance
            }
        }
    }
}