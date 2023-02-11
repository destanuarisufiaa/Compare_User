package com.compare.compare_user

import com.google.firebase.firestore.DocumentId

data class Menu(
    val namaMenu: String? = null,
    val Harga: String? = null,
    val Desc: String? = null,
    val Foto: String? = null,
    @DocumentId
    val docID: String = "",
)
