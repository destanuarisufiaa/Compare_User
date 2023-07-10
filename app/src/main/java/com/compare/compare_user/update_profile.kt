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

        //jika klik button back pada update profile maka akan berpindah pada halaman profile
        backk.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("direct", "back")
            startActivity(intent)
        }

        //cek permission upload gambar
        //mengaktifkan tombol id UploadImage (ImageView) untuk dapat di klik
        updatefoto.isEnabled = true

        //melakukan pemeriksaan izin untuk kamera apakah telah diaktifkan (diberikan)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
                //jika izin kamera belum diberikan
            )!= PackageManager.PERMISSION_GRANTED
        ){
            //maka meminta izin kamera dengan menggunakan "requestPermissions"
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }else{
            //jika izin telah diberikan, maka tidak perlu meminta izin.  dan tombol updatefoto dapat di klik untuk upload gambar
            updatefoto.isEnabled = true
        }

        //jika id updatefoto di klik, menjalankan fungsi selectImage
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

            //memasukkan update profile ke dalam edittext
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

        //jika id buttonUpdate di klik, menjalankan fungsi updateData
        buttonUpdate.setOnClickListener {
            updateData()
        }
    }

    private fun selectImage() {
        //membuat array "items" dengan 3 pilihan
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        //membuat variabel "builder"
        val builder = android.app.AlertDialog.Builder(this)
        //dengan judul "EaTrain"
        builder.setTitle(getString(R.string.app_name))
        //dan ikon aplikasi
        builder.setIcon(R.mipmap.ic_launcher)
        builder.setItems(items) { dialog: DialogInterface, item: Int ->
            if (items[item] == "Take Photo") {
                // Ambil gambar menggunakan kamera
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    // Pastikan ada aplikasi kamera yang dapat menangani intent ini
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        // Buat file gambar sementara untuk menyimpan hasil kamera
                        val photoFile: File? = try {
                            //membuat file gambar dengan fungsi createImageFile untuk penyimpanan gambar yang di capture
                            createImageFile()
                        } catch (ex: IOException) {
                            // Error saat membuat file
                            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT)
                                .show()
                            null
                        }
                        // Jika file berhasil dibuat, lanjutkan mengambil gambar dari kamera
                        photoFile?.also {
                            //memperoleh uri file yang akan digunakan untuk menyimpan hasil foto
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
            //mengambil gambar dari galeri
            else if (items[item] == "Choose from Library") {
                //membuat variabel intent untuk memilih gambar dari galeri
                val intent = Intent(Intent.ACTION_PICK)
                //mengatur tipe intent, untuk membatasi bahwa yang dipilih hanya pada tipe gambar
                intent.type = "image/*"
                //memulai aktivitas selectImage dengan intent dan kode permintaan 20
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 20)
                //jika opsi yang dipilih cancel
            } else if (items[item] == "Cancel") {
                //dialog akan ditutup
                dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //galeri
        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {
            val path : Uri? = data.data
            //crop
            path?.let { startCrop(it) }
        }

        //kamera
        if (requestCode == 10 && resultCode == RESULT_OK) {
            val imageUri = Uri.fromFile(File(currentPhotoPath))
            //Membuat variabe tujuan untuk menyimpan gambar hasil cropping
            val path = Uri.fromFile(File(cacheDir, "IMG_" + System.currentTimeMillis()))

            //crop
            // Mengatur opsi-opsi untuk fitur cropping
            val options = UCrop.Options()
            options.setCompressionQuality(80)
            options.setToolbarTitle(getString(R.string.app_name))
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.purple_700))
            options.setToolbarColor(ContextCompat.getColor(this, R.color.purple_700))
            options.setToolbarWidgetColor(Color.WHITE)
            options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.purple_700))
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
            //memulai aktivitas crop
            UCrop.of(imageUri!!, path)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(720,720)
                .withOptions(options)
                .start(this)

        }


        //menangkap hasil cropping dan update imageview
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            //mendapatkan URI hasil cropping menggunakan “UCrop.getOutput(data!!)”
            val resultUri = UCrop.getOutput(data!!)
            try {
                //jika berhasil, membuka input stream dari URI hasil cropping dan mengonversinya menjadi objek bitmap
                val inputStream = contentResolver.openInputStream(resultUri!!)
                // Bitmap diatur sebagai gambar di ImageView
                val bitmap = BitmapFactory.decodeStream(inputStream)
                updatefoto.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            //jika proses crop error mendapatkan pesan eror melalui toast
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
            // Simpan path file di variabel global (currentPhotoPath)
            currentPhotoPath = absolutePath
        }
    }

    private fun startCrop(it: Uri) {
        //Membuat URI tujuan untuk menyimpan gambar hasil cropping
        val destinationUri = Uri.fromFile(File(cacheDir, "IMG_" + System.currentTimeMillis()))
        // Mengatur opsi-opsi untuk fitur cropping
        val options = UCrop.Options()
        options.setCompressionQuality(80)
        options.setToolbarTitle(getString(R.string.app_name))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.purple_700))
        options.setToolbarColor(ContextCompat.getColor(this, R.color.purple_700))
        options.setToolbarWidgetColor(Color.WHITE)
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.purple_700))
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        //memulai aktivitas crop
        UCrop.of(it!!, destinationUri)
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .start(this)

    }

    //fungsi updateData
    private fun updateData() {
        //inisialisasi variabal radio grup gerbong yang dipilih
        val cekGenderRadioButtonId = gender1.checkedRadioButtonId
        //mengambil id dari radio button yang dipilih
        val listGender = findViewById<RadioButton>(cekGenderRadioButtonId)
        //mengambil teks dari radioButton lalu memasukkannya pada variabel hasilGender
        hasilGender = "${listGender.text}"

        //mengambil nilai inputan edit text pada masing-masing variabel
        //trim untuk menghapus spasi di awal dan akhir kata, guna menghindari eror
        val edNama = updateNama.text.toString().trim()
        val edEmail = updateEmail.text.toString().trim()
        val edPhone = updateNomor.text.toString().trim()
        val edGender = hasilGender.trim()

        updatefoto.isDrawingCacheEnabled = true
        updatefoto.buildDrawingCache()
//        val bitmap = (updatefoto.drawable as BitmapDrawable).bitmap
        ////Mengambil gambar dari ImageView updateFoto dan dikonversi menjadi objek Bitmap
        val bitmap = Bitmap.createBitmap(updateFoto.drawingCache)
        //Membuat objek untuk menampung data gambar yang diupload
        val baos = ByteArrayOutputStream()
        //Mengompresi gambar menjadi format JPEG dengan kualitas 100 dan menyimpannya dalam objek ByteArrayOutputStream.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        //Mengambil data gambar yang sudah dikompresi dari objek ByteArrayOutputStream dan mengonversinya menjadi array byte.
        //arraybyte = digunakan untuk menyimpan, mentransfer, atau memproses data gambar lebih lanjut.
        val data = baos.toByteArray()

        //UPLOAD
        //membuat progress dialoag (ikon loading)
        val builder = AlertDialog.Builder(this)
        //Mengatur dialog agar tidak dapat dibatalkan dengan menekan tombol back.
        builder.setCancelable(false)
        //Mengatur tampilan layout progres sebagai tampilan dialog.
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        //menampilkan dialog
        dialog.show()

//        val currentDate : String = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
        //mendapatkan instance FirebaseStorage
        val storage = FirebaseStorage.getInstance()
        //inisialisasi untuk menyimpan gambar ke folder "images_user" dengan nama file yg telah ditentukan
        val reference = storage.getReference("images_user").child("IMG"+ Date().time +".jpeg")
        //mengunggah gambar ke firebaseStorage
        var uploadTask = reference.putBytes(data)
        //jika gagal saat mengunggah gambar ke firebaseStorage
        uploadTask.addOnFailureListener {
            //menampilkan toast failed
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
        //jika sukses
        }.addOnSuccessListener { taskSnapshot ->
            //Mengecek apakah metadata dari taskSnapshot tidak null.
            if(taskSnapshot.metadata !=null){
                //jika referensi metadata dari taskSnapshot tidak null
                if(taskSnapshot.metadata!!.reference !=null){
                    //mengambil URL unduhan file yang diunggah ke Firebase Storage.
                    //jika telah complete
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnCompleteListener {
                        //Mengambil URL unduhan file yang diunggah ke Firebase Storage dan menyimpannya dalam variabel editfoto sebagai string
                        var editfoto = it.getResult().toString()

                        //Mengambil URL unduhan file gambar sebelumnya dari Firebase Storage dengan menggunakan imageURL
                        FirebaseStorage.getInstance().getReferenceFromUrl(imageURL).downloadUrl
                            //jika sukses
                            .addOnSuccessListener {
                            //Menghapus file gambar sebelumnya dari Firebase Storage dengan menggunakan imageURL.
                            FirebaseStorage.getInstance().getReferenceFromUrl(imageURL).delete()
                        }
                            .addOnFailureListener{
                                Toast.makeText(this, "File Not Exist, adding . . .", Toast.LENGTH_SHORT).show()
                            }

                        //Mendapatkan instance Firestore untuk melakukan update an data.
                        val dbupdate = FirebaseFirestore.getInstance()
                        //membuat objek hashmap yang berisi data profil yang akan diperbaharui
                        val bahanProfile = hashMapOf<String, Any>(
                            "email" to edEmail,
                            "name" to edNama,
                            "phone" to edPhone,
                            "gender" to edGender,
                            "foto" to editfoto,
                        )
                        val auth = FirebaseAuth.getInstance()
                        //Mendapatkan instance FirebaseAuth untuk mendapatkan UID (User ID) pengguna saat ini.
                        val uid = auth.currentUser?.uid
                        //Memperbarui data profil pengguna pada Firestore dengan menggunakan bahanProfile yang telah dibuat pada objek hashmap sebelumnya.
                        dbupdate.collection("users").document(uid!!).collection("Profil").document(uid!!).update(bahanProfile)
                            //jika sukses
                            .addOnSuccessListener { documentReference ->
                                //menampilkan toast atau pesan success
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                //dan berpindah pada halaman mainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                            //jika gagal
                            .addOnFailureListener { exception ->
                                //menutup dialog progress
                                dialog.dismiss()
                                //menampilkan toast atau pesan failed
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