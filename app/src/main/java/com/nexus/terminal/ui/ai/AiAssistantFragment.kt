package com.nexus.terminal.ui.ai

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.core.ai.AiMessage
import com.nexus.terminal.databinding.FragmentAiBinding
import kotlinx.coroutines.launch

class AiAssistantFragment : Fragment() {

    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<AiMessage>()
    private lateinit var chatAdapter: AiChatAdapter
    private var attachedCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachedCode = arguments?.getString(ARG_CONTEXT_CODE) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChat()
        setupQuickChips()
        setupApiKeyConfig()
        setupSendButton()

        if (messages.isEmpty()) {
            addAssistantMessage("Olá! Sou o assistente de código do Nexus Terminal. Como posso ajudar com seu código ou terminal hoje?")
            if (attachedCode.isNotEmpty()) {
                addAssistantMessage("✓ Código do editor anexado para análise.")
            }
        }
    }

    private fun setupChat() {
        chatAdapter = AiChatAdapter(messages)
        binding.rvAiMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAiMessages.adapter = chatAdapter
    }

    private fun setupQuickChips() {
        binding.chipExplainCode.setOnClickListener {
            sendPrompt("Por favor, explique o que este código faz passo a passo.")
        }

        binding.chipDebugError.setOnClickListener {
            sendPrompt("Encontre possíveis bugs, erros de sintaxe ou exceções no código.")
        }

        binding.chipOptimizeCode.setOnClickListener {
            sendPrompt("Como posso otimizar a performance e a legibilidade deste script?")
        }
    }

    private fun setupApiKeyConfig() {
        binding.btnConfigAiKey.setOnClickListener {
            val input = EditText(requireContext()).apply {
                hint = "sk-..."
                setText(NexusApplication.instance.preferences.aiApiKey)
                setSingleLine()
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Configurar Chave API")
                .setMessage("Insira sua chave da OpenAI, Gemini ou OpenRouter:")
                .setView(input)
                .setPositiveButton("Salvar") { _, _ ->
                    val key = input.text.toString().trim()
                    NexusApplication.instance.preferences.aiApiKey = key
                    NexusApplication.instance.aiAssistantService.setApiKey(key)
                    Toast.makeText(requireContext(), "Chave API salva!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun setupSendButton() {
        binding.btnSendAiPrompt.setOnClickListener {
            val text = binding.etAiPrompt.text.toString().trim()
            if (text.isNotEmpty()) {
                sendPrompt(text)
                binding.etAiPrompt.text.clear()
            }
        }
    }

    private fun sendPrompt(prompt: String) {
        addUserMessage(prompt)

        viewLifecycleOwner.lifecycleScope.launch {
            val response = NexusApplication.instance.aiAssistantService.askAssistant(
                prompt = prompt,
                contextCode = attachedCode
            )
            addAssistantMessage(response)
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(AiMessage(sender = "user", content = text))
        chatAdapter.updateMessages(messages)
        binding.rvAiMessages.scrollToPosition(messages.size - 1)
    }

    private fun addAssistantMessage(text: String) {
        messages.add(AiMessage(sender = "assistant", content = text))
        chatAdapter.updateMessages(messages)
        binding.rvAiMessages.scrollToPosition(messages.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONTEXT_CODE = "arg_context_code"

        fun newInstance(contextCode: String): AiAssistantFragment {
            return AiAssistantFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTEXT_CODE, contextCode)
                }
            }
        }
    }
}
