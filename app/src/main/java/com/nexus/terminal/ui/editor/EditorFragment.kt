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

        // Check if a file path was passed in arguments
        val pathFromArgs = arguments?.getString(ARG_FILE_PATH)
        val targetFile = if (!pathFromArgs.isNullOrEmpty()) {
            File(pathFromArgs)
        } else {
            currentFile ?: File(NexusApplication.instance.workspaceDir, "main.py")
        }

        loadFileContent(targetFile)
    }

    fun openFile(file: File) {
        currentFile = file
        currentLanguage = LanguageType.fromFileName(file.name)
        if (_binding != null) {
            loadFileContent(file)
        }
    }

    private fun loadFileContent(file: File) {
        currentFile = file
        currentLanguage = LanguageType.fromFileName(file.name)

        viewLifecycleOwner.lifecycleScope.launch {
            val content = FileManager.readText(file)
            _binding?.let { b ->
                isUpdatingTextInternally = true
                b.etCodeEditor.setText(content)
                b.tvActiveFileTab.text = file.name
                isUpdatingTextInternally = false
                updateLineNumbers()
                triggerSyntaxHighlight()
            }
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
        _binding?.let { b ->
            val lineCount = b.etCodeEditor.lineCount.coerceAtLeast(1)
            val sb = java.lang.StringBuilder()
            for (i in 1..lineCount) {
                sb.append(i).append("\n")
            }
            b.tvLineNumbers.text = sb.toString()
        }
    }

    private fun triggerSyntaxHighlight() {
        highlightJob?.cancel()
        highlightJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(150) // Debounce for smooth 60 FPS mobile typing
            _binding?.etCodeEditor?.text?.let { editable ->
                isUpdatingTextInternally = true
                SyntaxHighlighter.highlight(editable, currentLanguage)
                isUpdatingTextInternally = false
            }
        }
    }

    private fun setupSymbolKeys() {
        val insert = { sym: String ->
            _binding?.let { b ->
                val start = b.etCodeEditor.selectionStart.coerceAtLeast(0)
                b.etCodeEditor.text.insert(start, sym)
            }
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
                context?.let { ctx ->
                    Toast.makeText(
                        ctx,
                        if (ok) "✓ Arquivo salvo com sucesso!" else "Falha ao salvar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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

            _binding?.let { b ->
                b.layoutOutputDrawer.visibility = View.VISIBLE
                b.tvScriptOutput.text = "★ Executando: ${file.name}...\n\n"
            }

            NexusApplication.instance.pythonEngine.executeScript(file) { chunk ->
                activity?.runOnUiThread {
                    _binding?.tvScriptOutput?.append(chunk)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        highlightJob?.cancel()
        _binding = null
    }

    companion object {
        private const val ARG_FILE_PATH = "arg_file_path"

        fun newInstance(filePath: String? = null): EditorFragment {
            return EditorFragment().apply {
                arguments = Bundle().apply {
                    if (filePath != null) {
                        putString(ARG_FILE_PATH, filePath)
                    }
                }
            }
        }
    }
}
