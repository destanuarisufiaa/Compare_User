package com.compare.compare_user

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.compare.compare_user.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var ringtone: Ringtone
    private var isNotificationPlayed = false
    private var nama:String? = null
    private var previousStatus: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.getStringExtra("direct")
        if (bundle == "back"){
            replaceFragment(ProfilPengguna())
        }else
            if (bundle == "true")
            {
                replaceFragment(riwayat())
            } else replaceFragment(Home())

        //pemilihan item pada bottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(Home())
                R.id.riwayat -> replaceFragment(riwayat())
                R.id.profile -> replaceFragment(ProfilPengguna())
                else ->{
                }
            }

            true
        }
        // Mengambil referensi dari Firestore
        val db = Firebase.firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val fstore = db.collection("users").document(uid).collection("Profil").document(uid).get()
        fstore.addOnSuccessListener {
                nama = it.getString("name").toString().trim()
                Log.w("nama", nama!!)
            }
                .addOnFailureListener {
                    Log.w("gagal", it)
                }

        val pesananRef = FirebaseFirestore.getInstance().collection("pesanan")

        // Mendefinisikan uri dari file audio
        val ringtonePersiapanUri = Uri.parse("android.resource://com.compare.compare_user/" + R.raw.disiapkan)
        val ringtoneAntarUri = Uri.parse("android.resource://com.compare.compare_user/" + R.raw.diantar)
        val ringtoneSelesaiUri = Uri.parse("android.resource://com.compare.compare_user/" + R.raw.selesai)

        // Menambahkan listener untuk mengambil data
        pesananRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            // Memastikan bahwa ada data yang ditemukan
            if (snapshot != null && !snapshot.isEmpty) {
                // Reset isNotificationPlayed sebelum memulai perulangan
                isNotificationPlayed = false
                // Memulai perulangan untuk setiap dokumen dalam snapshot
                for (doc in snapshot.documents) {
                    //inisialisasi variabel nama user dengan mengambil dokumen namaUser pada koleksi profil
                    val namaUser = doc.getString("namaUser")
                    if (namaUser == nama){
                        //mengambil status pesanan
                        val statusPesanan = doc.getString("status")
                        // Memeriksa apakah status pesanan berubah
                        if (statusPesanan != previousStatus) {
                            // Memeriksa status pesanan dan memainkan ringtone yang sesuai
                            when (statusPesanan) {
                                "PERSIAPAN" -> {
                                    if (!isNotificationPlayed) {
                                        ringtone = RingtoneManager.getRingtone(applicationContext, ringtonePersiapanUri)
                                        ringtone.play()
                                        isNotificationPlayed = true
                                    }else{
                                        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneAntarUri)
                                        ringtone.stop()}

                                }
                                "ANTAR" -> {
                                    if (!isNotificationPlayed) {
                                        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneAntarUri)
                                        ringtone.play()
                                        isNotificationPlayed = true

                                    }else{
                                        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneAntarUri)
                                        ringtone.stop()}

                                }
//                        "SELESAI" -> {
//                            if (!isNotificationPlayed) {
//                                ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneSelesaiUri)
//                                ringtone.play()
//                                isNotificationPlayed = true
//                            }
//                        }
                                else -> {
                                    if (isNotificationPlayed) {
                                        ringtone.stop()
                                        isNotificationPlayed = false
                                    }
                                }

                            }
                        }
                        previousStatus = statusPesanan
                    }
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
            // Set nilai isNotificationPlayed menjadi false ketika terdapat perubahan data
            isNotificationPlayed = false
        }
    }

    //fungsi replace fragment pada fragment riwayat
    //menggantikan fragment saat ini dengan fragment baru (riwayat)
    private fun replaceFragment(fragment: riwayat){
        //digunakan untuk mendapatkan instance FragmentManager
        val fragmentManager = supportFragmentManager
        // untuk memulai transaksi fragment
        val fragmentTransaction = fragmentManager.beginTransaction()
        //menggantikan fragment dengan tampilan fragment riwayat
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
    //fungsi replace fragment pada fragment home
    //menggantikan fragment saat ini dengan fragment baru (home)
    private fun replaceFragment(fragment: Home) {
        //digunakan untuk mendapatkan instance FragmentManager
        val fragmentManager = supportFragmentManager
        // untuk memulai transaksi fragment
        val fragmentTransaction = fragmentManager.beginTransaction()
        //menggantikan fragment dengan tampilan fragment home
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
    //fungsi replace fragment pada fragment profile pengguna
    //menggantikan fragment saat ini dengan fragment baru (profilPengguna)
    private fun replaceFragment(fragment: ProfilPengguna) {
        //digunakan untuk mendapatkan instance FragmentManager
        val fragmentManager = supportFragmentManager
        // untuk memulai transaksi fragment
        val fragmentTransaction = fragmentManager.beginTransaction()
        //menggantikan fragment dengan tampilan fragment profilPengguna
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}