package com.example.shopease.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopease.databinding.CartimageviewBinding

class CartIAdapter() : RecyclerView.Adapter<CartIAdapter.CartImageViewHolder>() { // creating this adapter
    // just to handle the inserting and deleting of selected product images in the
    // cart in useractivity or main activity

    private val productImageMap = LinkedHashMap<String, String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        positon: Int
    ): CartImageViewHolder {
        val view = CartimageviewBinding.inflate(LayoutInflater.from(parent.context),parent,false) ;
        return CartImageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartImageViewHolder,
        position: Int
    ) {
        val imageUrl = productImageMap.values.toList()[position]

        with(holder.binding.cartImage) {
            Glide.with(this)
                .load(imageUrl)
                .circleCrop() // round image
                .into(this)
        }
    }

    override fun getItemCount(): Int {
        return productImageMap.size
    }
    // Adds/updates image for product
    fun submitImage(productId: String, imageUrl: String) {

        productImageMap[productId] = imageUrl
        notifyDataSetChanged()
    }

    // Removes image for productId
    fun removeImageByProductId(productId: String) {
        if(productImageMap.containsKey(productId)){
            productImageMap.remove(productId)
            notifyDataSetChanged()
        }
    }
    fun clearAll() {
        productImageMap.clear()
        notifyDataSetChanged()
    }
    class CartImageViewHolder(val binding: CartimageviewBinding) :
        RecyclerView.ViewHolder(binding.root)
}