package com.example.shopease.adapters
import com.example.shopease.R
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.shopease.adapters.OrderFragAdapter.ViewHolder
import com.example.shopease.databinding.OrdersItemViewBinding
import com.example.shopease.models.OrderedItems


class OrderFragAdapter(
    private val statusUpdater: OrderStatusUpdater
) : RecyclerView.Adapter<ViewHolder>(){

    val diffUtil = object : DiffUtil.ItemCallback<OrderedItems>(){
        override fun areItemsTheSame(
            p0: OrderedItems,
            p1: OrderedItems
        ): Boolean {
            return p0.OrderId == p1.OrderId
        }
        override fun areContentsTheSame(
            p0: OrderedItems,
            p1: OrderedItems
        ): Boolean {
            return p0 == p1
        }
    }
    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int
    ): ViewHolder {
        return ViewHolder(OrdersItemViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val order = differ.currentList[position]
        holder.binding.apply {
            textOrderDate.text = "Date: ${order.date}"

            when (order.OrderStatus) {
                0 -> {
                    textDeliveryStatus.text = "Getting packed"
                    textDeliveryStatus.setTextColor(Color.parseColor("#FFC107"))
                    orderAnimation.setAnimation("box.json")
                    orderAnimation.visibility = View.VISIBLE
                    textCancelledStatus.visibility = View.GONE
                }
                1 -> {
                    textDeliveryStatus.text = "On the way"
                    textDeliveryStatus.setTextColor(Color.parseColor("#2196F3"))
                    orderAnimation.setAnimation("delivery2.json")
                    orderAnimation.visibility = View.VISIBLE
                    textCancelledStatus.visibility = View.GONE
                }
                2 -> {
                    textDeliveryStatus.text = "Delivered"
                    textDeliveryStatus.setTextColor(Color.parseColor("#4CAF50"))
                    orderAnimation.setAnimation("deliveryComplete.json")
                    orderAnimation.visibility = View.VISIBLE
                    textCancelledStatus.visibility = View.GONE
                }
                3 -> {
                    textDeliveryStatus.text = "Cancelled"
                    textDeliveryStatus.setTextColor(Color.RED)
                    orderAnimation.visibility = View.GONE
                    textCancelledStatus.text = "Seller has cancelled the order"
                    textCancelledStatus.visibility = View.VISIBLE
                    btnCancelOrder.visibility = View.GONE
                }
                4 -> {
                    textDeliveryStatus.text = "Cancelled"
                    textDeliveryStatus.setTextColor(Color.RED)
                    orderAnimation.visibility = View.GONE
                    textCancelledStatus.text = "You have cancelled the order"
                    textCancelledStatus.visibility = View.VISIBLE
                    btnCancelOrder.visibility = View.GONE
                }
            }

            textProductTitle.text = order.productTitle
            textPrice.text = "₹${order.productPrice}"
            textQuantity.text = "Items: ${order.totalitemCount}"

            val list = arrayListOf<SlideModel>()
            order.ImageUrl.forEach { list.add(SlideModel(it)) }
            rvSlideImages.setImageList(list)



            // Cancel order with confirmation
            iconCancelTrigger.setOnClickListener {
                val dialogView = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.custom_cancelorder_box, null)

                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()

                // Access buttons inside the custom dialog
                val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
                val btnNo = dialogView.findViewById<Button>(R.id.btnNo)

                btnYes.setOnClickListener {
                    // Update status in Firebase
                    statusUpdater.updateOrderStatus(order.OrderId,4) // updating the order status in firebase
                    // Instantly update UI
                    textDeliveryStatus.text = "Cancelled"
                    textDeliveryStatus.setTextColor(Color.RED)
                    textCancelledStatus.text = "You have cancelled the order"
                    textCancelledStatus.visibility = View.VISIBLE
                    orderAnimation.visibility = View.GONE
                    btnCancelOrder.visibility = View.GONE



                    dialog.dismiss()
                }
                btnNo.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
            textCancelledStatus.setOnClickListener{
                val newlist = differ.currentList.toMutableList()
                newlist.removeAt(holder.adapterPosition)
                differ.submitList(newlist)
            }

        }
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    class ViewHolder(val binding : OrdersItemViewBinding) : RecyclerView.ViewHolder(binding.root)

}