package com.compare.compare_user

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.compare.compare_user.eventbus.UpdateCartEvent
import com.compare.compare_user.listener.ICartLoadListener
import com.compare.compare_user.listener.IRecyclerClickListener
import com.compare.compare_user.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.greenrobot.eventbus.EventBus

private lateinit var IDgaes: String

class MyAdapter (private val context: Context, private var MenuList: MutableList<Menu>, private val cartListener:ICartLoadListener) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val judulMenu : TextView = itemView.findViewById(R.id.recTittle)
        val HargaMenu : TextView = itemView.findViewById(R.id.recPrice)
        val Desc : TextView =  itemView.findViewById(R.id.recDesc)
        val fotoMenu : ImageView = itemView.findViewById(R.id.recImage)
        val card : CardView = itemView.findViewById(R.id.recCard)
        val documentID : TextView = itemView.findViewById(R.id.docID)
        val cart : Button = itemView.findViewById(R.id.AddToCart)


        private var clickListener:IRecyclerClickListener?=null

        fun setClickListener(clickListener: IRecyclerClickListener){
            this.clickListener = clickListener
        }

        init {
            cart.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener!!.onItemClickListener(v, adapterPosition)
        }
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
        IDgaes = MenuList[position].docID

        holder.cart.setOnClickListener(object : IRecyclerClickListener, View.OnClickListener {
            override fun onItemClickListener(view: View?, position: Int) {
                addToCart(MenuList[position])
            }

            override fun onClick(p0: View?) {
                addToCart(MenuList[holder.adapterPosition])
            }
        })

        holder.card.setOnClickListener {

            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("Image", MenuList[holder.adapterPosition].Foto)
            intent.putExtra("namaMenu", MenuList[holder.adapterPosition].namaMenu)
            intent.putExtra("Harga", MenuList[holder.adapterPosition].Harga)
            intent.putExtra("Desc", MenuList[holder.adapterPosition].Desc)
            context.startActivity(intent)
        }
    }

    private fun addToCart(menu: Menu) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
        val docID = FirebaseFirestore.getInstance().collection("users").document(uid)
        val DBcart = FirebaseFirestore.getInstance().collection("Cart").document()

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
                        cartListener.onLoadCartfailed("Success add to cart")
                    }
                    .addOnFailureListener{
                        cartListener.onLoadCartfailed(it.message)
                    }

            } else{
                val cartModel = CartModel()
                cartModel.key = menu.docID
                cartModel.name = menu.namaMenu
                cartModel.image = menu.Foto
                cartModel.price = menu.Harga
                cartModel.quantity = 1
                cartModel.totalPrice = menu.Harga!!.toFloat()

                docID.set(cartModel)
                    .addOnSuccessListener {
                        EventBus.getDefault().postSticky(UpdateCartEvent())
                        cartListener.onLoadCartfailed("Success add to cart")
                    }
                    .addOnFailureListener{
                        cartListener.onLoadCartfailed(it.message)
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return MenuList.size
    }
}