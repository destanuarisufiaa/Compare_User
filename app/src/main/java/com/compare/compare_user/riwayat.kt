package com.compare.compare_user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.compare.compare_user.databinding.FragmentRiwayatBinding
import com.google.firebase.firestore.FirebaseFirestore

class riwayat : Fragment() {

    private lateinit var binding: FragmentRiwayatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewPesanan.apply {
            layoutManager = LinearLayoutManager(context)

        }
        riwayatPesanan()
    }

    private fun riwayatPesanan() {
        val listPesananRiwayat = FirebaseFirestore.getInstance().collection("pesanan")
        listPesananRiwayat.get()
            .addOnSuccessListener { documents ->
                for (document in documents)
                {
                    val riwayat = documents.toObjects(dataRiwayat::class.java)
                    binding.recyclerViewPesanan.adapter = context?.let { RiwayatAdapter(it, riwayat) }

                }

            }
    }

}