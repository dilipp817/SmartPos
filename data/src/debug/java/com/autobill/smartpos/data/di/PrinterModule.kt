package com.autobill.smartpos.data.di

import com.autobill.smartpos.data.printer.FakePrinterImpl
import com.autobill.smartpos.data.repository.DebugPrinterRepositoryImpl
import com.autobill.smartpos.domain.printer.BillPrinter
import com.autobill.smartpos.domain.repository.PrinterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DEBUG build — wires [FakePrinterImpl] as [BillPrinter].
 * Receipts are formatted as plain text and logged to Logcat (tag: "FakePrinter").
 * No Bluetooth hardware required for development and testing.
 *
 * Binds [DebugPrinterRepositoryImpl] so the printer-picker dialog shows
 * mock "virtual printer" entries when no real Bluetooth devices are paired
 * (e.g. on the Android emulator).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PrinterModule {

    @Binds
    @Singleton
    abstract fun bindBillPrinter(impl: FakePrinterImpl): BillPrinter

    @Binds
    @Singleton
    abstract fun bindPrinterRepository(impl: DebugPrinterRepositoryImpl): PrinterRepository
}


