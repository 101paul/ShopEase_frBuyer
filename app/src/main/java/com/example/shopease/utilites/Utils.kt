package com.example.shopease.utilites

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.example.shopease.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    private lateinit var dialog : Dialog
    fun showtoast(context : Context ,msg : String ){
        Toast.makeText(context , msg , Toast.LENGTH_LONG).show()
    }
    fun showDialog(context : Context,msg : String){
        dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_box1)
        var ShowMsg1 = dialog.findViewById<TextView>(R.id.dialogT1)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT

        )
        ShowMsg1.text = msg?.toString()
        dialog.show()
    }
    fun shutDialog(context : Context){
        dialog.dismiss()
    }

    private var firebaseAuthInstance : FirebaseAuth?= null
    fun getAuthInstance() : FirebaseAuth{
        if(firebaseAuthInstance == null){
            firebaseAuthInstance = FirebaseAuth.getInstance()
        }
        return firebaseAuthInstance!!
    }
    fun getCurrentUserid() : String {
        val user = FirebaseAuth.getInstance().currentUser?.uid.toString()
        Log.d("FirebaseUserID", user ?: "NULL")

        return user
    }
    fun generateRandomId(length: Int = 12): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = Date()
        return formatter.format(date)
    }


}