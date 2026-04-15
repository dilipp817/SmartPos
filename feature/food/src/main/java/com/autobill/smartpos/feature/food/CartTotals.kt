package com.autobill.smartpos.feature.food

/**
 * Pre-computed cart display totals — produced by [CartViewModel], consumed by the UI.
 *
 * Moving the calculation here (instead of inside a @Composable) means the entire
 * subtotal/tax/total logic is testable with a plain JUnit test — no Compose runner needed:
 *
 *   @Test
 *   fun `tax is 18 percent of subtotal`() {
 *       // Arrange
 *       val fakeCart = listOf(
 *           CartItem(foodId = 1, foodName = "Pizza", foodPrice = 100.0, quantity = 2, ...)
 *       )
 *       // Act
 *       val totals = CartTotals.from(fakeCart)
 *       // Assert
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

