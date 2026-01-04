package com.miassolutions.milkledger.features.account.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemAccountBinding
import com.miassolutions.milkledger.features.account.model.AccountUi


class AccountListAdapter(
    private val onEditClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) :
    ListAdapter<AccountUi, AccountListAdapter.AccountVH>(Diff) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AccountVH {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountVH(binding)
    }

    override fun onBindViewHolder(
        holder: AccountVH,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    object Diff : DiffUtil.ItemCallback<AccountUi>() {
        override fun areItemsTheSame(old: AccountUi, new: AccountUi) = old.id == new.id
        override fun areContentsTheSame(old: AccountUi, new: AccountUi) = old == new
    }

    inner class AccountVH(val binding: ItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AccountUi) = with(binding) {
            tvAccountName.text = item.personName
            tvAccountType.text = item.accountType.name

            root.setBackgroundResource(item.bgDrawable)
            root.setOnClickListener {
                onEditClick(item.id)
            }

            root.setOnLongClickListener {
                onDeleteClick(item.id)
                true
            }


        }
    }
}