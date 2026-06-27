package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.core.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.BitVaultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Local Sleek Interface Theme Colors
val SleekBg = Color(0xFFF7F9FF)
val SleekPrimary = Color(0xFF1A1C1E)
val SleekSecondary = Color(0xFF2563EB)
val SleekTertiary = Color(0xFF7C3AED)
val SleekWhite = Color(0xFFFFFFFF)
val SleekGrayLight = Color(0xFFEFF6FF)
val SleekTextMuted = Color(0xFF6B7280)
val SleekTextDark = Color(0xFF1A1C1E)
val SleekBorder = Color(0xFFE2E8F0)
val EcoBlueBg = Color(0xFFE3EFFF)
val ColdPurpleBg = Color(0xFFF2E7FF)
val SuccessGreen = Color(0xFF16A34A)
val ErrorRed = Color(0xFFDC2626)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: BitVaultViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe flows
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allContracts by viewModel.allContracts.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allReferrals by viewModel.allReferrals.collectAsStateWithLifecycle()
    val allWallets by viewModel.allWallets.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()

    // Live telemetries
    val hashrate by viewModel.currentHashrate.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val fanSpeed by viewModel.fanSpeed.collectAsStateWithLifecycle()
    val voltage by viewModel.voltage.collectAsStateWithLifecycle()
    val activeShares by viewModel.activeShares.collectAsStateWithLifecycle()
    val sessionEarnings by viewModel.sessionEarnings.collectAsStateWithLifecycle()

    // Real-time live Bitcoin price states
    val btcPriceEur by viewModel.bitcoinPriceEur.collectAsStateWithLifecycle()
    val tickerStatus by viewModel.tickerStatus.collectAsStateWithLifecycle()
    val lastCheckedTime by viewModel.lastCheckedTime.collectAsStateWithLifecycle()
    val isTickerLoading by viewModel.isTickerLoading.collectAsStateWithLifecycle()
    val btcPriceDifferencePercent by viewModel.btcPriceDifferencePercent.collectAsStateWithLifecycle()
    val btcPriceDirection by viewModel.btcPriceDirection.collectAsStateWithLifecycle()

    // Local UI State
    var showSplash by remember { mutableStateOf(true) }
    var splashProgress by remember { mutableStateOf(0f) }
    var inAppToastMessage by remember { mutableStateOf<String?>(null) }

    // Dialog sheets state
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showWalletInputSettings by remember { mutableStateOf(false) }
    var showContractBuySettings by remember { mutableStateOf(false) }
    var showBankDetailsDialog by remember { mutableStateOf(false) }

    // Dialog input fields strings
    var depositFieldAmount by remember { mutableStateOf("") }
    var withdrawFieldAmount by remember { mutableStateOf("") }
    var withdrawFieldIban by remember { mutableStateOf("BE85 6508 1740 7206") }
    var walletAddressInput by remember { mutableStateOf("") }
    var walletTypeInput by remember { mutableStateOf("Bitcoin (BTC)") }

    // Handle incoming toast requests
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            inAppToastMessage = msg
        }
    }

    LaunchedEffect(inAppToastMessage) {
        if (inAppToastMessage != null) {
            delay(2500)
            inAppToastMessage = null
        }
    }

    // Splash timer simulation
    LaunchedEffect(Unit) {
        val dt = 30L
        for (i in 1..50) {
            delay(dt)
            splashProgress = i / 50f
        }
        showSplash = false
    }

    if (showSplash) {
        // Full splash screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SleekBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⛏",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "BitVault",
                    color = SleekPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "Premium Crypto Mining Platform",
                    color = SleekTextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Custom loading bar representation
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SleekBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(splashProgress)
                            .background(SleekSecondary)
                    )
                }
            }
        }
    } else if (!isLoggedIn) {
        // Login Screen
        AuthScreen(
            onLogin = { email, pass -> viewModel.login(email, pass) },
            onRegister = { name, email, pass -> viewModel.register(name, email, pass) }
        )
    } else {
        // Secure Application Shell
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = SleekBg,
            topBar = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .background(SleekWhite)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⛏",
                                fontSize = 22.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = "BitVault",
                                    color = SleekPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "v2.8.4 Stable",
                                    color = SleekSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            // Live blinking operational state
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (appConfig?.isMiningActive != false) SuccessGreen else SleekTextMuted
                                    )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SleekSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile?.userName?.split(" ")?.map { it.take(1) }?.joinToString("") ?: "MM",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekGrayLight,
                                    contentColor = SleekPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp).testTag("logout_button")
                            ) {
                                Text("Uitloggen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = SleekWhite,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = activeTab == "dash",
                        onClick = { viewModel.setTab("dash") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekSecondary,
                            unselectedIconColor = SleekTextMuted,
                            selectedTextColor = SleekSecondary,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = EcoBlueBg
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "calc",
                        onClick = { viewModel.setTab("calc") },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = "Rekenmachine") },
                        label = { Text("Calculator", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekSecondary,
                            unselectedIconColor = SleekTextMuted,
                            selectedTextColor = SleekSecondary,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = EcoBlueBg
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "wallet",
                        onClick = { viewModel.setTab("wallet") },
                        icon = { Icon(Icons.Default.Lock, contentDescription = "Wallet") },
                        label = { Text("Bitcoin Wallet", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekSecondary,
                            unselectedIconColor = SleekTextMuted,
                            selectedTextColor = SleekSecondary,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = EcoBlueBg
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "plans",
                        onClick = { viewModel.setTab("plans") },
                        icon = { Icon(Icons.Default.Star, contentDescription = "APK & Upgrade") },
                        label = { Text("Lidmaatschap", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekSecondary,
                            unselectedIconColor = SleekTextMuted,
                            selectedTextColor = SleekSecondary,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = EcoBlueBg
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "profile",
                        onClick = { viewModel.setTab("profile") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profiel") },
                        label = { Text("Profiel", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekSecondary,
                            unselectedIconColor = SleekTextMuted,
                            selectedTextColor = SleekSecondary,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = EcoBlueBg
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Active Tabs Content Router
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp) // Safety margin to avoid bottom bar overlap
                ) {
                    when (activeTab) {
                        "dash" -> DashboardTab(
                            hashrate = hashrate,
                            temperature = temperature,
                            fanSpeed = fanSpeed,
                            voltage = voltage,
                            activeShares = activeShares,
                            sessionEarnings = sessionEarnings,
                            activeContractsCount = allContracts.filter { it.isActive }.size,
                            referralsCount = allReferrals.size,
                            isMiningActive = appConfig?.isMiningActive != false,
                            onToggleMining = { viewModel.toggleMining() },
                            transactions = allTransactions.take(3),
                            contracts = allContracts,
                            onAddContractClick = { showContractBuySettings = true },
                            onAllTransactionsClick = { viewModel.setTab("plans") },
                            btcPriceEur = btcPriceEur,
                            tickerStatus = tickerStatus,
                            lastCheckedTime = lastCheckedTime,
                            isTickerLoading = isTickerLoading,
                            btcPriceDifferencePercent = btcPriceDifferencePercent,
                            btcPriceDirection = btcPriceDirection,
                            onRefreshBtcPrice = { viewModel.refreshBitcoinPrice() }
                        )
                        "calc" -> CalculatorTab(
                            config = appConfig ?: AppConfig(),
                            onSaveConfig = { hashrate, power, cost ->
                                viewModel.saveCalculatorConfig(hashrate, power, cost)
                            },
                            btcPriceEur = btcPriceEur
                        )
                        "wallet" -> WalletTab(
                            wallets = allWallets,
                            sessionEarnings = sessionEarnings,
                            transactions = allTransactions,
                            onGenerateWallet = { label -> viewModel.generateNewWallet(label) },
                            onImportWallet = { key, label -> viewModel.importWallet(key, label) },
                            onSendBitcoin = { from, to, amount -> viewModel.sendBitcoin(from, to, amount) },
                            onReceiveBitcoin = { to, amount -> viewModel.receiveBitcoin(to, amount) },
                            clipboardManager = clipboardManager,
                            btcPriceEur = btcPriceEur
                        )
                        "plans" -> MembershipTab(
                            userProfile = userProfile,
                            onSelectPlan = { id, name, price ->
                                viewModel.upgradePlan(id, name, price)
                            },
                            onDownloadApk = {
                                viewModel.showCustomToast("📥 BitVault APK download gestart in achtergrond...")
                            }
                        )
                        "profile" -> ProfileTab(
                            userProfile = userProfile,
                            referrals = allReferrals,
                            onTabSelect = { viewModel.setTab(it) },
                            onWalletAddressClick = {
                                walletAddressInput = userProfile?.walletAddress ?: ""
                                walletTypeInput = userProfile?.walletType ?: "Bitcoin (BTC)"
                                showWalletInputSettings = true
                            },
                            onMineVaultBankClick = { showBankDetailsDialog = true },
                            onLogoutClick = { viewModel.logout() }
                        )
                    }
                }

                // Smooth bottom in-app snackbar/toast
                AnimatedVisibility(
                    visible = inAppToastMessage != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(SleekPrimary)
                            .border(1.dp, SleekSecondary, RoundedCornerShape(50.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = inAppToastMessage ?: "",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ── DIALOGS AND BOTTOM SHEETS REPLICAS ──

                // Wallet Config Sheet Modal
                if (showWalletInputSettings) {
                    AlertDialog(
                        onDismissRequest = { showWalletInputSettings = false },
                        containerColor = SleekWhite,
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.updateWallet(walletAddressInput, walletTypeInput)
                                    showWalletInputSettings = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White)
                            ) {
                                Text("Opslaan", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showWalletInputSettings = false }) {
                                Text("Annuleren", color = SleekTextMuted)
                            }
                        },
                        title = {
                            Text("💰 Ontvangst Wallet Instellen", color = SleekPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Koppel hier je externe Bitcoin of Ethereum ontvangstadres zodat we live pool uitbetalingen handmatig of automatisch kunnen synchroniseren.",
                                    color = SleekTextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                OutlinedTextField(
                                    value = walletAddressInput,
                                    onValueChange = { walletAddressInput = it },
                                    label = { Text("Doel Wallet Adres") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SleekSecondary,
                                        unfocusedBorderColor = SleekBorder,
                                        focusedLabelColor = SleekSecondary,
                                        unfocusedLabelColor = SleekTextMuted
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp).testTag("wallet_address_input")
                                )

                                Text(
                                    text = "Selecteer Netwerk:",
                                    color = SleekPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val isBtcSelected = walletTypeInput.contains("Bitcoin")
                                    Button(
                                        onClick = { walletTypeInput = "Bitcoin (BTC)" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isBtcSelected) SleekSecondary else SleekGrayLight,
                                            contentColor = if (isBtcSelected) Color.White else SleekPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Bitcoin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { walletTypeInput = "Ethereum (ETH)" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!isBtcSelected) SleekSecondary else SleekGrayLight,
                                            contentColor = if (!isBtcSelected) Color.White else SleekPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Ethereum", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                }

                // Extra Contract buying modal
                if (showContractBuySettings) {
                    AlertDialog(
                        onDismissRequest = { showContractBuySettings = false },
                        containerColor = SleekWhite,
                        confirmButton = {
                            TextButton(onClick = { showContractBuySettings = false }) {
                                Text("Sluiten", color = SleekSecondary)
                            }
                        },
                        title = {
                            Text("⚡ Snel extra Hashrate Kopen", color = SleekPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Huur direct extra blockchain hash-capaciteit om je bitcoin-mining snelheid te boosten.",
                                    color = SleekTextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                ContractOfferRow(
                                    title = "S9 Basic ASIC",
                                    hashrate = 14.0,
                                    cost = 49.99,
                                    onBuy = {
                                        viewModel.buyContract("Contract BTC S9", 14.0, 49.99, "23 dec 2026")
                                        showContractBuySettings = false
                                    }
                                )

                                ContractOfferRow(
                                    title = "S19 PRO Antminer",
                                    hashrate = 110.0,
                                    cost = 249.99,
                                    onBuy = {
                                        viewModel.buyContract("Contract BTC S19", 110.0, 249.99, "23 dec 2027")
                                        showContractBuySettings = false
                                    }
                                )

                                ContractOfferRow(
                                    title = "T21 Hydro Mega",
                                    hashrate = 190.0,
                                    cost = 399.99,
                                    onBuy = {
                                        viewModel.buyContract("Contract Hydro T21", 190.0, 399.99, "23 jun 2028")
                                        showContractBuySettings = false
                                    }
                                )
                            }
                        }
                    )
                }

                // Bank details summary modal
                if (showBankDetailsDialog) {
                    AlertDialog(
                        onDismissRequest = { showBankDetailsDialog = false },
                        containerColor = SleekWhite,
                        confirmButton = {
                            Button(
                                onClick = { showBankDetailsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White)
                            ) {
                                Text("Begrepen", fontWeight = FontWeight.Bold)
                            }
                        },
                        title = {
                            Text("🏦 BitVault Bankrekening", color = SleekPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Voor officiële Euro stortingen, stuur een handmatige transactie naar:",
                                    color = SleekTextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SleekGrayLight),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        BankRowInfo(label = "Begunstigde", value = "Matty Moors") {
                                            clipboardManager.setText(AnnotatedString(it))
                                            viewModel.showCustomToast("Matty Moors gekopieerd! ✓")
                                        }
                                        BankRowInfo(label = "IBAN", value = "BE85650817407206") {
                                            clipboardManager.setText(AnnotatedString(it))
                                            viewModel.showCustomToast("IBAN gekopieerd! ✓")
                                        }
                                        BankRowInfo(label = "BIC/SWIFT", value = "REVOBEB2") {
                                            clipboardManager.setText(AnnotatedString(it))
                                            viewModel.showCustomToast("BIC gekopieerd! ✓")
                                        }
                                        BankRowInfo(label = "Bank", value = "Revolut Brussel") {}
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ── CUSTOM DETAILED TAB COMPOSABLES ──

@Composable
fun BtcPriceTicker(
    btcPriceEur: Double,
    tickerStatus: String,
    lastCheckedTime: Long,
    isTickerLoading: Boolean,
    btcPriceDifferencePercent: Double,
    btcPriceDirection: String,
    onRefreshBtcPrice: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ticker_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticker_angle"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = SleekWhite),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .testTag("btc_price_ticker_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row (Coin Info + Ticker Source Indicator + Refresh Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF7931A)), // Bitcoin Orange
                        contentAlignment = Alignment.Center
                    ) {
                        Text("₿", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "Bitcoin",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = SleekPrimary
                        )
                        Text(
                            text = "BTC / EUR",
                            fontSize = 11.sp,
                            color = SleekTextMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (tickerStatus.contains("Live")) EcoBlueBg else Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (tickerStatus.contains("Live")) SuccessGreen else Color(0xFFD97706))
                            )
                            Text(
                                text = tickerStatus,
                                color = if (tickerStatus.contains("Live")) SleekSecondary else Color(0xFFB45309),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshBtcPrice,
                        modifier = Modifier.size(36.dp).testTag("refresh_btc_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Bitcoin koers",
                            tint = SleekSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isTickerLoading) rotationAngle else 0f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Row (Price Display + Percent difference badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "€ " + String.format(java.util.Locale.GERMANY, "%,.2f", btcPriceEur),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPrimary,
                        modifier = Modifier.testTag("btc_ticker_price_text")
                    )

                    val timeFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    val lastUpdatedStr = timeFormat.format(java.util.Date(lastCheckedTime))
                    Text(
                        text = "Laatst gecontroleerd: $lastUpdatedStr",
                        fontSize = 10.sp,
                        color = SleekTextMuted
                    )
                }

                val isUp = btcPriceDirection == "up" || btcPriceDifferencePercent >= 0.0
                val percentString = String.format(java.util.Locale.US, "%+.2f%%", btcPriceDifferencePercent)
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isUp) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isUp) "▲" else "▼",
                            color = if (isUp) SuccessGreen else ErrorRed,
                            fontSize = 10.sp
                        )
                        Text(
                            text = percentString,
                            color = if (isUp) SuccessGreen else ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTab(
    hashrate: Double,
    temperature: Double,
    fanSpeed: Int,
    voltage: Double,
    activeShares: Int,
    sessionEarnings: Double,
    activeContractsCount: Int,
    referralsCount: Int,
    isMiningActive: Boolean,
    onToggleMining: () -> Unit,
    transactions: List<TransactionLog>,
    contracts: List<MiningContract>,
    onAddContractClick: () -> Unit,
    onAllTransactionsClick: () -> Unit,
    btcPriceEur: Double,
    tickerStatus: String,
    lastCheckedTime: Long,
    isTickerLoading: Boolean,
    btcPriceDifferencePercent: Double,
    btcPriceDirection: String,
    onRefreshBtcPrice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Real-time live Bitcoin price ticker component at the top of the dashboard
        BtcPriceTicker(
            btcPriceEur = btcPriceEur,
            tickerStatus = tickerStatus,
            lastCheckedTime = lastCheckedTime,
            isTickerLoading = isTickerLoading,
            btcPriceDifferencePercent = btcPriceDifferencePercent,
            btcPriceDirection = btcPriceDirection,
            onRefreshBtcPrice = onRefreshBtcPrice
        )

        // Mining Master Control Switch Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(SleekWhite)
                .border(1.dp, if (isMiningActive) SleekSecondary else SleekBorder, RoundedCornerShape(32.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mine status",
                        color = SleekTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = if (isMiningActive) "ACTIEF MINEN..." else "RESERVE MODUS",
                        color = if (isMiningActive) SleekSecondary else SleekPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isMiningActive) "Engine verifieert blocks op pool node." else "Klik start om te verbinden.",
                        color = SleekTextMuted,
                        fontSize = 10.sp
                    )
                }

                Switch(
                    checked = isMiningActive,
                    onCheckedChange = { onToggleMining() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SleekSecondary,
                        uncheckedThumbColor = SleekTextMuted,
                        uncheckedTrackColor = SleekGrayLight
                    ),
                    modifier = Modifier.testTag("mining_switch")
                )
            }
        }

        // 4 KPI Stats Grid
        CardGridTelemetry(
            hashrate = hashrate,
            sessionEarnings = sessionEarnings,
            activeContractsCount = activeContractsCount,
            referralsCount = referralsCount
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Hardware Diagnostics Telemetries Row
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekWhite),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚙ Hardware Status Telemetrie",
                    color = SleekPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HardwareMetric(label = "GPU Temp", value = "${String.format("%.1f", temperature)}°C", color = if (temperature > 78) ErrorRed else SuccessGreen)
                    HardwareMetric(label = "Ventilator", value = "$fanSpeed%", color = SuccessGreen)
                    HardwareMetric(label = "Spanning", value = "${String.format("%.1f", voltage)}V", color = SuccessGreen)
                    HardwareMetric(label = "Shares", value = "$activeShares", color = SleekSecondary)
                }
            }
        }

        // Interactive Asymmetry Style cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(EcoBlueBg)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⚡ Eco Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekPrimary)
                    Text("Bespaart 40% batterij op je mobiele processor tijdens actieve pool mining.", fontSize = 11.sp, color = SleekSecondary)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ColdPurpleBg)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🔒 Cold Vault", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekPrimary)
                    Text("Directe hardware link om offline koude bitcoin sleutels te decentraliseren.", fontSize = 11.sp, color = SleekTertiary)
                }
            }
        }

        // Active contracts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Actieve Capaciteit", color = SleekPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(
                text = "+ Contract Kopen",
                color = SleekSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onAddContractClick() }
                    .padding(4.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            contracts.forEach { contract ->
                ContractItemRow(contract = contract)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Recent Transaction Logs header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Laatste Activiteit", color = SleekPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(
                text = "Alle →",
                color = SleekSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onAllTransactionsClick() }
                    .padding(4.dp)
            )
        }

        // Transaction list card
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekWhite),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                transactions.forEach { tx ->
                    TxItemRow(tx = tx)
                }
            }
        }
    }
}

