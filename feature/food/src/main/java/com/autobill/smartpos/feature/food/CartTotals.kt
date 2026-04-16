package com.autobill.smartpos.feature.food

/**
 * Pre-computed cart display totals — produced by [CartViewModel], consumed by the UI.
 *
 * The calculation lives in [CartViewModel.cartTotals] (a derived StateFlow), keeping
 * all arithmetic out of the UI layer and making it testable with a plain JUnit test:
 *
 *   @Test
 *   fun `tax is 18 percent of subtotal`() {
 *       val vm = CartViewModel(
 *           getCartUseCase = FakeGetCartUseCase(flowOf(listOf(
 *               CartItem(foodId = 1L, foodName = "Pizza", foodPrice = 100.0, quantity = 2)
 *           ))),
 *           // ... other fake use-cases
 *       )
 *       val totals = vm.cartTotals.value
 *       assertEquals(200.0, totals.subtotal, 0.001)
 *       assertEquals(36.0,  totals.tax,      0.001)
 *       assertEquals(236.0, totals.total,    0.001)
 *   }
 *
 * ⚠️ Tax here is a local 18% estimate for cart-preview UX only.
 *    The real bill (with CGST 9% + SGST 9% breakdown) is computed server-side via
 *    POST /orders/{id}/generate-bill — never use [total] for actual payment.
 */
data class CartTotals(
    val itemCount: Int,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
) {
    companion object {
        /** Neutral zero state — emitted before any cart items exist. */
        val EMPTY = CartTotals(itemCount = 0, subtotal = 0.0, tax = 0.0, total = 0.0)
    }
}

