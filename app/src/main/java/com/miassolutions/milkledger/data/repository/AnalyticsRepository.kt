package com.miassolutions.milkledger.data.repository

import com.miassolutions.milkledger.core.extensions.toMillis
import com.miassolutions.milkledger.data.local.daos.TransactionDao
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.presentation.expenses.data.toDomain
import com.miassolutions.milkledger.domain.model.Transaction
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@Singleton
class AnalyticsRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    // -------------------------
    // CORE PROFIT
    // -------------------------

    fun getNetProfitBetween(start: LocalDate, end: LocalDate): Flow<Double> =
        transactionDao.sumProfitBetween(
            startMillis = start.toMillis(),
            endMillis = end.toMillis()
        )

    fun getNetProfitDaily(date: LocalDate): Flow<Double> =
        transactionDao.sumProfitForDate(date.toMillis())

    fun getNetProfitAll(): Flow<Double> =
        transactionDao.sumAllProfit()

    // -------------------------
    // OPTIONAL: RAW LEDGER (DEBUG / ADMIN)
    // -------------------------

    fun getLedgerBetween(start: LocalDate, end: LocalDate): Flow<List<Transaction>> =
        transactionDao.getBetween(
            start.toMillis(),
            end.toMillis()
        ).map { list -> list.map { it.toDomain() } }
}


