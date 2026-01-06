package com.miassolutions.milkledger.features.milk.balancehistory

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.features.customer.domain.model.CustomerHistoryUi
import com.miassolutions.milkledger.features.customer.domain.model.HistoryType
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class CustomerHistoryViewModel @Inject constructor(
    private val ledgerDao: LedgerDao // Repository use karna behtar hai, but direct DAO for brevity
) : ViewModel() {

    fun getHistory(accountId: String): Flow<List<CustomerHistoryUi>> {
        return ledgerDao.getLedgerHistory(accountId).map { entities ->
            entities.map { entity ->
                // Map Entity -> UI Model
                CustomerHistoryUi(
                    date = entity.dateMillis.toLocalDate(),
                    description = entity.note ?: entity.type.name,
                    // Agar Debit hai (Sale) to Amount, warna Credit (Payment)
                    amount = if (entity.debit > 0) entity.debit else entity.credit,
                    type = if (entity.debit > 0) HistoryType.DEBIT else HistoryType.CREDIT
                )
            }
        }
    }
}