package com.example.marketplacepertanian.productsold

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketplacepertanian.MainActivity
import com.example.marketplacepertanian.R
import com.example.marketplacepertanian.auth.SettingsActivity
import com.example.marketplacepertanian.chat.ChatActivity
import com.example.marketplacepertanian.databinding.ActivityMainBinding
import com.example.marketplacepertanian.databinding.ActivityProductSoldBinding
import com.example.marketplacepertanian.produk.AddProdukActivity
import com.example.marketplacepertanian.produk.Produk
import com.example.marketplacepertanian.produk.ProdukAdapter
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class ProductSoldActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductSoldBinding

    private lateinit var produkSoldRecyclerView: RecyclerView
    private lateinit var produkSoldArrayList: ArrayList<ProdukSold>
    private lateinit var produkSoldAdapter: ProdukSoldAdapter
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductSoldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        produkSoldRecyclerView = binding.produkListView
        produkSoldRecyclerView.layoutManager = LinearLayoutManager(this)
        produkSoldRecyclerView.setHasFixedSize(true)

        produkSoldArrayList = arrayListOf()
        produkSoldAdapter = ProdukSoldAdapter(produkSoldArrayList)

        produkSoldRecyclerView.adapter = produkSoldAdapter

        load_data()

        binding.btnAddProduk.setOnClickListener {
            val intentMain = Intent(this, AddProductSoldActivity::class.java)
            startActivity(intentMain)
        }

        swipeDelete()
        binding.txtSearchProduk.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val keyword = binding.txtSearchProduk.text.toString()
                if(keyword.isNotEmpty()) {
                    search_data(keyword)
                }
                else{
                    load_data()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_bottom_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_bottom_produkjual -> {
                    val intent = Intent(this, ProductSoldActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_bottom_setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_bottom_chat -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun load_data() {
        produkSoldArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("produk_sold").
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null){
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED)
                        produkSoldArrayList.add(dc.document.toObject(ProdukSold::class.java))
                }
                produkSoldAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun search_data(keyword :String) {
        produkSoldArrayList.clear()

        db = FirebaseFirestore.getInstance()

        val query = db.collection("produk_sold")
            .orderBy("nama_produk")
            .startAt(keyword)
            .get()
        query.addOnSuccessListener {
            produkSoldArrayList.clear()
            for (document in it) {
                produkSoldArrayList.add(document.toObject(ProdukSold::class.java))
            }
        }
    }

    private fun deleteProduk(produk_sold: ProdukSold, doc_id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Apakah ${produk_sold.nama_produk} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                lifecycleScope.launch {
                    db.collection("produk")
                        .document(doc_id).delete()
                    deleteFoto("img_produk/${produk_sold.nama_produk}.jpg")
                    Toast.makeText(
                        applicationContext,
                        produk_sold.nama_produk.toString() + " is deleted",
                        Toast.LENGTH_LONG
                    ).show()
                    load_data()
                }
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
                load_data()
            }
        val alert = builder.create()
        alert.show()

    }

    private fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                lifecycleScope.launch {
                    val produk_sold = produkSoldArrayList[position]
                    val personQuery = db.collection("produk_sold")
                        .whereEqualTo("nama_produk", produk_sold.nama_produk)
                        .whereEqualTo("ukuran", produk_sold.ukuran)
                        .whereEqualTo("harga_total", produk_sold.total_harga)
                        .whereEqualTo("kuantitas", produk_sold.quantity)
                        .get()
                        .await()

                    if (personQuery.documents.isNotEmpty()) {
                        for (document in personQuery) {
                            try {
                                deleteProduk(produk_sold, document.id)
                                load_data()
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        applicationContext,
                                        e.message.toString(),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                        }
                    }
                    else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "Produk yang ingin di hapus tidak ditemukan",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }).attachToRecyclerView(produkSoldRecyclerView)
    }

    private fun deleteFoto(file_name: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val deleteFileRef = storageRef.child(file_name)
        if (deleteFileRef != null) {
            deleteFileRef.delete().addOnSuccessListener {
                Log.e("deleted", "success")
            }.addOnFailureListener {
                Log.e("deleted", "failed")
            }
        }
    }
}