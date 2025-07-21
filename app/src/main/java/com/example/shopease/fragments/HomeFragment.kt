package com.example.shopease.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopease.R
import com.example.shopease.adapters.AdapterCategory
import com.example.shopease.adapters.BestSellerAdapter
import com.example.shopease.cartInterface.CartInterface
import com.example.shopease.databinding.FragmentHomeBinding
import com.example.shopease.databinding.ItemViewBestSellerBinding
import com.example.shopease.models.Product
import com.example.shopease.models.category
import com.example.shopease.roomDB.cartProducts
import com.example.shopease.utilites.productData
import com.example.shopease.viewModels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding : FragmentHomeBinding
    private val viewModel : UserViewModel by viewModels()
    private var CartListener : CartInterface?= null
    private lateinit var bestSellerAdapter : BestSellerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAllCategories()
        naviagationtosearchfragment()
        navigationtoprofilefragment()
        bestSellerAdapter = BestSellerAdapter(::addButtonClicked,::incrementButtonClick,::decrementButtonClick)
//        bestSellerAdapter = BestSellerAdapter()

        binding.rvBestsellerRecyclerView.adapter = bestSellerAdapter
        binding.rvBestsellerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Start shimmer
        binding.shrimer.visibility = View.VISIBLE
        binding.rvBestsellerRecyclerView.visibility = View.GONE

        // Collect best seller products
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getBestSellerProducts().collectLatest { productList ->
                    if (productList.isNotEmpty()) {
                        bestSellerAdapter.differ.submitList(productList)
                        binding.shrimer.visibility = View.GONE
                        binding.rvBestsellerRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.shrimer.visibility = View.GONE
                        binding.rvBestsellerRecyclerView.visibility = View.GONE
                    }
                }
            }
        }

    }

    private fun navigationtoprofilefragment() {
        binding.ivProfile.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    fun setAllCategories(){
        val categorylist = ArrayList<category>()
        for(i in 0 until productData.categoryProductName.size){
            categorylist.add(category(productData.categoryProductName[i], productData.categoryProductImage[i]))
        }
        binding.rvCategories.adapter = AdapterCategory(categorylist,::CategoryOnClicked)
    }
    fun naviagationtosearchfragment(){
        binding.etSearch.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }
    fun CategoryOnClicked(cat : category){
        val bundle = Bundle()
        bundle.putString("categoryName",cat.title)
        findNavController().navigate(R.id.action_homeFragment_to_catFragment,bundle)
    }

    fun addButtonClicked(product: Product, productBinding: ItemViewBestSellerBinding) {
        productBinding.tvAddText.visibility = View.GONE
        productBinding.tvAddTextP.visibility = View.VISIBLE
        productBinding.productcountvalue.text = "1"

        val itemCount = 1
        product.itemCount = itemCount

        product.productImageUrls?.let { imgUrls ->
            if (imgUrls.isNotEmpty()) {
                showImageOnCart(imgUrls, product.productRandomId.toString())
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            saveProductInRoomDatabase(product)
            viewModel.updateItemCount(product, itemCount)

            val newCount = viewModel.getCurrentTotalCartCount() + itemCount // manually update
            withContext(Dispatchers.Main) {
                viewModel.savingCartItemCount(newCount)
                CartListener?.showCartLayout(newCount)

                val updatedList = bestSellerAdapter.differ.currentList.map {
                    if (it.productRandomId == product.productRandomId) {
                        it.copy(itemCount = itemCount) // New object with updated count
                    } else it
                }
                bestSellerAdapter.differ.submitList(updatedList)

                // ✅ Update immediate UI elements if needed
                productBinding.tvAddText.visibility = View.GONE
                productBinding.tvAddTextP.visibility = View.VISIBLE
                productBinding.productcountvalue.text = itemCount.toString()
            }
        }
    }

    fun incrementButtonClick(product: Product, productBinding: ItemViewBestSellerBinding) {
        lifecycleScope.launch(Dispatchers.IO) {
            val currentCount = viewModel.getproductQuantity(product.productRandomId.toString())
            val stock = product.productStock?.toInt()

            if (stock != null && currentCount < stock) {
                val newCount = currentCount + 1
                product.itemCount = newCount

                saveProductInRoomDatabase(product)
                viewModel.updateItemCount(product, newCount)

                val totalCount = viewModel.getTotalCartItemCountFromRoom()

                withContext(Dispatchers.Main) {
                    // ✅ Update product view
                    productBinding.productcountvalue.text = newCount.toString()

                    // ✅ Update cart count and image
                    viewModel.savingCartItemCount(totalCount)
                    CartListener?.showCartLayout(totalCount)

                    // 🔥 Refresh the image on cart
                    product.productImageUrls?.let {
                        CartListener?.showImageincart(it, product.productRandomId.toString())
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "This product is out of stock", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun decrementButtonClick(product: Product, productBinding: ItemViewBestSellerBinding) {
        val currentCount = product.itemCount ?: 0
        val newCount = currentCount - 1

        if (newCount > 0) {
            product.itemCount = newCount
            productBinding.productcountvalue.text = newCount.toString()

            lifecycleScope.launch(Dispatchers.IO) {
                saveProductInRoomDatabase(product)
                viewModel.updateItemCount(product, newCount)

                val totalCount = viewModel.getTotalCartItemCountFromRoom()

                withContext(Dispatchers.Main) {
                    viewModel.savingCartItemCount(totalCount)
                    CartListener?.showCartLayout(totalCount)

                    // ✅ Trigger image quantity update
                    product.productImageUrls?.let {
                        CartListener?.removeProductFromCart(it, product.productRandomId.toString())
                    }
                }
            }

        } else {
            product.itemCount = 0
            productBinding.productcountvalue.text = "0"
            productBinding.tvAddText.visibility = View.VISIBLE
            productBinding.tvAddTextP.visibility = View.GONE

            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deletecartproduct(product.productRandomId.toString())
                viewModel.updateItemCount(product, 0)

                val totalCount = viewModel.getTotalCartItemCountFromRoom()

                withContext(Dispatchers.Main) {
                    viewModel.savingCartItemCount(totalCount)
                    CartListener?.showCartLayout(totalCount)

                    // ✅ Remove image from cart layout
                    product.productImageUrls?.let {
                        CartListener?.removeProductFromCart(it, product.productRandomId.toString())
                    }
                }
            }
        }
    }






    fun showImageOnCart(imageUrls : List<String>,productId : String){
        CartListener?.showImageincart(imageUrls,productId)
    }
    fun showRemoveImage(imageUrls: List<String>, productId: String) {
        CartListener?.removeProductFromCart(imageUrls, productId)
    }

    fun saveProductInRoomDatabase(product : Product){
        val cartProduct = cartProducts(
            productRandomId = product.productRandomId!!,
            productTitle = product.productTitle.toString(),
            productQuantity = product.productQuantity.toString().toInt(),
            productUnit = product.productUnit.toString() ,
            productImageUrls = product.productImageUrls?.get(0).toString() ,
            productPrice = product.productPrice.toString() ,
            productStock = product.productStock.toString().toInt() ,
            productCategory = product.productCategory.toString() ,
            adminUid = product.adminUid.toString(),
            itemCount = product.itemCount.toString().toInt()
        )
        lifecycleScope.launch(Dispatchers.IO){
            viewModel.insertcartproduct(cartProduct)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is CartInterface){
            CartListener = context
        }else{
            throw ClassCastException("Please implement cart listener")
        }
    }
}