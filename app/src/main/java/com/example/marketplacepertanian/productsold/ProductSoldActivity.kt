package com.example.marketplacepertanian.productsold

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.marketplacepertanian.MainActivity
import com.example.marketplacepertanian.R
import com.example.marketplacepertanian.auth.SettingsActivity
import com.example.marketplacepertanian.chat.ChatActivity
import com.example.marketplacepertanian.databinding.ActivityMainBinding
import com.example.marketplacepertanian.databinding.ActivityProductSoldBinding

class ProductSoldActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductSoldBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductSoldBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}