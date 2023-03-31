package com.compare.compare_user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.compare.compare_user.databinding.ActivityFormPemesananBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import kotlinx.android.synthetic.main.activity_form_pemesanan.*
class form_pemesanan : AppCompatActivity(), TransactionFinishedCallback{

    private lateinit var binding: ActivityFormPemesananBinding
    private val db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var namaKereta : EditText
    private lateinit var noGerbong : EditText
    private lateinit var noKursi : EditText
    private lateinit var nama:String
    private lateinit var phone:String
    private lateinit var email:String
    private lateinit var hasilGerbongKereta: String
    private lateinit var totall : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormPemesananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SdkUIFlowBuilder.init()
            .setClientKey("SB-Mid-client-Qe9BaZtS-PQZTOUm")
            .setContext(applicationContext)
            .setTransactionFinishedCallback(TransactionFinishedCallback {
                    result ->
//                if (result?.response != null) {
//                    when (result.status) {
//                        TransactionResult.STATUS_SUCCESS -> Toast.makeText(this, "Transaction Finished. ID: ${result.response.transactionId}", Toast.LENGTH_LONG).show()
//                        TransactionResult.STATUS_PENDING -> Toast.makeText(this, "Transaction Pending. ID: ${result.response.transactionId}", Toast.LENGTH_LONG).show()
//                        TransactionResult.STATUS_FAILED -> Toast.makeText(this, "Transaction Failed. ID: ${result.response.transactionId}. Message: ${result.response.statusMessage}", Toast.LENGTH_LONG).show()
//                    }
//                    result.response.validationMessages
//                } else if (result?.isTransactionCanceled == true) {
//                    Toast.makeText(this, "Transaction Canceled", Toast.LENGTH_LONG).show()
//                } else {
//                    if (result?.status.equals(TransactionResult.STATUS_INVALID, ignoreCase = true)) {
//                        Toast.makeText(this, "Transaction Invalid", Toast.LENGTH_LONG).show()
//                    } else {
//                        Toast.makeText(this, "Transaction Finished with failure.", Toast.LENGTH_LONG).show()
//                    }
//                }
            })
//            .setMerchantBaseUrl("https://eatrainapp.000webhostapp.com/charge/index.php/")
            .setMerchantBaseUrl("https://eatrainapp.000webhostapp.com/index.php/")
            .enableLog(true)
//            .setColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .setLanguage("id")
            .buildSDK()


        firebaseAuth = FirebaseAuth.getInstance()
        val userid = firebaseAuth.currentUser?.uid

        val ShowNamaPemesan = findViewById<TextView>(R.id.form_namapemesan)

        namaKereta = findViewById(R.id.txt_namaKereta)
        noGerbong = findViewById(R.id.txt_nomorGerbong)
        noKursi = findViewById(R.id.txt_nomorKursi)


        val bundle = intent.extras
        if (bundle != null) {
            binding.tvTotalForm.text = "Rp. " + bundle.getString("sumHarga").toString()
            totall = bundle.getString("sumHarga").toString()
        }

