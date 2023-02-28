package com.compare.compare_user

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FormAdapter (private val context: Context, private var CartList: MutableList<MenuCart>) : RecyclerView.Adapter<FormAdapter.MyViewHolder>() {

    class MyViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val namaMenuForm : TextView = itemView.findViewById(R.id.recTittleForm)
        val hargaItemForm : TextView = itemView.findViewById(R.id.recHargaItemForm)
        val quantityForm : TextView = itemView.findViewById(R.id.recQuantityForm)
        val fotoMenuForm : ImageView = itemView.findViewById(R.id.imagePesanan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val cardFormView =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_form, parent, false)
        return FormAdapter.MyViewHolder(cardFormView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(CartList[position].image).into(holder.fotoMenuForm)
        holder.namaMenuForm.text = CartList[position].name
        holder.hargaItemForm.text = CartList[position].price
        holder.quantityForm.text = "x" + CartList[position].quantity.toString()
    }

    override fun getItemCount(): Int {
        return CartList.size
    }
}