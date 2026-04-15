package com.autobill.smartpos.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.model.CartItem
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.usecase.AddToCartUseCase
import com.autobill.smartpos.domain.usecase.ClearCartUseCase
import com.autobill.smartpos.domain.usecase.DecreaseCartQuantityUseCase
import com.autobill.smartpos.domain.usecase.GetCartUseCase
import com.autobill.smartpos.domain.usecase.IncreaseCartQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel: Cart
 *
 * Single responsibility: manage cart state and expose pre-computed display totals.
 * Deliberately isolated from food-list concerns — [FoodViewModel] handles pagination.
 *
 * Testability: only 5 constructor params, all interfaces.
 *
 *   val vm = CartViewModel(
 *       getCartUseCase       = FakeGetCartUseCase(flowOf(listOf(pizzaItem))),
 *       addToCartUseCase     = FakeAddToCartUseCase(),
 *       increaseCartQuantityUseCase  = FakeIncreaseCartQuantityUseCase(),
 *       decreaseCartQuantityUseCase  = FakeDecreaseCartQuantityUseCase(),
 *       clearCartUseCase     = FakeClearCartUseCase(),
 *   )
 *   assertEquals(1, vm.cartItems.value.size)
 *   assertEquals(18.0, vm.cartTotals.value.tax, 0.001)  // 18% of 100
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val increaseCartQuantityUseCase: IncreaseCartQuantityUseCase,
    private val decreaseCartQuantityUseCase: DecreaseCartQuantityUseCase,
    private val clearCartUseCase: ClearCartUseCase,
) : ViewModel() {

    /**
     * Single subscription to CartRepository — one collection coroutine, one upstream observer.
     * [cartTotals] derives from this StateFlow (which is already hot/cached), so it does
     * NOT create a second subscription — it simply maps the already-live emissions.
     *
     * Flow graph:
     *   getCartUseCase() → cartItems (stateIn, 1 coroutine)
     *                           ↓ map { }
     *                      cartTotals (stateIn on hot StateFlow, 0 extra coroutines)
     */
    val cartItems: StateFlow<List<CartItem>> = getCartUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    /**
     * Pre-computed display totals derived from [cartItems].
     * Maps from the already-live [cartItems] StateFlow — no additional upstream subscription.
     * The composable reads these values directly — no arithmetic in the UI layer.
     *
     * ⚠️ Tax is a local 18% estimate for cart-preview UX only.
     *    Real breakdown (CGST/SGST) is computed server-side via POST /generate-bill.
     */
    val cartTotals: StateFlow<CartTotals> = cartItems
        .map { items ->
            val subtotal = items.sumOf { it.subtotal }
            val tax      = subtotal * 0.18
            CartTotals(
                itemCount = items.sumOf { it.quantity },
                subtotal  = subtotal,
                tax       = tax,
                total     = subtotal + tax,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CartTotals.EMPTY)

    /**
     * Add a food item to cart. Accepts the full [Food] domain model so this ViewModel
     * has no dependency on the food-list state held by [FoodViewModel].
     */
    fun addToCart(food: Food) {
        viewModelScope.launch { addToCartUseCase(food) }
    }

    /** Increase quantity of a cart item by 1. */
    fun increaseQuantity(foodId: Long) {
        viewModelScope.launch { increaseCartQuantityUseCase(foodId) }
    }

    /** Decrease quantity by 1 — removes item from cart if quantity reaches 0. */
    fun decreaseQuantity(foodId: Long) {
        viewModelScope.launch { decreaseCartQuantityUseCase(foodId) }
    }

    /** Remove all items from cart. */
    fun clearCart() {
        viewModelScope.launch { clearCartUseCase() }
    }

    /** Returns the current quantity of a food item in cart (0 = not in cart). */
    fun getCartQuantity(foodId: Long): Int =
        cartItems.value.find { it.foodId == foodId }?.quantity ?: 0
}



