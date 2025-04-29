package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class NoteAdapter(
    private val notes: MutableList<NoteItem>,
    private val onSelectionChanged: () -> Unit,
    private val onDeleteNote: (NoteItem) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.noteImage)
        val title: TextView = itemView.findViewById(R.id.noteTitle)
        val text: TextView = itemView.findViewById(R.id.noteText)
        val checkbox: CheckBox = itemView.findViewById(R.id.noteCheckBox)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteNoteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]

        Glide.with(holder.itemView.context)
            .load(note.imageUri)
            .into(holder.image)

        holder.title.text = note.title
        holder.text.text = note.noteText
        holder.checkbox.isChecked = note.isSelected

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            note.isSelected = isChecked
            onSelectionChanged()
        }

        holder.deleteBtn.setOnClickListener {
            onDeleteNote(note)
        }
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, NoteDetailActivity::class.java).apply {
                putExtra("imageUri", note.imageUri)
                putExtra("title", note.title)
                putExtra("noteText", note.noteText)
            }
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = notes.size

    fun getSelectedNotes(): List<NoteItem> = notes.filter { it.isSelected }
}
