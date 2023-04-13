package com.compare.compare_user

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class RiwayatAdapter(private val context: Context, private var ListPesanan: MutableList<dataRiwayat>) : RecyclerView.Adapter<RiwayatAdapter.MyViewHolder>() {

    class MyViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val namaPembeli : TextView = itemView.findViewById(R.id.recNamaPembeli)
        val orderID : TextView = itemView.findViewById(R.id.recOrderID)
        val namaKereta : TextView = itemView.findViewById(R.id.recNamaKereta)
        val namaGerbong : TextView = itemView.findViewById(R.id.recNamaGerbong)
        val nomorGerbong : TextView = itemView.findViewById(R.id.recNomorGerbong)
        val nomorKursi: TextView = itemView.findViewById(R.id.recNomorKursi)
        val status: TextView = itemView.findViewById(R.id.recstatusPesanan)
        val cardRiwayat : CardView = itemView.findViewById(R.id.recCardRiwayat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val CardListPesanan =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_riwayat, parent, false)
        return RiwayatAdapter.MyViewHolder(CardListPesanan)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.namaPembeli.text = ListPesanan[position].namaUser
        holder.orderID.text = ListPesanan[position].orderID
        holder.namaKereta.text = ListPesanan[position].namaKereta
        holder.namaGerbong.text = ListPesanan[position].Gerbong
        holder.nomorGerbong.text = ListPesanan[position].nomorGerbong
        holder.nomorKursi.text = ListPesanan[position].nomorKursi
        holder.status.text = ListPesanan[position].status

        holder.cardRiwayat.setOnClickListener {
            val intent = Intent(context, detailPesanan::class.java)
            intent.putExtra("orderID", ListPesanan[position].orderID)
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return ListPesanan.size
    }
}