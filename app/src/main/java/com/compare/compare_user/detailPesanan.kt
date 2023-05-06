package com.compare.compare_user

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.compare.compare_user.databinding.ActivityDetailPesananBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_detail_pesanan.*
import java.util.*
import java.util.concurrent.TimeUnit

class detailPesanan : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPesananBinding

    var db = Firebase.firestore
    val countdownsRef = db.collection("countdowns")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewItemRiwayat.apply {
            layoutManager = LinearLayoutManager(context)
        }

        fetchDataPesanan()

        val textView: TextView = findViewById(R.id.text_view_countdown)

        // Mendapatkan countdown terbaru dari Firebase Firestore
        countdownsRef.orderBy("start_time", Query.Direction.DESCENDING).limit(1)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val startTime = document.getLong("start_time")!!
                    val timeLeft = document.getLong("time_left")!!

                    // Hitung waktu yang tersisa
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - startTime
                    val timeLeftInMillis = timeLeft - elapsedTime

                    val countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(minutes)
                            val timeLeft = String.format("%02d:%02d", minutes, seconds)
                            textView.text = "Waktu tersisa: $timeLeft"
                        }

                        override fun onFinish() {
                            textView
                        }
                    }
                    countDownTimer.start()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting countdowns", exception)
            }

//        // Mendapatkan countdown terbaru dari Firebase Firestore
//        countdownsRef.orderBy("start_time", Query.Direction.DESCENDING).limit(1)
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    val startTime = document.getLong("start_time")!!
//                    val timeLeft = document.getLong("time_left")!!
//
//                    // Hitung waktu yang tersisa
//                    val currentTime = System.currentTimeMillis()
//                    val elapsedTime = currentTime - startTime
//                    val timeLeftInMillis = timeLeft - elapsedTime
//
//                    // Tampilkan countdown
//                    val hours = (timeLeftInMillis / 1000) / 3600
//                    val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
//                    val seconds = (timeLeftInMillis / 1000) % 60
//
//                    val timeLeftFormatted = if (hours > 0) {
//                        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
//                    } else {
//                        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
//                    }
//
//                    // Set text pada TextView
//                    countdownTextView.text = timeLeftFormatted
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error getting countdowns", exception)
//            }
    }

    private fun fetchDataPesanan() {
        val orderID = intent.getStringExtra("orderID").toString()
        val listRiwayatPesanan = mutableListOf<itemDataRiwayat>()
        val riwayatPesanan = FirebaseFirestore.getInstance().collection("pesanan").document("$orderID").collection("menu")
        riwayatPesanan.get()
            .addOnSuccessListener { documents ->
                for (document in documents)
                {

                    if (document.id != "total")
                    {
                        val pesananRiwayat = document.toObject(itemDataRiwayat::class.java)
                        listRiwayatPesanan.add(pesananRiwayat)
                    }
                    binding.recyclerViewItemRiwayat.adapter = itemRiwayatAdapter (this,listRiwayatPesanan)
                }
            }
        riwayatPesanan.document("total").get()
            .addOnSuccessListener {
                tv_totalForm.text = it.getString("total")
            }
    }
}