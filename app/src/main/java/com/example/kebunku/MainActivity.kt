package com.example.kebunku

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
import com.example.kebunku.databinding.ActivityMainBinding
import com.example.kebunku.produk.AddProdukActivity
import com.example.kebunku.produk.Produk
import com.example.kebunku.produk.ProdukAdapter
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var produkRecyclerView: RecyclerView
    private lateinit var produkArrayList: ArrayList<Produk>
    private lateinit var produkAdapter: ProdukAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        produkRecyclerView = binding.produkListView
        produkRecyclerView.layoutManager = LinearLayoutManager(this)
        produkRecyclerView.setHasFixedSize(true)

        produkArrayList = arrayListOf()
        produkAdapter = ProdukAdapter(produkArrayList)

        produkRecyclerView.adapter = produkAdapter

        load_data()
        swipeDelete()

        binding.btnAddProduk.setOnClickListener {
            val intentMain = Intent(this, AddProdukActivity::class.java)
            startActivity(intentMain)
        }

        binding.txtSearchProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val keyword = binding.txtSearchProduk.text.toString()
                if (keyword.isNotEmpty()) {
                    search_data(keyword)
                } else {
                    load_data()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

    }

    private fun load_data() {
        produkArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("produk").
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null){
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!){
                    if (dc.type == DocumentChange.Type.ADDED)
                        produkArrayList.add(dc.document.toObject(Produk::class.java))
                }
                produkAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun search_data(keyword :String) {
        produkArrayList.clear()

        db = FirebaseFirestore.getInstance()

        val query = db.collection("produk")
            .orderBy("nama")
            .startAt(keyword)
            .get()
        query.addOnSuccessListener {
            produkArrayList.clear()
            for (document in it) {
                produkArrayList.add(document.toObject(Produk::class.java))
            }
        }
    }

    private fun deleteProduk(produk: Produk, doc_id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Apakah ${produk.nama} ingin dihapus?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->

                lifecycleScope.launch {
                    db.collection("produk")
                        .document(doc_id).delete()

                    deleteFoto("img_produk/${produk.nama}.jpg")

                    Toast.makeText(
                        applicationContext,
                        produk.nama.toString() + " is deleted",
                        Toast.LENGTH_LONG
                    ).show()
                    load_data()
                }
            }
            .setNegativeButton("No") {dialog, id ->
                dialog.dismiss()
                load_data()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                lifecycleScope.launch {
                    val produk = produkArrayList[position]
                    val personQuery = db.collection("produk")
                        .whereEqualTo("nama", produk.nama)
                        .whereEqualTo("berat", produk.berat)
                        .whereEqualTo("harga", produk.harga)
                        .whereEqualTo("stok", produk.stok)
                        .whereEqualTo("deskripsi", produk.deskripsi)
                        .get()
                        .await()

                    if (personQuery.documents.isNotEmpty()) {
                        for (document in personQuery) {
                            try {
                                deleteProduk(produk, document.id)
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
                                "Uset yang ingin di hapus tidak ditemukan",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }
                }
            }
        }).attachToRecyclerView(produkRecyclerView)
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