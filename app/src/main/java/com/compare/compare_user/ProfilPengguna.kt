package com.compare.compare_user

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.compare.compare_user.databinding.FragmentHomeBinding
import com.compare.compare_user.databinding.FragmentProfilPenggunaBinding
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profil_pengguna.*
import java.io.ByteArrayOutputStream

class ProfilPengguna : Fragment() {
    lateinit var imageURL : String
    lateinit var binding : FragmentProfilPenggunaBinding
    private val db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfilPenggunaBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        firebaseAuth = FirebaseAuth.getInstance()
        val userid = firebaseAuth.currentUser?.uid

        val ShowName = view.findViewById<TextView>(R.id.txt_nama)
        val ShowPhone = view.findViewById<TextView>(R.id.txt_nomor)
        val ShowGender = view.findViewById<TextView>(R.id.txt_gender)
        val ShowEmail = view.findViewById<TextView>(R.id.txt_email)
        val statuss = view.findViewById<TextView>(R.id.statuss)
        val foto = view.findViewById<ImageView>(R.id.uploadimage_user)


        val docRef = db.collection("users").document(userid!!).collection("Profil").document(userid!!)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    val phone = document.getString("phone")
                    val gender = document.getString("gender")
                    val email = document.getString("email")
                    imageURL = document.getString("foto").toString().trim()

                    ShowName.text = "$name"
                    ShowPhone.text = "$phone"
                    ShowGender.text = "$gender"
                    ShowEmail.text = "$email"
                    Glide.with(this)
                            .load(imageURL)
                            .into(foto)

                    statuss.text = "Succes dapat data user $userid"
                }
            }
        btn_logout.setOnClickListener {
            showLogoutConfirmationDialog()

        }

        updateprofile.setOnClickListener {
            requireActivity().run {
                val intent = Intent (this, update_profile::class.java)
                    .putExtra("nama",ShowName.text.toString().trim())
                    .putExtra("phone", ShowPhone.text.toString().trim())
                    .putExtra("gender", ShowGender.text.toString().trim())
                    .putExtra("email", ShowEmail.text.toString().trim())
                    .putExtra("foto", imageURL)
                startActivity(intent)
                finish()
            }
        }

        binding.btnChangePass.setOnClickListener {
            changePass()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Konfirmasi Keluar")
        alertDialogBuilder.setMessage("Apakah Anda yakin ingin log out?")

        val positiveText = "Logout"
        val neutralText = "Keluar Aplikasi"
        val negativeText = "Batal"

        // Membuat SpannableString untuk menerapkan warna pada teks pilihan
        val spannablePositive = SpannableString(positiveText)
        spannablePositive.setSpan(
            ForegroundColorSpan(Color.RED),
            0,
            positiveText.length,
            0
        )

        val spannableNeutral = SpannableString(neutralText)
        spannableNeutral.setSpan(
            ForegroundColorSpan(Color.parseColor("#FF6200EE")),
            0,
            neutralText.length,
            0
        )

        val spannableNegative = SpannableString(negativeText)
        spannableNegative.setSpan(
            ForegroundColorSpan(Color.parseColor("#04cc18")),
            0,
            negativeText.length,
            0
        )

        alertDialogBuilder.setPositiveButton(spannablePositive) { dialog: DialogInterface, _: Int ->
            // Tindakan yang akan dilakukan jika tombol "Ya" ditekan
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, logindanregister::class.java)
            startActivity(intent)
        }

        alertDialogBuilder.setNeutralButton(spannableNeutral) { dialog: DialogInterface, _: Int ->
            finishAffinity(requireActivity())
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton(spannableNegative) { dialog: DialogInterface, _: Int ->
            // Tindakan yang akan dilakukan jika tombol "Batal" ditekan
            dialog.dismiss()
        }

        // Membuat bentuk dengan radius 20dp dan background berwarna putih
        val radius = resources.getDimensionPixelSize(R.dimen.dialog_corner_radius).toFloat()
        val outerRadii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        val shapeDrawable = ShapeDrawable(RoundRectShape(outerRadii, null, null))
        shapeDrawable.paint.color = Color.WHITE
        shapeDrawable.paint.style = Paint.Style.FILL

        // Membuat drawable dengan stroke berwarna hijau
        val strokeWidth = 10f
        val strokeColor = Color.parseColor("#FF3700B3")
        val strokeDrawable = ShapeDrawable(RoundRectShape(outerRadii, null, null))
        strokeDrawable.paint.color = Color.TRANSPARENT
        strokeDrawable.paint.style = Paint.Style.STROKE
        strokeDrawable.paint.strokeWidth = strokeWidth
        strokeDrawable.paint.color = strokeColor

        // Menggabungkan background dan stroke menjadi satu drawable
        val layers: Array<Drawable> = arrayOf(shapeDrawable, strokeDrawable)
        val layerDrawable = LayerDrawable(layers)

        // Mengatur background drawable pada dialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(layerDrawable)
        alertDialog.show()
    }

    private fun changePass() {
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser

        binding.cvCurrentPass.visibility = View.VISIBLE

        binding.btnCancel.setOnClickListener {
            binding.cvCurrentPass.visibility = View.GONE
        }

        binding.btnConfirm.setOnClickListener btnConfirm@{
            val pass = binding.edtCurrentPassword.text.toString()
            if (pass.isEmpty()){
                binding.edtCurrentPassword.error = "Password Tidak Boleh Kosong"
                binding.edtCurrentPassword.requestFocus()
                return@btnConfirm
            }
            user.let {
                val userCredential = EmailAuthProvider.getCredential(it?.email!!,pass)
                it.reauthenticate(userCredential).addOnCompleteListener {  task ->
                    when {
                        task.isSuccessful -> {
                            binding.cvCurrentPass.visibility = View.GONE
                            binding.cvUpdatePass.visibility = View.VISIBLE
                        }
                        task.exception is FirebaseAuthInvalidCredentialsException -> {
                            binding.edtCurrentPassword.error = "Password Salah"
                            binding.edtCurrentPassword.requestFocus()
                        }
                        else -> {
                            Toast.makeText(activity, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            binding.btnNewCancel.setOnClickListener {
                binding.cvCurrentPass.visibility = View.GONE
                binding.cvUpdatePass.visibility = View.GONE
            }
            binding.btnNewChange.setOnClickListener newChangePassword@{
                val newPass = binding.edtNewPass.text.toString()
                val passConfirm = binding.edtConfirmPass.text.toString()

                if (newPass.isEmpty()){
                    binding.edtCurrentPassword.error = "Password Tidak Boleh Kosong"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }
                if (passConfirm.isEmpty()){
                    binding.edtCurrentPassword.error = "Ulangi Password Baru"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }
                if (newPass.length < 6) {
                    binding.edtCurrentPassword.error = "Password harus lebih dari 6 karakter"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }
                if (passConfirm.length < 6) {
                    binding.edtCurrentPassword.error = "Password Tidak Sama"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }
                if (newPass != passConfirm) {
                    binding.edtCurrentPassword.error = "Password Tidak Sama"
                    binding.edtCurrentPassword.requestFocus()
                    return@newChangePassword
                }
                user?.let {
                    user.updatePassword(newPass).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(activity, "Password Berhasil di Update", Toast.LENGTH_SHORT).show()
                            successLogout()
                        }else {
                            Toast.makeText(activity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }

    private fun successLogout() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val intent = Intent(context, logindanregister::class.java)
        startActivity(intent)
        activity?.finish()

        Toast.makeText(activity, "Silahkan Login Kembali", Toast.LENGTH_SHORT).show()
    }

}