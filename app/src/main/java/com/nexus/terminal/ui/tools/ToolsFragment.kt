package com.nexus.terminal.ui.tools

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nexus.terminal.core.tools.CryptoUtils
import com.nexus.terminal.core.tools.NetworkUtils
import com.nexus.terminal.core.tools.ProcessMonitor
import com.nexus.terminal.core.tools.SshKeyGen
import com.nexus.terminal.databinding.FragmentToolsBinding
import kotlinx.coroutines.launch

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolCards()
    }

    private fun setupToolCards() {
        // Network Ping & HTTP
        binding.cardToolNetwork.setOnClickListener {
            showNetworkDialog()
        }

        // Process Monitor
        binding.cardToolProcess.setOnClickListener {
            loadProcesses()
        }

        // Crypto & Hashes
        binding.cardToolCrypto.setOnClickListener {
            showCryptoDialog()
        }

        // SSH Keygen
        binding.cardToolSsh.setOnClickListener {
            generateSshKey()
        }
    }

    private fun showNetworkDialog() {
        val input = EditText(requireContext()).apply {
            hint = "google.com ou https://httpbin.org/get"
            setSingleLine()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Teste de Rede & HTTP")
            .setView(input)
            .setPositiveButton("Executar") { _, _ ->
                val target = input.text.toString().trim()
                if (target.isNotEmpty()) {
                    binding.tvToolOutput.text = "★ Executando teste de conectividade para $target...\n"
                    viewLifecycleOwner.lifecycleScope.launch {
                        val result = if (target.startsWith("http://") || target.startsWith("https://")) {
                            NetworkUtils.executeHttpRequest(target)
                        } else {
                            NetworkUtils.pingHost(target)
                        }
                        binding.tvToolOutput.text = result
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadProcesses() {
        binding.tvToolOutput.text = "★ Carregando lista de processos ativos...\n"
        viewLifecycleOwner.lifecycleScope.launch {
            val processes = ProcessMonitor.listProcesses()
            val sb = StringBuilder("PID\t\tUSER\t\tCOMANDO\n")
            sb.append("----------------------------------------\n")
            processes.take(25).forEach { p ->
                sb.append("${p.pid}\t\t${p.user}\t\t${p.command}\n")
            }
            binding.tvToolOutput.text = sb.toString()
        }
    }

    private fun showCryptoDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Texto para calcular Hashes e Base64"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Gerador Criptográfico")
            .setView(input)
            .setPositiveButton("Calcular") { _, _ ->
                val text = input.text.toString()
                val md5 = CryptoUtils.md5(text)
                val sha256 = CryptoUtils.sha256(text)
                val b64 = CryptoUtils.base64Encode(text)

                val result = """
                    Entrada: "$text"
                    
                    • MD5:
                    $md5
                    
                    • SHA-256:
                    $sha256
                    
                    • Base64:
                    $b64
                """.trimIndent()

                binding.tvToolOutput.text = result
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun generateSshKey() {
        binding.tvToolOutput.text = "★ Gerando par de chaves SSH RSA 2048-bit...\n"
        viewLifecycleOwner.lifecycleScope.launch {
            val pair = SshKeyGen.generateRsaKeyPair()
            val output = """
                ✓ Chave SSH RSA gerada com sucesso!
                
                Chave Pública (Copie e adicione ao GitHub -> SSH Keys):
                ${pair.publicKey}
                
                Chave Privada:
                ${pair.privateKey}
            """.trimIndent()
            binding.tvToolOutput.text = output
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
