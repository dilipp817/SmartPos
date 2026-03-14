package com.autobill.smartpos.core.common

sealed interface FoodResponse<out T> {
    data object Loading : FoodResponse<Nothing>
    data class Success<T>(val data: T) : FoodResponse<T>
    data class Error(val message: String, val throwable: Throwable? = null) : FoodResponse<Nothing>
}

