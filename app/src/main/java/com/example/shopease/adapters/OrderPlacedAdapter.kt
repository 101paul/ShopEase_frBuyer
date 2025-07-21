package com.example.shopease.adapters
import com.example.shopease.R
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.models.SlideModel
import com.example.shopease.databinding.ItemViewProductBinding
import com.example.shopease.databinding.OrderplacedproductsitemviewBinding
import com.example.shopease.models.Product
import com.example.shopease.roomDB.cartProducts

class OrderPlacedAdapter(
    val incrementB: (cartProducts, OrderplacedproductsitemviewBinding) -> Unit,
    val decrementB: (cartProducts, OrderplacedproductsitemviewBinding) -> Unit
) : RecyclerView.Adapter<viewHolder>(){
    private lateinit var binding : OrderplacedproductsitemviewBinding
    val differ = AsyncListDiffer(this,diffUtil())
    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int
    ): viewHolder {
        binding = OrderplacedproductsitemviewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int){
        val currentProduct = differ.currentList.get(position)
        holder.binding.apply {

            tvProductTitle.text = currentProduct.productTitle
            tvProductQuantity.text = "${currentProduct.productQuantity}${currentProduct.productUnit}"
            tvProductPrice.text = "₹${currentProduct.productPrice}"
             val imageList = ArrayList<SlideModel>()
            val imageUrl = currentProduct.productImageUrls.toString()
             with(ImageHolder){
                 Glide.with(this).load(imageUrl).into(this)
             }

            productcountvalue.text = currentProduct.itemCount.toString()

            incrementButton.setOnClickListener { incrementB(currentProduct, this) }
            decremntButton.setOnClickListener { decrementB(currentProduct, this) }

         }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}
