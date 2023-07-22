package com.tugas.kebunku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tugas.kebunku.R
import com.tugas.kebunku.chat.ChatActivity
import com.tugas.kebunku.databinding.ActivityMainBinding
import com.tugas.kebunku.produk.AddProdukActivity
import com.tugas.kebunku.produk.Produk
import com.tugas.kebunku.produk.ProdukAdapter
import com.tugas.kebunku.settings.SettingsActivity
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

        // Untuk nambah produk ketika button add di klik
        binding.btnAddProduk.setOnClickListener{
            val intentMain = Intent(this, AddProdukActivity::class.java)
            startActivity(intentMain)
        }

        swipeDelete()

        // search produk
        binding.txtSearchProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val keyword = binding.txtSearchProduk.text.toString()
                if (keyword.isNotEmpty()) {
                    search_data(keyword)
                } else {
                    load_data()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }
        })

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_bottom_home -> {
                    val intent = Intent(this, MainActivity::class.java)
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

    private fun search_data(keyword: String) {
        produkArrayList.clear()

        db = FirebaseFirestore.getInstance()

        val query = db.collection("products")
            .orderBy("nama_produk")
            .startAt(keyword)
            .get()

        query.addOnSuccessListener {
            produkArrayList.clear()
            for (document in it) {
                produkArrayList.add(document.toObject(Produk::class.java))
            }
        }
    }

    private fun load_data() {
        produkArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("products").addSnapshotListener(object :
        EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED)
                        produkArrayList.add(dc.document.toObject(Produk::class.java))
                }
                produkAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun deleteProduk(produk: Produk, doc_id:String){
        val builder = AlertDialog.Builder(this)

        builder.setMessage("Apakah ${produk.nama_produk} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                lifecycleScope.launch {
                    db.collection("products")
                        .document(doc_id).delete()
                    deleteFoto("img_produk/${produk.nama_produk}.jpg")
                    Toast.makeText(
                        applicationContext,
                        produk.nama_produk.toString() + " is deleted",
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

    private fun deleteFoto(file_name: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val deleteFileRef = storageRef.child(file_name)
        if (deleteFileRef != null){
            deleteFileRef.delete().addOnSuccessListener {
                Log.e("deleted", "succes")
            }.addOnFailureListener{
                Log.e("deleted", "failed")
            }
        }
    }

    private fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                lifecycleScope.launch{
                    val produk = produkArrayList[position]
                    val personQuery = db.collection("products")
                        .whereEqualTo("nama_produk", produk.nama_produk)
                        .whereEqualTo("berat", produk.berat)
                        .whereEqualTo("harga", produk.harga)
                        .whereEqualTo("stok", produk.stok)
                        .whereEqualTo("deskripsi", produk.deskripsi)
                        .get()
                        .await()

                    if(personQuery.documents.isNotEmpty()){
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
                    } else {
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

        }).attachToRecyclerView(produkRecyclerView)
    }
}