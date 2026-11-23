package com.nexus.terminal.ui.packages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexus.terminal.R
import com.nexus.terminal.core.python.PythonPackage
import com.nexus.terminal.databinding.ItemPackageBinding

class PackagesAdapter(
    private var packages: List<PythonPackage> = emptyList(),
    private val onActionClick: (PythonPackage) -> Unit
) : RecyclerView.Adapter<PackagesAdapter.ViewHolder>() {

    fun updatePackages(newPackages: List<PythonPackage>) {
        packages = newPackages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPackageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(packages[position])
    }

    override fun getItemCount(): Int = packages.size

    inner class ViewHolder(private val binding: ItemPackageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pkg: PythonPackage) {
            binding.tvPkgName.text = pkg.name
            binding.tvPkgVersion.text = "v${pkg.version} • ${pkg.summary}"

            if (pkg.isInstalled) {
                binding.btnPkgAction.text = "Desinstalar"
                binding.btnPkgAction.setTextColor(binding.root.context.resources.getColor(R.color.accent_crimson, null))
            } else {
                binding.btnPkgAction.text = "Instalar"
                binding.btnPkgAction.setTextColor(binding.root.context.resources.getColor(R.color.accent_emerald, null))
            }

            binding.btnPkgAction.setOnClickListener {
                onActionClick(pkg)
            }
        }
    }
}
