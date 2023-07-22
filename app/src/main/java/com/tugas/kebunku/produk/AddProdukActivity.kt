package com.tugas.kebunku.produk

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tugas.kebunku.MainActivity
import com.tugas.kebunku.R
import com.tugas.kebunku.databinding.ActivityAddProdukBinding
import java.io.ByteArrayOutputStream

class AddProdukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProdukBinding
    private val firestoreDatabase = FirebaseFirestore.getInstance()

    private val REQ_CAM = 101
    private lateinit var imqUri : Uri
    private var dataGambar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BtnAddProduk.setOnClickListener{
            addProduk()
        }

        binding.BtnImgProduk.setOnClickListener{
            openCamera()
        }
    }

    private fun addProduk() {
        var nama_produk : String = binding.TxtAddNamaProduk.text.toString()
        var berat : String = binding.TxtAddBerat.text.toString()
        var harga : String = binding.TxtAddHarga.text.toString()
        var stok : String = binding.TxtAddStok.text.toString()
        var deskripsi: String = binding.TxtAddDeskripsi.text.toString()

        val produk: MutableMap<String, Any> = HashMap()
        produk["nama_produk"] = nama_produk
        produk["berat"] = berat
        produk["harga"] = harga
        produk["stok"] = stok
        produk["deskripsi"] = deskripsi

        if(dataGambar != null) {
            uploadPictFirebase(dataGambar!!, "${nama_produk}")

            firestoreDatabase.collection("products").add(produk)
                .addOnSuccessListener {
                    val intentMain = Intent(this, MainActivity::class.java)
                    startActivity(intentMain)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_CAM && resultCode == RESULT_OK){
            dataGambar = data?.extras?.get("data") as Bitmap
            binding.BtnImgProduk.setImageBitmap(dataGambar)
        }
    }

    private fun uploadPictFirebase(img_bitmap: Bitmap, file_name: String) {
        val baos = ByteArrayOutputStream()
        val ref = FirebaseStorage.getInstance().reference.child("img_produk/${file_name}.jpg")
        img_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val img = baos.toByteArray()
        ref.putBytes(img)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    ref.downloadUrl.addOnCompleteListener{Task ->
                        Task.result.let { Uri ->
                            imqUri = Uri
                            binding.BtnImgProduk.setImageBitmap(img_bitmap)
                        }
                    }
                }
            }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }
}