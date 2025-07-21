package com.example.shopease.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.databinding.CategoryItemViewBinding
import com.example.shopease.models.category

class AdapterCategory (
    val categoryList: ArrayList<category>,
    val CategoryOnClicked: (category) -> Unit
)
    : RecyclerView.Adapter<AdapterCategory.CategoryViewHolder>()
{
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        return CategoryViewHolder(
            CategoryItemViewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {
        val category = categoryList[position]
        holder.binding.apply {
            categoryimg.setImageResource(category.image!!.toInt())
            categorytitle.text = category.title.toString()
        }
        holder.itemView.setOnClickListener{
            CategoryOnClicked(category)
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    class CategoryViewHolder(
       val binding : CategoryItemViewBinding) : RecyclerView.ViewHolder(binding.root)
}
