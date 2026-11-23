package com.nexus.terminal.ui.projects

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexus.terminal.MainActivity
import com.nexus.terminal.NexusApplication
import com.nexus.terminal.core.project.ProjectModel
import com.nexus.terminal.core.project.ProjectTemplateType
import com.nexus.terminal.databinding.FragmentProjectsBinding
import kotlinx.coroutines.launch
import java.io.File

class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    private lateinit var projectsAdapter: ProjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupNewProjectButton()
        loadProjects()
    }

    private fun setupRecyclerView() {
        projectsAdapter = ProjectsAdapter(
            onProjectClick = { project ->
                val entry = File(project.path, project.entryPoint)
                (activity as? MainActivity)?.navigateToEditor(entry)
            },
            onRunClick = { project ->
                val entry = File(project.path, project.entryPoint)
                (activity as? MainActivity)?.navigateToEditor(entry)
            },
            onDeleteClick = { project ->
                showDeleteDialog(project)
            }
        )
        binding.rvProjectsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProjectsList.adapter = projectsAdapter
    }

    private fun setupNewProjectButton() {
        binding.btnNewProject.setOnClickListener {
            showCreateProjectDialog()
        }
    }

    private fun showCreateProjectDialog() {
        val templates = ProjectTemplateType.values()
        val templateNames = templates.map { it.displayName }.toTypedArray()
        var selectedTemplate = templates[0]

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(requireContext()).apply {
            hint = "Nome do projeto (ex: MeuBot)"
            setSingleLine()
        }
        val etDesc = EditText(requireContext()).apply {
            hint = "Descrição breve (opcional)"
            setSingleLine()
        }

        layout.addView(etName)
        layout.addView(etDesc)

        AlertDialog.Builder(requireContext())
            .setTitle("Novo Projeto Nexus")
            .setView(layout)
            .setSingleChoiceItems(templateNames, 0) { _, which ->
                selectedTemplate = templates[which]
            }
            .setPositiveButton("Criar") { _, _ ->
                val name = etName.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val newProj = NexusApplication.instance.projectManager.createProject(
                            name = name,
                            template = selectedTemplate,
                            description = desc
                        )
                        loadProjects()
                        Toast.makeText(requireContext(), "Projeto '${newProj.name}' criado!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteDialog(project: ProjectModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir projeto '${project.name}'?")
            .setMessage("Todos os arquivos do projeto serão apagados permanentemente.")
            .setPositiveButton("Excluir") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    NexusApplication.instance.projectManager.deleteProject(project)
                    loadProjects()
                    Toast.makeText(requireContext(), "Projeto excluído", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadProjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            val list = NexusApplication.instance.projectManager.listProjects()
            projectsAdapter.updateProjects(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
