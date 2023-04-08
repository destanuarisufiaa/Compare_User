package com.compare.compare_user

import com.google.firebase.firestore.DocumentId

class dataRiwayat(
    val namaKereta : String? = null,
    val Gerbong : String? = null,
    val nomorGerbong : String? = null,
    val nomorKursi : String? = null,
    val namaUser : String? = null,
    val status : String? = null,
    @DocumentId
    val orderID: String = "",

)
