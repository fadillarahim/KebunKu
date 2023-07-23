package com.example.marketplacepertanian.productsold

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
import com.example.marketplacepertanian.produk.EditProdukActivity
import com.example.marketplacepertanian.produk.Produk
import com.example.marketplacepertanian.produk.ProdukAdapter
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProdukSoldAdapter (private val produksoldList: ArrayList<ProdukSold>) :
    RecyclerView.Adapter<ProdukSoldAdapter.ProdukSoldViewHolder>(){

    private lateinit var activity: AppCompatActivity
    class ProdukSoldViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama_produk: TextView = itemView.findViewById(R.id.TVLNamaProduk)
        val berat_produk: TextView = itemView.findViewById(R.id.TVLUkuranProduk)
        val kuantitas_produk: TextView = itemView.findViewById(R.id.TVLKuantitas)
        val total_harga: TextView = itemView.findViewById(R.id.TVLTotalHarga)
        val img_produk: ImageView = itemView.findViewById(R.id.IMLFotoProduk)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukSoldViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.produk_sold_list_layout, parent, false)
        return ProdukSoldViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProdukSoldViewHolder, position: Int) {
        val produk_sold: ProdukSold = produksoldList[position]
        holder.nama_produk.text = produk_sold.nama_produk
        holder.berat_produk.text = produk_sold.berat
        holder.total_harga.text = produk_sold.total_harga
        holder.kuantitas_produk.text = produk_sold.quantity


        val storageRef = FirebaseStorage.getInstance().reference.child("img_produk/${produk_sold.nama_produk}.jpg")
        val localfile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            holder.img_produk.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Log.e("foto ?", "gagal")
        }
    }

    override fun getItemCount(): Int {
        return  produksoldList.size
    }
}