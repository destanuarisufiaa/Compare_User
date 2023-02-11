package com.compare.compare_user

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.compare.compare_user.databinding.ActivityUpdateProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_update_profile.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class update_profile : AppCompatActivity() {

    private lateinit var updateNama : EditText
    private lateinit var updateEmail : EditText
    private lateinit var updateGender : EditText
    private lateinit var updateNomor : EditText
    private lateinit var updateFoto : ImageView
    private lateinit var buttonUpdate : Button
    private lateinit var binding: ActivityUpdateProfileBinding
    lateinit var auth : FirebaseAuth
    var imageURL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //cek permission upload gambar
        updatefoto.isEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )!= PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }else{
            updatefoto.isEnabled = true
        }

        updatefoto.setOnClickListener {
            selectImage()
        }

        //inisialisasi layout
        updateNama = findViewById(R.id.update_nama)
        updateEmail = findViewById(R.id.update_email)
        updateGender = findViewById(R.id.update_gender)
        updateNomor = findViewById(R.id.update_nomor)
        updateFoto= findViewById(R.id.updatefoto)
        buttonUpdate = findViewById(R.id.btn_update)

        //mengambil deskripsi menu yang akan diedit dari detail
        val bundle = intent.extras
        if (bundle != null) {
            val namaUpdate = bundle.getString("nama")?.trim()
            val emailUpdate = bundle.getString("email")?.trim()
            val genderUpdate = bundle.getString("gender")?.trim()
            val phoneUpdate = bundle.getString("phone")?.trim()

            updateNama.setText("$namaUpdate")
            updateEmail.setText("$emailUpdate")
            updateGender.setText("$genderUpdate")
            updateNomor.setText("$phoneUpdate")
            imageURL = bundle.getString("foto")!!
            if (bundle.getString("foto") != ""){
                Glide.with(this).load(bundle?.getString("foto")).into(updateFoto)
            }

        }

        buttonUpdate.setOnClickListener {
            updateData()
        }
    }

    private fun selectImage() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name))
        builder.setIcon(R.mipmap.ic_launcher)
        builder.setItems(items) { dialog: DialogInterface, item: Int ->
            if (items[item] == "Take Photo") {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 10)
            } else if (items[item] == "Choose from Library") {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 20)
            } else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {
            val path : Uri? = data.data
            val thread = Thread {
                try {
                    val inputStream = contentResolver.openInputStream(path!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    updatefoto.post { updatefoto.setImageBitmap(bitmap) }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }


        if (requestCode == 10 && resultCode == RESULT_OK) {
            val extras = data!!.extras
            val thread = Thread {
                val bitmap = extras!!["data"] as Bitmap?
                updatefoto.post { updatefoto.setImageBitmap(bitmap) }
            }
            thread.start()
        }
    }

    private fun updateData() {
        val edNama = updateNama.text.toString().trim()
        val edEmail = updateEmail.text.toString().trim()
        val edPhone = updateNomor.text.toString().trim()
        val edGender = updateGender.text.toString().trim()

        updatefoto.isDrawingCacheEnabled = true
        updatefoto.buildDrawingCache()
        val bitmap = (updatefoto.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        //UPLOAD
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

//        val currentDate : String = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
        val storage = FirebaseStorage.getInstance()
        val reference = storage.getReference("images_user").child("IMG"+ Date().time +".jpeg")
        var uploadTask = reference.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            if(taskSnapshot.metadata !=null){
                if(taskSnapshot.metadata!!.reference !=null){
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnCompleteListener {
//
//                        FirebaseStorage.getInstance().getReferenceFromUrl(imageURL).downloadUrl.addOnSuccessListener {
//                            FirebaseStorage.getInstance().getReferenceFromUrl(imageURL).delete()
//                        }
//                            .addOnFailureListener{
//                                Toast.makeText(this, "File Not Exist, adding . . .", Toast.LENGTH_SHORT).show()
//                            }
                        var editfoto = it.getResult().toString()
                        val dbupdate = FirebaseFirestore.getInstance()
                        val bahanProfile = hashMapOf<String, Any>(
                            "email" to edEmail,
                            "name" to edNama,
                            "phone" to edPhone,
                            "gender" to edGender,
//                            "foto" to editfoto,
                        )
                        val uid = auth.currentUser?.uid
                        dbupdate.collection("users").document(uid!!).update(bahanProfile)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { exception ->
                                dialog.dismiss()
                                Toast.makeText(this, "Failed!, gagal $uid", Toast.LENGTH_SHORT).show()
                            }
                    }
                }else{
                    dialog.dismiss()
                    Toast.makeText(this, "Failed 1!", Toast.LENGTH_SHORT).show()
                }
            }else{
                dialog.dismiss()
                Toast.makeText(this, "Failed 2!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}