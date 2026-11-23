package com.nexus.terminal.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.core.storage.ZipUtils
import com.nexus.terminal.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackupActions()
    }

    private fun setupBackupActions() {
        binding.btnExportBackup.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val backupFile = File(
                    NexusApplication.instance.workspaceDir,
                    "NexusTerminal_Backup_$timestamp.zip"
                )
                val ok = ZipUtils.zipDirectory(
                    NexusApplication.instance.projectsDir,
                    backupFile
                )
                if (ok) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Backup Gerado com Sucesso!")
                        .setMessage("O backup dos seus projetos foi salvo em:\n${backupFile.absolutePath}")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Falha ao gerar backup", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnImportBackup.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Para restaurar, selecione o arquivo ZIP no Gerenciador de Arquivos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
