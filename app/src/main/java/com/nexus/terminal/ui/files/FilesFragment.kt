package com.nexus.terminal.ui.files

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexus.terminal.MainActivity
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.core.storage.FileItem
import com.nexus.terminal.core.storage.FileManager
import com.nexus.terminal.databinding.FragmentFilesBinding
import kotlinx.coroutines.launch
import java.io.File

class FilesFragment : Fragment() {

    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!

    private var currentDirectory: File = NexusApplication.instance.workspaceDir
    private lateinit var filesAdapter: FilesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbar()
        loadDirectory(currentDirectory)
    }

    private fun setupRecyclerView() {
        filesAdapter = FilesAdapter(
            onItemClick = { item ->
                if (item.isDirectory) {
                    loadDirectory(item.file)
                } else {
                    // Open in code editor
                    (activity as? MainActivity)?.navigateToEditor(item.file)
                }
            },
            onOptionsClick = { item ->
                showFileOptionsDialog(item)
            }
        )
        binding.rvFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFiles.adapter = filesAdapter
    }

    private fun setupToolbar() {
        binding.btnNewFile.setOnClickListener {
            showCreateFileDialog()
        }

        binding.btnRefreshFiles.setOnClickListener {
            loadDirectory(currentDirectory)
        }
    }

    private fun loadDirectory(dir: File) {
        currentDirectory = dir
        binding.tvCurrentPath.text = dir.absolutePath

        viewLifecycleOwner.lifecycleScope.launch {
            val items = FileManager.listFiles(dir)
            filesAdapter.updateFiles(items)
        }
    }

    private fun showCreateFileDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Nome do arquivo (ex: script.py)"
            setSingleLine()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Criar Novo Arquivo")
            .setView(input)
            .setPositiveButton("Criar") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        FileManager.createFile(currentDirectory, name)
                        loadDirectory(currentDirectory)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showFileOptionsDialog(item: FileItem) {
        val options = arrayOf("Abrir no Editor", "Renomear", "Excluir")

        AlertDialog.Builder(requireContext())
            .setTitle(item.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (!item.isDirectory) {
                            (activity as? MainActivity)?.navigateToEditor(item.file)
                        } else {
                            loadDirectory(item.file)
                        }
                    }
                    1 -> showRenameDialog(item)
                    2 -> showDeleteDialog(item)
                }
            }
            .show()
    }

    private fun showRenameDialog(item: FileItem) {
        val input = EditText(requireContext()).apply {
            setText(item.name)
            setSingleLine()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Renomear")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != item.name) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        FileManager.renameFile(item.file, newName)
                        loadDirectory(currentDirectory)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteDialog(item: FileItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir ${item.name}?")
            .setMessage("Essa ação não poderá ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    FileManager.deleteFile(item.file)
                    loadDirectory(currentDirectory)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
