package com.nexus.terminal.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexus.terminal.core.project.ProjectModel
import com.nexus.terminal.databinding.ItemProjectBinding

class DashboardProjectsAdapter(
    private var projects: List<ProjectModel> = emptyList(),
    private val onProjectClick: (ProjectModel) -> Unit,
    private val onRunClick: (ProjectModel) -> Unit
) : RecyclerView.Adapter<DashboardProjectsAdapter.ViewHolder>() {

    fun updateProjects(newProjects: List<ProjectModel>) {
        projects = newProjects
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(projects[position])
    }

    override fun getItemCount(): Int = projects.size

    inner class ViewHolder(private val binding: ItemProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(project: ProjectModel) {
            binding.tvProjectName.text = project.name
            binding.tvProjectTemplateBadge.text = project.templateType.displayName
            binding.tvProjectDescription.text = if (project.description.isNotEmpty()) {
                project.description
            } else {
                "Ponto de entrada: ${project.entryPoint}"
            }
            binding.tvProjectMeta.text = "${project.entryPoint} • Nexus Station"

            binding.root.setOnClickListener {
                onProjectClick(project)
            }

            binding.btnProjectRun.setOnClickListener {
                onRunClick(project)
            }
        }
    }
}
