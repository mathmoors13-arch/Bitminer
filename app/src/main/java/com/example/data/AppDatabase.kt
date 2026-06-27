package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        MiningContract::class,
        TransactionLog::class,
        ReferralUser::class,
        BitcoinWallet::class,
        AppConfig::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun miningContractDao(): MiningContractDao
    abstract fun transactionLogDao(): TransactionLogDao
    abstract fun referralUserDao(): ReferralUserDao
    abstract fun bitcoinWalletDao(): BitcoinWalletDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bitvault_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        private suspend fun populateDatabase(db: AppDatabase) {
            // Initial Config
            db.appConfigDao().insertConfig(
                AppConfig(
                    isMiningActive = true,
                    currentHashrateMultiplier = 1.0,
                    calcHashrateTh = 142.0,
                    calcPowerWatts = 3200.0,
                    calcElectricCostKwh = 0.12
                )
            )

            // Initial User Profile: Matty Moors
            db.userProfileDao().insertOrUpdateProfile(
                UserProfile(
                    id = 1,
                    userName = "Matty Moors",
                    userEmail = "demo@minevault.app",
                    currentPlan = "Pro",
                    walletAddress = "bc1q7ywp3g7l9slw5t359sm44766gslws2lqsw2lq9",
                    walletType = "Bitcoin (BTC)"
                )
            )

            // Initial Secure Wallet
            db.bitcoinWalletDao().insertWallet(
                BitcoinWallet(
                    id = 1,
                    address = "bc1q7ywp3g7l9slw5t359sm44766gslws2lqsw2lq9",
                    secureKeyHex = "f3a8b23c899c32101e9900223fa0f91ab81d9f8ce3da6213569ee9a3d41bc99a",
                    seedWords = "orbit radar nuclear typical expand genuine segment solid track robust unique visual",
                    balanceBtc = 0.01842,
                    label = "BitVault Hoofdwallet ⛏",
                    isImported = false
                )
            )

            // Initial Mining Contracts
            db.miningContractDao().insertContract(
                MiningContract(name = "Contract #1 — BTC Basic", hashrate = 50.0, expiresAt = "21 dec 2026", isActive = true)
            )
            db.miningContractDao().insertContract(
                MiningContract(name = "Contract #2 — ETH Pro", hashrate = 92.0, expiresAt = "21 mrt 2027", isActive = true)
            )

            // Initial Mock Referrals
            db.referralUserDao().insertReferral(
                ReferralUser(name = "Alex V.", joinedAt = "10 jun 2026", amountEarned = 14.40, status = "Actief")
            )
            db.referralUserDao().insertReferral(
                ReferralUser(name = "Sara M.", joinedAt = "5 jun 2026", amountEarned = 22.80, status = "Actief")
            )
            db.referralUserDao().insertReferral(
                ReferralUser(name = "Tom B.", joinedAt = "1 jun 2026", amountEarned = 0.0, status = "Pending")
            )

            // Initial Transactions
            db.transactionLogDao().insertTransaction(
                TransactionLog(title = "Mining uitbetaling", sub = "Contract #1 — BTC", amount = 18.40, type = "MINING")
            )
            db.transactionLogDao().insertTransaction(
                TransactionLog(title = "Referral bonus", sub = "Via jouw uitnodiging", amount = 4.20, type = "REFERRAL")
            )
            db.transactionLogDao().insertTransaction(
                TransactionLog(title = "Lidmaatschap betaald", sub = "Plan Pro — bankoverschrijving", amount = -29.99, type = "MEMBERSHIP")
            )
        }
    }
}
