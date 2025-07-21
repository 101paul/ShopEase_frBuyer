package com.example.shopease.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.shopease.R
import com.example.shopease.databinding.FragmentAddressBinding
import com.example.shopease.viewModels.UserViewModel
import kotlinx.coroutines.launch

class addressFragment : Fragment() {
    private lateinit var binding : FragmentAddressBinding
    private val viewModel : UserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         getBacktoHomeFrag()
         getAddress()
    }
    private fun getBacktoHomeFrag(){
        binding.toolbar.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_addressFragment_to_profileFragment)
        }
    }
    private fun getAddress(){
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.getUserAddressFromFB().collect{address->
                binding.address.text = address
            }
        }
    }
}