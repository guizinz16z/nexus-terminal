package com.nexus.terminal.ui.editor

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nexus.terminal.MainActivity
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.core.editor.LanguageType
import com.nexus.terminal.core.editor.SyntaxHighlighter
import com.nexus.terminal.core.storage.FileManager
import com.nexus.terminal.databinding.FragmentEditorBinding
import com.nexus.terminal.ui.ai.AiAssistantFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class EditorFragment : Fragment() {

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!

    private var currentFile: File? = null
    private var currentLanguage = LanguageType.PYTHON
    private var highlightJob: Job? = null
    private var isUpdatingTextInternally = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEditor()
        setupSymbolKeys()
        setupActions()

        if (currentFile == null) {
            val defaultScript = File(NexusApplication.instance.workspaceDir, "main.py")
            openFile(defaultScript)
        }
    }

    fun openFile(file: File) {
        currentFile = file
        currentLanguage = LanguageType.fromFileName(file.name)

        viewLifecycleOwner.lifecycleScope.launch {
            val content = FileManager.readText(file)
            isUpdatingTextInternally = true
            binding.etCodeEditor.setText(content)
            binding.tvActiveFileTab.text = file.name
            isUpdatingTextInternally = false
            updateLineNumbers()
            triggerSyntaxHighlight()
        }
    }

    private fun setupEditor() {
        binding.etCodeEditor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingTextInternally) return
                updateLineNumbers()
                triggerSyntaxHighlight()
            }
        })
    }

    private fun updateLineNumbers() {
        val lineCount = binding.etCodeEditor.lineCount.coerceAtLeast(1)
        val sb = java.lang.StringBuilder()
        for (i in 1..lineCount) {
            sb.append(i).append("\n")
        }
        binding.tvLineNumbers.text = sb.toString()
    }

    private fun triggerSyntaxHighlight() {
        highlightJob?.cancel()
        highlightJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(150) // Debounce for smooth 60 FPS mobile typing
            val editable = binding.etCodeEditor.text
            if (editable != null) {
                isUpdatingTextInternally = true
                SyntaxHighlighter.highlight(editable, currentLanguage)
                isUpdatingTextInternally = false
            }
        }
    }

    private fun setupSymbolKeys() {
        val insert = { sym: String ->
            val start = binding.etCodeEditor.selectionStart
            binding.etCodeEditor.text.insert(start, sym)
        }

        binding.symTab.setOnClickListener { insert("    ") }
        binding.symColon.setOnClickListener { insert(":") }
        binding.symParenOpen.setOnClickListener { insert("()") }
        binding.symParenClose.setOnClickListener { insert(")") }
        binding.symBracketOpen.setOnClickListener { insert("[]") }
        binding.symBracketClose.setOnClickListener { insert("]") }
        binding.symBraceOpen.setOnClickListener { insert("{}") }
        binding.symBraceClose.setOnClickListener { insert("}") }
        binding.symEquals.setOnClickListener { insert(" = ") }
        binding.symQuote.setOnClickListener { insert("\"\"") }
        binding.symHash.setOnClickListener { insert("# ") }
        binding.symDot.setOnClickListener { insert(".") }
    }

    private fun setupActions() {
        // Save
        binding.btnEditorSave.setOnClickListener {
            val file = currentFile ?: File(NexusApplication.instance.workspaceDir, "main.py")
            val content = binding.etCodeEditor.text.toString()
            viewLifecycleOwner.lifecycleScope.launch {
                val ok = FileManager.writeText(file, content)
                Toast.makeText(
                    requireContext(),
                    if (ok) "✓ Arquivo salvo com sucesso!" else "Falha ao salvar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Run
        binding.btnEditorRun.setOnClickListener {
            runCurrentScript()
        }

        // Close output drawer
        binding.btnCloseOutput.setOnClickListener {
            binding.layoutOutputDrawer.visibility = View.GONE
        }

        // AI Assistant
        binding.btnEditorAi.setOnClickListener {
            val code = binding.etCodeEditor.text.toString()
            val aiFragment = AiAssistantFragment.newInstance(contextCode = code)
            (activity as? MainActivity)?.showFragment(aiFragment)
        }
    }

    private fun runCurrentScript() {
        val file = currentFile ?: File(NexusApplication.instance.workspaceDir, "main.py")
        val content = binding.etCodeEditor.text.toString()

        viewLifecycleOwner.lifecycleScope.launch {
            FileManager.writeText(file, content)

            binding.layoutOutputDrawer.visibility = View.VISIBLE
            binding.tvScriptOutput.text = "★ Executando: ${file.name}...\n\n"

            NexusApplication.instance.pythonEngine.executeScript(file) { chunk ->
                activity?.runOnUiThread {
                    binding.tvScriptOutput.append(chunk)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