        val docRef =
            db.collection("users").document(userid!!).collection("Profil").document(userid!!)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    ShowNamaPemesan.text = "$name"
                    nama= "$name"
                    val nomor = document.getString("phone")
                    val emaill = document.getString("email")
                    phone= nomor.toString()
                    email= emaill.toString()
                }
            }
        binding.recyclerViewForm.apply {
            layoutManager = LinearLayoutManager(context)
        }

        fetchDataForm()

        binding.btnBayar.setOnClickListener {
            var itemDetails = ArrayList<com.midtrans.sdk.corekit.models.ItemDetails>()
            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
            val dataMidtrans = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart")
            dataMidtrans.get()
                .addOnSuccessListener { documents ->
                    for (document in documents){
                        val namaItemId = document.getString("key").toString()
                        val total = document.getString("price").toString().toDouble()
//                        val totalharga = document.getString("totalPrice").toString().toDouble()
                        val jumlah = document.get("quantity").toString().toInt()
                        val namaaa = document.getString("name").toString()


                        val detail = com.midtrans.sdk.corekit.models.ItemDetails("$namaItemId", total, jumlah, "$namaaa")

//                        val detail = com.midtrans.sdk.corekit.models.ItemDetails(namaItemId, harga, jumlah, nama)
                        itemDetails.add(detail)
//                        itemDetails.add(com.midtrans.sdk.corekit.models.ItemDetails("cobase", totalHarga, jumlah, "$namaaa"))
                    }
                }

//            val dataArrayList = arrayListOf<MidCart>()
//            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
//            val StorageForm = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart")
//            StorageForm.get()
//                .addOnSuccessListener { documents ->
//                    for (document in documents){
//                        val dataMid = document.toObject(MidCart::class.java)
//                        dataArrayList.add(dataMid)
//                    }
//                }
//                .addOnFailureListener {
//
//                }

//            val trans = totall.toDouble()

            val transactionRequest = TransactionRequest("Eatrain-App-" + System.currentTimeMillis().toString()+"", totall.toDouble())
//            val itemDetails = ArrayList<com.midtrans.sdk.corekit.models.ItemDetails>()
//            for (data in dataArrayList) {
//                val detail = com.midtrans.sdk.corekit.models.ItemDetails(data.docID, data.price!!.toDouble(), data.quantity, data.name  )
//                itemDetails.add(detail)
//            }

            uiKitDetails(transactionRequest, nama.trim(), phone, email)
            transactionRequest.itemDetails = itemDetails
//            MidtransSDK.getInstance().setTransactionRequest(transactionRequest("101",2000, 1, "John"))
            MidtransSDK.getInstance().startPaymentUiFlow(this)
            MidtransSDK.getInstance().transactionRequest = transactionRequest

            simpandata()
        }
    }


    fun uiKitDetails(transactionRequest: TransactionRequest, name:String, HP:String, email2:String){
        val customerDetails = CustomerDetails()
        customerDetails.customerIdentifier = name
        customerDetails.firstName = name
        customerDetails.phone = HP
        customerDetails.email = email2
        val shippingAddress = ShippingAddress ()
//        shippingAddress.phone = "$phone"
        customerDetails.shippingAddress = shippingAddress
        val billingAddress = BillingAddress ()
//        billingAddress.phone = "$phone"
        customerDetails.billingAddress = billingAddress

        transactionRequest.customerDetails = customerDetails
    }

    private fun fetchDataForm() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString().trim()
        val StorageForm = FirebaseFirestore.getInstance().collection("users").document(uid!!).collection("Cart")
        StorageForm.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val cartMenu = documents.toObjects(MenuCart::class.java)
                    binding.recyclerViewForm.adapter = this.let { FormAdapter (it, cartMenu) }
                }
            }
            .addOnFailureListener {

            }
    }

    private fun simpandata() {

        val cekGerbongRadioButtonId = rg_gerbong.checkedRadioButtonId
        val listGerbongKereta = findViewById<RadioButton>(cekGerbongRadioButtonId)
        hasilGerbongKereta = "${listGerbongKereta.text}"

        val gerbong = hasilGerbongKereta.trim()
        val inputKereta = namaKereta.text.toString().trim()
        val inputGerbong = noGerbong.text.toString().trim()
        val inputKursi = noKursi.text.toString().trim()
        if (inputKereta=="" || inputGerbong=="" || inputKursi == "" || gerbong == ""){
            Toast.makeText(this, "Mohon isi semua form yang tersedia terlebih dahulu", Toast.LENGTH_LONG).show()
        }
        else {
//            val gerbong = hasilGerbongKereta.trim()
            val inputKereta = namaKereta.text.toString().trim()
            val inputGerbong = noGerbong.text.toString().trim()
            val inputKursi = noKursi.text.toString().trim()

            val dbupdate = FirebaseFirestore.getInstance()
            val pesanan = hashMapOf<String, Any>(
                "namaUser" to nama,
                "namaKereta" to inputKereta,
                "Gerbong" to gerbong,
                "nomorGerbong" to inputGerbong,
                "nomorKursi" to inputKursi,
            )
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid
            dbupdate.collection("pesanan").document(nama).collection("identitas").document(nama)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        Toast.makeText(
                            this,
                            "Mohon selesaikan pesanan sebelumnya terlebih dahulu",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        dbupdate.collection("pesanan").document(nama).collection("identitas")
                            .document(nama).set(pesanan)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed!, gagal $uid", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                }

            val StorageForm = FirebaseFirestore.getInstance().collection("users").document(uid!!)
                .collection("Cart")
            val listPesanan = dbupdate.collection("pesanan").document(nama).collection("menu")

            StorageForm.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        listPesanan.document().set(document)
                        var total = binding.tvTotalForm.text.toString()
                        val totalHarga = hashMapOf(
                            "total" to "$total",
                        )
                        listPesanan.document("total").set(totalHarga)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error gaes", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onTransactionFinished(result: TransactionResult?) {
        if (result != null) {
            if (result.response != null) {
                when (result.status) {
                    TransactionResult.STATUS_SUCCESS -> Toast.makeText(this, "Transaction Finished. ID: " + result.response.transactionId, Toast.LENGTH_LONG).show()
                    TransactionResult.STATUS_PENDING -> Toast.makeText(this, "Transaction Pending. ID: " + result.response.transactionId, Toast.LENGTH_LONG).show()
                    TransactionResult.STATUS_FAILED -> Toast.makeText(this, "Transaction Failed. ID: " + result.response.transactionId.toString() + ". Message: " + result.response.statusMessage, Toast.LENGTH_LONG).show()
                }
//                result.response.validationMessages
            } else if (result.isTransactionCanceled) {
                Toast.makeText(this, "Transaction Canceled", Toast.LENGTH_LONG).show()
            } else {
                if (result.status.equals(TransactionResult.STATUS_INVALID, true)) {
                    Toast.makeText(this, "Transaction Invalid", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Transaction Finished with failure.", Toast.LENGTH_LONG).show()
                }
            }
        }
        TODO("Not yet implemented")
    }

}