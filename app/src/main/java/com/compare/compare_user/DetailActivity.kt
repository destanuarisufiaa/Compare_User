package com.compare.compare_user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.compare.compare_user.databinding.ActivityDetailBinding
import com.compare.compare_user.eventbus.UpdateCartEvent
import com.compare.compare_user.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detail.*
import org.greenrobot.eventbus.EventBus

class DetailActivity : AppCompatActivity() {
    var imageURL = ""
    private lateinit var dokumenID:String
    private lateinit var detailTittle : TextView
    private lateinit var detailHarga : TextView
    private lateinit var detailDesc : TextView
    private lateinit var detailImage : ImageView
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detailTittle = findViewById(R.id.detailTittle)
        detailHarga = findViewById(R.id.detailHarga)
        detailDesc = findViewById(R.id.detailDesc)
        detailImage = findViewById(R.id.detailImage)


        val bundle = intent.extras
        if (bundle != null) {
            dokumenID = bundle.getString("namaMenu").toString().trim()
            binding.detailTittle.text = bundle.getString("namaMenu")
            val harga = bundle.getString("Harga")
            binding.detailHarga.text = "Rp. $harga"
            binding.detailDesc.text = bundle.getString("Desc")
            imageURL = bundle.getString("Image")!!
            Glide.with(this).load(bundle.getString("Image")).into(binding.detailImage)
        }
        buttonCart.setOnClickListener {
            addToCart()
        }
    }

    private fun addToCart() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
        val docID = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart").document(
            dokumenID)
        docID.get().addOnSuccessListener {
            if(it.exists()){
                val cartModel = it.toObject(CartModel::class.java)
                cartModel!!.quantity = cartModel!!.quantity+1
                val updateData: MutableMap<String, Any> = HashMap()
//                cartModel!!.quantity = cartModel!!.quantity+1
                updateData["quantity"] = cartModel!!.quantity
                updateData["totalPrice"] = cartModel!!.quantity * cartModel.price!!.toFloat()

                docID.update(updateData)
                    .addOnSuccessListener {
                        EventBus.getDefault().postSticky(UpdateCartEvent())
                        Toast.makeText(this,"Berhasil Menambahkan ke Keranjang",Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
                        Toast.makeText(this,"${it.message}", Toast.LENGTH_SHORT).show()
                    }

            } else{
                val cartModel = CartModel()
                cartModel.key = dokumenID.trim()
                cartModel.name = detailTittle.text.toString().trim()
                cartModel.image = imageURL.trim()
                cartModel.price = detailHarga.text.toString().trim()
                cartModel.quantity = 1
                cartModel.totalPrice = detailHarga.text!!.toString().toFloat()

                docID.set(cartModel)
                    .addOnSuccessListener {
                        EventBus.getDefault().postSticky(UpdateCartEvent())
//                        cartListener.onLoadCartfailed("Success add to cart")
                        Toast.makeText(this,"Berhasil Menambahkan ke Keranjang",Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
//                        cartListener.onLoadCartfailed(it.message)
                        Toast.makeText(this,"${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}