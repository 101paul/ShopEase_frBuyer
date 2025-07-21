package com.example.shopease.viewModels

import android.app.Application  // ✅ This was missing
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.shopease.models.Orders
import com.example.shopease.models.OrdersRetrieveFromFB
import com.example.shopease.models.Product
import com.example.shopease.models.ProductOrderStats
import com.example.shopease.models.bestseller
import com.example.shopease.models.users
import com.example.shopease.roomDB.CartProductDatabase
import com.example.shopease.roomDB.ProductDao
import com.example.shopease.roomDB.cartProducts
import com.example.shopease.utilites.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.internal.operators.single.SingleDoOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import kotlin.collections.mutableListOf
import kotlin.toString

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences : SharedPreferences = application.getSharedPreferences("MyPref1",MODE_PRIVATE)
    private val productDao: ProductDao = CartProductDatabase.getDataBase(application).getproductdao()

    fun fetchAllProducts(): Flow<List<Product>> = callbackFlow {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("AllProducts")
            .limitToFirst(100) // Limit data for faster initial load


        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<Product>()
                if (snapshot.exists()) {
                    for (productSnap in snapshot.children) {
                        try {
                            val product = productSnap.getValue(Product::class.java)
                            Log.d("FirebaseTest", "Fetched name = $snapshot")

                            product?.let { products.add(it) }
                        } catch (e: Exception) {
                            Log.e("Firebase123", "Error parsing product: ${e.message}")
                        }
                    }
                }
                trySend(products)
                close()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                trySend(emptyList())
                close(error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
//        awaitClose{}
    }.flowOn(Dispatchers.IO)

    fun getcategoryproduct(category: String): Flow<List<Product>> = callbackFlow {
        val dbms = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("productCategory")
            .child(category)

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val product = mutableListOf<Product>()
                if (snapshot.exists()) {
                    for (value in snapshot.children) {
                        val data = value.getValue(Product::class.java)
                        data?.let { product.add(it) }
                    }
                }
                trySend(product)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CategoryProductFetch", error.message)
                Log.e("FIREBASE_ERROR", "Code=${error.code}, Msg=${error.message}")

                trySend(emptyList())
                close(error.toException())
            }
        }

        dbms.addValueEventListener(eventListener)
        awaitClose { dbms.removeEventListener(eventListener) }
    }.flowOn(Dispatchers.IO)

    private fun getvalueproduct2(category : String) : Flow<List<Product>> = callbackFlow { // this one is the second way to get
        // data from firebase database
        val dbms = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("productCategory")
            .child(category)
        val listener = myValueListener(
            onSuccess = {trySend(it)} ,
            onError = {trySend(emptyList());
                close(it)}
        )
        dbms.addValueEventListener(listener)
        awaitClose{dbms.removeEventListener(listener)}
    }.flowOn(Dispatchers.IO)

    fun getallorders(orderId : String) : Flow<List<Orders>> = callbackFlow {
        val dbms = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("Orders")
            .child(orderId)
            .orderByChild("orderStatus")
        val eventValueListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Orders>()
                if(snapshot.exists()){
                    for(data in snapshot.children){
                        val order = data.getValue(Orders::class.java)

                        if(order?.orderingUserid == Utils.getCurrentUserid()){
                            list.add(order!!)
                        }
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
                close(error.toException())}

        }
        dbms.addValueEventListener(eventValueListener)
        awaitClose{dbms.removeEventListener(eventValueListener)}
    }.flowOn(Dispatchers.IO)

    fun getAllOrdersForCurrentUser(): Flow<List<OrdersRetrieveFromFB>> = callbackFlow {
        val userId = Utils.getCurrentUserid()

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("Orders")
            .child(userId) //Now querying by userId, not orderId
            .orderByChild("orderStatus") // Optional ordering

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<OrdersRetrieveFromFB>()
                for (data in snapshot.children) {
                    val order = data.getValue(OrdersRetrieveFromFB::class.java)
                    order?.let {
                        orders.add(it.copy(OrderId = data.key ?: "")) // ✅ Include the key
                    }
                }
                trySend(orders)
//                close() because of this , it cannot update the delivery change when updated by the seller , The callback flow closes immediately after the first response, so it never listens again
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
                close(error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
//        awaitClose { dbRef.removeEventListener(listener) }
        awaitClose{}

    }.flowOn(Dispatchers.IO)


    fun getUserPhnNumber() : Flow<String> = callbackFlow{
        val userid = Utils.getCurrentUserid()
        val dbms = FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(userid)
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snaptshot: DataSnapshot) {
                if(snaptshot.exists()){
                   val PhnNumber = snaptshot.child("userPhnNumber").getValue(String::class.java)
                   if(PhnNumber != null ){
                       trySend(PhnNumber)
//                       close()
                   }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend("N/A")

                Log.e("firebaseNumber","reason -> $error")
                close(error.toException())
            }

        }
        dbms.addValueEventListener(eventListener)
        awaitClose{dbms.removeEventListener(eventListener)}
//        awaitClose{}


    }.flowOn(Dispatchers.IO)

    fun getUserName() : Flow<String> = callbackFlow{
        val userid = Utils.getCurrentUserid()
        val dbms = FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(userid)
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snaptshot: DataSnapshot) {
                if(snaptshot.exists()){
                    val Name = snaptshot.child("userName").getValue(String::class.java)
                    if(Name != null ){
                        trySend(Name)
//                        close()

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend("N/A")
                close(error.toException())
            }

        }
        dbms.addValueEventListener(eventListener)
        awaitClose{dbms.removeEventListener(eventListener)}
//        awaitClose{}

    }.flowOn(Dispatchers.IO)


    //saving the profile picture in the firebase storage


        fun uploadProfileImage(uri: Uri, onResult: (Boolean, String?) -> Unit) {
            val userId = Utils.getCurrentUserid()
            val storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/$userId.jpg")

            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Save URL to Realtime Database
                        val dbRef = FirebaseDatabase.getInstance()
                            .getReference("AllUsers")
                            .child("Users")
                            .child(userId)
                            .child("profileImageUrl")

                        dbRef.setValue(downloadUri.toString())
                            .addOnSuccessListener {
                                onResult(true, downloadUri.toString()) // success
                            }
                            .addOnFailureListener { e ->
                                onResult(false, null) // failed to save URL
                            }
                    }
                }
                .addOnFailureListener {
                    onResult(false, null) // failed to upload
                }

        }



      fun getProfileImageUrl(): Flow<String?> = callbackFlow {
          val userId = Utils.getCurrentUserid()
          val dbRef = FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(userId)
            .child("profileImageUrl")


          val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.getValue(String::class.java)
                trySend(url)
//                close()
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
                close(error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener)}

      }.flowOn(Dispatchers.IO)
    fun getUserAddressFromFB(): Flow<String?> = callbackFlow {
        val userId = Utils.getCurrentUserid()
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(userId)
            .child("userAddress")


        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.getValue(String::class.java)
                trySend(url)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
                close(error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener)}

    }.flowOn(Dispatchers.IO)


    // very important , need some revise ***

    fun getBestSellerProducts(): Flow<List<Product>> = callbackFlow {
        val productStatsMap = mutableMapOf<String, Int>() // productId -> totalQuantity
        val allOrdersRef = FirebaseDatabase.getInstance().getReference("Admins/Orders")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    Log.d("BESTSELLER_DEBUG", "Found user with orders: ${userSnapshot.key}")

                    for (orderSnapshot in userSnapshot.children) {
                        Log.d("BESTSELLER_DEBUG", "Order found: ${orderSnapshot.key}")
                        val orderListSnapshot = orderSnapshot.child("orderList")
                        for (productSnapshot in orderListSnapshot.children) {
                            val productId = productSnapshot.child("productRandomId").getValue(String::class.java) ?: continue
                            val quantity = productSnapshot.child("productQuantity").getValue(Int::class.java) ?: 0
                            Log.d("BESTSELLER_DEBUG", "Product ID: $productId, Qty: $quantity")

                            productStatsMap[productId] = productStatsMap.getOrDefault(productId, 0) + quantity
                        }
                    }
                }

                Log.d("BESTSELLER_DEBUG", "Stats map: $productStatsMap")

                val topBestSellerIds = productStatsMap.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { it.key }

                Log.d("BESTSELLER_DEBUG", "Top best seller IDs: $topBestSellerIds")

                // ✅ Fetch products from Admins/AllProducts
                val allProductsRef = FirebaseDatabase.getInstance()
                    .getReference("Admins")
                    .child("AllProducts")

                allProductsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val productList = mutableListOf<Product>()
                        for (productSnap in snapshot.children) {
                            val key = productSnap.key ?: continue
                            val product = productSnap.getValue(Product::class.java)

                            Log.d("MATCH_CHECK", "Checking product with key=$key")

                            if (topBestSellerIds.contains(key)) {
                                product?.productRandomId = key // Ensure ID is set
                                product?.let {
                                    productList.add(it)
                                    Log.d("BESTSELLER_FOUND", "✅ Added: ${it.productTitle}")
                                }
                            }
                        }

                        Log.d("BESTSELLER_DEBUG", "Total matched best sellers: ${productList.size}")
                        trySend(productList).isSuccess
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close(error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        allOrdersRef.addListenerForSingleValueEvent(listener)
        awaitClose { allOrdersRef.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)





    fun updateItemCount(product: Product, itemCount: Int) {
        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("AllProducts")
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(itemCount)

        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("productCategory")
            .child(product.productCategory.toString())
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(itemCount)
    }

    fun updateItemCountfromroomdb(product: cartProducts, itemCount: Int) { // this one updating the itemcount from room db
        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("AllProducts")
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(itemCount)

        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("productCategory")
            .child(product.productCategory.toString())
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(itemCount)
    }

    fun saveUserAddress(user : users){
        FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(user.uid!!)
            .setValue(user)
    }

    fun savingOrdersDetails(Orders : Orders){
        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("Orders")
            .child(Utils.getCurrentUserid())
            .child(Orders.OrderId.toString())
            .setValue(Orders)
    }
    fun updateDeliveryStatus(orderId: String, status: Int) {
        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("Orders")
            .child(Utils.getCurrentUserid())
            .child(orderId)
            .child("orderStatus")
            .setValue(status)
            .addOnSuccessListener {
                Log.d("FirebaseUpdate", "OrderStatus updated to $status for orderId: $orderId")
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseUpdate", "Failed to update OrderStatus: ${error.message}")
            }
    }
    fun updateStockQuantity(product : cartProducts,updatedStock:Int){
        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("AllProducts")
            .child(product.productRandomId.toString())
            .child("productStock")
            .setValue(updatedStock)

        // Update in "productCategory"
        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("productCategory")
            .child(product.productCategory.toString())
            .child(product.productRandomId.toString())
            .child("productStock")
            .setValue(updatedStock)

        // Optional: update itemCount if needed
        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("AllProducts")
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(0)

        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("productCategory")
            .child(product.productCategory.toString())
            .child(product.productRandomId.toString())
            .child("itemCount")
            .setValue(0)
    }
    fun clearItemCountInFirebase(adminUid: String, productRandomId: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("AllProducts")
            .child(adminUid)
            .child(productRandomId)
            .child("itemCount")

        ref.setValue(0)
    }
    suspend fun resetAllItemCounts() {
        productDao.resetAllItemCounts()
    }
    suspend fun deletecartproduct(productId : String){
        productDao.deleteCartProduct(productId)
    }
    suspend fun deleleteallcartproduct(){
        productDao.deleteAllCartProducts()
    }
    suspend fun deleteAllcartproduct(){
        withContext(Dispatchers.IO){
            productDao.deleteAllCartProducts()
        }
    }
    suspend fun insertcartproduct(cartproduct : cartProducts){
        productDao.insertProduct(cartproduct)
    }
    suspend fun getproductQuantity(productId : String) : Int {
        return withContext(Dispatchers.IO){
            productDao.getproudctQuantity(productId)
        }
    }
    suspend fun getallproducts(): List<cartProducts> {
        return withContext(Dispatchers.IO){
            productDao.getallproducts()
        }
    }
    suspend fun getTotalCartItemCountFromRoom(): Int {
        return productDao.getallproducts().sumOf { it.itemCount }
    }

    fun saveProfileImageUri(uri: Uri) {
        sharedPreferences.edit().putString("profile_image_uri", uri.toString()).apply()
    }

    // Retrieve saved image URI (nullable)
    fun getProfileImageUri(): Uri? {
        val uriString = sharedPreferences.getString("profile_image_uri", null)
        return uriString?.let { Uri.parse(it) }
    }
    // Saving user address in the share pref
    fun savingActualAdress(usersData : users){
        sharedPreferences.edit().putString("address data",usersData.userAddress).apply()
    }

//    fun saveAddressStatus(){
//        sharedPreferences.edit().putBoolean("address",true).apply()
//    }
//    fun getAddrssSavedStatus() : Boolean {
//        return sharedPreferences.getBoolean("address",false)
//    }

    fun savingCartItemCount(itemCount : Int){
        sharedPreferences.edit().putInt("itemCount1",itemCount).apply()
    }
    fun getCurrentTotalCartCount() : Int{
        return sharedPreferences.getInt("itemCount1",0)
    }
    fun fetchCartItemCount() : MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
//        totalItemCount.value = sharedPreferences.getInt("itemCount1",0) this one function on only main thread
        totalItemCount.postValue(sharedPreferences.getInt("itemCount1",0)) // it is safer as it works on any thread
        return totalItemCount
    }



class myValueListener (private val onSuccess:(List<Product>)->Unit,
    private val onError: (Exception)->Unit)
    : ValueEventListener{
    override fun onDataChange(snapshot: DataSnapshot) {
        val products = mutableListOf<Product>()
        if(snapshot.exists()){
            for(value in snapshot.children){
                val data = value.getValue(Product::class.java)
                data?.let{
                    products?.add(it)
                }
            }
        }
        onSuccess(products)
    }

    override fun onCancelled(error: DatabaseError) {
        onError(error.toException())
    }
}}