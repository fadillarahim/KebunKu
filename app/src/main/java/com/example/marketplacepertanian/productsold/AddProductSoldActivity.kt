package com.example.marketplacepertanian.productsold

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.example.marketplacepertanian.MainActivity
import com.example.marketplacepertanian.R
import com.example.marketplacepertanian.databinding.ActivityAddProductSoldBinding
import com.example.marketplacepertanian.databinding.ActivityAddProdukBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddProductSoldActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductSoldBinding
    private val firestoreDatabase = FirebaseFirestore.getInstance()

    private val REQ_CAM = 101
    private lateinit var imgUri : Uri
    private var dataGambar : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProductSoldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BtnAddProduk.setOnClickListener {
            addProduk()
        }

        binding.BtnImgProduk.setOnClickListener {
            openCamera()
        }
    }

    fun addProduk() {
        var nama_produk : String = binding.TxtAddNamaProduk.text.toString()
        var ukuran : String = binding.TxtAddUkuran.text.toString()
        var total_harga : String = binding.TxtAddTotalHarga.text.toString()
        var kuantitas : String = binding.TxtAddQuantity.text.toString()


        val produk_sold: MutableMap<String, Any> = HashMap()
        produk_sold["nama_produk"] = nama_produk
        produk_sold["ukuran"] = ukuran
        produk_sold["total_harga"] = total_harga
        produk_sold["kuantitas"] = kuantitas


        if (dataGambar != null) {
            uploadPictFirebase(dataGambar!!, "${nama_produk}")

            firestoreDatabase.collection("produk_sold").add(produk_sold)
                .addOnSuccessListener {
                    val intentMain = Intent(this, ProductSoldActivity::class.java)
                    startActivity(intentMain)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAM && resultCode == RESULT_OK) {
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
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener { Task ->
                        Task.result.let { Uri ->
                            imgUri = Uri
                            binding.BtnImgProduk.setImageBitmap(img_bitmap)
                        }
                    }
                }
            }
    }
}