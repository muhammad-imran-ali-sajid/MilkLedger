package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.ProfitDao
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.data.mapper.toFirestore
import com.miassolutions.milkledger.data.mapper.toProfit
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import com.miassolutions.milkledger.domain.model.Profit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class ProfitRepository @Inject constructor(
    private val dao: ProfitDao,
    private val firestoreSyncHelper: FirestoreSyncHelper,
    private val mDao: AnalyticsRepository
) {


    private companion object {
        private const val TAG = "ProfitRepository"
        private const val PROFIT_COLLECTION = "profits"
    }

    // --------------------------------------------------------------------
    // CRUD
    // --------------------------------------------------------------------

    suspend fun upsertProfit(profitEntity: ProfitEntity) = dao.upsert(profitEntity)


    suspend fun saveProfit(profitDouble: Double) {

        val entity = ProfitEntity(
            netProfit = profitDouble,
            updatedAt = LocalDateTime.now().toString(),
        )

        dao.upsert(entity)

    }

    suspend fun upsert(profit: ProfitEntity) {

        dao.upsert(profit)
        try {
            val firestoreModel = profit.toFirestore()
            firestoreSyncHelper.uploadSingle(
                collectionName = PROFIT_COLLECTION,
                documentId = profit.profitId,
                data = firestoreModel
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync insertOrUpdate for profit ID: ${profit.profitId}", e)
        }

    }

    suspend fun deleteProfit(profitId: String) {

        dao.deleteProfit(profitId)

        try {
            firestoreSyncHelper.deleteDocument(
                collectionName = PROFIT_COLLECTION,
                documentId = profitId,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete for profit ID: ${profitId}", e)
        }
    }


    suspend fun synchronizationProfits() {
        Log.d(TAG, "Starting full profits synchronization...")

        try {
            val remoteProfits =
                firestoreSyncHelper.downloadCollection<ProfitEntity>(PROFIT_COLLECTION)
            if (remoteProfits.isNotEmpty()) {

                dao.upsertAll(remoteProfits)
                Log.d(TAG, "Downloaded and merged from the firestore")
            } else {
                Log.d(TAG, "No remote item found")
            }

            val allLocalProfits = dao.getAll()
            if (allLocalProfits.isNotEmpty()) {
                firestoreSyncHelper.uploadCollection(
                    collectionName = PROFIT_COLLECTION,
                    dataList = allLocalProfits,
                    idExtractor = { it.profitId }
                )
                Log.d(TAG, "Uploaded All profits")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Full note sync failed ${e.localizedMessage}", e)
        }
    }

    suspend fun upsertAll(profitList: List<ProfitEntity>) {

        dao.upsertAll(profitList)


    }

    suspend fun getProfitById(id: String): ProfitEntity? = dao.getProfitById(id)

    fun getAllProfitList(): Flow<List<ProfitEntity>> = dao.getAllProfitFlow()

     fun getAllNetProfit(): Flow<List<ProfitEntity>> = dao.getAllFlow()


    // --------------------------------------------------------------------
    // LIST FILTERS
    // --------------------------------------------------------------------

    fun getDaily(date: LocalDate): Flow<List<ProfitEntity>> =
        dao.getDaily(date)


//    fun getDailyProfit(date: LocalDate): Flow<List<Profit>> = combine(
//        dao.getDailyFlow(date),
//        mDao.getProfitToday(date)
//    ) { a, b ->
//        val x = a
//        val y = b
//
//
//
//    }

    fun getWeekly(start: LocalDate, end: LocalDate): Flow<List<ProfitEntity>> =
        dao.getBetweenFlow(start, end)


    suspend fun getWeeklyWithNetProfit(start: LocalDate, end: LocalDate): List<Profit> {
        val np = mDao.getProfitBetween(start, end).first()
        val rp = dao.getBetween(start, end).map { it.toProfit().copy(netProfit = np) }

        return buildList {
            addAll(rp)
        }
    }

    suspend fun getMonthly(date: LocalDate): Flow<List<ProfitEntity>> {
        val ym = "${date.year}-${"%02d".format(date.monthValue)}"
        return dao.getMonthly(ym)
    }

    suspend fun getYearly(date: LocalDate): Flow<List<ProfitEntity>> =
        dao.getYearly(date.year.toString())

    suspend fun getCustom(start: LocalDate, end: LocalDate): List<ProfitEntity> =
        dao.getBetween(start, end)


    // --------------------------------------------------------------------
    // NET PROFIT CALCULATIONS (Sales - Purchases - Expenses)
    // --------------------------------------------------------------------

    fun getNetBusinessProfit(): Flow<Double> = mDao.getBusinessProfitAll()

    fun getNetProfitAfterPersonal() : Flow<Double> = mDao.getProfitAllAfterPersonal()

    fun getProfitToday(date: LocalDate): Flow<Double?> = mDao.getProfitToday(date)

    fun getProfitAfterPersonalExpenses(date: LocalDate): Flow<Double?> =
        mDao.getProfitAfterPersonalExpensesToday(date)

    fun getProfitAfterPersonalExpensesBetween(start: LocalDate, end: LocalDate): Flow<Double?> =
        mDao.getProfitAfterPersonalExpensesBetween(start, end)

    suspend fun getNetProfitOnce(): Double = getNetBusinessProfit().firstOrZero()

    // Daily
    fun getNetBusinessProfitDaily(date: LocalDate): Flow<Double> =
        combine(
            mDao.getTotalSalesDaily(date),
            mDao.getTotalPurchasesDaily(date),
            mDao.getTotalBusinessExpensesDaily(date),
        ) { s, p, e ->

            val totalSales = s ?: 0.0
            val totalPurchases = p ?: 0.0
            val totalExpenses = e ?: 0.0

            totalSales - (totalPurchases + totalExpenses)
        }

    // Weekly / Custom
    suspend fun getNetProfitWeekly(start: LocalDate, end: LocalDate): Double {
        val sales = mDao.getTotalSalesBetween(start, end).firstOrZero()
        val purchases = mDao.getTotalPurchasesBetween(start, end).firstOrZero()
        val expenses = mDao.getTotalExpensesBetween(start, end).firstOrZero()
        return sales - (purchases + expenses)
    }

    suspend fun getNetProfitCustom(start: LocalDate, end: LocalDate): Double =
        getNetProfitWeekly(start, end) // Same logic

    // Monthly
    suspend fun getNetProfitMonthly(year: Int, month: Int): Double {
        val start = LocalDate.of(year, month, 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())
        return getNetProfitWeekly(start, end)
    }

    // Yearly
    suspend fun getNetProfitYearly(year: Int): Double {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        return getNetProfitWeekly(start, end)
    }


    // --------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------

    private suspend fun Flow<Double?>.firstOrZero(): Double {
        return this.firstOrNull() ?: 0.0
    }


}
