package com.nexus.terminal.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexus.terminal.R
import com.nexus.terminal.core.storage.FileItem
import com.nexus.terminal.databinding.ItemFileBinding

class FilesAdapter(
    private var files: List<FileItem> = emptyList(),
    private val onItemClick: (FileItem) -> Unit,
    private val onOptionsClick: (FileItem) -> Unit
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    fun updateFiles(newFiles: List<FileItem>) {
        files = newFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    inner class ViewHolder(private val binding: ItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FileItem) {
            binding.tvFileName.text = item.name
            binding.tvFileDetails.text = "${item.formattedSize} • ${item.formattedDate}"

            val iconRes = when {
                item.isDirectory -> R.drawable.ic_nav_files
                item.extension in listOf("py", "pyw") -> R.drawable.ic_python
                item.extension in listOf("sh", "bash") -> R.drawable.ic_nav_terminal
                item.extension in listOf("json", "xml", "yaml", "yml") -> R.drawable.ic_nav_editor
                else -> R.drawable.ic_nav_editor
            }
            binding.ivFileIcon.setImageResource(iconRes)

            binding.root.setOnClickListener {
                onItemClick(item)
            }

            binding.btnFileOptions.setOnClickListener {
                onOptionsClick(item)
            }
        }
    }
}
