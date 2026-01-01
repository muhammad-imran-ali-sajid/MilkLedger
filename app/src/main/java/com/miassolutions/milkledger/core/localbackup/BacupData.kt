package com.miassolutions.milkledger.core.localbackup

import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.local.entities.TransactionEntity

data class BackupData(
    val customers: List<CustomerEntity> ,
    val suppliers: List<SupplierEntity> ,
    val purchases: List<PurchaseEntity>,
    val sales: List<SalesEntity>,
    val expenses: List<ExpensesEntity>,
    val notes: List<NoteEntity>,
    val transactions: List<TransactionEntity>
)
