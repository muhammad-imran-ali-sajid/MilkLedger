package com.miassolutions.milkledger.features.account.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.databinding.ItemAccountBinding
import com.miassolutions.milkledger.features.account.model.AccountUi
import com.miassolutions.milkledger.utils.extensions.setBalanceColorWithRoundRupee
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat


class AccountListAdapter(
    private val onEditClick: (String) -> Unit,
    private val onNavClick: (id: String, name: String, type: String) -> Unit
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


            // 1. Name & Subtitle
            tvName.text = item.name

            // 2. Initial Letter
            val initial = item.name.firstOrNull()?.toString()?.uppercase() ?: "?"
            tvInitial.text = initial


            // 3. Color Logic (Customer vs Supplier)
            val isCustomer = item.type == AccountType.CUSTOMER

            tvSubtitle.text = "Since: ${item.openingDate.toCompleteDateFormat()}"

            if (isCustomer) {
                viewIndicator.setBackgroundResource(R.color.teal_700)
                layoutIcon.background.setTint(
                    ContextCompat.getColor(
                        root.context,
                        R.color.teal_700
                    )
                )
            } else {
                viewIndicator.setBackgroundResource(R.color.orange_700)
                layoutIcon.background.setTint(
                    ContextCompat.getColor(
                        root.context,
                        R.color.orange_700
                    )
                )
            }

            root.setOnLongClickListener {
                onEditClick(item.id)
                true
            }

            // --- 🔥 FIXED BALANCE LOGIC ---
            val rawBalance = item.initialBalance ?: 0L

            // Logic:
            // Customer: Positive (+) = Lena hai (Asset) -> Green
            // Supplier: Positive (+) = Dena hai (Liability) -> Isay Negative (-) bana den taake Red ho jaye

            val displayBalance = if (item.type == AccountType.SUPPLIER) {
                -rawBalance
            } else {
                rawBalance
            }

            tvBalance.setBalanceColorWithRoundRupee(displayBalance)

            tvRate.text = item.defaultRate.toString()

            root.setOnClickListener {
                onNavClick(item.id, item.name, item.type.name)
            }

        }
    }
}