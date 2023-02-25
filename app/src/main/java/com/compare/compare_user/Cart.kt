package com.compare.compare_user

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.compare.compare_user.databinding.ActivityCartBinding
import com.compare.compare_user.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.cart_item.*
import kotlinx.android.synthetic.main.fragment_home.*

class Cart : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(context)
        }
        fetchData()
    }

    private fun fetchData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
        val Storage = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart")

        Storage.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val cartMenu = documents.toObjects(MenuCart::class.java)
                    binding.recyclerViewCart.adapter = this.let { CartAdapter (it, cartMenu) }
                }
            }
            .addOnFailureListener { }
        Storage.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            var totalHarga = 0f
            for (document in snapshot!!){
                totalHarga += document.get("totalPrice").toString().toFloat()
                tv_total.text = totalHarga.toInt().toString()
            }

        }

    }
}