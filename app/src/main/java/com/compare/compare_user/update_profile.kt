package com.compare.compare_user

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.compare.compare_user.databinding.ActivityUpdateProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_update_profile.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class update_profile : AppCompatActivity() {

    private lateinit var updateNama : EditText
    private lateinit var updateEmail : EditText
    private lateinit var updateNomor : EditText
    private lateinit var updateFoto : ImageView
    private lateinit var gender1 : RadioGroup
    private lateinit var buttonUpdate : Button
    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var hasilGender: String
    private lateinit var currentPhotoPath : String
    var imageURL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        backk.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("direct", "back")
            startActivity(intent)
        }

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
        gender1 = findViewById(R.id.rg_gender_update)
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
            updateNomor.setText("$phoneUpdate")
            if (genderUpdate.equals("Pria")){
                gender1.check(R.id.male)
            }else if (genderUpdate.equals("Wanita")){
                gender1.check(R.id.female)
            }

            imageURL = bundle.getString("foto")!!
            if (imageURL != ""){
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
                // Ambil gambar menggunakan kamera
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    // Pastikan ada aplikasi kamera yang dapat menangani intent ini
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        // Buat file gambar sementara untuk menyimpan hasil kamera
                        val photoFile: File? = try {
                            createImageFile()
                        } catch (ex: IOException) {
                            // Error saat membuat file
                            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT)
                                .show()
                            null
                        }
                        // Jika file berhasil dibuat, lanjutkan mengambil gambar dari kamera
                        photoFile?.also {
                            val photoURI: Uri = FileProvider.getUriForFile(
                                this,
                                "com.compare.compare_user.fileprovider",
                                it
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            startActivityForResult(takePictureIntent, 10)
                        }
                    }
                }
            }
            else if (items[item] == "Choose from Library") {
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
            //crop
            path?.let { startCrop(it) }
        }

        //kamera
        if (requestCode == 10 && resultCode == RESULT_OK) {
            val imageUri = Uri.fromFile(File(currentPhotoPath))
            val path = Uri.fromFile(File(cacheDir, "IMG_" + System.currentTimeMillis()))

            //crop
            val options = UCrop.Options()
            options.setCompressionQuality(80)
            options.setToolbarTitle(getString(R.string.app_name))
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.purple_700))
            options.setToolbarColor(ContextCompat.getColor(this, R.color.purple_700))
            options.setToolbarWidgetColor(Color.WHITE)
            options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.purple_700))
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
            UCrop.of(imageUri!!, path)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(720,720)
                .withOptions(options)
                .start(this)

        }


        //menangkap hasil cropping dan update imageview
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            try {
                val inputStream = contentResolver.openInputStream(resultUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                updatefoto.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, cropError?.message, Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Membuat nama file
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Simpan path file di variabel global
            currentPhotoPath = absolutePath
        }
    }

    private fun startCrop(it: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "IMG_" + System.currentTimeMillis()))
        val options = UCrop.Options()
        options.setCompressionQuality(80)
        options.setToolbarTitle(getString(R.string.app_name))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.purple_700))
        options.setToolbarColor(ContextCompat.getColor(this, R.color.purple_700))
        options.setToolbarWidgetColor(Color.WHITE)
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.purple_700))
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        UCrop.of(it!!, destinationUri)
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .start(this)

    }

    private fun updateData() {
        val cekGenderRadioButtonId = gender1.checkedRadioButtonId
        val listGender = findViewById<RadioButton>(cekGenderRadioButtonId)
        hasilGender = "${listGender.text}"

        val edNama = updateNama.text.toString().trim()
        val edEmail = updateEmail.text.toString().trim()
        val edPhone = updateNomor.text.toString().trim()
        val edGender = hasilGender.trim()

        updatefoto.isDrawingCacheEnabled = true
        updatefoto.buildDrawingCache()
//        val bitmap = (updatefoto.drawable as BitmapDrawable).bitmap
        val bitmap = Bitmap.createBitmap(updateFoto.drawingCache)
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
                        var editfoto = it.getResult().toString()

                        FirebaseStorage.getInstance().getReferenceFromUrl(imageURL).downloadUrl
                            .addOnSuccessListener {
                            FirebaseStorage.getInstance().getReferenceFromUrl(imageURL).delete()
                        }
                            .addOnFailureListener{
                                Toast.makeText(this, "File Not Exist, adding . . .", Toast.LENGTH_SHORT).show()
                            }

                        val dbupdate = FirebaseFirestore.getInstance()
                        val bahanProfile = hashMapOf<String, Any>(
                            "email" to edEmail,
                            "name" to edNama,
                            "phone" to edPhone,
                            "gender" to edGender,
                            "foto" to editfoto,
                        )
                        val auth = FirebaseAuth.getInstance()
                        val uid = auth.currentUser?.uid
                        dbupdate.collection("users").document(uid!!).collection("Profil").document(uid!!).update(bahanProfile)
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