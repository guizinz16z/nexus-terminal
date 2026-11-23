package com.nexus.terminal.ui.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexus.terminal.R
import com.nexus.terminal.core.ai.AiMessage
import com.nexus.terminal.databinding.ItemChatAiBinding

class AiChatAdapter(
    private var messages: List<AiMessage> = emptyList()
) : RecyclerView.Adapter<AiChatAdapter.ViewHolder>() {

    fun updateMessages(newMessages: List<AiMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatAiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class ViewHolder(private val binding: ItemChatAiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: AiMessage) {
            val isUser = message.sender.equals("user", ignoreCase = true)
            binding.tvMessageSender.text = if (isUser) "Você" else "Nexus AI"
            binding.tvMessageSender.setTextColor(
                binding.root.context.resources.getColor(
                    if (isUser) R.color.accent_cyan else R.color.accent_violet,
                    null
                )
            )
            binding.tvMessageContent.text = message.content
        }
    }
}
