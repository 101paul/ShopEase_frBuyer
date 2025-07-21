package com.example.shopease.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopease.cartInterface.CartInterface
import com.example.shopease.cartInterface.CartInterfaceManager
import com.example.shopease.adapters.CartIAdapter
import com.example.shopease.databinding.ActivityUsersBinding
import com.example.shopease.viewModels.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory


class UsersActivity() : AppCompatActivity() , CartInterface {
    private lateinit var binding : ActivityUsersBinding
    var cartIAdapter = CartIAdapter()
    private val cartProductQuantities = LinkedHashMap<String, Int>()
    private var cartvisible = false
    private val viewModel : UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CartInterfaceManager.cartInterface = this
        setupUI()
        showCartLayout(viewModel.getCurrentTotalCartCount())
        cartbuttonclicked()
        restoreCartImagesFromRoom()
        getitemcountonCart()
//        FirebaseApp.initializeApp(this)
//        Firebase.analytics.setAnalyticsCollectionEnabled(true)
        // inside onCreate() of Application or MainActivity
        // inside onCreate() of Application or MainActivity


        val consentMap = mapOf(
            FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to FirebaseAnalytics.ConsentStatus.GRANTED,
            FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.GRANTED
        )
        Firebase.analytics.setConsent(consentMap)
        Firebase.analytics.setAnalyticsCollectionEnabled(true)
    }



    private fun cartbuttonclicked() {
        binding.cartbutton.apply{
            setOnClickListener{
                try{
                    Toast.makeText(this@UsersActivity, "Cart button clicked", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@UsersActivity, OrderPlacedActivity::class.java))
                }catch(e:Exception){
                    e.printStackTrace()
                    Toast.makeText(this@UsersActivity,"the error is ${e.message}}",Toast.LENGTH_LONG).show()
                }

            }
        }
    }

   private fun getitemcountonCart() {
        viewModel.fetchCartItemCount().observe(this){
            if(it>0){
                binding.cartx.visibility = View.VISIBLE
                binding.productcartvalue.text = it.toString()
            }else{
                binding.cartx.visibility = View.GONE
            }
        }
    }
    override fun showCartLayout(itemcount: Int){
        val updatedCount = itemcount.coerceAtLeast(0)
        if (updatedCount > 0) {
            if (!cartvisible) {
                binding.cartx.visibility = View.VISIBLE
                binding.rvCartImages.visibility = View.VISIBLE // ✅ ensure visible
                binding.productcartvalue.text = updatedCount.toString()
                binding.cartx.translationX = 100f
                binding.cartx.animate()
                    .translationX(0f)
                    .setDuration(600)
                    .start()
                cartvisible = true
            }
        }else {
            binding.cartx.visibility = View.GONE
            binding.rvCartImages.visibility = View.GONE
            binding.productcartvalue.text = "0"
            cartvisible = false
        }
    }

    override fun showImageincart(imageUrls: List<String>, productId: String) {
//        val firstImage = imageUrls.firstOrNull() ?: return
//
//        val currentQty = cartProductQuantities[productId] ?: 0
//        cartProductQuantities[productId] = currentQty + 1
//
//        if (currentQty == 0) { // Only add image if this is the first addition
//            cartIAdapter.submitImage(productId, firstImage)
//        }
    val firstImage = imageUrls.firstOrNull() ?: return

    val currentQty = cartProductQuantities[productId] ?: 0
    cartProductQuantities[productId] = currentQty + 1

    if (currentQty == 0) {
        cartIAdapter.submitImage(productId, firstImage)
    }

    val totalItems = cartProductQuantities.values.sum()
    viewModel.savingCartItemCount(totalItems)
    showCartLayout(totalItems)
    binding.productcartvalue.text = totalItems.toString()
   }



    override fun removeProductFromCart(imageUrls: List<String>, productId: String) {
//        val currentQty = cartProductQuantities[productId] ?: return
//
//        if (currentQty > 1) {
//            cartProductQuantities[productId] = currentQty - 1
//        } else {
//            cartProductQuantities.remove(productId)
//            cartIAdapter.removeImageByProductId(productId)
//        }
//
//        val totalItems = cartProductQuantities.values.sum()
//        viewModel.savingCartItemCount(totalItems) //
//        showCartLayout(totalItems)
//        binding.productcartvalue.text = totalItems.toString()
//
//        if (totalItems == 0) {
//            binding.cartx.visibility = View.GONE
//            binding.rvCartImages.visibility = View.GONE
//            cartvisible = false
//        }
        val currentQty = cartProductQuantities[productId] ?: return

        if (currentQty > 1) {
            cartProductQuantities[productId] = currentQty - 1
        } else {
            cartProductQuantities.remove(productId)
            cartIAdapter.removeImageByProductId(productId)
        }

        val totalItems = cartProductQuantities.values.sum()
        viewModel.savingCartItemCount(totalItems)
        showCartLayout(totalItems)
        binding.productcartvalue.text = totalItems.toString()

        if (totalItems == 0) {
            binding.cartx.visibility = View.GONE
            binding.rvCartImages.visibility = View.GONE
            cartvisible = false
        }
    }
    override fun savingCartItemCount(itemCount: Int) {
        viewModel.savingCartItemCount(itemCount)
        showCartLayout(itemCount)
    }

    override fun removeProductFromCartfromOrderPlacedActivity(
        imageUrl: String,
        productId: String
    ) {
        val currentQty = cartProductQuantities[productId] ?: return

        if (currentQty > 1) {
            cartProductQuantities[productId] = currentQty - 1
        } else {
            cartProductQuantities.remove(productId)
            cartIAdapter.removeImageByProductId(productId)
        }

        val totalItems = cartProductQuantities.values.sum()
        viewModel.savingCartItemCount(totalItems)
        showCartLayout(totalItems)
        binding.productcartvalue.text = totalItems.toString()

        if (totalItems == 0) {
            binding.cartx.visibility = View.GONE
            binding.rvCartImages.visibility = View.GONE
            cartvisible = false
        }
    }

