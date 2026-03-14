package com.autobill.smartpos.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DispatchersModule {
    val io: CoroutineDispatcher = Dispatchers.IO
}
