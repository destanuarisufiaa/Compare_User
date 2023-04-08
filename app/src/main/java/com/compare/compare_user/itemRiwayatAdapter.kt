package com.compare.compare_user

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class itemRiwayatAdapter (private val context: Context, private var itemPesanan: MutableList<itemDataRiwayat>) : RecyclerView.Adapter<itemRiwayatAdapter.MyViewHolder>() {

    class MyViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView){
        val namaMenuRiwayat : TextView = itemView.findViewById(R.id.recTittleRiwayat)
        val hargaItemRiwayat : TextView = itemView.findViewById(R.id.recHargaItemRiwayat)
        val quantityRiwayat : TextView = itemView.findViewById(R.id.recQuantityRiwayat)
        val fotoMenuRiwayat : ImageView = itemView.findViewById(R.id.imagePesananRiwayat)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val cardFormView =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_riwayat, parent, false)
        return itemRiwayatAdapter.MyViewHolder(cardFormView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(itemPesanan[position].image).into(holder.fotoMenuRiwayat)
        holder.namaMenuRiwayat.text = itemPesanan[position].name
        holder.hargaItemRiwayat.text = itemPesanan[position].price
        holder.quantityRiwayat.text = "x" + itemPesanan[position].quantity.toString()
    }

    override fun getItemCount(): Int {
        return itemPesanan.size
    }
}