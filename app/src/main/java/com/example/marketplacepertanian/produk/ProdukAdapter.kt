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
        val nama_produk: TextView = itemView.findViewById(R.id.TVLNamaProduk)
        val ukuran_produk: TextView = itemView.findViewById(R.id.TVLUkuranProduk)
        val stok_produk: TextView = itemView.findViewById(R.id.TVLStokProduk)
        val harga_produk: TextView = itemView.findViewById(R.id.TVLHargaProduk)
        val img_produk: ImageView = itemView.findViewById(R.id.IMLFotoProduk)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.produk_list_layout, parent, false)
        return ProdukViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val produk: Produk = produkList[position]
        holder.nama_produk.text = produk.nama_produk
        holder.ukuran_produk.text = produk.ukuran
        holder.stok_produk.text = produk.stok
        holder.harga_produk.text = produk.harga

        holder.itemView.setOnClickListener {
            activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, EditProdukActivity::class.java).apply{
                putExtra("nama_produk", produk.nama_produk.toString())
                putExtra("ukuran_produk", produk.ukuran.toString())
                putExtra("stok_produk", produk.stok.toString())
                putExtra("harga_produk", produk.harga.toString())


            })
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("img_produk/${produk.nama_produk}.jpg")
        val localfile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            holder.img_produk.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Log.e("foto ?", "gagal")
        }
    }

    override fun getItemCount(): Int {
        return  produkList.size
    }
}