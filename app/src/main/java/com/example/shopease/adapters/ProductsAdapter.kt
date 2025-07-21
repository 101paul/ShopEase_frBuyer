package com.example.shopease.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.shopease.search.FilteringProducts

import com.example.shopease.adapters.ProductsAdapter.productViewHolder
import com.example.shopease.databinding.ItemViewProductBinding
import com.example.shopease.models.Product
import kotlin.toString

class ProductsAdapter(
    val addClickButton: (Product, ItemViewProductBinding) -> Unit,
    val incrementB: (Product, ItemViewProductBinding) -> Unit,
    val decrementB: (Product, ItemViewProductBinding) -> Unit,
    val function: (List<String>, String) -> Unit,
    val function1: (List<String>, String) -> Unit,
    ) : RecyclerView.Adapter<productViewHolder>(),Filterable{
    val diffUtil = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(
            oldItem: Product,
            newItem: Product
        ): Boolean {
            return oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(
            oldItem: Product,
            newItem: Product
        ): Boolean {
            return oldItem == newItem

        }
    }
    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup,position: Int): productViewHolder {
        return productViewHolder(
            ItemViewProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }


    override fun onBindViewHolder(holder: productViewHolder, position:Int) {
        holder.binding.apply{
            var current_product = differ.currentList[position]
            var imageList = ArrayList<SlideModel>()
            current_product.productImageUrls?.forEach { imageUrl ->
                imageList.add(SlideModel(imageUrl.toString()))
            }
            tvProductTitle.text = current_product.productTitle
            val quantity =  current_product.productQuantity.toString() + current_product.productUnit.toString()
            tvProductQuantity.text = quantity
            val price =  "₹"+current_product.productPrice.toString()
            tvProductPrice.text = price
            ivImageSldier.setImageList(imageList)

            if ((current_product.itemCount ?: 0) > 0) {
                tvAddText.visibility = View.GONE
                tvAddTextP.visibility = View.VISIBLE
                productcountvalue.text = current_product.itemCount.toString()
            } else {
                tvAddText.visibility = View.VISIBLE
                tvAddTextP.visibility = View.GONE
                productcountvalue.text = "0"
            }

            tvAddText.setOnClickListener{
                addClickButton(current_product,this)
            }
            incrementButton.setOnClickListener{
                incrementB(current_product,this)
            }
            decremntButton.setOnClickListener{
                decrementB(current_product,this)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var originalList = listOf<Product>()
    fun submitListWithOriginal(list: List<Product>) {
        originalList = list.toList() // toList() function is used to make sure no changes happend to the originalList , it makes a new copy of list
        differ.submitList(list.toList())
    }

    override fun getFilter(): Filter {
        return FilteringProducts(this, ArrayList(originalList)).apply {
            FilteringProducts.filterCallback = null // prevent stale callback
        }
    }
    class productViewHolder(val binding : ItemViewProductBinding) : RecyclerView.ViewHolder(binding.root)

}