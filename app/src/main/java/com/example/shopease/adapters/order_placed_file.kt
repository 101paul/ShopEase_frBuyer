package com.example.shopease.adapters

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.databinding.ItemViewProductBinding
import com.example.shopease.databinding.OrderplacedproductsitemviewBinding
import com.example.shopease.models.Product
import com.example.shopease.roomDB.cartProducts

class viewHolder(val binding: OrderplacedproductsitemviewBinding) : RecyclerView.ViewHolder(binding.root)

class diffUtil1 : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(
        newItem: Product,
        oldItem: Product
    ): Boolean {
        return newItem.productRandomId == oldItem.productRandomId

    }

    override fun areContentsTheSame(
        newContent: Product,
        oldContent: Product
    ): Boolean {
        return newContent == oldContent
    }
}

class diffUtil : DiffUtil.ItemCallback<cartProducts>(){
    override fun areItemsTheSame(
        newItem: cartProducts,
        oldItem: cartProducts
    ): Boolean {
        return newItem.productRandomId == oldItem.productRandomId
    }

    override fun areContentsTheSame(
        newContent: cartProducts,
        oldContent: cartProducts
    ): Boolean {
        return newContent == oldContent
    }

}