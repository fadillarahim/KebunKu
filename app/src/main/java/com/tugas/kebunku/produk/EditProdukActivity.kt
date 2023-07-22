package com.tugas.kebunku.produk

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.tugas.kebunku.MainActivity
import com.tugas.kebunku.R
import com.tugas.kebunku.databinding.ActivityEditProdukBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class EditProdukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProdukBinding
    private val db = FirebaseFirestore.getInstance()

    private val REQ_CAM = 101
    private lateinit var imqUri : Uri
    private var dataGambar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val curr_product = setDefaultValue()

        binding.BtnEditProduk.setOnClickListener{
            val new_data_produk = newProduk()

            updateProduk(curr_product as Produk, new_data_produk)

            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
            finish()
        }

        showFoto()

        binding.BtnImgProduk.setOnClickListener{
            openCamera()
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

    private fun showFoto() {
        val intent = intent
        val nama_produk = intent.getStringExtra("nama_produk").toString()

        val storageRef = FirebaseStorage.getInstance().reference.child("img_produk/${nama_produk}.jpg")
        val localfile = File.createTempFile("tempImage", "jpg")

        storageRef.getFile(localfile).addOnCompleteListener{
            val bitmap =  BitmapFactory.decodeFile(localfile.absolutePath)
            binding.BtnImgProduk.setImageBitmap(bitmap)
        }.addOnCompleteListener{
            Log.e("foto", "gagal")
        }
    }

    private fun updateProduk(produk: Produk, newProdukMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = db.collection("products")
            .whereEqualTo("nama_produk", produk.nama_produk)
            .whereEqualTo("berat", produk.berat)
            .whereEqualTo("harga", produk.harga)
            .whereEqualTo("stok", produk.stok)
            .whereEqualTo("deskripsi", produk.deskripsi)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    db.collection("products").document(document.id).set(
                        newProdukMap,
                        SetOptions.merge()
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditProdukActivity,
                            e.message, Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }else{
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@EditProdukActivity,
                    "No product matched the query.", Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun setDefaultValue(): Array<Any> {
        val intent = intent
        val nama_produk = intent.getStringExtra("nama_produk").toString()
        val berat = intent.getStringExtra("berat").toString()
        val harga = intent.getStringExtra("harga").toString()
        val stok = intent.getStringExtra("stok").toString()
        val deskripsi = intent.getStringExtra("deskripsi").toString()

        binding.TxtEditNamaProduk.setText(nama_produk)
        binding.TxtEditBerat.setText(berat)
        binding.TxtEditHarga.setText(harga)
        binding.TxtEditStok.setText(stok)
        binding.TxtEditDeskripsi.setText(deskripsi)

        val curr_produk = Produk(nama_produk, berat, harga, stok, deskripsi)
        return arrayOf(curr_produk)
    }

    private fun newProduk(): Map<String, Any> {
        var nama_produk : String = binding.TxtEditNamaProduk.text.toString()
        var berat : String = binding.TxtEditBerat.text.toString()
        var harga : String = binding.TxtEditHarga.text.toString()
        var stok : String = binding.TxtEditStok.text.toString()
        var deskripsi: String = binding.TxtEditDeskripsi.text.toString()

        if(dataGambar != null){
            uploadPictFirebase(dataGambar!!, "${nama_produk}")
        }

        val produk = mutableMapOf<String, Any>()
        produk["nama_produk"] = nama_produk
        produk["berat"] = berat
        produk["harga"] = harga
        produk["stok"] = stok
        produk["deskripsi"] = deskripsi

        return produk
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_CAM && resultCode == RESULT_OK){
            dataGambar = data?.extras?.get("data") as Bitmap
            binding.BtnImgProduk.setImageBitmap(dataGambar)
        }
    }
}