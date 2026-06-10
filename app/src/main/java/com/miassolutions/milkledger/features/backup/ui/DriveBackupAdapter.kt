package com.miassolutions.milkledger.features.backup.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemDriveBackupBinding
import com.miassolutions.milkledger.features.backup.drive.DriveBackupFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DriveBackupAdapter(
    private val onRestoreClicked: (DriveBackupFile) -> Unit
) : ListAdapter<DriveBackupFile, DriveBackupAdapter.BackupViewHolder>(DiffCallback) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val binding = ItemDriveBackupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BackupViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class BackupViewHolder(
        private val binding: ItemDriveBackupBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: DriveBackupFile) {
            binding.tvFileName.text = item.name
            
            val dateText = item.modifiedTimeMillis?.let {
                formatDateTime(it)
            } ?: "Unknown date"
            
            val sizeText = item.sizeBytes?.let {
                formatFileSize(it)
            } ?: "Unknown size"
            
            binding.tvFileInfo.text = "$dateText\n$sizeText"
            
            binding.btnRestore.setOnClickListener {
                onRestoreClicked(item)
            }
        }
    }
    
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DriveBackupFile>() {
            override fun areItemsTheSame(
                oldItem: DriveBackupFile,
                newItem: DriveBackupFile
            ): Boolean {
                return oldItem.fileId == newItem.fileId
            }
            
            override fun areContentsTheSame(
                oldItem: DriveBackupFile,
                newItem: DriveBackupFile
            ): Boolean {
                return oldItem == newItem
            }
        }
        
        private fun formatDateTime(timeMillis: Long): String {
            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            return formatter.format(Date(timeMillis))
        }
        
        private fun formatFileSize(bytes: Long): String {
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            
            return if (mb >= 1) {
                String.format(Locale.getDefault(), "%.2f MB", mb)
            } else {
                String.format(Locale.getDefault(), "%.0f KB", kb)
            }
        }
    }
}