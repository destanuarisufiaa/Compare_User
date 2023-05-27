package com.compare.compare_user

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.compare.compare_user.databinding.FragmentRiwayatBinding
import com.google.firebase.auth.FirebaseAuth
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val auth = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Profil").document(uid!!)
        auth.get()
            .addOnSuccessListener {
               val nama = it.getString("name").toString()

                //mengambil riwayat
                val listPesananRiwayat = FirebaseFirestore.getInstance().collection("pesanan").whereEqualTo("namaUser", "$nama" )
                listPesananRiwayat.addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        // Jika terjadi error pada listener
                        return@addSnapshotListener
                    }

                    // Jika tidak ada error, kita cek apakah snapshot berisi data
                    if (snapshots != null && !snapshots.isEmpty) {
                        val riwayat = snapshots.toObjects(dataRiwayat::class.java)
                        //requireContext() digunakan untuk mengambil context fragment yang dipastikan selalu tidak null.
                        binding.recyclerViewPesanan.adapter = requireContext().let { RiwayatAdapter(it, riwayat) }

                    }
                }
            }


    }
}