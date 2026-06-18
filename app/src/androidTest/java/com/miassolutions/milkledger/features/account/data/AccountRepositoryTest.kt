package com.miassolutions.milkledger.features.account.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.account.domain.usecase.DeleteAccountResult
import com.miassolutions.milkledger.features.account.domain.usecase.DeleteAccountUseCase
import com.miassolutions.milkledger.features.backup.data.BackupFileManager
import com.miassolutions.milkledger.features.backup.data.BackupPrefs
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.backup.data.BackupValidator
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveBackupDataSource
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveServiceFactory
import com.miassolutions.milkledger.utils.extensions.toMillis
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class AccountRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupPrefs: BackupPrefs
    private lateinit var repository: AccountRepository
    private lateinit var deleteAccount: DeleteAccountUseCase

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

        repository = AccountRepository(
            accountDao = database.accountDao(),
            ledgerDao = database.ledgerDao(),
            backupRepository = backupRepository,
            db = database
        )
        deleteAccount = DeleteAccountUseCase(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveCustomerWithPositiveOpeningBalance_createsOpeningBalanceDebit() = runBlocking {
        val openingDate = LocalDate.of(2026, 1, 1)

        repository.saveAccount(
            account = account(
                id = CUSTOMER_ID,
                name = "Ali Customer",
                type = AccountType.CUSTOMER,
                initialBalance = 25_000L
            ),
            openingDate = openingDate
        )

        val savedAccount = database.accountDao().getAccountById(CUSTOMER_ID)
        assertNotNull(savedAccount)
        assertEquals("Ali Customer", savedAccount?.name)
        assertEquals(AccountType.CUSTOMER, savedAccount?.accountType)
        assertEquals(25_000L, savedAccount?.initialBalance)

        val ledger = database.ledgerDao().getAllLedgerEntriesForBackup().single()
        assertEquals(CUSTOMER_ID, ledger.accountId)
        assertEquals(CUSTOMER_ID, ledger.referenceId)
        assertEquals(openingDate.toMillis(), ledger.dateMillis)
        assertEquals(LedgerEntryType.OPENING_BALANCE, ledger.type)
        assertEquals(25_000L, ledger.debit)
        assertEquals(0L, ledger.credit)
        assertEquals(0L, ledger.profitImpact)
        assertEquals("Opening Balance", ledger.note)

        assertEquals(25_000L, repository.getCurrentBalance(CUSTOMER_ID))
        assertTrue(backupPrefs.getLastDataChangedAt() > 0L)
    }

    @Test
    fun saveSupplierWithPositiveOpeningBalance_createsOpeningBalanceCredit() = runBlocking {
        val openingDate = LocalDate.of(2026, 1, 1)

        repository.saveAccount(
            account = account(
                id = SUPPLIER_ID,
                name = "Bashir Supplier",
                type = AccountType.SUPPLIER,
                initialBalance = 30_000L
            ),
            openingDate = openingDate
        )

        val ledger = database.ledgerDao().getAllLedgerEntriesForBackup().single()
        assertEquals(SUPPLIER_ID, ledger.accountId)
        assertEquals(LedgerEntryType.OPENING_BALANCE, ledger.type)
        assertEquals(0L, ledger.debit)
        assertEquals(30_000L, ledger.credit)
        assertEquals(0L, ledger.profitImpact)

        assertEquals(-30_000L, repository.getCurrentBalance(SUPPLIER_ID))
    }

    @Test
    fun saveAccountAgain_updatesExistingOpeningBalanceLedger() = runBlocking {
        repository.saveAccount(
            account = account(
                id = CUSTOMER_ID,
                name = "Ali Customer",
                type = AccountType.CUSTOMER,
                initialBalance = 25_000L
            ),
            openingDate = LocalDate.of(2026, 1, 1)
        )

        repository.saveAccount(
            account = account(
                id = CUSTOMER_ID,
                name = "Ali Customer Updated",
                type = AccountType.CUSTOMER,
                initialBalance = -5_000L
            ),
            openingDate = LocalDate.of(2026, 1, 5)
        )

        val savedAccount = database.accountDao().getAccountById(CUSTOMER_ID)
        assertEquals("Ali Customer Updated", savedAccount?.name)
        assertEquals(-5_000L, savedAccount?.initialBalance)

        val ledgers = database.ledgerDao().getAllLedgerEntriesForBackup()
        assertEquals(1, ledgers.size)

        val ledger = ledgers.single()
        assertEquals(LocalDate.of(2026, 1, 5).toMillis(), ledger.dateMillis)
        assertEquals(0L, ledger.debit)
        assertEquals(5_000L, ledger.credit)

        assertEquals(-5_000L, repository.getCurrentBalance(CUSTOMER_ID))
    }

    @Test
    fun deleteAccountUseCase_blocksAccountWhenBalanceIsNotZero() = runBlocking {
        repository.saveAccount(
            account = account(
                id = CUSTOMER_ID,
                name = "Ali Customer",
                type = AccountType.CUSTOMER,
                initialBalance = 25_000L
            ),
            openingDate = LocalDate.of(2026, 1, 1)
        )

        val result = deleteAccount(CUSTOMER_ID)

        assertEquals(DeleteAccountResult.BalanceNotZero(25_000L), result)
        assertNull(database.accountDao().getAccountById(CUSTOMER_ID)?.deletedAtMillis)
        assertNull(database.ledgerDao().getAllLedgerEntriesForBackup().single().deletedAtMillis)
    }

    @Test
    fun deleteAccountUseCase_softDeletesAccountAndOpeningBalanceWhenBalanceIsZero() = runBlocking {
        repository.saveAccount(
            account = account(
                id = CUSTOMER_ID,
                name = "Ali Customer",
                type = AccountType.CUSTOMER,
                initialBalance = 0L
            ),
            openingDate = LocalDate.of(2026, 1, 1)
        )

        val result = deleteAccount(CUSTOMER_ID)

        assertEquals(DeleteAccountResult.Deleted, result)
        assertNotNull(database.accountDao().getAccountById(CUSTOMER_ID)?.deletedAtMillis)
        assertNotNull(database.ledgerDao().getAllLedgerEntriesForBackup().single().deletedAtMillis)
    }

    private fun account(
        id: String,
        name: String,
        type: AccountType,
        initialBalance: Long
    ): Account {
        return Account(
            createdDate = LocalDate.of(2026, 1, 1),
            accountId = id,
            name = name,
            phone = null,
            type = type,
            sortOrder = 1,
            defaultRate = 100.0,
            isActive = true,
            currentBalance = null,
            advanceAmount = 0L,
            initialBalance = initialBalance
        )
    }

    private companion object {
        const val CUSTOMER_ID = "customer-account-1"
        const val SUPPLIER_ID = "supplier-account-1"
    }
}
