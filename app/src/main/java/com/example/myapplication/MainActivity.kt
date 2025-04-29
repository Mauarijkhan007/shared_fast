package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.File
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
data class FolderInfo(
    val name: String,
    var noteCount: Int
)

class MainActivity : AppCompatActivity() {

    private lateinit var folderAdapter: FolderAdapter
    private val folderList = mutableListOf<FolderInfo>()



    private fun saveFoldersToStorage() {
        val names = folderList.map { it.name }
        val json = Gson().toJson(names)
        File(filesDir, "folders.json").writeText(json)
    }


    private fun loadFoldersFromStorage() {
        val file = File(filesDir, "folders.json")
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<MutableList<String>>() {}.type
            val names: MutableList<String> = Gson().fromJson(json, type)

            folderList.clear()
            for (name in names) {
                val count = getNoteCountForFolder(name)
                folderList.add(FolderInfo(name, count))
            }
        }
    }
    private fun getNoteCountForFolder(folderName: String): Int {
        val file = File(filesDir, "$folderName.json")
        if (!file.exists()) return 0

        val json = file.readText()
        val type = object : TypeToken<List<NoteItem>>() {}.type
        val notes: List<NoteItem> = Gson().fromJson(json, type)
        return notes.size
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadFoldersFromStorage()


        val recyclerView: RecyclerView = findViewById(R.id.folderRecyclerView)
        val fab: FloatingActionButton = findViewById(R.id.addFolderFab)

        folderAdapter = FolderAdapter(
            folderList,
            onClick = { folderName ->
                val intent = Intent(this, FolderDetailActivity::class.java)
                intent.putExtra("folderName", folderName)
                startActivity(intent)
            },
            onLongClick = { folderName -> deleteFolder(folderName) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = folderAdapter

        fab.setOnClickListener {
            showCreateFolderDialog()
        }
    }
    override fun onResume() {
        super.onResume()

        // Refresh each folder's note count
        folderList.forEach {
            it.noteCount = getNoteCountForFolder(it.name)
        }

        folderAdapter.notifyDataSetChanged()
    }

    private fun showCreateFolderDialog() {
        val input = EditText(this).apply {
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorText))
        }



        AlertDialog.Builder(this)
            .setTitle("New Folder")
            .setMessage("Enter folder name:")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val folderName = input.text.toString().trim()

                if (folderName.isEmpty()) {
                    Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val folderAlreadyExists = folderList.any { it.name.equals(folderName, ignoreCase = true) }
                if (folderAlreadyExists) {
                    Toast.makeText(this, "A folder with this name already exists", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                folderList.add(FolderInfo(folderName, 0))
                folderAdapter.notifyItemInserted(folderList.size - 1)
                saveFoldersToStorage()
            }

            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFolder(folderName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Folder")
            .setMessage("Are you sure you want to delete \"$folderName\" and all its notes?")
            .setPositiveButton("Delete") { _, _ ->
                val file = File(filesDir, "$folderName.json")
                if (file.exists()) file.delete()

                val index = folderList.indexOfFirst { it.name == folderName }
                if (index != -1) {
                    folderList.removeAt(index)
                    folderAdapter.notifyItemRemoved(index)
                    saveFoldersToStorage()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}