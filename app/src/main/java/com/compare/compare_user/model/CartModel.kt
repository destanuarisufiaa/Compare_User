package com.compare.compare_user.model

import com.google.firebase.firestore.DocumentId

class CartModel {
    var key:String?=null
    var name:String?=null
    var price:String?=null
    var image:String?=null
    var quantity = 0
    var totalPrice = 0f

    @DocumentId
    val docID:String = ""
}