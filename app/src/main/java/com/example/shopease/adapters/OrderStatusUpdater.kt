package com.example.shopease.adapters

interface OrderStatusUpdater {
    fun updateOrderStatus(orderId: String, status: Int)
}