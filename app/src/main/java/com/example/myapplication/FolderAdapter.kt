package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(
    private val folders: List<FolderInfo>,
    private val onClick: (String) -> Unit,
    private val onLongClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.folderName)
        val noteCount: TextView = itemView.findViewById(R.id.noteCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.name.text = folder.name
        holder.noteCount.text = "${folder.noteCount} notes"
        holder.itemView.setOnClickListener {
            onClick(folder.name)
        }
        holder.itemView.setOnLongClickListener {
            onLongClick(folder.name)
            true
        }

    }

    override fun getItemCount(): Int = folders.size

}
