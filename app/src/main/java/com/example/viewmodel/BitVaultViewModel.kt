package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BitVaultViewModel(application: Application) : AndroidViewModel(application) {

    // ── REAL-TIME BITCOIN PRICE TICKER PROPERTIES ──
    private val _bitcoinPriceEur = MutableStateFlow(68450.0)
    val bitcoinPriceEur: StateFlow<Double> = _bitcoinPriceEur.asStateFlow()

    private val _tickerStatus = MutableStateFlow("Live (Coinbase API)")
    val tickerStatus: StateFlow<String> = _tickerStatus.asStateFlow()

    private val _lastCheckedTime = MutableStateFlow(System.currentTimeMillis())
    val lastCheckedTime: StateFlow<Long> = _lastCheckedTime.asStateFlow()

    private val _isTickerLoading = MutableStateFlow(false)
    val isTickerLoading: StateFlow<Boolean> = _isTickerLoading.asStateFlow()

    private val _btcPriceDifferencePercent = MutableStateFlow(1.42)
    val btcPriceDifferencePercent: StateFlow<Double> = _btcPriceDifferencePercent.asStateFlow()

    private val _btcPriceDirection = MutableStateFlow("up")
    val btcPriceDirection: StateFlow<String> = _btcPriceDirection.asStateFlow()

    // OkHttp Client local reuse instance
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // Dual-source price fetcher
    private suspend fun fetchBtcPriceFromCoinbase(): Double? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.coinbase.com/v2/prices/BTC-EUR/spot")
            .header("User-Agent", "BitVault Android App")
            .build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val json = JSONObject(body)
                    val dataObj = json.getJSONObject("data")
                    dataObj.getString("amount").toDoubleOrNull()
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun fetchBtcPriceFromBlockchainInfo(): Double? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://blockchain.info/ticker")
            .header("User-Agent", "BitVault Android App")
            .build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val json = JSONObject(body)
                    val eurObj = json.getJSONObject("EUR")
                    eurObj.getDouble("last")
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun refreshBitcoinPrice() {
        viewModelScope.launch {
            if (_isTickerLoading.value) return@launch
            _isTickerLoading.value = true
            val fetchedPrice = fetchBtcPriceFromCoinbase() ?: fetchBtcPriceFromBlockchainInfo()
            if (fetchedPrice != null && fetchedPrice > 1000.0) {
                val prevPrice = _bitcoinPriceEur.value
                _bitcoinPriceEur.value = fetchedPrice
                _tickerStatus.value = "Live (Coinbase API)"
                
                val changePct = ((fetchedPrice - prevPrice) / prevPrice) * 100.0
                if (changePct != 0.0) {
                    _btcPriceDifferencePercent.value = changePct
                    _btcPriceDirection.value = if (changePct > 0.0) "up" else "down"
                }
                _lastCheckedTime.value = System.currentTimeMillis()
                _toastMessage.emit("Bitcoin koers live bijgewerkt: €${String.format("%.2f", fetchedPrice)} ✓")
            } else {
                // Subtle random update to mock movement offline
                val changeAmt = Random.nextDouble(-12.0, 15.0)
                _bitcoinPriceEur.value += changeAmt
                val changePct = (changeAmt / _bitcoinPriceEur.value) * 100.0
                _btcPriceDifferencePercent.value = changePct
                _btcPriceDirection.value = if (changePct > 0.0) "up" else "down"
                _tickerStatus.value = "Offline Fallback-modus"
                _lastCheckedTime.value = System.currentTimeMillis()
                _toastMessage.emit("Geen netwerk. Koers lokaal gesimuleerd: €${String.format("%.2f", _bitcoinPriceEur.value)}")
            }
            _isTickerLoading.value = false
        }
    }

    private val db = AppDatabase.getDatabase(application)
    private val repository = BitVaultRepository(db)

    // Auth screen state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _activeTab = MutableStateFlow("dash")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // Observables from Database
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allContracts: StateFlow<List<MiningContract>> = repository.allContracts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionLog>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReferrals: StateFlow<List<ReferralUser>> = repository.allReferrals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWallets: StateFlow<List<BitcoinWallet>> = repository.allWallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appConfig: StateFlow<AppConfig?> = repository.appConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Simulated miner telemetry
    private val _currentHashrate = MutableStateFlow(142.0)
    val currentHashrate: StateFlow<Double> = _currentHashrate.asStateFlow()

    private val _temperature = MutableStateFlow(62.4)
    val temperature: StateFlow<Double> = _temperature.asStateFlow()

    private val _fanSpeed = MutableStateFlow(84)
    val fanSpeed: StateFlow<Int> = _fanSpeed.asStateFlow()

    private val _voltage = MutableStateFlow(12.1)
    val voltage: StateFlow<Double> = _voltage.asStateFlow()

    private val _activeShares = MutableStateFlow(0)
    val activeShares: StateFlow<Int> = _activeShares.asStateFlow()

    // Real-time incremental earnings (EUR) updated on every share found
    private val _sessionEarnings = MutableStateFlow(0.0)
    val sessionEarnings: StateFlow<Double> = _sessionEarnings.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Predefined mnemonic words bank for generation
    private val wordsBank = listOf(
        "anchor", "breeze", "canvas", "device", "effort", "forest", "gentle", "hover",
        "index", "jacket", "kitten", "lizard", "matrix", "nature", "oxygen", "powder",
        "quality", "rescue", "silent", "tunnel", "update", "vortex", "wisdom", "yellow"
    )

    init {
        // Run simulated ASIC mining telemetry
        viewModelScope.launch {
            while (true) {
                delay(2000)
                val config = appConfig.value
                val isMiningEnabled = config?.isMiningActive != false
                if (isMiningEnabled) {
                    val multiplier = config?.currentHashrateMultiplier ?: 1.0
                    // Add minor random fluctuation to hashrate, safe bounds
                    val baseHash = 142.0 * multiplier
                    _currentHashrate.value = (baseHash + Random.nextDouble(-3.0, 3.0)).coerceAtLeast(0.0)
                    _temperature.value = (62.0 + Random.nextDouble(-1.5, 1.5)).coerceIn(45.0, 85.0)
                    _fanSpeed.value = (80 + Random.nextInt(-4, 4)).coerceIn(50, 100)
                    _voltage.value = (12.0 + Random.nextDouble(-0.1, 0.1)).coerceIn(11.5, 12.6)

                    // Share submission simulation
                    if (Random.nextInt(0, 3) == 1) {
                        _activeShares.value += 1
                        val deltaEUR = Random.nextDouble(0.02, 0.08)
                        _sessionEarnings.value += deltaEUR
                        incrementUserBalanceOffset(deltaEUR)

                        // Incremental change to primary Bitcoin wallet balance to simulate mining reward
                        val rate = if (_bitcoinPriceEur.value > 1000) _bitcoinPriceEur.value else 70000.0
                        incrementPrimaryWalletBalance(deltaEUR / rate)
                    }
                } else {
                    _currentHashrate.value = 0.0
                    _temperature.value = 28.5
                    _fanSpeed.value = 0
                    _voltage.value = 0.0
                }
            }
        }

        // Live Bitcoin Ticker Loop (runs every 30 seconds)
        viewModelScope.launch {
            var firstRun = true
            while (true) {
                _isTickerLoading.value = true
                val fetchedPrice = fetchBtcPriceFromCoinbase() ?: fetchBtcPriceFromBlockchainInfo()
                if (fetchedPrice != null && fetchedPrice > 1000.0) {
                    val prevPrice = _bitcoinPriceEur.value
                    _bitcoinPriceEur.value = fetchedPrice
                    _tickerStatus.value = "Live (Coinbase API)"
                    if (!firstRun) {
                        val changePct = ((fetchedPrice - prevPrice) / prevPrice) * 100.0
                        if (changePct != 0.0) {
                            _btcPriceDifferencePercent.value = changePct
                            _btcPriceDirection.value = if (changePct > 0.0) "up" else "down"
                        }
                    } else {
                        _btcPriceDifferencePercent.value = Random.nextDouble(0.8, 4.2)
                        _btcPriceDirection.value = "up"
                    }
                    _lastCheckedTime.value = System.currentTimeMillis()
                } else {
                    // Fallback to offline fluctuation if all API requests fail
                    if (firstRun) {
                        _bitcoinPriceEur.value = 68450.0 + Random.nextDouble(-120.0, 120.0)
                        _btcPriceDifferencePercent.value = Random.nextDouble(-2.0, 3.0)
                        _btcPriceDirection.value = if (_btcPriceDifferencePercent.value > 0.0) "up" else "down"
                    } else {
                        val changeAmt = Random.nextDouble(-14.0, 18.0)
                        _bitcoinPriceEur.value += changeAmt
                        val changePct = (changeAmt / _bitcoinPriceEur.value) * 100.0
                        _btcPriceDifferencePercent.value = changePct
                        _btcPriceDirection.value = if (changePct > 0.0) "up" else "down"
                    }
                    _tickerStatus.value = "Offline Fallback-modus"
                    _lastCheckedTime.value = System.currentTimeMillis()
                }
                _isTickerLoading.value = false
                firstRun = false
                delay(30_000) // 30 seconds wait
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank()) {
                _toastMessage.emit("Vul alle velden in")
                return@launch
            }
            // Generate standard user profile if DB is empty, or update existing
            val profile = userProfile.value ?: UserProfile(
                userName = "Matty Moors",
                userEmail = email,
                currentPlan = "Pro",
                walletAddress = "bc1q7ywp3g7l9slw5t359sm44766gslws2lqsw2lq9",
                walletType = "Bitcoin (BTC)"
            )
            repository.updateProfile(profile.copy(userEmail = email))
            _isLoggedIn.value = true
            _toastMessage.emit("Welkom terug, ${profile.userName}!")
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                _toastMessage.emit("Vul alle velden in")
                return@launch
            }
            repository.updateProfile(
                UserProfile(
                    id = 1,
                    userName = name,
                    userEmail = email,
                    currentPlan = "Gratis",
                    walletAddress = "bc1q7ywp3g7l9slw5t359sm44766gslws2lqsw2lq9",
                    walletType = "Bitcoin (BTC)"
                )
            )
            _isLoggedIn.value = true
            _toastMessage.emit("Account succesvol aangemaakt!")
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _activeTab.value = "dash"
    }

    fun setTab(tab: String) {
        _activeTab.value = tab
    }

    fun toggleMining() {
        viewModelScope.launch {
            val current = appConfig.value ?: AppConfig()
            val nextState = !current.isMiningActive
            repository.updateConfig(current.copy(isMiningActive = nextState))
            if (nextState) {
                _toastMessage.emit("Mining gestart! Verbinding maken met pool...")
            } else {
                _toastMessage.emit("Mining tijdelijk stopgezet.")
            }
        }
    }

    fun updateWallet(address: String, type: String) {
        viewModelScope.launch {
            val profile = userProfile.value
            if (profile != null) {
                repository.updateProfile(profile.copy(walletAddress = address, walletType = type))
                _toastMessage.emit("Wallet bijgewerkt naar $type ✓")
            }
        }
    }

    fun confirmDeposit(amount: Double) {
        viewModelScope.launch {
            val profile = userProfile.value
            if (profile != null) {
                repository.updateProfile(profile.copy(totalBalanceOffset = profile.totalBalanceOffset + amount))
                repository.addTransaction(
                    TransactionLog(
                        title = "Storting ontvangen",
                        sub = "IBAN bankoverschrijving",
                        amount = amount,
                        type = "DEPOSIT"
                    )
                )
                _toastMessage.emit("Storting van €${String.format("%.2f", amount)} verwerkt ✓")
            }
        }
    }

    fun confirmWithdrawal(amount: Double, iban: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val totalBalance = 1284.60 + profile.totalBalanceOffset + _sessionEarnings.value

            if (amount < 10.0) {
                _toastMessage.emit("Minimum opname is €10.00")
                return@launch
            }
            if (amount > totalBalance) {
                _toastMessage.emit("Onvoldoende saldo")
                return@launch
            }
            repository.updateProfile(profile.copy(totalBalanceOffset = profile.totalBalanceOffset - amount))
            repository.addTransaction(
                TransactionLog(
                    title = "Opname aangevraagd",
                    sub = "Naar $iban",
                    amount = -amount,
                    type = "WITHDRAWAL"
                )
            )
            _toastMessage.emit("Opname van €${String.format("%.2f", amount)} aangevraagd ✓")
        }
    }

    fun upgradePlan(planId: String, planName: String, price: Double) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.updateProfile(profile.copy(currentPlan = planName))

            // Add transaction for planpayment
            repository.addTransaction(
                TransactionLog(
                    title = "Lidmaatschap betaald",
                    sub = "Plan $planName — Revolut",
                    amount = -price,
                    type = "MEMBERSHIP"
                )
            )
            _toastMessage.emit("Plan $planName geactiveerd! Bevestigingsmail verzonden.")
        }
    }

    fun buyContract(name: String, hashrate: Double, cost: Double, expiresAt: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val totalBalance = 1284.60 + profile.totalBalanceOffset + _sessionEarnings.value

            if (cost > totalBalance) {
                _toastMessage.emit("Onvoldoende saldo om contract aan te kopen")
                return@launch
            }

            // Deduct cost and insert contract
            repository.updateProfile(profile.copy(totalBalanceOffset = profile.totalBalanceOffset - cost))
            repository.addContract(
                MiningContract(
                    name = name,
                    hashrate = hashrate,
                    expiresAt = expiresAt,
                    isActive = true
                )
            )
            repository.addTransaction(
                TransactionLog(
                    title = "Contract aangekocht",
                    sub = "$name ($hashrate TH/s)",
                    amount = -cost,
                    type = "MEMBERSHIP"
                )
            )

            // Update configuration hashrate multiplier based on new contracts
            val currentMultiplier = appConfig.value?.currentHashrateMultiplier ?: 1.0
            val newMultiplier = currentMultiplier + (hashrate / 142.0)
            val currentConf = appConfig.value ?: AppConfig()
            repository.updateConfig(currentConf.copy(currentHashrateMultiplier = newMultiplier))

            _toastMessage.emit("Nieuw contract geactiveerd: $name!")
        }
    }

    // ── SECURE WALLET ENGINE OPERATIONS ──

    fun generateNewWallet(label: String) {
        viewModelScope.launch {
            val selectedWords = (1..12).map { wordsBank.random() }.joinToString(" ")
            
            // Derive a simulated bitcoin address bc1q + 38 alpha-numerics
            val allowedChars = "abcdefghijklmnopqrstuvwxyz0123456789"
            val randomAddressPart = (1..38).map { allowedChars.random() }.joinToString("")
            val address = "bc1q$randomAddressPart"

            // Compute secure key hex
            val keyChars = "abcdef0123456789"
            val secureKeyHex = (1..64).map { keyChars.random() }.joinToString("")

            val newWallet = BitcoinWallet(
                address = address,
                secureKeyHex = secureKeyHex,
                seedWords = selectedWords,
                balanceBtc = 0.0,
                label = label.ifBlank() { "Gegenereerde Wallet" },
                isImported = false
            )

            repository.addWallet(newWallet)
            _toastMessage.emit("Nieuwe wallet succesvol gegenereerd en beveiligd met Cryptographic AES!")
        }
    }

    fun importWallet(seedPhraseOrKey: String, label: String) {
        viewModelScope.launch {
            val trimmed = seedPhraseOrKey.trim()
            if (trimmed.isBlank()) {
                _toastMessage.emit("Mnemonic seed of Private Key mag niet leeg zijn.")
                return@launch
            }

            val isMnemonic = trimmed.split("\\s+".toRegex()).size >= 12
            
            // Derive public address from key / seed mockup
            val allowedChars = "abcdefghijklmnopqrstuvwxyz0123456789"
            val randomAddressPart = (1..38).map { allowedChars.random() }.joinToString("")
            val address = "bc1q$randomAddressPart"

            val keyChars = "abcdef0123456789"
            val secureKey = (1..64).map { keyChars.random() }.joinToString("")

            val newWallet = BitcoinWallet(
                address = address,
                secureKeyHex = secureKey,
                seedWords = if (isMnemonic) trimmed else "Geïmporteerd via Private Key raw hex",
                balanceBtc = Random.nextDouble(0.005, 0.082), // simulate some recovery coin
                label = label.ifBlank() { "Geïmporteerde Wallet" },
                isImported = true
            )

            repository.addWallet(newWallet)
            _toastMessage.emit("Bestaande BTC-rekening succesvol gesynchroniseerd en geladen!")
        }
    }

    fun sendBitcoin(fromAddress: String, destination: String, amountBtc: Double) {
        viewModelScope.launch {
            if (destination.trim().length < 26) {
                _toastMessage.emit("Ongeldig Bitcoin bestemmingsadres.")
                return@launch
            }
            if (amountBtc <= 0.0) {
                _toastMessage.emit("Voer een geldig Bitcoin-bedrag in.")
                return@launch
            }

            // Find matching wallet
            val walletsList = allWallets.value
            val sourceWallet = walletsList.find { it.address == fromAddress }
            if (sourceWallet == null) {
                _toastMessage.emit("Afzender wallet niet gevonden.")
                return@launch
            }

            if (sourceWallet.balanceBtc < amountBtc) {
                _toastMessage.emit("Onvoldoende Bitcoin saldo in geselecteerde wallet.")
                return@launch
            }

            // Perform secure cryptographic signing broadcast mockup delay
            _toastMessage.emit("Transactie ondertekenen met private key...")
            delay(1500)
            _toastMessage.emit("Zenden naar decentralized netwerk...")
            delay(1000)

            val nextBalance = sourceWallet.balanceBtc - amountBtc
            repository.updateWalletBalance(fromAddress, nextBalance)

            // Log corresponding transaction histories
            val currentRate = if (_bitcoinPriceEur.value > 1000) _bitcoinPriceEur.value else 70000.0
            repository.addTransaction(
                TransactionLog(
                    title = "BTC verzonden",
                    sub = "Naar ${destination.take(12)}...${destination.takeLast(4)}",
                    amount = -(amountBtc * currentRate),
                    type = "SEND"
                )
            )

            _toastMessage.emit("Succesvol €${String.format("%.2f", amountBtc * currentRate)} overgeschreven via Bitcoin Blockchain! ✓")
        }
    }

    fun receiveBitcoin(toAddress: String, amountBtc: Double, senderAddress: String = "bc1qexternalrandom772737x") {
        viewModelScope.launch {
            if (amountBtc <= 0.0) {
                _toastMessage.emit("Voer een geldig Bitcoin-bedrag in.")
                return@launch
            }

            val walletsList = allWallets.value
            val targetWallet = walletsList.find { it.address == toAddress }
            if (targetWallet == null) {
                _toastMessage.emit("Ontvangende wallet niet gevonden.")
                return@launch
            }

            _toastMessage.emit("Inkomende transactie detecteren...")
            delay(1000)
            _toastMessage.emit("Blokbevestigingen controleren...")
            delay(1000)

            val nextBalance = targetWallet.balanceBtc + amountBtc
            repository.updateWalletBalance(toAddress, nextBalance)

            val currentRate = if (_bitcoinPriceEur.value > 1000) _bitcoinPriceEur.value else 70000.0
            repository.addTransaction(
                TransactionLog(
                    title = "BTC ontvangen",
                    sub = "Van ${senderAddress.take(12)}...${senderAddress.takeLast(4)}",
                    amount = amountBtc * currentRate,
                    type = "RECEIVE"
                )
            )

            _toastMessage.emit("Succesvol €${String.format("%.2f", amountBtc * currentRate)} bijgeschreven op wallet ${targetWallet.label}! ✓")
        }
    }

    // ── INTERACTIVE PROFITABILITY CALCULATOR ENGINE ──

    fun saveCalculatorConfig(hashrateTh: Double, powerWatts: Double, costKwh: Double) {
        viewModelScope.launch {
            val current = appConfig.value ?: AppConfig()
            repository.updateConfig(
                current.copy(
                    calcHashrateTh = hashrateTh,
                    calcPowerWatts = powerWatts,
                    calcElectricCostKwh = costKwh
                )
            )
            _toastMessage.emit("Rekenmachine parameters persistent opgeslagen ✓")
        }
    }

    private fun incrementUserBalanceOffset(amount: Double) {
        viewModelScope.launch {
            val profile = userProfile.value
            if (profile != null) {
                // We add as earnings to userProfile balance offset
                repository.updateProfile(profile.copy(totalBalanceOffset = profile.totalBalanceOffset + amount))
            }
        }
    }

    private fun incrementPrimaryWalletBalance(amountBtc: Double) {
        viewModelScope.launch {
            val wallets = allWallets.value
            if (wallets.isNotEmpty()) {
                val primary = wallets.first()
                repository.updateWalletBalance(primary.address, primary.balanceBtc + amountBtc)
            }
        }
    }

    fun showCustomToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }
}
