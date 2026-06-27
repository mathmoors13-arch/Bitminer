package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)
}

@Dao
interface MiningContractDao {
    @Query("SELECT * FROM mining_contracts")
    fun getAllContracts(): Flow<List<MiningContract>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: MiningContract)

    @Query("DELETE FROM mining_contracts")
    suspend fun clearContracts()
}

@Dao
interface TransactionLogDao {
    @Query("SELECT * FROM transaction_logs ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionLog)

    @Query("DELETE FROM transaction_logs")
    suspend fun clearTransactions()
}

@Dao
interface ReferralUserDao {
    @Query("SELECT * FROM referral_users")
    fun getAllReferrals(): Flow<List<ReferralUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralUser)

    @Query("DELETE FROM referral_users")
    suspend fun clearReferrals()
}

@Dao
interface BitcoinWalletDao {
    @Query("SELECT * FROM bitcoin_wallets ORDER BY id ASC")
    fun getAllWallets(): Flow<List<BitcoinWallet>>

    @Query("SELECT * FROM bitcoin_wallets WHERE address = :address LIMIT 1")
    suspend fun getWalletByAddress(address: String): BitcoinWallet?

    @Query("UPDATE bitcoin_wallets SET balanceBtc = :balance WHERE address = :address")
    suspend fun updateWalletBalance(address: String, balance: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: BitcoinWallet)

    @Delete
    suspend fun deleteWallet(wallet: BitcoinWallet)

    @Query("DELETE FROM bitcoin_wallets")
    suspend fun clearWallets()
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<AppConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)
}
