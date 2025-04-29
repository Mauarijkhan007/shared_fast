package com.example.myapplication

import android.net.Uri

data class NoteItem(
    val imageUri: String, // 👈 was Uri before
    val title: String,
    val noteText: String,
    val timestamp: String,
    var isSelected: Boolean = false
)

