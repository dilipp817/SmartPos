package com.smartpos.app;

import com.smartpos.app.model.Bill;
import com.smartpos.app.model.CartItem;
import com.smartpos.app.model.Product;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class BillCalculationTest {

    @Test
    public void testBillCalculation_correctSubtotal() {
        List<CartItem> items = new ArrayList<>();
        items.add(new CartItem(new Product(1, "Rice", "Food", 350.00, 10), 2));
        items.add(new CartItem(new Product(2, "Tea", "Beverages", 220.00, 5), 1));

        Bill bill = new Bill("Test Customer", items, 18.0);

        assertEquals(920.00, bill.getSubtotal(), 0.01);
    }

    @Test
    public void testBillCalculation_correctTax() {
        List<CartItem> items = new ArrayList<>();
        items.add(new CartItem(new Product(1, "Rice", "Food", 1000.00, 10), 1));

        Bill bill = new Bill("Test Customer", items, 18.0);

        assertEquals(180.00, bill.getTaxAmount(), 0.01);
    }

    @Test
    public void testBillCalculation_correctTotal() {
        List<CartItem> items = new ArrayList<>();
        items.add(new CartItem(new Product(1, "Rice", "Food", 1000.00, 10), 1));

        Bill bill = new Bill("Test Customer", items, 18.0);

        assertEquals(1180.00, bill.getTotal(), 0.01);
    }

    @Test
    public void testBillHasBillNumber() {
        List<CartItem> items = new ArrayList<>();
        items.add(new CartItem(new Product(1, "Item", "Category", 100.00, 5), 1));

        Bill bill = new Bill("Customer", items, 18.0);

        assertNotNull(bill.getBillNumber());
        assertTrue(bill.getBillNumber().startsWith("INV-"));
    }

    @Test
    public void testCartItemSubtotal() {
        Product product = new Product(1, "Test Product", "Test", 250.00, 10);
        CartItem cartItem = new CartItem(product, 3);
        assertEquals(750.00, cartItem.getSubtotal(), 0.01);
    }
}
