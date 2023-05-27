package com.compare.compare_user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.compare.compare_user.databinding.ActivityLogindanregisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_logindanregister.*

class logindanregister : AppCompatActivity() {

    lateinit var binding :ActivityLogindanregisterBinding
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLogindanregisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.GantiPassword.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        binding.tvToRegister.setOnClickListener {
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener{
            val email = binding.edtEmailLogin.text.toString()
            val password = binding.edtPasswordLogin.text.toString()

            //Validasi Email
            if (email.isEmpty()) {
                binding.edtEmailLogin.error = "Email Harus Di isi"
                binding.edtEmailLogin.requestFocus()
                return@setOnClickListener
            }
            //Validasi Email Tidak Sesuai
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtEmailLogin.error = "Email Tidak Valid"
                binding.edtEmailLogin.requestFocus()
                return@setOnClickListener
            }
            //Validasi password
            if (password.isEmpty()) {
                binding.edtPasswordLogin.error = "Password Harus Diisi"
                binding.edtPasswordLogin.requestFocus()
                return@setOnClickListener
            }

            LoginFirebase(email,password)
        }
    }
    private fun LoginFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val db = FirebaseFirestore.getInstance()
                    val cekDoc = db.collection("users").document(uid!!).collection("Profil").document(uid)
                    cekDoc.get().addOnSuccessListener {
                        if (it.getString("email") == auth.currentUser?.email){
                            val nama = it.getString("name")
                            Toast.makeText(this, "Selamat datang, $nama", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java).also {
                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                        }else
                        {
                            auth.signOut()
                            Toast.makeText(this, "SILAHKAN LOGIN ULANG, DATA TIDAK DITEMUKAN", Toast.LENGTH_SHORT).show()
                        }
                    }
                        .addOnFailureListener(){
                            Toast.makeText(this, "GAGAL MEMBACA DATA. SILAHKAN LOGIN ULANG", Toast.LENGTH_SHORT).show()

                        }
                } else {
                    Toast.makeText(this, "Email or Password incorrect, please try again!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
            val intent = Intent (this, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}