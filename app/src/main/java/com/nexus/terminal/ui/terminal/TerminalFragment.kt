package com.nexus.terminal.ui.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.R
import com.nexus.terminal.core.terminal.TerminalSession
import com.nexus.terminal.core.terminal.TerminalSessionManager
import com.nexus.terminal.databinding.FragmentTerminalBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TerminalFragment : Fragment() {

    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTerminalTabs()
        setupSpecialKeys()
        setupCommandInput()
        observeActiveSession()
    }

    private fun setupTerminalTabs() {
        binding.btnNewSession.setOnClickListener {
            TerminalSessionManager.createNewSession(
                workingDir = NexusApplication.instance.workspaceDir
            )
            refreshTabsUI()
        }

        binding.btnTerminalClear.setOnClickListener {
            TerminalSessionManager.activeSessionFlow.value?.clearBuffer()
        }

        binding.btnTerminalStop.setOnClickListener {
            TerminalSessionManager.activeSessionFlow.value?.sendSigInt()
        }
    }

    private fun refreshTabsUI() {
        val b = _binding ?: return
        val sessions = TerminalSessionManager.sessionsFlow.value
        val active = TerminalSessionManager.activeSessionFlow.value

        b.layoutSessionTabs.removeAllViews()
        b.layoutSessionTabs.addView(b.btnNewSession)

        for (session in sessions) {
            val tabView = TextView(requireContext()).apply {
                text = session.name
                textSize = 12f
                setPadding(24, 12, 24, 12)
                setTextColor(
                    if (session == active) {
                        resources.getColor(R.color.accent_violet, null)
                    } else {
                        resources.getColor(R.color.text_secondary, null)
                    }
                )
                setBackgroundResource(R.drawable.bg_glass_card)
                setOnClickListener {
                    TerminalSessionManager.selectSession(session)
                    refreshTabsUI()
                }
            }
            b.layoutSessionTabs.addView(tabView, 0)
        }
    }

    private fun setupSpecialKeys() {
        val getActive = { TerminalSessionManager.activeSessionFlow.value }

        binding.keyEsc.setOnClickListener {
            getActive()?.sendRaw("\u001B")
        }

        binding.keyTab.setOnClickListener {
            getActive()?.sendRaw("\t")
        }

        binding.keyCtrl.setOnClickListener {
            getActive()?.sendRaw("\u0000")
        }

        binding.keyAlt.setOnClickListener {
            getActive()?.sendRaw("\u001B")
        }

        binding.keyUp.setOnClickListener {
            val prevCmd = getActive()?.getPreviousCommand()
            _binding?.let { b ->
                if (prevCmd != null) {
                    b.etTerminalInput.setText(prevCmd)
                    b.etTerminalInput.setSelection(prevCmd.length)
                } else {
                    getActive()?.sendRaw("\u001B[A")
                }
            }
        }

        binding.keyDown.setOnClickListener {
            val nextCmd = getActive()?.getNextCommand()
            _binding?.let { b ->
                if (nextCmd != null) {
                    b.etTerminalInput.setText(nextCmd)
                    b.etTerminalInput.setSelection(nextCmd.length)
                } else {
                    getActive()?.sendRaw("\u001B[B")
                }
            }
        }

        binding.keyLeft.setOnClickListener {
            getActive()?.sendRaw("\u001B[D")
        }

        binding.keyRight.setOnClickListener {
            getActive()?.sendRaw("\u001B[C")
        }

        binding.keyPipe.setOnClickListener {
            insertTextToInput("| ")
        }

        binding.keySlash.setOnClickListener {
            insertTextToInput("/")
        }

        binding.keyDash.setOnClickListener {
            insertTextToInput("-")
        }

        binding.keyTilde.setOnClickListener {
            insertTextToInput("~")
        }
    }

    private fun insertTextToInput(text: String) {
        _binding?.let { b ->
            val cursor = b.etTerminalInput.selectionStart.coerceAtLeast(0)
            b.etTerminalInput.text.insert(cursor, text)
        }
    }

    private fun setupCommandInput() {
        binding.btnTerminalSend.setOnClickListener {
            submitCommand()
        }

        binding.etTerminalInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitCommand()
                true
            } else {
                false
            }
        }
    }

    private fun submitCommand() {
        _binding?.let { b ->
            val command = b.etTerminalInput.text.toString()
            if (command.isNotBlank()) {
                TerminalSessionManager.activeSessionFlow.value?.executeCommand(command)
                b.etTerminalInput.text.clear()
            }
        }
    }

    private fun observeActiveSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            TerminalSessionManager.activeSessionFlow.collectLatest { session ->
                refreshTabsUI()
                session?.outputFlow?.collectLatest { text ->
                    _binding?.let { b ->
                        b.tvTerminalOutput.text = text
                        b.scrollTerminalOutput.post {
                            _binding?.scrollTerminalOutput?.fullScroll(View.FOCUS_DOWN)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
