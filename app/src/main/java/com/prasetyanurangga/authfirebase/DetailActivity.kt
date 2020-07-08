package com.prasetyanurangga.authfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.prasetyanurangga.authfirebase.databinding.ActivityDetailBinding
import com.prasetyanurangga.authfirebase.databinding.ActivityLayoutMainBinding
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Inizializaze Firebase Auth
        auth = Firebase.auth

        val name = intent.getStringExtra("data_name")
        val from = intent.getStringExtra("data_from")

        txt_name.text = name
        txt_from.text = from

        btn_so.setOnClickListener {
            auth.signOut().apply {
                startActivity(Intent(this@DetailActivity, MainActivity::class.java))
            }
        }
    }
}
