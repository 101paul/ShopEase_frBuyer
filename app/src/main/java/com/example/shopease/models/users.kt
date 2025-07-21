package com.example.shopease.models

data class users(
    val uid : String?= null,
    val userPhnNumber : String ?= null,
    val userAddress : String?= null,
    val userName : String ?= null,
    var userToken : String ?= null
)
