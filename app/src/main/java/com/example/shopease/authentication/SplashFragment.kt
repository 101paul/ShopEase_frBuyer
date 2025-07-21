package com.example.shopease.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.shopease.R
import com.example.shopease.activity.UsersActivity
import com.example.shopease.databinding.FragmentSplashBinding
import com.example.shopease.viewModels.AuthViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private val viewModel : AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch{
            delay(2000)
            viewModel.CurrentUser.collect{
                if(it){
                    startActivity(Intent(requireActivity() , UsersActivity::class.java))
                    requireActivity().finish()
                }else{
                    findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
                }
            }
        }
    }
}