// ── NEW PROFITABILITY CALCULATOR TAB COMPOSABLE ──

@Composable
fun CalculatorTab(
    config: AppConfig,
    onSaveConfig: (Double, Double, Double) -> Unit,
    btcPriceEur: Double
) {
    var hashrateInput by remember { mutableStateOf(config.calcHashrateTh.toString()) }
    var powerInput by remember { mutableStateOf(config.calcPowerWatts.toString()) }
    var costInput by remember { mutableStateOf(config.calcElectricCostKwh.toString()) }

    val hashrateValue = hashrateInput.toDoubleOrNull() ?: 0.0
    val powerValue = powerInput.toDoubleOrNull() ?: 0.0
    val costValue = costInput.toDoubleOrNull() ?: 0.0

    // Math Constants for present network (circa June 2026 BTC specifications)
    // 1 TH/s harvests circa 0.0000014 BTC daily
    val dailyBtcPerTh = 0.0000014

    val dailyGrossBtc = hashrateValue * dailyBtcPerTh
    val dailyGrossEur = dailyGrossBtc * btcPriceEur

    val dailyPowerKwh = (powerValue * 24.0) / 1000.0
    val dailyPowerCostEur = dailyPowerKwh * costValue

    val dailyNetEur = dailyGrossEur - dailyPowerCostEur
    val weeklyNetEur = dailyNetEur * 7.0
    val monthlyNetEur = dailyNetEur * 30.0

    val dailyNetBtc = dailyNetEur / btcPriceEur
    val weeklyNetBtc = weeklyNetEur / btcPriceEur
    val monthlyNetBtc = monthlyNetEur / btcPriceEur

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "📊 Bitcoin Mining Profitability Calculator",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = SleekPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Schat live opbrengsten op basis van hashrate vermogen, machine stroomverbruik en de lokale energieprijzen.",
            fontSize = 12.sp,
            color = SleekTextMuted,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekWhite),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Invoer Gegevens", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekPrimary)

                OutlinedTextField(
                    value = hashrateInput,
                    onValueChange = { hashrateInput = it },
                    label = { Text("Hashrate vermogen (TH/s)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekSecondary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("calc_hashrate_input")
                )

                OutlinedTextField(
                    value = powerInput,
                    onValueChange = { powerInput = it },
                    label = { Text("Power consumptie (Watts)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekSecondary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("calc_power_input")
                )

                OutlinedTextField(
                    value = costInput,
                    onValueChange = { costInput = it },
                    label = { Text("Elektriciteit kost (€/kWh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekSecondary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("calc_cost_input")
                )

                Button(
                    onClick = { onSaveConfig(hashrateValue, powerValue, costValue) },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_calc_btn")
                ) {
                    Text("Sla parameters persistent op", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live calculator results outputs
        Text("Geschatte Netto Verdiensten (€70.000/BTC)", fontWeight = FontWeight.Black, fontSize = 14.sp, color = SleekPrimary, modifier = Modifier.padding(bottom = 10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfitResultRow(title = "Dagelijkse winst", netEur = dailyNetEur, netBtc = dailyNetBtc, grossEur = dailyGrossEur, fuelEur = dailyPowerCostEur)
            ProfitResultRow(title = "Wekelijkse winst", netEur = weeklyNetEur, netBtc = weeklyNetBtc, grossEur = dailyGrossEur * 7, fuelEur = dailyPowerCostEur * 7)
            ProfitResultRow(title = "Maandelijkse winst", netEur = monthlyNetEur, netBtc = monthlyNetBtc, grossEur = dailyGrossEur * 30, fuelEur = dailyPowerCostEur * 30)
        }
    }
}

// ── NEW SECURE WALLET TAB COMPOSABLE ──

@Composable
fun WalletTab(
    wallets: List<BitcoinWallet>,
    sessionEarnings: Double,
    transactions: List<TransactionLog>,
    onGenerateWallet: (String) -> Unit,
    onImportWallet: (String, String) -> Unit,
    onSendBitcoin: (String, String, Double) -> Unit,
    onReceiveBitcoin: (String, Double) -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    btcPriceEur: Double
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var walletLabel by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importKeysInput by remember { mutableStateOf("") }
    var importLabel by remember { mutableStateOf("") }

    var showSendDialog by remember { mutableStateOf(false) }
    var sendFromAddress by remember { mutableStateOf("") }
    var sendDestination by remember { mutableStateOf("") }
    var sendAmount by remember { mutableStateOf("") }

    var showReceiveDialog by remember { mutableStateOf(false) }
    var receiveToAddress by remember { mutableStateOf("") }
    var receiveAmount by remember { mutableStateOf("") }
    var receiveSenderAddress by remember { mutableStateOf("") }

    var activeFilter by remember { mutableStateOf("alle") }
    var selectedTx by remember { mutableStateOf<TransactionLog?>(null) }

    val totalWalletBtc = wallets.sumOf { it.balanceBtc }
    val totalWalletEur = totalWalletBtc * btcPriceEur

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Grand Balance Display Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SleekPrimary, Color.Black)
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Gedecentraliseerd Bitcoin Saldo",
                    color = SleekTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "€ ${String.format("%.2f", totalWalletEur)} EUR",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "≈ ${String.format("%.6f", totalWalletBtc)} BTC",
                    color = EcoBlueBg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekSecondary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ Genereren", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekTertiary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔑 Importeren", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List of physical local secure wallets
        Text("Jouw Sleutels & Adressen", fontWeight = FontWeight.Black, fontSize = 14.sp, color = SleekPrimary, modifier = Modifier.padding(bottom = 10.dp))

        if (wallets.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekWhite),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Geen actieve hardware wallets.", color = SleekTextMuted, fontSize = 12.sp)
                    Text("Klik hierboven op genereren of importeren om crypto opslag te starten.", color = SleekTextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            wallets.forEach { wallet ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekWhite),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, SleekBorder),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(wallet.label, fontWeight = FontWeight.Black, fontSize = 14.sp, color = SleekPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (wallet.isImported) ColdPurpleBg else EcoBlueBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(if (wallet.isImported) "Recovery" else "Local AES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = SleekPrimary)
                            }
                        }

                        Text(
                            text = wallet.address,
                            color = SleekTextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp).clickable {
                                clipboardManager.setText(AnnotatedString(wallet.address))
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp).background(SleekGrayLight, RoundedCornerShape(8.dp)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Saldo", fontSize = 9.sp, color = SleekTextMuted)
                                Text("${String.format("%.6f", wallet.balanceBtc)} BTC", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // CTA button row to send or receive from this specific wallet
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    sendFromAddress = wallet.address
                                    sendDestination = ""
                                    sendAmount = ""
                                    showSendDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("Verzenden ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    receiveToAddress = wallet.address
                                    receiveAmount = ""
                                    receiveSenderAddress = "bc1q" + (1000..9999).random() + "testaddr" + (10..99).random()
                                    showReceiveDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekSecondary, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("Ontvangen ↙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Collapsible secure key info view
                        var showKeys by remember { mutableStateOf(false) }
                        IconButton(onClick = { showKeys = !showKeys }) {
                            Text(if (showKeys) "🙈 Verberg Private Key parameters" else "👁 Toon cryptografisch protocol weergaven", fontSize = 11.sp, color = SleekSecondary, fontWeight = FontWeight.Bold)
                        }

                        if (showKeys) {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                Text("Private Key Hex:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                                Text(wallet.secureKeyHex, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = ErrorRed)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("12-Woorden Seed:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                                Text(wallet.seedWords, fontSize = 11.sp, color = SleekTertiary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── WALLET TRANSACTION HISTORY VIEW ──
        Text("Transactiegeschiedenis (Bitcoin Ledger)", fontWeight = FontWeight.Black, fontSize = 14.sp, color = SleekPrimary)
        Text("Live-overzicht van on-chain Bitcoin overdrachten, stortingen en uitbetalingen.", fontSize = 11.sp, color = SleekTextMuted, modifier = Modifier.padding(bottom = 6.dp))

        // Filter chips inside the wallet tab
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            val filters = listOf("Alle", "Verzonden ↗", "Ontvangen ↙")
            filters.forEach { filter ->
                val isSelected = (filter == "Alle" && activeFilter == "alle") ||
                                 (filter == "Verzonden ↗" && activeFilter == "send") ||
                                 (filter == "Ontvangen ↙" && activeFilter == "receive")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) SleekSecondary else SleekWhite)
                        .border(1.dp, if (isSelected) Color.Transparent else SleekBorder, RoundedCornerShape(20.dp))
                        .clickable {
                            activeFilter = when (filter) {
                                "Verzonden ↗" -> "send"
                                "Ontvangen ↙" -> "receive"
                                else -> "alle"
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color.White else SleekTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Transactions list filtered nicely
        val filteredTx = when (activeFilter) {
            "send" -> transactions.filter { it.type == "SEND" || it.amount < 0 }
            "receive" -> transactions.filter { it.type == "RECEIVE" || (it.amount > 0 && it.type != "MEMBERSHIP" && it.type != "REFERRAL") }
            else -> transactions
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekWhite),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (filteredTx.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Geen on-chain Bitcoin transacties gevonden voor dit filter.", color = SleekTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    filteredTx.forEachIndexed { index, tx ->
                        WalletTxRow(
                            tx = tx,
                            onClick = { selectedTx = tx },
                            btcPriceEur = btcPriceEur
                        )
                        if (index < filteredTx.lastIndex) {
                            HorizontalDivider(color = SleekBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        // Sub dialogues modals
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = SleekWhite,
                confirmButton = {
                    Button(
                        onClick = {
                            onGenerateWallet(walletLabel)
                            walletLabel = ""
                            showCreateDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White)
                    ) {
                        Text("Uitvoeren", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Annuleren", color = SleekTextMuted)
                    }
                },
                title = { Text("Nieuwe Sleutel Genereren") },
                text = {
                    OutlinedTextField(
                        value = walletLabel,
                        onValueChange = { walletLabel = it },
                        label = { Text("Naam / Label") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }

        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                containerColor = SleekWhite,
                confirmButton = {
                    Button(
                        onClick = {
                            onImportWallet(importKeysInput, importLabel)
                            importKeysInput = ""
                            importLabel = ""
                            showImportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White)
                    ) {
                        Text("Importeren", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Annuleren", color = SleekTextMuted)
                    }
                },
                title = { Text("Bestaande Account Herstellen") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Voer je 12-woordige backupphrase of hexadecimale private key in.", fontSize = 11.sp, color = SleekTextMuted)
                        OutlinedTextField(
                            value = importKeysInput,
                            onValueChange = { importKeysInput = it },
                            label = { Text("Backup Phrase / Seed") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = importLabel,
                            onValueChange = { importLabel = it },
                            label = { Text("Naam / Label") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }

        if (showSendDialog) {
            AlertDialog(
                onDismissRequest = { showSendDialog = false },
                containerColor = SleekWhite,
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedAmount = sendAmount.toDoubleOrNull() ?: 0.0
                            onSendBitcoin(sendFromAddress, sendDestination, parsedAmount)
                            showSendDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White)
                    ) {
                        Text("Ethereum/Bitcoin zenden", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSendDialog = false }) {
                        Text("Annuleren", color = SleekTextMuted)
                    }
                },
                title = { Text("Beveiligd Uitsturen") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Van: $sendFromAddress", fontSize = 10.sp, color = SleekTextMuted)
                        OutlinedTextField(
                            value = sendDestination,
                            onValueChange = { sendDestination = it },
                            label = { Text("Doel Bitcoin-Adres") },
                            modifier = Modifier.fillMaxWidth().testTag("send_dest_input")
                        )
                        OutlinedTextField(
                            value = sendAmount,
                            onValueChange = { sendAmount = it },
                            label = { Text("Bedrag in BTC") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("send_amount_input")
                        )
                    }
                }
            )
        }

        if (showReceiveDialog) {
            AlertDialog(
                onDismissRequest = { showReceiveDialog = false },
                containerColor = SleekWhite,
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedAmount = receiveAmount.toDoubleOrNull() ?: 0.0
                            onReceiveBitcoin(receiveToAddress, parsedAmount)
                            showReceiveDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekSecondary, contentColor = Color.White)
                    ) {
                        Text("Simuleer Ontvangst", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReceiveDialog = false }) {
                        Text("Annuleren", color = SleekTextMuted)
                    }
                },
                title = { Text("Inkomend Bitcoin Boeken") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Ontvangen op: $receiveToAddress", fontSize = 10.sp, color = SleekTextMuted)
                        Text("Dit simuleert een blockchain transactie van een extern adres direct naar deze wallet.", fontSize = 11.sp, color = SleekTextMuted)
                        OutlinedTextField(
                            value = receiveAmount,
                            onValueChange = { receiveAmount = it },
                            label = { Text("Bedrag in BTC") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("receive_amount_input")
                        )
                        OutlinedTextField(
                            value = receiveSenderAddress,
                            onValueChange = { receiveSenderAddress = it },
                            label = { Text("Afzender Bitcoin-Adres") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }

        // Simulated Cryptographic details popup dialog
        if (selectedTx != null) {
            val tx = selectedTx!!
            val isIncoming = tx.amount > 0
            val btcVal = tx.amount / btcPriceEur
            val systemTime = System.currentTimeMillis()
            val isPending = (systemTime - tx.timestamp) < 120_000 && (tx.type == "SEND" || tx.type == "RECEIVE")
            val txHash = "f1a8" + (100_000..999_999).random() + "bc903bd81a3d92040e" + (100..999).random() + "da3b76a"

            AlertDialog(
                onDismissRequest = { selectedTx = null },
                containerColor = SleekWhite,
                confirmButton = {
                    Button(
                        onClick = { selectedTx = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sluiten", fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transactiedetails", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SleekPrimary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isIncoming) EcoBlueBg else ColdPurpleBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (isIncoming) "Inkomend" else "Uitgaand", fontSize = 9.sp, fontWeight = FontWeight.Black, color = SleekPrimary)
                        }
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(color = SleekBorder)
                        
                        // Amount Hero
                        Column(
                            modifier = Modifier.fillMaxWidth().background(SleekGrayLight, RoundedCornerShape(16.dp)).padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Totaal Bedrag", fontSize = 10.sp, color = SleekTextMuted)
                            Text(
                                text = (if (isIncoming) "+" else "") + String.format("%.6f", btcVal) + " BTC",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = if (isIncoming) SuccessGreen else SleekPrimary
                            )
                            Text("≈ € " + String.format("%.2f", tx.amount) + " EUR", fontSize = 12.sp, color = SleekTextMuted)
                        }

                        // Grid-like key value rows
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Protocol Status", fontSize = 11.sp, color = SleekTextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isPending) Color(0xFFF59E0B) else SuccessGreen))
                                    Text(
                                        text = if (isPending) "In behandeling (1/6 blocks)" else "Succesvol Bevestigd",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isPending) Color(0xFFD97706) else SuccessGreen
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Transactie Type", fontSize = 11.sp, color = SleekTextMuted)
                                Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SleekPrimary)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tijdstip", fontSize = 11.sp, color = SleekTextMuted)
                                val fullDateStr = java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp))
                                Text(fullDateStr, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = SleekPrimary)
                            }

                            Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                Text("Cryptografische Hash (TXID)", fontSize = 10.sp, color = SleekTextMuted)
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(SleekBg, RoundedCornerShape(6.dp)).clickable {
                                        clipboardManager.setText(AnnotatedString(txHash))
                                    }.padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = txHash.take(18) + "..." + txHash.takeLast(10),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = SleekSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("📋", fontSize = 10.sp)
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Blockchain Netwerkkosten", fontSize = 11.sp, color = SleekTextMuted)
                                Text("0.00001550 BTC (~ € 1.08)", fontWeight = FontWeight.Normal, fontSize = 11.sp, color = SleekPrimary)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Block hoogte", fontSize = 11.sp, color = SleekTextMuted)
                                Text("#849204", fontWeight = FontWeight.Normal, fontSize = 11.sp, color = SleekPrimary)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MembershipTab(
    userProfile: UserProfile?,
    onSelectPlan: (String, String, Double) -> Unit,
    onDownloadApk: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Direct APK Install block from design prompt instructions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(SleekWhite)
                .border(2.dp, SleekSecondary, RoundedCornerShape(32.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(EcoBlueBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("VERIFIED APK BUILD", fontSize = 10.sp, fontWeight = FontWeight.Black, color = SleekSecondary)
                    }
                    Text("14.2 MB", fontSize = 11.sp, color = SleekTextMuted)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Bitcoin Mining Simplified for Mobile Devices",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = SleekPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                Text(
                    text = "Installeer de BitVault mobiele controller rechtstreeks op externe apparatuur om ongestoord bitcoin te genereren.",
                    fontSize = 12.sp,
                    color = SleekTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Button(
                    onClick = { onDownloadApk() },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("download_apk_btn"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("📥 Download APK Bestanden", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EcoBlueBg)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("ℹ ", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(
                            text = "Om BitVault succesvol te installeren op je Android systeem: Schakel 'Installatie van onbekende bronnen' in via je Systeem Beveiligingsinstellingen.",
                            fontSize = 11.sp,
                            color = SleekSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Active Membership upgrading offers
        Text("Premium Hashrate Upgrading", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekPrimary, modifier = Modifier.padding(bottom = 12.dp))

        PlanUpgradeCard(
            planName = "Starter Miner",
            priceLabel = "€ 9.99 eenmalig",
            desc = "Hevel hashrate direct met +15 TH/s om mining loops te verdrievoudigen.",
            isCurrent = userProfile?.currentPlan == "Starter",
            onUpgrade = { onSelectPlan("starter", "Starter", 9.99) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PlanUpgradeCard(
            planName = "Pro ASIC Node",
            priceLabel = "€ 29.99 eenmalig",
            desc = "Hevel hashrate direct met +50 TH/s voor onbelemmerde dagelijkse bitcoin transfers.",
            isCurrent = userProfile?.currentPlan == "Pro",
            onUpgrade = { onSelectPlan("pro", "Pro", 29.99) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PlanUpgradeCard(
            planName = "Enterprise Rig Corp",
            priceLabel = "€ 99.99 eenmalig",
            desc = "Ontgrendel +180 TH/s dedicated cloud pools met direct priority support.",
            isCurrent = userProfile?.currentPlan == "Enterprise",
            onUpgrade = { onSelectPlan("enterprise", "Enterprise", 99.99) }
        )
    }
}

@Composable
fun ProfileTab(
    userProfile: UserProfile?,
    referrals: List<ReferralUser>,
    onTabSelect: (String) -> Unit,
    onWalletAddressClick: () -> Unit,
    onMineVaultBankClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Upper Profile Briefing
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekWhite),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SleekSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile?.userName?.split(" ")?.map { it.take(1) }?.joinToString("") ?: "MM",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(userProfile?.userName ?: "Matty Moors", fontWeight = FontWeight.Black, fontSize = 20.sp, color = SleekPrimary)
                Text(userProfile?.userEmail ?: "demo@minevault.app", fontSize = 13.sp, color = SleekTextMuted)

                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(EcoBlueBg)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text("LIDMAATSCHAP: ${userProfile?.currentPlan ?: "Pro"}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SleekSecondary)
                }
            }
        }

        // Section 2: Account options links
        Text("Account Besturing", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekPrimary, modifier = Modifier.padding(bottom = 10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekWhite),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileControlRow(label = "💰 Koppel crypto uitbetaling wallet", iconText = "🔗") {
                    onWalletAddressClick()
                }
                ProfileControlRow(label = "🏦 BitVault bankoverschrijving gegevens", iconText = "🏦") {
                    onMineVaultBankClick()
                }
                ProfileControlRow(label = "📊 Opbrengsten & Kalkulator", iconText = "📈") {
                    onTabSelect("calc")
                }
                ProfileControlRow(label = "🔐 Systeem Veiligheidslogboeken", iconText = "🛡️") {
                    onLogoutClick()
                }
            }
        }

        // Referral affiliate earnings sub-dashboard
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Affiliate Referrals Programma", fontWeight = FontWeight.Black, fontSize = 14.sp, color = SleekPrimary)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SleekSecondary)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("${referrals.size} Actief", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekWhite),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Deel je persoonlijke BitVault link. Verdien direct 10% commissie op elke contract- upgrade gekocht door je vrienden!",
                    color = SleekTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // List of referred users
                referrals.forEach { ref ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SleekGrayLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(ref.name.take(2), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SleekPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(ref.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Joined op ${ref.joinedAt}", fontSize = 10.sp, color = SleekTextMuted)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("€ ${String.format("%.2f", ref.amountEarned)} Commission", fontWeight = FontWeight.Black, fontSize = 12.sp, color = SuccessGreen)
                            Text(ref.status, fontSize = 10.sp, color = if (ref.status == "Actief") SuccessGreen else ErrorRed)
                        }
                    }
                    Divider(color = SleekBorder, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ── GENERIC SUBCOMPONENTS ──

@Composable
fun CardGridTelemetry(
    hashrate: Double,
    sessionEarnings: Double,
    activeContractsCount: Int,
    referralsCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TelemetryCard(
                label = "LIVE HASHRATE",
                value = "${String.format("%.1f", hashrate)} TH/s",
                icon = "⛏",
                color = SleekSecondary,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                label = "DAG OPBRENGSTEN",
                value = "€ ${String.format("%.2f", sessionEarnings)}",
                icon = "📈",
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TelemetryCard(
                label = "ACTIEVE NODES",
                value = "$activeContractsCount Nodes",
                icon = "🏢",
                color = SleekTertiary,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                label = "PARTNERS COOP",
                value = "$referralsCount Vrienden",
                icon = "👥",
                color = SleekPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TelemetryCard(
    label: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekWhite),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = SleekTextMuted)
                Text(icon, fontSize = 18.sp)
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun HardwareMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = SleekTextMuted, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun ContractItemRow(contract: MiningContract) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekWhite),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(0.5.dp, SleekBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔌", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                Column {
                    Text(contract.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Expires op ${contract.expiresAt}", fontSize = 10.sp, color = SleekTextMuted)
                }
            }
            Text("${contract.hashrate} TH/s", fontWeight = FontWeight.Black, color = SleekSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun TxItemRow(tx: TransactionLog) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (tx.amount > 0) EcoBlueBg else ColdPurpleBg),
                contentAlignment = Alignment.Center
            ) {
                Text(if (tx.amount > 0) "↙" else "↗", fontWeight = FontWeight.Bold, color = SleekPrimary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(tx.sub, fontSize = 10.sp, color = SleekTextMuted)
            }
        }

        Text(
            text = (if (tx.amount > 0) "+" else "") + " € ${String.format("%.2f", tx.amount)}",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = if (tx.amount > 0) SuccessGreen else SleekPrimary
        )
    }
    Divider(color = SleekBorder, thickness = 0.5.dp)
}

@Composable
fun WalletTxRow(
    tx: TransactionLog,
    onClick: () -> Unit,
    btcPriceEur: Double = 70000.0
) {
    val isIncoming = tx.amount > 0
    val systemTime = System.currentTimeMillis()
    val isPending = (systemTime - tx.timestamp) < 120_000 && (tx.type == "SEND" || tx.type == "RECEIVE")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isIncoming) EcoBlueBg else ColdPurpleBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isIncoming) "↙" else "↗",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isIncoming) SuccessGreen else ErrorRed
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = tx.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SleekPrimary
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isPending) Color(0xFFF59E0B) else SuccessGreen)
                    )
                    Text(
                        text = if (isPending) "In behandeling (1/6 blocks)" else "Bevestigd op-chain",
                        fontSize = 9.sp,
                        color = if (isPending) Color(0xFFD97706) else SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val dateStr = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp))
                Text(
                    text = "${tx.sub} • $dateStr",
                    fontSize = 10.sp,
                    color = SleekTextMuted
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            val btcVal = tx.amount / btcPriceEur
            Text(
                text = (if (isIncoming) "+" else "") + String.format("%.6f", btcVal) + " BTC",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = if (isIncoming) SuccessGreen else SleekPrimary
            )
            Text(
                text = "€ " + String.format("%.2f", tx.amount),
                fontSize = 10.sp,
                color = SleekTextMuted
            )
        }
    }
}

@Composable
fun ContractOfferRow(
    title: String,
    hashrate: Double,
    cost: Double,
    onBuy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekGrayLight),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Snelheid: $hashrate TH/s", fontSize = 11.sp, color = SleekTextMuted)
            }
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("€ $cost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BankRowInfo(label: String, value: String, onCopy: ((String) -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 10.sp, color = SleekTextMuted)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = SleekPrimary,
            modifier = Modifier.clickable(enabled = onCopy != null) { onCopy?.invoke(value) }
        )
    }
}

@Composable
fun ProfileControlRow(label: String, iconText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(iconText, fontSize = 16.sp, modifier = Modifier.padding(end = 10.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
        }
        Text("→", color = SleekTextMuted)
    }
    Divider(color = SleekBorder, thickness = 0.5.dp)
}

@Composable
fun PlanUpgradeCard(
    planName: String,
    priceLabel: String,
    desc: String,
    isCurrent: Boolean,
    onUpgrade: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekWhite),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isCurrent) SleekSecondary else SleekBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(planName, fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekPrimary)
                Text(priceLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekSecondary)
            }
            Text(desc, fontSize = 11.sp, color = SleekTextMuted, modifier = Modifier.padding(vertical = 8.dp))

            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) SuccessGreen else SleekPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isCurrent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isCurrent) "Reeds Actief ✓" else "Upgrade Nu naar $planName", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfitResultRow(
    title: String,
    netEur: Double,
    netBtc: Double,
    grossEur: Double,
    fuelEur: Double
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekWhite),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPrimary)
                Text("Gross: €${String.format("%.2f", grossEur)} | Fuel: €${String.format("%.2f", fuelEur)}", fontSize = 10.sp, color = SleekTextMuted)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "€ ${String.format("%.2f", netEur)} EUR",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (netEur > 0) SuccessGreen else ErrorRed
                )
                Text("≈ ${String.format("%.6f", netBtc)} BTC", fontSize = 11.sp, color = SleekSecondary)
            }
        }
    }
}

// ── CUSTOM REGISTRATION / AUTH SCREEN ──

@Composable
fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("mathmoors13@gmail.com") }
    var passwordInput by remember { mutableStateOf("adim") }
    var nameInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekWhite),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⛏", fontSize = 48.sp)
                Text(
                    text = if (isSignUp) "Maak Account Aan" else "Inloggen bij BitVault",
                    color = SleekPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                if (isSignUp) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Gebruikersnaam") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekSecondary, unfocusedBorderColor = SleekBorder),
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                    )
                }

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("E-mailadres") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekSecondary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Wachtwoord") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SleekSecondary, unfocusedBorderColor = SleekBorder),
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                )

                Button(
                    onClick = {
                        if (isSignUp) {
                            onRegister(nameInput, emailInput, passwordInput)
                        } else {
                            onLogin(emailInput, passwordInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).testTag("auth_submit_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isSignUp) "Registreren" else "Inloggen", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(
                        text = if (isSignUp) "Lid? Inloggen" else "Geen account? Nu registreren",
                        color = SleekSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
