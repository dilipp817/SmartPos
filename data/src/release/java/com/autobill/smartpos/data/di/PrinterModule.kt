package com.autobill.smartpos.data.di

import com.autobill.smartpos.data.printer.BluetoothEscPosPrinterImpl
import com.autobill.smartpos.data.repository.PrinterRepositoryImpl
import com.autobill.smartpos.domain.printer.BillPrinter
import com.autobill.smartpos.domain.repository.PrinterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RELEASE build — wires [BluetoothEscPosPrinterImpl] as [BillPrinter].
 * Sends raw ESC/POS bytes over a Bluetooth RFCOMM socket to the paired thermal printer.
 * Printer must be selected by the cashier in Settings before use.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PrinterModule {

    @Binds
    @Singleton
    abstract fun bindBillPrinter(impl: BluetoothEscPosPrinterImpl): BillPrinter

    @Binds
    @Singleton
    abstract fun bindPrinterRepository(impl: PrinterRepositoryImpl): PrinterRepository
}

