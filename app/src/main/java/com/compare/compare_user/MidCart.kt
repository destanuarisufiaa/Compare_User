package com.compare.compare_user

import com.google.firebase.firestore.DocumentId

class MidCart {
    var key:String?=null
    var name:String?=null
    var price:String?=null
    var image:String?=null
    var quantity = 0
    var totalPrice = 0f

    @DocumentId
    val docID:String = ""
}