package com.compare.compare_user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.compare.compare_user.databinding.FragmentHomeBinding
import com.compare.compare_user.eventbus.UpdateCartEvent
import com.compare.compare_user.listener.ICartLoadListener
import com.compare.compare_user.model.CartModel
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class Home : Fragment(), ICartLoadListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var idgaes : String
    private lateinit var cartLoadListener: ICartLoadListener

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateCartEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCartEvent::class.java)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public fun onUpdateCartEvent(event: UpdateCartEvent){
        countCartFromFirebase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }
        init()
        fetchData()
        countCartFromFirebase()
    }

    private fun init() {
        cartLoadListener =this
    }

    private fun countCartFromFirebase() {
        val cartModels:MutableList<CartModel> = ArrayList()
        var FStore = FirebaseFirestore.getInstance().collection("Cart")
        FStore.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val cartModel = document.toObject(CartModel::class.java)
                    cartModel!!.key = document.id
                    cartModels.add(cartModel)
                }
                cartLoadListener.onLoadCartSuccess(cartModels)
            }.addOnFailureListener {
                cartLoadListener.onLoadCartfailed(it.message)
            }

    }


    private fun fetchData() {
        FirebaseFirestore.getInstance().collection("Menu")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    idgaes = document.id
                    val menu = documents.toObjects(Menu::class.java)
                    binding.recyclerView.adapter = context?.let { MyAdapter (it, menu, cartLoadListener) }
                }

            }
            .addOnFailureListener {

            }
    }

    override fun onLoadCartSuccess(cartModelList: List<CartModel>) {
        var cartSum = 0
        for (cartModel in cartModelList!!) cartSum += cartModel!!.quantity
        badge!!.setNumber(cartSum)
    }

    override fun onLoadCartfailed(message: String?) {
//        Toast.makeText(activity, "$activity.exception?.message}", Toast.LENGTH_SHORT).show()
        Toast.makeText(activity, "SUKSESSSSS", Toast.LENGTH_SHORT).show()
    }
}