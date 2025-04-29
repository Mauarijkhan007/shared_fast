package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FolderDetailActivity : AppCompatActivity() {

    private lateinit var adapter: NoteAdapter
    private val noteList = mutableListOf<NoteItem>()
    private lateinit var folderName: String

    private val REQUEST_IMAGE_PICK = 100
    private val REQUEST_IMAGE_CAPTURE = 101

    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_detail)

        folderName = intent.getStringExtra("folderName") ?: "UnknownFolder"
        title = folderName

        val recyclerView: RecyclerView = findViewById(R.id.noteRecyclerView)
        val shareFab = findViewById<FloatingActionButton>(R.id.fabShareNotes)

        shareFab.setOnClickListener {
            val selectedNotes = adapter.getSelectedNotes()
            if (selectedNotes.isEmpty()) {
                Toast.makeText(this, "No notes selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uris = ArrayList<Uri>().apply {
                selectedNotes.forEach { add(Uri.parse(it.imageUri)) }
            }

            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share selected notes via"))
        }

        val fab: FloatingActionButton = findViewById(R.id.fabAddNote)

        loadNotesFromStorage(folderName)

        adapter = NoteAdapter(
            noteList,
            onSelectionChanged = {},
            onDeleteNote = { note ->
                val index = noteList.indexOf(note)
                noteList.removeAt(index)
                adapter.notifyItemRemoved(index)
                saveNotesToStorage(folderName)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            showAddNoteDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_folder_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_share) {
            val selectedNotes = adapter.getSelectedNotes()
            if (selectedNotes.isEmpty()) {
                Toast.makeText(this, "No notes selected", Toast.LENGTH_SHORT).show()
                return true
            }

            val uris = ArrayList<Uri>().apply {
                selectedNotes.forEach { add(Uri.parse(it.imageUri)) }
            }

            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share selected notes via"))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)

        val preview = dialogView.findViewById<ImageView>(R.id.imagePreview)
        val title = dialogView.findViewById<EditText>(R.id.inputNoteTitle)
        val noteText = dialogView.findViewById<EditText>(R.id.inputNoteText)
        val pickBtn = dialogView.findViewById<Button>(R.id.buttonPickImage)
        val cameraBtn = dialogView.findViewById<Button>(R.id.buttonCaptureImage)

        pickBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        cameraBtn.setOnClickListener {
            val file = File.createTempFile("sharedfast_", ".jpg", cacheDir)
            cameraImageUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val imageUri = selectedImageUri ?: cameraImageUri
                if (imageUri != null) {
                    val savedUri = saveImageToMediaStore(imageUri)
                    if (savedUri != null) {
                        val note = NoteItem(
                            imageUri = savedUri.toString(),
                            title = title.text.toString(),
                            noteText = noteText.text.toString(),
                            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                        )
                        noteList.add(note)
                        adapter.notifyItemInserted(noteList.size - 1)
                        saveNotesToStorage(folderName)
                    }
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                }

                selectedImageUri = null
                cameraImageUri = null
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    selectedImageUri = data?.data
                }
                REQUEST_IMAGE_CAPTURE -> {
                    selectedImageUri = cameraImageUri
                }
            }
        }
    }

    private fun saveImageToMediaStore(sourceUri: Uri): Uri? {
        val resolver = contentResolver
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val name = "SharedFast_$timestamp.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SharedFast")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            resolver.openOutputStream(uri).use { out ->
                resolver.openInputStream(sourceUri).use { input ->
                    input?.copyTo(out!!)
                }
            }
        }

        return uri
    }

    private fun saveNotesToStorage(folderName: String) {
        val json = Gson().toJson(noteList)
        val file = File(filesDir, "$folderName.json")
        file.writeText(json)
    }

    private fun loadNotesFromStorage(folderName: String) {
        val file = File(filesDir, "$folderName.json")
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<MutableList<NoteItem>>() {}.type
            val notes: MutableList<NoteItem> = Gson().fromJson(json, type)
            noteList.clear()
            noteList.addAll(notes)
        }
    }
}
