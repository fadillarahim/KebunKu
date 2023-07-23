package com.example.marketplacepertanian.produk

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.marketplacepertanian.R
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProdukAdapter(private val produkList: ArrayList<Produk>) :
    RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>(){

    private lateinit var activity: AppCompatActivity
    class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.TVLNama)
        val stok: TextView = itemView.findViewById(R.id.TVLStok)
        val harga: TextView = itemView.findViewById(R.id.TVLHarga)
        val img_produk: ImageView = itemView.findViewById(R.id.IMLGambarProduk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.produk_list_layout, parent, false)
        return ProdukViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val produk: Produk = produkList[position]
        holder.nama.text = produk.nama
        holder.stok.text = produk.stok
        holder.harga.text = produk.harga

        holder.itemView.setOnClickListener {
            activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, EditProdukActivity::class.java).apply {
                putExtra("nama", produk.nama.toString())
                putExtra("berat", produk.berat.toString())
                putExtra("stok", produk.stok.toString())
                putExtra("harga", produk.harga.toString())
                putExtra("deskripsi", produk.deskripsi.toString())
            })
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("img_produk/${produk.nama}.jpg")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            holder.img_produk.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.e("foto ?", "gagal")
        }
    }

    override fun getItemCount(): Int {
        return produkList.size
    }
}
