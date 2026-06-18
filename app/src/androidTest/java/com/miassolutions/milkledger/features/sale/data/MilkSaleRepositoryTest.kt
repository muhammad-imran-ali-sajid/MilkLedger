package com.miassolutions.milkledger.features.sale.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.core.localdb.milk.TransactionType
import com.miassolutions.milkledger.features.backup.data.BackupFileManager
import com.miassolutions.milkledger.features.backup.data.BackupPrefs
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.backup.data.BackupValidator
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveBackupDataSource
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveServiceFactory
import com.miassolutions.milkledger.features.sale.model.UpdateSaleRequest
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MilkSaleRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupPrefs: BackupPrefs
    private lateinit var repository: MilkSaleRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        backupPrefs = BackupPrefs(context)

        val backupRepository = BackupRepository(
            database = database,
            backupFileManager = BackupFileManager(context),
            backupValidator = BackupValidator(),
            googleDriveBackupDataSource = GoogleDriveBackupDataSource(
                GoogleDriveServiceFactory(context)
            ),
            backupPrefs = backupPrefs
        )

        repository = MilkSaleRepository(
            milkDao = database.milkDao(),
            ledgerDao = database.ledgerDao(),
            accountDao = database.accountDao(),
            backupRepository = backupRepository,
            db = database
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveMilkSale_createsMilkTransactionSaleLedgerAndCashReceivedLedger() = runBlocking {
        seedCustomer()

        val saleDate = LocalDate.of(2026, 1, 15)
        repository.saveMilkSale(
            saleDate = saleDate,
            paymentDate = saleDate,
            accountId = CUSTOMER_ID,
            volume = 10.0,
            deduction = 1.5,
            rate = 200.0,
            amountPaid = 50_000L,
            note = "Morning sale"
        )

        val milk = database.milkDao().getAllMilkTransactionsForBackup().single()
        assertEquals(CUSTOMER_ID, milk.accountId)
        assertEquals(saleDate.toMillis(), milk.dateMillis)
        assertEquals(saleDate.toMillis(), milk.paymentDateMillis)
        assertEquals(TransactionType.SALE, milk.type)
        assertEquals(10.0, milk.volume, 0.0)
        assertEquals(1.5, milk.deduction, 0.0)
        assertEquals(8.5, milk.quantity, 0.0)
        assertEquals(200.0, milk.rateUsed, 0.0)
        assertEquals(170_000L, milk.totalAmount)
        assertEquals("Morning sale", milk.notes)

        val ledgers = database.ledgerDao().getAllLedgerEntriesForBackup()
        assertEquals(2, ledgers.size)

        val saleLedger = ledgers.single { it.type == LedgerEntryType.MILK_SALE }
        assertEquals(milk.milkTransId, saleLedger.referenceId)
        assertEquals(CUSTOMER_ID, saleLedger.accountId)
        assertEquals(saleDate.toMillis(), saleLedger.dateMillis)
        assertEquals(170_000L, saleLedger.debit)
        assertEquals(0L, saleLedger.credit)
        assertEquals(170_000L, saleLedger.profitImpact)
        assertEquals("Sale: 10.0 - 1.5 = 8.5 L", saleLedger.note)

        val paymentLedger = ledgers.single { it.type == LedgerEntryType.CASH_RECEIVED }
        assertEquals(milk.milkTransId, paymentLedger.referenceId)
        assertEquals(CUSTOMER_ID, paymentLedger.accountId)
        assertEquals(saleDate.toMillis(), paymentLedger.dateMillis)
        assertEquals(0L, paymentLedger.debit)
        assertEquals(50_000L, paymentLedger.credit)
        assertEquals(0L, paymentLedger.profitImpact)
        assertEquals(CUSTOMER_NAME, paymentLedger.note)

        assertTrue(backupPrefs.getLastDataChangedAt() > 0L)
    }

    @Test
    fun updateMilkSale_updatesMilkTransactionAndRelatedLedgers() = runBlocking {
        seedCustomer()

        val originalDate = LocalDate.of(2026, 1, 15)
        repository.saveMilkSale(
            saleDate = originalDate,
            paymentDate = originalDate,
            accountId = CUSTOMER_ID,
            volume = 10.0,
            deduction = 1.0,
            rate = 100.0,
            amountPaid = 10_000L,
            note = "Original sale"
        )

        val saleId = database.milkDao().getAllMilkTransactionsForBackup().single().milkTransId
        val updatedDate = LocalDate.of(2026, 1, 16)
        val paymentDate = LocalDate.of(2026, 1, 17)

        repository.updateMilkSale(
            UpdateSaleRequest(
                saleId = saleId,
                accountId = CUSTOMER_ID,
                date = updatedDate,
                paymentDate = paymentDate,
                volume = 12.0,
                deduction = 2.0,
                rate = 150.0,
                amountPaid = 20_000L,
                note = "Updated sale"
            )
        )

        val milk = database.milkDao().getAllMilkTransactionsForBackup().single()
        assertEquals(saleId, milk.milkTransId)
        assertEquals(updatedDate.toMillis(), milk.dateMillis)
        assertEquals(paymentDate.toMillis(), milk.paymentDateMillis)
        assertEquals(12.0, milk.volume, 0.0)
        assertEquals(2.0, milk.deduction, 0.0)
        assertEquals(10.0, milk.quantity, 0.0)
        assertEquals(150.0, milk.rateUsed, 0.0)
        assertEquals(150_000L, milk.totalAmount)
        assertEquals("Updated sale", milk.notes)

        val ledgers = database.ledgerDao().getAllLedgerEntriesForBackup()
        assertEquals(2, ledgers.size)

        val saleLedger = ledgers.single { it.type == LedgerEntryType.MILK_SALE }
        assertEquals(updatedDate.toMillis(), saleLedger.dateMillis)
        assertEquals(150_000L, saleLedger.debit)
        assertEquals(0L, saleLedger.credit)
        assertEquals(150_000L, saleLedger.profitImpact)
        assertEquals("Sale: 12.0 - 2.0 = 10.0 L", saleLedger.note)

        val paymentLedger = ledgers.single { it.type == LedgerEntryType.CASH_RECEIVED }
        assertEquals(updatedDate.toMillis(), paymentLedger.dateMillis)
        assertEquals(0L, paymentLedger.debit)
        assertEquals(20_000L, paymentLedger.credit)
        assertEquals(0L, paymentLedger.profitImpact)
        assertEquals("$CUSTOMER_NAME\n(Dated: 17 Jan 2026)", paymentLedger.note)
    }

    @Test
    fun deleteSale_softDeletesMilkTransactionAndRelatedLedgers() = runBlocking {
        seedCustomer()

        repository.saveMilkSale(
            saleDate = LocalDate.of(2026, 1, 15),
            paymentDate = LocalDate.of(2026, 1, 15),
            accountId = CUSTOMER_ID,
            volume = 10.0,
            deduction = 1.0,
            rate = 100.0,
            amountPaid = 10_000L,
            note = "Sale to delete"
        )

        val saleId = database.milkDao().getAllMilkTransactionsForBackup().single().milkTransId
        repository.deleteSale(saleId)

        val milk = database.milkDao().getAllMilkTransactionsForBackup().single()
        assertNotNull(milk.deletedAtMillis)

        val ledgers = database.ledgerDao().getAllLedgerEntriesForBackup()
        assertEquals(2, ledgers.size)
        ledgers.forEach { ledger ->
            assertEquals(saleId, ledger.referenceId)
            assertNotNull(ledger.deletedAtMillis)
        }
    }

    private suspend fun seedCustomer() {
        database.accountDao().insert(
            AccountEntity(
                accountId = CUSTOMER_ID,
                name = CUSTOMER_NAME,
                phone = null,
                accountType = AccountType.CUSTOMER,
                sortOrder = 1,
                advanceAmount = 0L,
                defaultRate = 200.0,
                initialBalance = 0L,
                createdAtMillis = LocalDate.of(2026, 1, 1).toMillis()
            )
        )
    }

    private companion object {
        const val CUSTOMER_ID = "customer-1"
        const val CUSTOMER_NAME = "Ali Milk Shop"
    }
}
