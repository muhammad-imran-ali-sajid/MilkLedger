package com.miassolutions.milkledger.features.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemAccountBinding

data class Item(
    val id: Int,
    val title: String,
    val type: ItemType
)

enum class ItemType {
    FIRST, SECOND
}

data class AccountUi(
    val id: Int,
    val title: String,
    val typeLabel: String,
    @DrawableRes val backgroundRes: Int
)

fun Item.toUi(): AccountUi {
    return AccountUi(
        id = id,
        title = title,
        typeLabel = type.name,
        backgroundRes = when (type) {
            ItemType.FIRST -> R.drawable.bg_expense
            ItemType.SECOND -> R.drawable.bg_sale
        }
    )
}


class AccountListAdapter(private val onItemClick: (String) -> Unit) :
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
            tvAccountName.text = item.title
            tvAccountType.text = item.typeLabel

            root.setBackgroundResource(item.backgroundRes)
            root.setOnClickListener {
                onItemClick(item.title)
            }


        }
    }
}