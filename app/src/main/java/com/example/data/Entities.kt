package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val userName: String,
    val userEmail: String,
    val currentPlan: String,
    val walletAddress: String,
    val walletType: String,
    val totalBalanceOffset: Double = 0.0 // To track custom earnings or deposits added/withdrawn
)

@Entity(tableName = "mining_contracts")
data class MiningContract(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val hashrate: Double,
    val expiresAt: String,
    val isActive: Boolean
)

@Entity(tableName = "transaction_logs")
data class TransactionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val sub: String,
    val amount: Double,
    val type: String, // "MINING", "REFERRAL", "MEMBERSHIP", "DEPOSIT", "WITHDRAWAL", "SEND", "RECEIVE"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "referral_users")
data class ReferralUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val joinedAt: String,
    val amountEarned: Double,
    val status: String // "Actief", "Pending"
)

@Entity(tableName = "bitcoin_wallets")
data class BitcoinWallet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val address: String,
    val secureKeyHex: String, // Simulated encrypted private key hex
    val seedWords: String,    // 12 words secret backup pattern
    val balanceBtc: Double,
    val label: String,
    val isImported: Boolean
)

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val isMiningActive: Boolean = true,
    val currentHashrateMultiplier: Double = 1.0,
    val calcHashrateTh: Double = 100.0,     // In TH/s (ASIC scale)
    val calcPowerWatts: Double = 2200.0,    // In Watts
    val calcElectricCostKwh: Double = 0.12  // Cost in $/kWh
)
