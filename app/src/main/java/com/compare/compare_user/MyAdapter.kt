package com.compare.compare_user

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.compare.compare_user.eventbus.UpdateCartEvent
import com.compare.compare_user.listener.ICartLoadListener
import com.compare.compare_user.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.greenrobot.eventbus.EventBus

private lateinit var IDgaes:String

class MyAdapter (private val context: Context, private var MenuList: MutableList<Menu>, private val cartListener:ICartLoadListener) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val judulMenu : TextView = itemView.findViewById(R.id.recTittle)
        val HargaMenu : TextView = itemView.findViewById(R.id.recPrice)
        val Desc : TextView =  itemView.findViewById(R.id.recDesc)
        val fotoMenu : ImageView = itemView.findViewById(R.id.recImage)
        val card : CardView = itemView.findViewById(R.id.recCard)
        val documentID : TextView = itemView.findViewById(R.id.docID)
        val cart : Button = itemView.findViewById(R.id.AddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val menuView =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return MyViewHolder(menuView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(MenuList[position].Foto).into(holder.fotoMenu)
        holder.judulMenu.text = MenuList[position].namaMenu
        holder.HargaMenu.text = MenuList[position].Harga
        holder.Desc.text = MenuList[position].Desc
        holder.documentID.text = MenuList[position].docID

        holder.cart.setOnClickListener {
            IDgaes = MenuList[holder.adapterPosition].namaMenu.toString().trim()

            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
            val docID = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart").document(
                "$IDgaes")
            docID.get().addOnSuccessListener {
                if(it.exists()){
                    val cartModel = it.toObject(CartModel::class.java)
                    cartModel!!.quantity = cartModel!!.quantity+1
                    val updateData: MutableMap<String, Any> = HashMap()
                    updateData["quantity"] = cartModel!!.quantity
                    updateData["totalPrice"] = cartModel!!.quantity * cartModel.price!!.toFloat()

                    docID.update(updateData)
                        .addOnSuccessListener {
                            EventBus.getDefault().postSticky(UpdateCartEvent())
//                            cartListener.onLoadCartfailed("Success add to cart")
                            Toast.makeText(context,"Berhasil Ditambahkan ke Keranjang",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{
                            cartListener.onLoadCartfailed(it.message)
                        }

                } else{
                    val cartModel = CartModel()
                    cartModel.key = MenuList[position].docID
                    cartModel.name = MenuList[position].namaMenu
                    cartModel.image = MenuList[position].Foto
                    cartModel.price = MenuList[position].Harga
                    cartModel.quantity = 1
                    cartModel.totalPrice = MenuList[position].Harga!!.toFloat()

                    docID.set(cartModel)
                        .addOnSuccessListener {
                            EventBus.getDefault().postSticky(UpdateCartEvent())
//                            cartListener.onLoadCartfailed("Success add to cart")
                            Toast.makeText(context,"Berhasil Ditambahkan ke Keranjang",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{
                            cartListener.onLoadCartfailed(it.message)
                        }
                }
            }
        }

        holder.card.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("Image", MenuList[holder.adapterPosition].Foto)
            intent.putExtra("namaMenu", MenuList[holder.adapterPosition].namaMenu)
            intent.putExtra("Harga", MenuList[holder.adapterPosition].Harga)
            intent.putExtra("Desc", MenuList[holder.adapterPosition].Desc)
            intent.putExtra("docID", MenuList[holder.adapterPosition].docID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return MenuList.size
    }
}