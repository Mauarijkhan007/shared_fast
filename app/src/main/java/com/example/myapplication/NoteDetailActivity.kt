package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class NoteDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val imageView = findViewById<ImageView>(R.id.detailImage)
        val titleView = findViewById<TextView>(R.id.detailTitle)
        val textView = findViewById<TextView>(R.id.detailText)

        val imageUri = intent.getStringExtra("imageUri")
        val title = intent.getStringExtra("title")
        val noteText = intent.getStringExtra("noteText")

        Glide.with(this)
            .load(Uri.parse(imageUri))
            .into(imageView)

        titleView.text = title
        textView.text = noteText
    }
}
