package com.example.shopease.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopease.databinding.ItemViewBestSellerBinding
import com.example.shopease.models.Product
import com.example.shopease.R

class BestSellerAdapter(
    val OnAdd : (Product, ItemViewBestSellerBinding) -> Unit,
    val OnIncre : (Product, ItemViewBestSellerBinding) -> Unit,
    val OnDre : (Product, ItemViewBestSellerBinding) -> Unit
) : RecyclerView.Adapter<BestSellerAdapter.ViewHolder>() {

    val diffUtil = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(
            p0: Product,
            p1: Product
        ): Boolean {
            return p0.productRandomId == p1.productRandomId
        }

        override fun areContentsTheSame(
            p0: Product,
            p1: Product
        ): Boolean {
            return p0 == p1
        }

    }

    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        p1: Int
    ): ViewHolder {
return  ViewHolder(ItemViewBestSellerBinding.inflate(LayoutInflater.from(parent.context),parent,false))}

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val product = differ.currentList[position]
        val binding = holder.binding

        binding.textProductName.text = product.productTitle

        val price = product.productPrice?.toString()?.toIntOrNull()
        binding.textProductPrice.text = price?.let { "₹$it" } ?: "N/A"

        Glide.with(holder.itemView.context)
            .load(product.productImageUrls?.getOrNull(0))
            .fitCenter()
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(binding.imageProduct)

        // Show quantity layout if itemCount > 0
        if (product.itemCount.toString().toInt() > 0) {
            binding.tvAddText.visibility = View.GONE
            binding.tvAddTextP.visibility = View.VISIBLE
            binding.productcountvalue.text = product.itemCount.toString()
        } else {
            binding.tvAddText.visibility = View.VISIBLE
            binding.tvAddTextP.visibility = View.GONE
            binding.productcountvalue.text = "0"
        }

        // Fix click mapping
        binding.tvAddText.setOnClickListener {
            OnAdd(product, binding)
        }

        binding.incrementButton.setOnClickListener {
            OnIncre(product, binding)
        }

        binding.decremntButton.setOnClickListener {
            OnDre(product, binding)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    class ViewHolder(val binding : ItemViewBestSellerBinding) : RecyclerView.ViewHolder(binding.root)
}