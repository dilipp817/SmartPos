package com.autobill.smartpos.data

import kotlinx.coroutines.Dispatchers

// Coroutine Dispatchers Module
// Provides different dispatchers for various types of operations
object DispatchersModule {
    // IO Dispatcher: Used for network calls and database operations
    val io = Dispatchers.IO

    // Main Dispatcher: Used for UI updates
    val main = Dispatchers.Main

    // Default Dispatcher: Used for CPU-intensive operations
    val default = Dispatchers.Default
}

