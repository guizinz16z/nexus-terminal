package com.nexus.terminal.ui.dashboard

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexus.terminal.MainActivity
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.databinding.FragmentDashboardBinding
import com.nexus.terminal.ui.git.GitFragment
import com.nexus.terminal.ui.packages.PackagesFragment
import com.nexus.terminal.ui.projects.ProjectsFragment
import com.nexus.terminal.ui.settings.SettingsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var projectsAdapter: DashboardProjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProjectsRecyclerView()
        setupClickListeners()
        loadSystemMetrics()
        loadProjects()
    }

    private fun setupProjectsRecyclerView() {
        projectsAdapter = DashboardProjectsAdapter(
            onProjectClick = { project ->
                val mainActivity = activity as? MainActivity
                val entryFile = File(project.path, project.entryPoint)
                mainActivity?.navigateToEditor(entryFile)
            },
            onRunClick = { project ->
                val mainActivity = activity as? MainActivity
                val entryFile = File(project.path, project.entryPoint)
                mainActivity?.navigateToEditor(entryFile)
            }
        )
        binding.rvRecentProjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentProjects.adapter = projectsAdapter
    }

    private fun setupClickListeners() {
        val mainActivity = activity as? MainActivity

        binding.cardActionTerminal.setOnClickListener {
            mainActivity?.navigateToTerminal()
        }

        binding.cardActionEditor.setOnClickListener {
            mainActivity?.navigateToEditor()
        }

        binding.cardActionPackages.setOnClickListener {
            mainActivity?.showFragment(PackagesFragment(), "packages")
        }

        binding.cardActionGit.setOnClickListener {
            mainActivity?.showFragment(GitFragment(), "git")
        }

        binding.btnSeeAllProjects.setOnClickListener {
            mainActivity?.showFragment(ProjectsFragment(), "projects")
        }

        binding.btnOpenSettings.setOnClickListener {
            mainActivity?.showFragment(SettingsFragment(), "settings")
        }
    }

    private fun loadSystemMetrics() {
        val appContext = context?.applicationContext ?: NexusApplication.instance
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            try {
                // RAM info
                val actManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memInfo = ActivityManager.MemoryInfo()
                actManager.getMemoryInfo(memInfo)

                val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
                val availRamGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
                val usedRamGb = totalRamGb - availRamGb
                val ramText = String.format(Locale.US, "%.1f / %.1f GB", usedRamGb, totalRamGb)

                // Storage info
                val stat = StatFs(Environment.getDataDirectory().path)
                val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
                val freeStorageGb = bytesAvailable / (1024.0 * 1024.0 * 1024.0)
                val storageText = String.format(Locale.US, "%.1f GB Livres", freeStorageGb)

                // Python version
                val pyVersion = NexusApplication.instance.pythonEngine.getPythonVersion()

                withContext(Dispatchers.Main) {
                    _binding?.let { b ->
                        b.tvRamUsage.text = ramText
                        b.tvStorageUsage.text = storageText
                        b.tvPythonVersion.text = pyVersion
                        b.tvKernelInfo.text = "Android ${Build.VERSION.RELEASE} • ${Build.SUPPORTED_ABIS.firstOrNull() ?: "aarch64"}"
                    }
                }
            } catch (e: Exception) {
                // Ignore any metric read error gracefully
            }
        }
    }

    private fun loadProjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            val projects = NexusApplication.instance.projectManager.listProjects()
            _binding?.let {
                projectsAdapter.updateProjects(projects.take(4))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
