package com.compare.compare_user.listener

import com.compare.compare_user.model.CartModel

interface ICartLoadListener {
    fun onLoadCartSuccess(cartModelList: MutableList<CartModel>)
    fun onLoadCartfailed(message:String?)
}