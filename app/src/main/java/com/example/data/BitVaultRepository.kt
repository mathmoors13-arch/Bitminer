package com.example.data

import kotlinx.coroutines.flow.Flow

class BitVaultRepository(private val db: AppDatabase) {

    val userProfile: Flow<UserProfile?> = db.userProfileDao().getUserProfile()
    val allContracts: Flow<List<MiningContract>> = db.miningContractDao().getAllContracts()
    val allTransactions: Flow<List<TransactionLog>> = db.transactionLogDao().getAllTransactions()
    val allReferrals: Flow<List<ReferralUser>> = db.referralUserDao().getAllReferrals()
    val allWallets: Flow<List<BitcoinWallet>> = db.bitcoinWalletDao().getAllWallets()
    val appConfig: Flow<AppConfig?> = db.appConfigDao().getConfig()

    suspend fun updateProfile(profile: UserProfile) {
        db.userProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun addTransaction(transaction: TransactionLog) {
        db.transactionLogDao().insertTransaction(transaction)
    }

    suspend fun updateConfig(config: AppConfig) {
        db.appConfigDao().insertConfig(config)
    }

    suspend fun addContract(contract: MiningContract) {
        db.miningContractDao().insertContract(contract)
    }

    suspend fun addWallet(wallet: BitcoinWallet) {
        db.bitcoinWalletDao().insertWallet(wallet)
    }

    suspend fun deleteWallet(wallet: BitcoinWallet) {
        db.bitcoinWalletDao().deleteWallet(wallet)
    }

    suspend fun updateWalletBalance(address: String, balance: Double) {
        db.bitcoinWalletDao().updateWalletBalance(address, balance)
    }
}