//    fun restoreCartImagesFromRoom() {
//            lifecycleScope.launch(Dispatchers.IO) {
//                val products = viewModel.getallproducts()
//                val imageMap = mutableListOf<Pair<String, String>>()
//                val quantities = LinkedHashMap<String, Int>()
//                var totalCount = 0
//
//                products.forEach { product ->
//                    val id = product.productRandomId
//                    val image = product.productImageUrls
//                    val count = product.productQuantity?: 0
//                        if (!id.isNullOrEmpty() && !image.isNullOrEmpty()) {
//                            imageMap.add(Pair(id,image))
//                            quantities[id] = count
//                            totalCount += count
//
//                        }
//                    }
//                withContext(Dispatchers.Main) {
//                    cartProductQuantities.clear()
//                    cartProductQuantities.putAll(quantities)
//                    showCartLayout(totalCount)
//                    imageMap.forEach { (id, image) ->
//                        cartIAdapter.submitImage(id, image)
//                    }
//                }
//            }
//        }
fun restoreCartImagesFromRoom() {
    lifecycleScope.launch(Dispatchers.IO) {
        val products = viewModel.getallproducts()
        val imageMap = mutableListOf<Pair<String, String>>()
        val quantities = LinkedHashMap<String, Int>()
        var totalCount = 0

        products.forEach { product ->
            val id = product.productRandomId
            val image = product.productImageUrls
            val count = product.itemCount ?: 0  // FIXED here

            if (!id.isNullOrEmpty() && !image.isNullOrEmpty() && count > 0) {
                imageMap.add(Pair(id, image))
                quantities[id] = count
                totalCount += count
            }
        }

        withContext(Dispatchers.Main) {
            cartProductQuantities.clear()
            cartProductQuantities.putAll(quantities)
            showCartLayout(totalCount)
            imageMap.forEach { (id, image) ->
                cartIAdapter.submitImage(id, image)
            }
        }
    }
}

    private fun setupUI() {
        binding.rvCartImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCartImages.adapter = cartIAdapter
        binding.rvCartImages.visibility = View.GONE
    }


    override fun onDestroy() {
        super.onDestroy()
        // Clean up
        if (CartInterfaceManager.cartInterface === this) {
            CartInterfaceManager.cartInterface = null
        }
    }
}


