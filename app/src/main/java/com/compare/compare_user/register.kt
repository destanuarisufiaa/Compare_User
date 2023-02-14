package com.compare.compare_user

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ImageView
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
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_update_profile.*
import java.io.ByteArrayOutputStream
import java.util.*

class register : AppCompatActivity() {

    lateinit var binding :ActivityRegisterBinding
    lateinit var auth : FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gender = findViewById<TextView>(R.id.txt_gender_register)
        val gender1 = findViewById<RadioGroup>(R.id.rg_gender1_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.tvToLogin.setOnClickListener {
            val intent = Intent(this, logindanregister::class.java)
            startActivity(intent)
        }
        binding.btnRegister.setOnClickListener {

            val bitmap = (fotobawaan.getDrawable() as BitmapDrawable).getBitmap()
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            val email = binding.edtEmailRegister.text.toString()
            val password = binding.edtPasswordRegister.text.toString()
            val nama = binding.edtNamaRegister.text.toString()
            val phone = binding.edtNomorhpRegister.text.toString()

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

            val storage = FirebaseStorage.getInstance()
            val reference = storage.getReference("images_user").child("IMG"+ Date().time +".jpeg")
            var uploadTask = reference.putBytes(data)
            uploadTask.addOnFailureListener {
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener { taskSnapshot ->
                if(taskSnapshot.metadata !=null){
                    if(taskSnapshot.metadata!!.reference !=null){
                        taskSnapshot.metadata!!.reference!!.downloadUrl.addOnCompleteListener {
                            var foto = it.getResult().toString()
                            RegisterFirebase(email,password, nama, phone, hasilGender, foto)
                        }
                    }else{
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }

    private fun RegisterFirebase(email: String, password: String, nama: String, phone: String, hasilGender: String, foto : String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    Toast.makeText(this, "Register Berhasil", Toast.LENGTH_SHORT).show()
                    val user = hashMapOf<String, Any>(
                        "email" to email,
                        "name" to nama,
                        "phone" to phone,
                        "gender" to hasilGender,
                        "foto" to foto,
                    )
                    val uid = auth.currentUser?.uid
                    firestore.collection("users").document(uid!!).collection("Profil").document(uid!!)
                        .set(user)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error adding document $exception")
                        }

                    auth.signOut()
                    val intent = Intent (this, logindanregister::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,"${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}