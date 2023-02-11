package com.compare.compare_user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.compare.compare_user.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    var imageURL = ""
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
            binding.detailTittle.text = bundle.getString("namaMenu")
            binding.detailHarga.text = bundle.getString("Harga")
            binding.detailDesc.text = bundle.getString("Desc")
            imageURL = bundle.getString("Image")!!
            Glide.with(this).load(bundle.getString("Image")).into(binding.detailImage)
        }
    }
}