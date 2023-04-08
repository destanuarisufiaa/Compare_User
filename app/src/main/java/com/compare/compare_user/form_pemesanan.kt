package com.compare.compare_user

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.midtrans.sdk.corekit.models.snap.TransactionResult.STATUS_SUCCESS
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import kotlinx.android.synthetic.main.activity_form_pemesanan.*
class form_pemesanan : AppCompatActivity(), TransactionFinishedCallback {

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
            .setTransactionFinishedCallback(this)
            .setMerchantBaseUrl("https://eatrainapp.000webhostapp.com/index.php/")
            .enableLog(true)
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
                        itemDetails.add(detail)
                    }
                }

            val transactionRequest = TransactionRequest("Eatrain-App-" + System.currentTimeMillis().toString()+"", totall.toDouble())

            uiKitDetails(transactionRequest, nama.trim(), phone, email)
            transactionRequest.itemDetails = itemDetails
//            MidtransSDK.getInstance().setTransactionRequest(transactionRequest("101",2000, 1, "John"))
            MidtransSDK.getInstance().startPaymentUiFlow(this)
            MidtransSDK.getInstance().transactionRequest = transactionRequest
        }
    }

    fun uiKitDetails(transactionRequest: TransactionRequest, name:String, HP:String, email2:String){
        val customerDetails = CustomerDetails()
        customerDetails.customerIdentifier = name
        customerDetails.firstName = name
        customerDetails.phone = HP
        customerDetails.email = email2
        val shippingAddress = ShippingAddress ()
        customerDetails.shippingAddress = shippingAddress
        val billingAddress = BillingAddress ()
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

    private fun simpandata(orderID:String) {

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
            val dbupdate = FirebaseFirestore.getInstance()
            val pesanan = hashMapOf<String, Any>(
                "namaUser" to nama,
                "namaKereta" to inputKereta,
                "Gerbong" to gerbong,
                "nomorGerbong" to inputGerbong,
                "nomorKursi" to inputKursi,
                "status" to "On Proccess"
            )
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid
            dbupdate.collection("pesanan").document(orderID)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        Toast.makeText(
                            this,
                            "Mohon selesaikan pesanan sebelumnya terlebih dahulu",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
//                        dbupdate.collection("pesanan").document("identitas").collection(orderID)
                        dbupdate.collection("pesanan").document(orderID)
                            .set(pesanan)
                            .addOnSuccessListener { documentReference ->
                                //pindah ke riwayat
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                intent.putExtra("direct", "true")
                                startActivity(intent)
//                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed!, $exception", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                }

            //menyalin cart ke pesanan
            val StorageForm = FirebaseFirestore.getInstance().collection("users").document(uid!!)
                .collection("Cart")
            val listPesanan = dbupdate.collection("pesanan").document(orderID).collection("menu")

            StorageForm.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        listPesanan.document().set(data)
                        var total = binding.tvTotalForm.text.toString()
                        val totalHarga = hashMapOf(
                            "total" to "$total",
                        )
                        listPesanan.document("total").set(totalHarga)
                        //menghapus cart
                        StorageForm.document(document.id).delete()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error gaes", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onTransactionFinished(result: TransactionResult?) {
        if(result?.response?.transactionStatus == STATUS_SUCCESS){
            //memindahkan pesanan ke riwayat dan menghapus cart
            val orderID = result.response?.transactionId
            simpandata(orderID.toString())

//            val intent = Intent(applicationContext, MainActivity::class.java)
//            startActivity(intent)
            Toast.makeText(this, "Pesanan Telah Dibuat", Toast.LENGTH_LONG).show()
        }

        if(result?.response?.transactionStatus == TransactionResult.STATUS_PENDING){
            val intent = Intent(applicationContext, Cart::class.java)
            startActivity(intent)
            Toast.makeText(this, "Transaksi Dibatalkan", Toast.LENGTH_LONG).show()
        }
    }

}