package com.example.shopease.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopease.R
import com.example.shopease.adapters.OrderFragAdapter
import com.example.shopease.adapters.OrderStatusUpdater
import com.example.shopease.databinding.FragmentOrderBinding
import com.example.shopease.models.OrderedItems
import com.example.shopease.utilites.Utils
import com.example.shopease.viewModels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class OrderFragment : Fragment() , OrderStatusUpdater{
    private lateinit var binding : FragmentOrderBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapter : OrderFragAdapter
    private var shrimmerJob : Job ?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOrderBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        setUpUI()
        getAllOrder()
        navigatetoprofile()
    }
    private fun getAllOrder() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.getAllOrdersForCurrentUser().collect { orderList ->

                    if (!orderList.isNullOrEmpty()) {
                        val orderedList = mutableListOf<OrderedItems>()

                        for (order in orderList) {
                            var totalPrice = 0
                            val totalItems = order.totalItemcount
                            val imageUrls = ArrayList<String>()
                            val title = StringBuilder()
                            Log.d("ORDER_LIST_CHECK", "OrderId=${order.OrderId} => OrderList is ${order.OrderList}")
                            Log.d("FULL_ORDER", order.toString())

                            order.OrderList?.forEach { item ->
                                Log.d("ORDER_ITEM", "Title=${item.productTitle}, itemcount=${item.itemCount}, Price=${item.productPrice}, Img=${item.productImageUrls}, totalItems = ${order.totalItemcount}")

//
                                val price = item.productPrice.toString().toInt()
                                val itemCount = item.itemCount.toString().toInt()
                                totalPrice += (price * itemCount)

                                item.productTitle?.let {
                                    title.append(it).append(", ")
                                }

                                item.productImageUrls?.let {
                                    if (it.isNotEmpty()) imageUrls.add(it)
                                }
                            }

                            val orderedItem = OrderedItems(
                                OrderId = order.OrderId ?: "N/A",
                                date = order.OrderDate ?: "",
                                OrderStatus = order.orderStatus ?: 0,
                                productTitle = title.trimEnd(',', ' ').toString(),
                                productPrice = totalPrice.toString(),
                                totalitemCount = totalItems.toString().toInt() ,
                                ImageUrl = imageUrls
                            )

                            Log.d("Check", "Order: ${orderedItem.OrderId}, Items: $totalItems, Price: $totalPrice")

                            orderedList.add(orderedItem)
                        }



                        binding.rvOrders.visibility = View.VISIBLE
                        binding.shimmer.visibility = View.GONE
                        binding.noorder.visibility = View.GONE
//                    Log.d("OrderFragment", "Orders to show: ${orderedList.size}")
                        adapter.differ.submitList(orderedList)
                    } else {
                        binding.noorder.visibility = View.VISIBLE
                    }
                }
            }
        }

    }


    private fun navigatetoprofile(){
        binding.toolbar.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_orderFragment_to_profileFragment)
        }
    }
    private fun setUpUI(){
        adapter = OrderFragAdapter(this)
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        binding.noorder.visibility = View.GONE
        binding.rvOrders.visibility = View.GONE
        binding.shimmer.visibility = View.VISIBLE

        shrimmerJob?.cancel()
        shrimmerJob = viewLifecycleOwner.lifecycleScope.launch{
            delay(10_000) // 10sec
            if(adapter.itemCount == 0){
                binding.shimmer.visibility = View.GONE
                binding.noorder.visibility = View.VISIBLE
            }
        }
    }

    override fun updateOrderStatus(orderId: String, status: Int) {
        lifecycleScope.launch(Dispatchers.IO){
            viewModel.updateDeliveryStatus(orderId, status)
        }
    }
}