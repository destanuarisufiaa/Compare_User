package com.compare.compare_user

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.compare.compare_user.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cart.*

class Cart : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var sumHarga:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(context)
        }
        fetchData()

        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
        val bahanDelete = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart")

        btn_next.setOnClickListener {
            val intent = Intent (this, form_pemesanan::class.java)
            intent.putExtra("totalBayar", tv_total.text)
            intent.putExtra("sumHarga", sumHarga)
            startActivity(intent)

        }
        //DELETE SEMUA ITEM CART
        btn_delete.setOnClickListener {
            //bahanDelete NGAMBIL DARI FIRESTORE
            bahanDelete.get()
                //jika ada, semua cart dimasukkan ke dalam variabel Documents
                .addOnSuccessListener { Documents ->
                    for (document in Documents){
                        //IDdoc = id setiap dokumen menu
                        val IDdoc = document.id
                        //kemudian dihapus masing-masing dokumen ID
                        bahanDelete.document(IDdoc).delete()
                        }
                    //jika berhasil menampilkan toast
                    Toast.makeText(this, "Berhasil Menghapus Semua Cart", Toast.LENGTH_SHORT).show()
                    //cara update untuk tampilan activity (merefresh tampilannya jika semua telah dihapus)
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    //jika dikeranjang telah terhapus semua, maka akan berpindah ke halaman HOME
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }.addOnFailureListener {
                    Toast.makeText(this, "Gagal Menghapus Semua Cart", Toast.LENGTH_SHORT).show()
                }
                }
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
            .addOnFailureListener {

            }
        //untuk memulai pembacaan secara realtime jika terdapat perubahan data pada cart
        //dan dimasukkan ke dalam variabel snapshot dg exception di deklrasikan dengan variabel e
        Storage.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            var totalHarga = 0f
            //seleksi kondisi, jika snapshot (cart) tidak ada data
            if (snapshot!!.isEmpty){
                //membuat tombol next tidak tersedia jika keranjang kosong
                btn_next.setVisibility(View.INVISIBLE)
                // maka pada total di setting menjadi Rp. 0
                tv_total.text = "Rp. 0"
            }else{
                //jika pada snapshot (cart) ada
                for (document in snapshot!!){
                    //maka menghitung total harga
                    totalHarga += document.get("totalPrice").toString().toFloat()
                    tv_total.text = "Rp. " + totalHarga.toInt().toString()
                    sumHarga = totalHarga.toInt().toString()
                }
            }


        }

    }

}