package com.example.shopease.fragments

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.shopease.R
import com.example.shopease.databinding.FragmentProfileBinding
import com.example.shopease.viewModels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {
    private lateinit var binding : FragmentProfileBinding
    private lateinit var pickImageLauncher : ActivityResultLauncher<String>
    private val viewModel : UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.imageProfile.setImageURI(uri) // Optimistic UI update
                viewModel.uploadProfileImage(uri) { success, url ->
                    if (success) {
                        Toast.makeText(requireContext(), "Profile picture saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.btnEditProfilePic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        binding.textUserName.visibility = View.GONE
        binding.phnNumber.visibility = View.GONE
        setProfilePicture()
        navigatetoHomeFragment()
        navigatetoorders()
        userNumandName()
        navigatetoaddress()
    }


    private fun navigatetoHomeFragment(){
         binding.toolbar.setNavigationOnClickListener{
             findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
         }
    }
    private fun navigatetoorders(){
        binding.sectionOrders.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_orderFragment)
        }
    }
    private fun navigatetoaddress(){
        binding.sectionAddress.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_addressFragment)
        }
    }
    private fun userNumandName(){
            viewLifecycleOwner.lifecycleScope.launch {
                launch {
                    viewModel.getUserPhnNumber().collect { phnNumber ->
                        binding.phnNumber.text = phnNumber
                        binding.phnNumber.visibility = View.VISIBLE

                    }
                }
                launch {
                    viewModel.getUserName().collect { name ->
                        binding.textUserName.text = name
                        binding.textUserName.visibility = View.VISIBLE
                    }
                }
            }
        }
    private fun setProfilePicture(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getProfileImageUrl().collect { url ->
                if (!url.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(url)
                        .placeholder(R.drawable.backgroundforprofilepicture) // fallback image
                        .into(binding.imageProfile)
                }
            }
        }

    }
}