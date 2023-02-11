package com.compare.compare_user

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.compare.compare_user.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class register : AppCompatActivity() {

    lateinit var binding :ActivityRegisterBinding
    lateinit var auth : FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        fun getURLForResource(resourceId: Int): Uri {
            //use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
            return Uri.parse("android.resource://" + android.R::class.java.getPackage().name + "/" + resourceId)
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gender = findViewById<TextView>(R.id.txt_gender_register)
        val gender1 = findViewById<RadioGroup>(R.id.rg_gender1_register)
        val imageURL = getURLForResource(R.drawable.update_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.tvToLogin.setOnClickListener {
            val intent = Intent(this, logindanregister::class.java)
            startActivity(intent)
        }
        binding.btnRegister.setOnClickListener {

            val email = binding.edtEmailRegister.text.toString()
            val password = binding.edtPasswordRegister.text.toString()
            val nama = binding.edtNamaRegister.text.toString()
            val phone = binding.edtNomorhpRegister.text.toString()
//            val foto = imageURL.toString().trim()
            val foto = ""

            val cekGenderRadioButtonId = gender1.checkedRadioButtonId
            val listGender = findViewById<RadioButton>(cekGenderRadioButtonId)

            val hasilGender = "${listGender.text}"
            gender.text = hasilGender


            //Validasi Email
            if (email.isEmpty()) {
                binding.edtEmailRegister.error = "Email Harus Di isi"
                binding.edtEmailRegister.requestFocus()
                return@setOnClickListener
            }
            //Validasi Email Tidak Sesuai
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtEmailRegister.error = "Email Tidak Valid"
                binding.edtEmailRegister.requestFocus()
                return@setOnClickListener
            }
            //Validasi password
            if (password.isEmpty()) {
                binding.edtPasswordRegister.error = "Password Harus Diisi"
                binding.edtPasswordRegister.requestFocus()
                return@setOnClickListener
            }

            //Validasi panjang password
            if (password.length < 6) {
                binding.edtPasswordRegister.error = "Password Minimal 6 Karakter"
                binding.edtPasswordRegister.requestFocus()
                return@setOnClickListener
            }

            RegisterFirebase(email,password, nama, phone, hasilGender, foto)
        }
    }

    private fun RegisterFirebase(email: String, password: String, nama: String, phone: String, hasilGender: String, imageURL : String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    Toast.makeText(this, "Register Berhasil", Toast.LENGTH_SHORT).show()
                    val user = hashMapOf<String, Any>(
                        "email" to email,
                        "name" to nama,
                        "phone" to phone,
                        "gender" to hasilGender,
                        "foto" to imageURL,
                    )
                    val uid = auth.currentUser?.uid
                    firestore.collection("users").document(uid!!)
                        .set(user)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error adding document $exception")
                        }

                    val intent = Intent (this, logindanregister::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,"${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}