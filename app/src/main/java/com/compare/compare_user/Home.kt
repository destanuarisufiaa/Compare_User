package com.compare.compare_user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.compare.compare_user.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class Home : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)

        }
        fetchData()
    }

    private fun fetchData() {
        FirebaseFirestore.getInstance().collection("Menu")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val menu = documents.toObjects(Menu::class.java)
                    binding.recyclerView.adapter = context?.let { MyAdapter (it, menu) }
                }

            }
            .addOnFailureListener {

            }
    }
}