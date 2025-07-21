package com.example.shopease.search

import android.widget.Filter
import com.example.shopease.adapters.ProductsAdapter
import com.example.shopease.models.Product
import kotlin.apply
import kotlin.collections.filter
import kotlin.text.contains
import kotlin.text.isNullOrEmpty
import kotlin.text.lowercase
import kotlin.text.trim

class FilteringProducts(
    val adapter : ProductsAdapter ,
    val originalList : ArrayList<Product>
) : Filter() {

    companion object {
        var filterCallback: ((Boolean) -> Unit)? = null
    }
    override fun performFiltering(constraint: CharSequence?): FilterResults? {
        val filteredList = if (constraint.isNullOrEmpty()) {
            originalList
        } else {
            val query = constraint.toString().trim().lowercase()
            originalList.filter {// this filter is a collection function , it only creates
                // a new list and store the items which matches it criterias
                it.productTitle?.lowercase()?.contains(query) ==  true ||
                   it.productCategory?.lowercase()?.contains(query) ==  true
            }
        }

        return FilterResults().apply {
            values = filteredList
        }
    }
    override fun publishResults(
        constraint: CharSequence?,
        results: FilterResults?
    ) {
        val filtered = results?.values as List<Product>
        adapter.differ.submitList(filtered.toList()) // Always submit a fresh copy Sometimes your RecyclerView won’t update if the data
        // object reference is the same as before (even if its contents changed) in the case of diffutil if we use , thats why we use .toList()
       // creates a new immutable copy of the list.
        filterCallback?.invoke(filtered.isEmpty()) // Notify UI (for Lottie)
    }
}