package com.autobill.smartpos.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.data.local.AppPrefsDataStore
import com.autobill.smartpos.domain.model.PrintJob
import com.autobill.smartpos.domain.model.PrintLineItem
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.printer.PrintError
import com.autobill.smartpos.domain.printer.PrinterDevice
import com.autobill.smartpos.domain.printer.toPrintError
import com.autobill.smartpos.domain.usecase.GetPairedBluetoothDevicesUseCase
import com.autobill.smartpos.domain.usecase.LogoutUseCase
import com.autobill.smartpos.domain.usecase.ObserveRestaurantUseCase
import com.autobill.smartpos.domain.usecase.ObserveSelectedPrinterUseCase
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import com.autobill.smartpos.domain.usecase.PrintBillUseCase
import com.autobill.smartpos.domain.usecase.SaveSelectedPrinterUseCase
import com.autobill.smartpos.R
import com.autobill.smartpos.util.BluetoothPermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 *
 * Combines three live data sources:
 *  1. [ObserveSessionUseCase]    — username / email / role
 *  2. [ObserveRestaurantUseCase] — restaurant name / currency
 *  3. [AppPrefsDataStore]        — isDarkTheme preference
 *
 * All three are observed as hot flows so the UI always reflects the latest state.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val observeRestaurantUseCase: ObserveRestaurantUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val appPrefsDataStore: AppPrefsDataStore,
    private val observeSelectedPrinterUseCase: ObserveSelectedPrinterUseCase,
    private val getPairedBluetoothDevicesUseCase: GetPairedBluetoothDevicesUseCase,
    private val saveSelectedPrinterUseCase: SaveSelectedPrinterUseCase,
    private val printBillUseCase: PrintBillUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Observe session (user profile)
        observeSessionUseCase()
            .onEach { user ->
                _uiState.update {
                    it.copy(
                        username = user?.username ?: "",
                        email    = user?.email    ?: "",
                        role     = user?.role     ?: "",
                    )
                }
            }
            .launchIn(viewModelScope)

        // Observe restaurant (name, currency)
        observeRestaurantUseCase()
            .onEach { restaurant ->
                _uiState.update {
                    it.copy(
                        restaurantName = restaurant?.outletName ?: "",
                        currency       = "INR", // Always INR in v1 — contract §8.2
                    )
                }
            }
            .launchIn(viewModelScope)

        // Observe theme preference
        appPrefsDataStore.observeIsDarkTheme()
            .onEach { dark -> _uiState.update { it.copy(isDarkTheme = dark) } }
            .launchIn(viewModelScope)

        // Observe persisted printer selection so the UI always shows the saved device.
        observeSelectedPrinterUseCase()
            .onEach { printer -> _uiState.update { it.copy(selectedPrinter = printer) } }
            .launchIn(viewModelScope)
    }

    // ── User actions ──────────────────────────────────────────────────────────

    fun toggleDarkTheme() {
        val newValue = !_uiState.value.isDarkTheme
        viewModelScope.launch {
            appPrefsDataStore.setDarkTheme(newValue)
            // uiState updates automatically via the observed flow above
        }
    }

    fun logout() {
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            logoutUseCase()
            // After clearSession(), ObserveSessionUseCase emits null →
            // MainActivity's sessionState resolves to Resolved(null) →
            // NavHost navigates to Login automatically.
        }
    }

    // ── Printer selection ─────────────────────────────────────────────────────

    /**
     * Load bonded devices and open the picker dialog.
     * Must be called AFTER the BLUETOOTH_CONNECT permission is granted (API 31+).
     */
    fun openPrinterPicker() {
        val btEnabled = BluetoothPermissionHelper.isBluetoothEnabled(context)
        val devices = if (btEnabled) getPairedBluetoothDevicesUseCase() else emptyList()
        _uiState.update { it.copy(pairedDevices = devices, showPrinterPickerDialog = true, isBluetoothEnabled = btEnabled) }
    }

    fun dismissPrinterPicker() {
        _uiState.update { it.copy(showPrinterPickerDialog = false) }
    }

    fun selectPrinter(device: PrinterDevice) {
        viewModelScope.launch {
            saveSelectedPrinterUseCase(device)
            _uiState.update { it.copy(showPrinterPickerDialog = false) }
        }
    }

    // ── Test Print ────────────────────────────────────────────────────────────

    /**
     * Sends a sample receipt to the saved printer.
     * Enabled only when [SettingsUiState.selectedPrinter] is non-null.
     *
     * Builds a synthetic [PrintJob] from the live restaurant / session state so
     * the cashier can verify alignment and font size without placing a real order.
     */
    fun testPrint() {
        if (_uiState.value.isTestPrinting) return
        _uiState.update { it.copy(isTestPrinting = true, testPrintResult = null) }
        viewModelScope.launch {
            val state = _uiState.value
            val now = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
            val job = PrintJob(
                restaurantName    = state.restaurantName.ifEmpty { "SmartPos Restaurant" },
                restaurantAddress = "Test Receipt — Printer Alignment Check",
                orderNumber       = "TEST-001",
                orderType         = "Dine-In",
                tableNumber       = "T-01",
                cashierName       = state.username.ifEmpty { "Cashier" },
                timestamp         = now,
                items = listOf(
                    PrintLineItem("Paneer Butter Masala",                  2, 180.0, 360.0),
                    PrintLineItem("Garlic Naan",                           3,  35.0, 105.0),
                    PrintLineItem("Mango Lassi (Special Summer Edition)",  1,  80.0,  80.0),
                ),
                subtotal       = 545.0,
                discountAmount = 45.0,
                cgstAmount     = 12.50,
                sgstAmount     = 12.50,
                totalAmount    = 525.00,
            )
            when (val result = printBillUseCase(job)) {
                is Result.Success ->
                    _uiState.update { it.copy(isTestPrinting = false, testPrintResult = context.getString(R.string.print_test_success)) }
                is Result.Failure -> {
                    val msg = when (val err = result.exception.toPrintError()) {
                        PrintError.NoPrinterConfigured -> context.getString(R.string.print_error_no_printer)
                        PrintError.BluetoothDisabled   -> context.getString(R.string.app_print_error_bluetooth_disabled)
                        PrintError.ConnectionFailed    -> context.getString(R.string.app_print_error_connection_failed)
                        is PrintError.Unknown          -> err.message
                    }
                    _uiState.update { it.copy(isTestPrinting = false, testPrintResult = msg) }
                }
                else -> Unit
            }
        }
    }

    fun onTestPrintResultConsumed() = _uiState.update { it.copy(testPrintResult = null) }
}
