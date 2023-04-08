package com.compare.compare_user

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.compare.compare_user.eventbus.UpdateCartEvent
import com.compare.compare_user.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cart.*
import org.greenrobot.eventbus.EventBus
import org.w3c.dom.Text

class CartAdapter(private val context: Context, private var CartList: MutableList<MenuCart>) : RecyclerView.Adapter<CartAdapter.MyViewHolder>() {

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val namaMenu : TextView = itemView.findViewById(R.id.tv_nama)
        val harga : TextView = itemView.findViewById(R.id.tv_harga)
        val priceasli : TextView = itemView.findViewById(R.id.tv_hargaAsli)
        val foto : ImageView = itemView.findViewById(R.id.img_produk)
        val quantity : TextView = itemView.findViewById(R.id.tv_jumlah)
        val cartplus : ImageView = itemView.findViewById(R.id.btn_tambah)
        val cartmin: ImageView = itemView.findViewById(R.id.btn_kurang)
        val deleteholder : ImageView = itemView.findViewById(R.id.btn_delete2)
//        val total : TextView = itemView.findViewById(R.id.tv_harga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val cartView =
            LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return MyViewHolder (cartView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(CartList[position].image).into(holder.foto)
        holder.namaMenu.text = CartList[position].name
        holder.priceasli.text = CartList[position].price
        holder.harga.text = (CartList[position].price.toString().toInt() * CartList[position].quantity.toString().toInt()).toString()
        holder.quantity.text = CartList[position].quantity.toString()


        val IDgaes = CartList[position].name.toString().trim()
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
        val docID = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart").document(
            "$IDgaes")

        cekCart()

        holder.cartplus.setOnClickListener {
            docID.get().addOnSuccessListener {
                val cartModel = it.toObject(MenuCart::class.java)
                cartModel!!.quantity = cartModel!!.quantity +1
                val updateData: MutableMap<String, Any> = HashMap()
                updateData["quantity"] = cartModel!!.quantity
                val totalharga = cartModel!!.quantity * cartModel.price!!.toFloat()
                updateData["totalPrice"] = totalharga

                docID.update(updateData)
                    .addOnSuccessListener {
                        EventBus.getDefault().postSticky(UpdateCartEvent())
                        holder.quantity.text = cartModel!!.quantity.toString()
                        holder.harga.text = totalharga.toInt().toString()
                        Toast.makeText(context, "Berhasil Menambahkan", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Gagal Menambahkan", Toast.LENGTH_SHORT).show()
                    }


            }.addOnFailureListener {
                        Toast.makeText(context, "Gagal Membaca Data Cart", Toast.LENGTH_SHORT).show()
                    }
        }

        holder.cartmin.setOnClickListener {
            docID.get().addOnSuccessListener {
                val cartModel = it.toObject(MenuCart::class.java)
                cartModel!!.quantity = cartModel!!.quantity -1
                if (cartModel!!.quantity > 0){
                    val updateData: MutableMap<String, Any> = HashMap()
                    updateData["quantity"] = cartModel!!.quantity
                    val totalharga = cartModel!!.quantity * cartModel.price!!.toFloat()
                    updateData["totalPrice"] = totalharga
                    docID.update(updateData)
                        .addOnSuccessListener {
                            EventBus.getDefault().postSticky(UpdateCartEvent())
                            holder.quantity.text = cartModel!!.quantity.toString()
                            holder.harga.text = totalharga.toInt().toString()
                            Toast.makeText(context, "Berhasil Mengurangi", Toast.LENGTH_SHORT).show()

                        }.addOnFailureListener {
                            Toast.makeText(context, "Gagal Mengurangi", Toast.LENGTH_SHORT).show()
                        }
                }else{
                    docID.delete()
                        .addOnSuccessListener {
                            CartList.removeAt(position)
                            notifyDataSetChanged()

                        }
                    notifyDataSetChanged()

                }

            }.addOnFailureListener {
                Toast.makeText(context, "Gagal Membaca Data Cart", Toast.LENGTH_SHORT).show()
            }
        }

        // apabila tombol delete per holder item di klik
        holder.deleteholder.setOnClickListener {
            //docID merupakan variabel yang dideklrasikan diatas (dibaris 49)
            //mengambil dokumen dari setiap menu berdasarkan nama menu nya
            docID.delete()
                .addOnSuccessListener {
                    //menghapus pada tampilan activity sesuai posisi menu yang dihapus
                    CartList.removeAt(position)
                    //fungsi notifyDataSetChanged untuk mengupdate tampilan bahwa telah dihapus
                    notifyDataSetChanged()

//                    if (CartList.isEmpty()) {
//                        Toast.makeText(context, "Keranjang Kosong", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(context,MainActivity::class.java)
//                        context.startActivity(intent)
//                    }
                }
        }
        }

    private fun cekCart() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
        val Storage = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart")

        Storage.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            //seleksi kondisi, jika snapshot (cart) tidak ada data
            if (snapshot!!.isEmpty){
                Toast.makeText(context, "Keranjang Kosong", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }


        }
    }


    override fun getItemCount(): Int {
        return CartList.size
        notifyDataSetChanged()
    }
    }