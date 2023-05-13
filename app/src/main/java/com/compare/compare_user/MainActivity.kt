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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var ringtone: Ringtone
    private var isNotificationPlayed = false

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
        val pesananRef = db.collection("pesanan")

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
                for (doc in snapshot.documents) {
                    val statusPesanan = doc.getString("status")

                    // Memeriksa status pesanan dan memainkan ringtone yang sesuai
                    when (statusPesanan) {
                        "PERSIAPAN" -> {
                            if (!isNotificationPlayed) {
                                ringtone = RingtoneManager.getRingtone(applicationContext, ringtonePersiapanUri)
                                ringtone.play()
                                isNotificationPlayed = true
                            }
                        }
                        "ANTAR" -> {
                            if (!isNotificationPlayed) {
                                ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneAntarUri)
                                ringtone.play()
                                isNotificationPlayed = true
                            }
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
            } else {
                Log.d(TAG, "Current data: null")
            }
            // Set nilai isNotificationPlayed menjadi false ketika terdapat perubahan data
            isNotificationPlayed = false
        }
    }

    private fun replaceFragment(fragment: riwayat){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
    private fun replaceFragment(fragment: Home) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun replaceFragment(fragment: ProfilPengguna) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}