package com.nexus.terminal.ui.packages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.core.python.PythonPackage
import com.nexus.terminal.databinding.FragmentPackagesBinding
import kotlinx.coroutines.launch

class PackagesFragment : Fragment() {

    private var _binding: FragmentPackagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var packagesAdapter: PackagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPackagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchAndInstall()
        loadInstalledPackages()
    }

    private fun setupRecyclerView() {
        packagesAdapter = PackagesAdapter(
            onActionClick = { pkg ->
                if (pkg.isInstalled) {
                    uninstallPackage(pkg.name)
                } else {
                    installPackage(pkg.name)
                }
            }
        )
        binding.rvPackages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPackages.adapter = packagesAdapter
    }

    private fun setupSearchAndInstall() {
        binding.btnPipInstall.setOnClickListener {
            val query = binding.etPackageName.text.toString().trim()
            if (query.isNotEmpty()) {
                installPackage(query)
            }
        }
    }

    private fun installPackage(name: String) {
        _binding?.let { b ->
            b.tvPipLog.text = ""
            b.btnPipInstall.isEnabled = false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val success = NexusApplication.instance.pipManager.installPackage(name) { line ->
                activity?.runOnUiThread {
                    _binding?.tvPipLog?.append(line)
                }
            }
            _binding?.btnPipInstall?.isEnabled = true
            if (success) {
                context?.let { ctx ->
                    Toast.makeText(ctx, "Pacote $name instalado!", Toast.LENGTH_SHORT).show()
                }
                _binding?.etPackageName?.text?.clear()
                loadInstalledPackages()
            }
        }
    }

    private fun uninstallPackage(name: String) {
        _binding?.tvPipLog?.text = ""
        viewLifecycleOwner.lifecycleScope.launch {
            val success = NexusApplication.instance.pipManager.uninstallPackage(name) { line ->
                activity?.runOnUiThread {
                    _binding?.tvPipLog?.append(line)
                }
            }
            if (success) {
                context?.let { ctx ->
                    Toast.makeText(ctx, "Pacote $name removido!", Toast.LENGTH_SHORT).show()
                }
                loadInstalledPackages()
            }
        }
    }

    private fun loadInstalledPackages() {
        viewLifecycleOwner.lifecycleScope.launch {
            val installed = NexusApplication.instance.pipManager.listInstalledPackages()
            _binding?.let {
                packagesAdapter.updatePackages(installed)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
