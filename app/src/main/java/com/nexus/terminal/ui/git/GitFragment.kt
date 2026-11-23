package com.nexus.terminal.ui.git

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.core.git.GitManager
import com.nexus.terminal.databinding.FragmentGitBinding
import kotlinx.coroutines.launch
import java.io.File

class GitFragment : Fragment() {

    private var _binding: FragmentGitBinding? = null
    private val binding get() = _binding!!

    private val repoDir: File by lazy {
        NexusApplication.instance.workspaceDir
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()
        refreshGitStatus()
    }

    private fun setupActions() {
        binding.btnGitCommit.setOnClickListener {
            val msg = binding.etCommitMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val out = GitManager.commit(repoDir, msg)
                    Toast.makeText(requireContext(), "Commit efetuado!", Toast.LENGTH_SHORT).show()
                    binding.etCommitMessage.text.clear()
                    refreshGitStatus()
                }
            } else {
                Toast.makeText(requireContext(), "Insira uma mensagem de commit", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGitClone.setOnClickListener {
            showCloneDialog()
        }

        binding.btnGitPull.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val out = GitManager.pull(repoDir)
                binding.tvGitStatusLog.text = out
            }
        }

        binding.btnGitPush.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val out = GitManager.push(repoDir)
                binding.tvGitStatusLog.text = out
            }
        }
    }

    private fun refreshGitStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            val status = GitManager.getStatus(repoDir)
            binding.tvGitBranch.text = "Branch Atual: ${status.branch}"

            val sb = StringBuilder()
            if (status.isClean) {
                sb.append("✓ Diretório de trabalho limpo. Nenhuma alteração pendente.")
            } else {
                if (status.modifiedFiles.isNotEmpty()) {
                    sb.append("Modificados:\n")
                    status.modifiedFiles.forEach { sb.append("  • $it\n") }
                }
                if (status.untrackedFiles.isNotEmpty()) {
                    sb.append("\nNão rastreados:\n")
                    status.untrackedFiles.forEach { sb.append("  ? $it\n") }
                }
            }
            binding.tvGitStatusLog.text = sb.toString()
        }
    }

    private fun showCloneDialog() {
        val input = EditText(requireContext()).apply {
            hint = "https://github.com/usuario/repo.git"
            setSingleLine()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Clonar Repositório Git")
            .setView(input)
            .setPositiveButton("Clonar") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    val repoName = url.substringAfterLast("/").removeSuffix(".git")
                    val target = File(NexusApplication.instance.projectsDir, repoName)
                    viewLifecycleOwner.lifecycleScope.launch {
                        GitManager.clone(url, target) { log ->
                            activity?.runOnUiThread {
                                binding.tvGitStatusLog.append(log)
                            }
                        }
                    }
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
