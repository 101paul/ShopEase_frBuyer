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
import com.example.shopease.databinding.FragmentCatBinding
import com.example.shopease.databinding.ItemViewProductBinding
import com.example.shopease.models.Product
import com.example.shopease.roomDB.cartProducts
import com.example.shopease.viewModels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class catFragment : Fragment() {
    private lateinit var binding: FragmentCatBinding
    private lateinit var cartAdapter: ProductsAdapter
    private val viewModel: UserViewModel by viewModels()
    private var shimmerTimeOutJob: Job? = null
    private var searchJob: Job? = null
    private var CartListener : CartInterface?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCatBinding.inflate(inflater)
        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categoryName = getproductcategory()
        setToolbartitle(categoryName)
        searchProducts()
        backtohomefrag()
        viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getcategoryproduct(categoryName)
                    .debounce(300) // Avoid rapid repeated emissions on startup
                    .collectLatest { productList ->
                        fetchCategoryProduct(productList)
                    }
            }
        }

    }
    private fun setToolbartitle(categoryN: String) {
        binding.categoryname.text = categoryN
    }

    private fun getproductcategory(): String {
        val bundle = arguments
        return bundle?.getString("categoryName").toString() ?:""
    }




    private fun fetchCategoryProduct(productsFromFirebase: List<Product>) { // the reason to use this code on the previous code
        //is the make the ui more active i.e itecount on the product which is there on item view
        // which may get hindered due to network issue
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {

            // ✅ Initialize Firebase product list as mutable
            val firebaseList: MutableList<Product> = productsFromFirebase.toMutableList()

            // ✅ Initialize Room cart product list (local database)
            val cartProductsList: List<cartProducts> = withContext(Dispatchers.IO){
                viewModel.getallproducts()
            }
            val cartMap = cartProductsList.associateBy { it.productRandomId }

            for (product in firebaseList) {
                cartMap[product.productRandomId]?.let {
                    product.itemCount = it.itemCount
                }
            }

            withContext(Dispatchers.Main) {
                delay(300) // what it does it allow layout to settle
                if (firebaseList.isEmpty()) {
                    showEmptyState()
                } else {
                    binding.shrimer.visibility = View.GONE
                    binding.rvCartProduct.visibility = View.VISIBLE
                    binding.productEmpty.visibility = View.GONE
//                    cartAdapter.submitListWithOriginal(firebaseList)
                    if (cartAdapter.differ.currentList != firebaseList) {
                        cartAdapter.submitListWithOriginal(firebaseList)
                    }
                }
            }
        }
    }



    private fun showEmptyState() {
        binding.shrimer.visibility = View.GONE
        binding.rvCartProduct.visibility = View.GONE
        binding.productEmpty.visibility = View.VISIBLE
        binding.emptyCartLottie.visibility = View.VISIBLE
    }
    private fun backtohomefrag(){
        binding.backtohome.setOnClickListener{
            findNavController().navigate(R.id.action_catFragment_to_homeFragment)
        }
    }

    private fun setupUI() {
        cartAdapter = ProductsAdapter(::addButtonClicked,::incrementButtonClick,::decrementButtonClick,::showImageOnCart,::showRemoveImage)
        binding.rvCartProduct.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCartProduct.adapter = cartAdapter

        binding.shrimer.visibility = View.VISIBLE
        binding.rvCartProduct.visibility = View.GONE
        binding.productEmpty.visibility = View.GONE
        binding.emptyCartLottie.visibility = View.GONE

        // Start shimmer timeout fallback (max wait 10 sec)
        shimmerTimeOutJob?.cancel()
        shimmerTimeOutJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(10_000)
            Log.d("shrimmer time out","it is working ok")
            if (cartAdapter.itemCount == 0) {
                showEmptyState()
            }
        }
    }
    private fun searchProducts(){
        binding.searchbox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(500) // Debounce time
                    if (::cartAdapter.isInitialized) {
                        cartAdapter.getFilter().filter(query)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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


