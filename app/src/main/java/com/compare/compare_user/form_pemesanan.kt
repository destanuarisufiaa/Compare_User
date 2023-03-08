package com.compare.compare_user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.compare.compare_user.databinding.ActivityCartBinding
import com.compare.compare_user.databinding.ActivityFormPemesananBinding
import com.compare.compare_user.databinding.CartItemBinding
import com.compare.compare_user.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class form_pemesanan : AppCompatActivity() {

    private lateinit var binding: ActivityFormPemesananBinding
    private val db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var namaKereta : EditText
    private lateinit var noGerbong : EditText
    private lateinit var noKursi : EditText
    private lateinit var nama:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormPemesananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val userid = firebaseAuth.currentUser?.uid

        val ShowNamaPemesan = findViewById<TextView>(R.id.form_namapemesan)

        namaKereta = findViewById(R.id.txt_namaKereta)
        noGerbong = findViewById(R.id.txt_nomorGerbong)
        noKursi = findViewById(R.id.txt_nomorKursi)


        val bundle = intent.extras
        if (bundle != null) {
            binding.tvTotalForm.text = bundle.getString("totalBayar").toString()
        }

        binding.btnNext.setOnClickListener {
            simpandata()
        }

        val docRef =
            db.collection("users").document(userid!!).collection("Profil").document(userid!!)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    ShowNamaPemesan.text = "$name"
                    nama="$name"
                }
            }
        binding.recyclerViewForm.apply {
            layoutManager = LinearLayoutManager(context)
        }
        fetchDataForm()




    }

    private fun fetchDataForm() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
        val StorageForm = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart")

        StorageForm.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val cartMenu = documents.toObjects(MenuCart::class.java)
                    binding.recyclerViewForm.adapter = this.let { FormAdapter (it, cartMenu) }
                }
            }
            .addOnFailureListener {

            }
    }

    private fun simpandata() {
        val inputKereta = namaKereta.text.toString().trim()
        val inputGerbong = noGerbong.text.toString().trim()
        val inputKursi = noKursi.text.toString().trim()
        if (inputKereta=="" || inputGerbong=="" || inputKursi == ""){
            Toast.makeText(this, "Mohon isi semua form yang tersedia terlebih dahulu", Toast.LENGTH_LONG).show()
        }
        else{
            val inputKereta = namaKereta.text.toString().trim()
            val inputGerbong = noGerbong.text.toString().trim()
            val inputKursi = noKursi.text.toString().trim()

            val dbupdate = FirebaseFirestore.getInstance()
            val pesanan = hashMapOf<String, Any>(
                "namaUser" to nama,
                "namaKereta" to inputKereta,
                "nomorGerbong" to inputGerbong,
                "nomorKursi" to inputKursi,
            )
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid
            dbupdate.collection("pesanan").document(nama).collection("identitas").document(nama)
                .get()
                .addOnSuccessListener {
                    if(it.exists()){
                        Toast.makeText(this, "Mohon selesaikan pesanan sebelumnya terlebih dahulu", Toast.LENGTH_SHORT).show()
                    }else
                    {
                        dbupdate.collection("pesanan").document(nama).collection("identitas").document(nama).set(pesanan)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, Home::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed!, gagal $uid", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                }

            val StorageForm = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart")
            val listPesanan = dbupdate.collection("pesanan").document(nama).collection("menu")

            StorageForm.get()
                .addOnSuccessListener { documents ->
                    for (document in documents){
                        listPesanan.document().set(document)
                        var total = binding.tvTotalForm.text.toString()
                        val totalHarga = hashMapOf(
                            "total" to "$total",
                        )
                        listPesanan.document("total").set(totalHarga)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error gaes" , Toast.LENGTH_SHORT).show()
                }

        }

    }

}