package com.tugas.kebunku.produk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.tugas.kebunku.R

class ProdukAdapter(private val produkList:ArrayList<Produk>) : RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>(){

    private lateinit var activity: AppCompatActivity

    class ProdukViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView){
        val nama_produk : TextView = ItemView.findViewById(R.id.TVLNamaProduk)
        val stok : TextView = ItemView.findViewById(R.id.TVLStock)
        val harga: TextView = ItemView.findViewById(R.id.TVLHarga)
        val img_produk : ImageView = itemView.findViewById(R.id.IMLGambarProduk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.produk_list_layout, parent, false)
        return ProdukViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val produk:Produk = produkList[position]
        holder.nama_produk.text = produk.nama_produk
        holder.stok.text = produk.stok
        
    }
}