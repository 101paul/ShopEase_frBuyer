package com.example.shopease.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shopease.cartInterface.CartInterface
import com.example.shopease.R
import com.example.shopease.adapters.ProductsAdapter
import com.example.shopease.databinding.FragmentSearchBinding
import com.example.shopease.databinding.ItemViewProductBinding
import com.example.shopease.models.Product
import com.example.shopease.roomDB.cartProducts
import com.example.shopease.search.FilteringProducts
import com.example.shopease.search.FilteringProducts.Companion.filterCallback
import com.example.shopease.viewModels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class searchFragment : Fragment() {
    private val viewModel : UserViewModel by viewModels()
    private lateinit var binding : FragmentSearchBinding
    private lateinit var cartAdapter : ProductsAdapter
    private var searchJob: Job? = null
    private var shimmerTimeoutJob: Job? = null
    private var CartListener : CartInterface?= null
    private var fullProductList: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater,container,false)
        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FilteringProducts.filterCallback = filterCallback  // connect callback
        searchProducts()
        navigatetohomefrag()
        Log.d("hello","hello this is paul")
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchAllProducts()
                    .debounce(300) // Avoid rapid repeated emissions on startup
                    .collectLatest { productList ->
                        getAllTheProductsSafely(productList)
                    }
            }
        }
    }

private fun getAllTheProductsSafely(productList: List<Product>) {
    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
        val firebaseList = productList.toMutableList()
        fullProductList = firebaseList
        val cartProductsList = viewModel.getallproducts()

        val cartMap = cartProductsList.associateBy { it.productRandomId } // associateBy is a function used to convert list to map and sort it by productRadomId


        for (product in firebaseList) {
            cartMap[product.productRandomId]?.let { matchedCart ->
                product.itemCount = matchedCart.itemCount
            }
        }

        withContext(Dispatchers.Main) {
            binding.shrimer.visibility = View.GONE
            if (firebaseList.isEmpty()) {
                delay(1000)
                showEmptyState()
            } else {
                binding.rvProducts.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
                cartAdapter.submitListWithOriginal(firebaseList)
            }
        }
    }
}


    private fun searchProducts() {
        binding.searchbox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(100)
                    if (::cartAdapter.isInitialized) {
                        if (query.isEmpty()) {
                            // Reset full list when search is cleared
                            cartAdapter.submitListWithOriginal(fullProductList)
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvProducts.visibility = View.VISIBLE
                        } else {
                            cartAdapter.getFilter().filter(query)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showEmptyState() {
        binding.shrimer.visibility = View.GONE
        binding.rvProducts.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
    }

    private fun setupUI() {
        cartAdapter = ProductsAdapter(
            ::addButtonClicked,
            ::incrementButtonClick,
            ::decrementButtonClick,
            ::showImageOnCart,
            ::showRemoveImage
        )
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = cartAdapter

        binding.shrimer.visibility = View.VISIBLE
        binding.rvProducts.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE

        // Start shimmer timeout fallback (max wait 10 sec)
        shimmerTimeoutJob?.cancel()
        shimmerTimeoutJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(10_000)
            if (cartAdapter.itemCount == 0) {
                showEmptyState()
            }
        }
    }
    fun navigatetohomefrag(){
        binding.backtohome.setOnClickListener{
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }
    }

    fun addButtonClicked(product: Product, productBinding: ItemViewProductBinding) {
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

                val updatedList = cartAdapter.differ.currentList.map {
                    if (it.productRandomId == product.productRandomId) {
                        it.copy(itemCount = itemCount) // New object with updated count
                    } else it
                }
                cartAdapter.submitListWithOriginal(updatedList)

                // ✅ Update immediate UI elements if needed
                productBinding.tvAddText.visibility = View.GONE
                productBinding.tvAddTextP.visibility = View.VISIBLE
                productBinding.productcountvalue.text = itemCount.toString()
            }
        }
    }

    fun incrementButtonClick(product: Product, productBinding: ItemViewProductBinding) {
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

    fun decrementButtonClick(product: Product, productBinding: ItemViewProductBinding) {
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

