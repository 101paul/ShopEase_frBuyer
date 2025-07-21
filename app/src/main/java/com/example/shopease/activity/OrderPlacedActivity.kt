package com.example.shopease.activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Index
import com.example.shopease.cartInterface.CartInterface
import com.example.shopease.cartInterface.CartInterfaceManager
import com.example.shopease.utilites.Utils
import com.example.shopease.adapters.CartIAdapter
import com.example.shopease.adapters.OrderPlacedAdapter
import com.example.shopease.databinding.ActivityOrderPlacedBinding
import com.example.shopease.databinding.AddressLayoutBinding
import com.example.shopease.databinding.OrderplacedproductsitemviewBinding
import com.example.shopease.models.OrderRequest
import com.example.shopease.models.OrderResponse
import com.example.shopease.models.Orders
import com.example.shopease.models.users
import com.example.shopease.retrofitInterface.RazorpayApi
import com.example.shopease.roomDB.cartProducts
import com.example.shopease.viewModels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class OrderPlacedActivity : AppCompatActivity(), PaymentResultListener{
    private lateinit var api: RazorpayApi

    private val cartProductQuantities = LinkedHashMap<String, Int>()
    private lateinit var binding : ActivityOrderPlacedBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapter : OrderPlacedAdapter
    private lateinit var cartdapter : CartIAdapter
    private var CartListener : CartInterface ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlacedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = OrderPlacedAdapter(::incrementButtonClick, ::decrementButtonClick)
        CartListener = CartInterfaceManager.cartInterface // Gets the reference
        setUpUi()
        returnbacktoOrder()
        getProduct()
        OrderButtonClick()
        displayUserAddress()



    }

    private fun displayUserAddress() {
        lifecycleScope.launch{
            viewModel.getUserAddressFromFB().collectLatest { address->
                binding.address.text = address
            }
        }
        binding.editbutton.setOnClickListener{
            val addressLayout = AddressLayoutBinding.inflate(LayoutInflater.from(this))
            val alertDialog = AlertDialog.Builder(this)
                .setView(addressLayout.root)
                .show()
            addressLayout.addButton.setOnClickListener{
                saveAddress(alertDialog,addressLayout)
            }
            addressLayout.stopsign.setOnClickListener{
                Toast.makeText(this@OrderPlacedActivity, "box dismissed", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }

        }
    }

    private fun OrderButtonClick() {
        binding.placeOrderBtn.setOnClickListener{
            val isAddressSaved = viewModel.getUserAddressFromFB().toString()
                if(!isAddressSaved.isNullOrEmpty()){
                    val retrofit = Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:8080/") // this is the api endpoint
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val totalamount = adapter.differ.currentList.sumOf {
                        (it.productPrice.toString().toInt() * it.itemCount.toString().toInt())
                    }
                    val amount = if(totalamount>150) totalamount else totalamount+99
                    val OrderRequest = OrderRequest(amount,"INR","receipt#001")


                    api = retrofit.create(RazorpayApi::class.java)
                    api.createOrder(OrderRequest).enqueue(object : Callback<OrderResponse> {
                        override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                            if (response.isSuccessful) {
                                val orderResponse = response.body()
                                orderResponse?.let {
                                    launchRazorpayCheckout( it.id, it.amount)
                                }
                            }
                        }

                        override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                            Log.e("Razorpay", "Error: ${t.message}")
                        }
                    })
                }else{
                    val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this@OrderPlacedActivity))

                    val alertDialog = AlertDialog.Builder(this@OrderPlacedActivity)
                        .setView(addressLayoutBinding.root)
                        .create()

                    alertDialog.show()

                    addressLayoutBinding.addButton.setOnClickListener{
                        saveAddress(alertDialog,addressLayoutBinding)
                    }
                }
            }
        }



    private fun saveAddress(alertDialog: AlertDialog,addressLayoutBinding : AddressLayoutBinding){
        Utils.showDialog(this@OrderPlacedActivity,"Processing ... ")

        val address = addressLayoutBinding.address.text.toString()
        val phoneNumber = addressLayoutBinding.phoneNumber.text.toString()
        val pinCode = addressLayoutBinding.pincode.text.toString()
        val state = addressLayoutBinding.stateName.text.toString()
        val city = addressLayoutBinding.cityName.text.toString()
        val name = addressLayoutBinding.UserName.text.toString()

        val UserAddress = "$address , $city($state) , $pinCode "
        val User = users(
            userAddress = UserAddress,
            uid = Utils.getCurrentUserid() ,
            userName = name,
            userPhnNumber = "+91$phoneNumber"
        )
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.saveUserAddress(User) // It is saving inside the firebase realtime database
//            viewModel.saveAddressStatus()
            viewModel.savingActualAdress(User) // and this one inside the share pref
            withContext(Dispatchers.Main) {
                displayUserAddress() // to refresh UI
            }
        }
        alertDialog.dismiss()
        Utils.shutDialog(this)
        Utils.showtoast(this@OrderPlacedActivity,"Saved!")
    }

    private fun returnbacktoOrder() {
        binding.toolbar.setNavigationOnClickListener{
            val intent = Intent(this@OrderPlacedActivity, UsersActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    private fun setUpUi(){
        binding.recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        binding.recyclerview.adapter = adapter
    }



   private fun getProduct(){ // retrieving all the selected products from room db via viewModel
       lifecycleScope.launch(Dispatchers.IO){
           lifecycleScope.launch(Dispatchers.IO) {
               val data = viewModel.getallproducts()
               Log.d("DEBUG_CART", "Loaded items: $data")
               withContext(Dispatchers.Main) {
                   adapter.differ.submitList(data)
                   calculateTotalAmount()
               }
           }
       }
    }

    private fun calculateTotalAmount() {
        val currentList = adapter.differ.currentList

        val totalAmount = currentList.sumOf {
            val price = it.productPrice.toString().toIntOrNull() ?: 0
            val qty = it.itemCount ?: 0
            Log.d("DEBUG_CALC", "Price: $price, Count: $qty")
            price * qty // ✅ This must be the LAST line in the lambda
        }

        binding.subtotalvalue.text = "₹$totalAmount"

        if (totalAmount > 150) {
            binding.deliveryvalue.text = "free"
            binding.grandtotalvalue.text = "₹$totalAmount"
            binding.totalpriceoncheckout.text = "₹$totalAmount"
        } else {
            val deliveryAmount = 99
            val grandTotal = totalAmount + deliveryAmount
            binding.deliveryvalue.text = "₹$deliveryAmount"
            binding.grandtotalvalue.text = "₹$grandTotal"
            binding.totalpriceoncheckout.text = "₹$grandTotal"
        }
//        binding.deliveryvalue.text = "free"
//        binding.grandtotalvalue.text = "₹$totalAmount"
//        binding.totalpriceoncheckout.text = "₹$totalAmount"
        if(totalAmount == 0){
            Toast.makeText(this,"Cart is empty , Returning back ...",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@OrderPlacedActivity, UsersActivity::class.java))
            finish()
        }
        savingDataOrderRequestBody(totalAmount)


    }



    fun incrementButtonClick(product: cartProducts, productBinding: OrderplacedproductsitemviewBinding) {
        lifecycleScope.launch(Dispatchers.IO) {
            val quantity = viewModel.getproductQuantity(product.productRandomId)
            val stock = product.productStock

            if (quantity < stock) {
                val newCount = quantity + 1
                val updatedProduct = product.copy(itemCount = newCount)

                saveProductInRoomDatabase(updatedProduct)
                viewModel.updateItemCountfromroomdb(updatedProduct, newCount)

                withContext(Dispatchers.Main) {
                    val newList = adapter.differ.currentList.toMutableList()
                    val index = newList.indexOfFirst { it.productRandomId == updatedProduct.productRandomId }

                    if (index != -1) {
                        newList[index] = updatedProduct
                        adapter.differ.submitList(newList.toList()) {
                            //  CALLBACK after list updated, now recalculate
                            calculateTotalAmount()
                        }
                    }

                    productBinding.productcountvalue.text = newCount.toString()

                    val current = viewModel.getCurrentTotalCartCount()
                    val updatedCount = current + 1
                    viewModel.savingCartItemCount(updatedCount)
                    CartListener?.showCartLayout(updatedCount)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OrderPlacedActivity, "This product is out of stock", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun decrementButtonClick(product: cartProducts, productBinding: OrderplacedproductsitemviewBinding) {
        val itemCount = productBinding.productcountvalue.text.toString().toIntOrNull() ?: 0
        val newCount = itemCount - 1

        if (newCount > 0) {
            val updatedProduct = product.copy(itemCount = newCount)

            lifecycleScope.launch(Dispatchers.IO) {
                saveProductInRoomDatabase(updatedProduct)
                viewModel.updateItemCountfromroomdb(updatedProduct, newCount)

                val current = viewModel.getCurrentTotalCartCount()
                val updatedCount = (current - 1).coerceAtLeast(0)

                withContext(Dispatchers.Main) {
                    productBinding.productcountvalue.text = newCount.toString()
                    viewModel.savingCartItemCount(updatedCount)
                    CartListener?.showCartLayout(updatedCount)

                    val newList = adapter.differ.currentList.toMutableList()
                    val index = newList.indexOfFirst { it.productRandomId == updatedProduct.productRandomId }
                    if (index != -1) {
                        newList[index] = updatedProduct
                        adapter.differ.submitList(newList.toList()) {
                            calculateTotalAmount()
                        }
                    }
                }
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deletecartproduct(product.productRandomId.toString())
                viewModel.updateItemCountfromroomdb(product, 0)

                val current = viewModel.getCurrentTotalCartCount()
                val updatedCount = (current - 1).coerceAtLeast(0)

                withContext(Dispatchers.Main) {
                    viewModel.savingCartItemCount(updatedCount)
                    CartListener?.showCartLayout(updatedCount)

                    val newList = adapter.differ.currentList.toMutableList()
                    val index = newList.indexOfFirst { it.productRandomId == product.productRandomId }
                    if (index != -1) {
                        newList.removeAt(index)
                        adapter.differ.submitList(newList.toList()) {
                            calculateTotalAmount()
                        }
                    }

                    // ✅ Remove image from cart if available
                    product.productImageUrls?.firstOrNull()?.let { firstImage ->
                        CartListener?.removeProductFromCartfromOrderPlacedActivity(firstImage.toString(), product.productRandomId.toString())
                    }

                    // ✅ ✅ ✅ Always check cart count and finish activity if empty
                    if (updatedCount == 0) {
//                        Toast.makeText(this@OrderPlacedActivity, "Cart is empty. Returning...", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@OrderPlacedActivity, UsersActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }


                    product.productImageUrls?.firstOrNull()?.let { firstImage ->
                        CartListener?.removeProductFromCartfromOrderPlacedActivity(firstImage.toString(), product.productRandomId.toString())
                    }
                }
            }
        }
    }


fun saveProductInRoomDatabase(product: cartProducts) {
    val cartProduct = cartProducts(
        productRandomId = product.productRandomId,
        productTitle = product.productTitle,
        productQuantity = product.productQuantity,

        productUnit = product.productUnit,
        productImageUrls = product.productImageUrls,
        productPrice = product.productPrice,
        productStock = product.productStock,
        productCategory = product.productCategory,
        adminUid = product.adminUid,
        itemCount = product.itemCount
    )
    lifecycleScope.launch(Dispatchers.IO) {
        viewModel.insertcartproduct(cartProduct)
    }
}

  fun saveOrderDetailsToOrderDataClass(orderId: String) {
    lifecycleScope.launch(Dispatchers.IO) {
        val allProducts = viewModel.getallproducts().filterNotNull()

        // Correctly collect the address from Flow
        val address = viewModel.getUserAddressFromFB().firstOrNull() ?: "No Address"

        val totalQuantity = allProducts.sumOf { it.itemCount ?: 0 }

        val order = Orders(
            userAddress = address,
            orderingUserid = Utils.getCurrentUserid(),
            OrderList = allProducts,
            OrderId = orderId,
            orderStatus = 0,
            userId = Utils.getCurrentUserid(),
            OrderDate = Utils.getCurrentDate(),
            totalItemcount = totalQuantity
        )
        viewModel.savingOrdersDetails(order)
    }
}

    fun launchRazorpayCheckout(orderId: String, amount: Int) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_Q4MKbpzLnoXV7O")

        val options = JSONObject().apply {
            put("name", "ShopEase")
            put("description", "Test Payment")
            put("order_id", orderId)
            put("currency", "INR")
            put("amount", amount) // already in paise

            put("prefill", JSONObject().apply {
                put("contact", "9876543210")        // dummy test‑mode phone
                put("email",   "test@demo.com")     // dummy test‑mode email
            })
        }
        checkout.open(this@OrderPlacedActivity, options)
    }

    private fun savingDataOrderRequestBody(totalamount : Int) : Int{
       return totalamount
    }


override fun onPaymentSuccess(razorpayPaymentID: String?) {
    Toast.makeText(this, "Payment successful: $razorpayPaymentID", Toast.LENGTH_SHORT).show()

    lifecycleScope.launch(Dispatchers.IO) {
        val orderedProducts = viewModel.getallproducts().filterNotNull()

        if (orderedProducts.isEmpty()) {
            Log.e("OrderPlacedActivity", "❌ Cart empty during payment success")
            withContext(Dispatchers.Main) {
                Toast.makeText(this@OrderPlacedActivity, "Cart was empty! Order not saved", Toast.LENGTH_SHORT).show()
            }
            return@launch
        }

        // ✅ STEP 1: Save order
        val address = viewModel.getUserAddressFromFB().firstOrNull() ?: "No Address"
        val totalQuantity = orderedProducts.sumOf { it.itemCount ?: 0 }

        val order = Orders(
            userAddress = address,
            orderingUserid = Utils.getCurrentUserid(),
            OrderList = orderedProducts,
            OrderId = razorpayPaymentID.toString(),
            orderStatus = 0,
            userId = Utils.getCurrentUserid(),
            OrderDate = Utils.getCurrentDate(),
            totalItemcount = totalQuantity
        )
        viewModel.savingOrdersDetails(order)

        //  Update Firebase stock
        orderedProducts.forEach { product ->
            val updatedStock = (product.productStock!! - product.itemCount).coerceAtLeast(0)
            product.productStock = updatedStock
            viewModel.updateStockQuantity(product, updatedStock)
            viewModel.clearItemCountInFirebase(product.adminUid, product.productRandomId)
        }

        //  Delete all cart products only AFTER saving order
        viewModel.deleteAllcartproduct()

        //  Reset item counts & update UI
        viewModel.resetAllItemCounts()
        viewModel.savingCartItemCount(0)

        withContext(Dispatchers.Main) {
            CartListener?.showCartLayout(0)
            startActivity(Intent(this@OrderPlacedActivity, OrderComplete::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

}




    override fun onPaymentError(p0: Int, response: String?) {
//        Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_SHORT).show()
        Log.e("RPayment error","Payment faild due to $response")
    }

    private fun deleteAllCartProducts() {
       lifecycleScope.launch(Dispatchers.IO){
           viewModel.deleteAllcartproduct() // deleting all the products from the cart i.e from room db
           // also we need to make the item count to zero in share pref
           viewModel.savingCartItemCount(0)
           withContext(Dispatchers.Main){
               CartListener?.showCartLayout(0)
           }
       }
    }
    fun updateStockForAllOrderedItems() {
        lifecycleScope.launch(Dispatchers.IO) {
            val products = viewModel.getallproducts()
            for (product in products) {
                val stock = product.productStock!!
                val orderedQty = product.itemCount
                val updatedStock = (stock - orderedQty).coerceAtLeast(0)
                product.productStock = updatedStock
                viewModel.updateStockQuantity(product,updatedStock)

                viewModel.clearItemCountInFirebase(product.adminUid, product.productRandomId)
            }
        }
    }
}


