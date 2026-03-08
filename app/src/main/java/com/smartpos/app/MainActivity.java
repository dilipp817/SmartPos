package com.smartpos.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpos.app.adapter.ProductAdapter;
import com.smartpos.app.model.CartItem;
import com.smartpos.app.model.Product;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnAddToCartListener {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> products;
    private List<CartItem> cart = new ArrayList<>();
    private FloatingActionButton fabCart;
    private TextView tvCartCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv_products);
        fabCart = findViewById(R.id.fab_view_cart);
        tvCartCount = findViewById(R.id.tv_cart_count);

        products = getSampleProducts();
        adapter = new ProductAdapter(products, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabCart.setOnClickListener(v -> {
            if (cart.isEmpty()) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, CartActivity.class);
                CartActivity.setCart(cart);
                startActivityForResult(intent, 100);
            }
        });

        updateCartBadge();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            cart = CartActivity.getCart();
            if (resultCode == RESULT_OK && data != null && data.getBooleanExtra("cleared", false)) {
                cart.clear();
                // Restore stock using a map for O(n) lookup
                List<Product> originals = getSampleProducts();
                java.util.Map<Integer, Integer> originalStock = new java.util.HashMap<>();
                for (Product orig : originals) {
                    originalStock.put(orig.getId(), orig.getStock());
                }
                for (Product p : products) {
                    Integer stock = originalStock.get(p.getId());
                    if (stock != null) p.setStock(stock);
                }
                adapter.updateProducts(products);
            }
            updateCartBadge();
        }
    }

    @Override
    public void onAddToCart(Product product) {
        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                if (item.getQuantity() < product.getStock()) {
                    item.setQuantity(item.getQuantity() + 1);
                    product.setStock(product.getStock() - 1);
                    adapter.updateProducts(products);
                    updateCartBadge();
                    Toast.makeText(this, product.getName() + " quantity updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No more stock available", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        if (product.getStock() > 0) {
            cart.add(new CartItem(product, 1));
            product.setStock(product.getStock() - 1);
            adapter.updateProducts(products);
            updateCartBadge();
            Toast.makeText(this, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Out of stock!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCartBadge() {
        int count = 0;
        for (CartItem item : cart) count += item.getQuantity();
        if (count > 0) {
            tvCartCount.setVisibility(View.VISIBLE);
            tvCartCount.setText(String.valueOf(count));
        } else {
            tvCartCount.setVisibility(View.GONE);
        }
    }

    private List<Product> getSampleProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product(1, "Basmati Rice (5kg)", "Food", 350.00, 50));
        list.add(new Product(2, "Whole Wheat Atta (10kg)", "Food", 480.00, 40));
        list.add(new Product(3, "Tata Tea Premium (500g)", "Beverages", 220.00, 60));
        list.add(new Product(4, "Amul Butter (500g)", "Dairy", 275.00, 30));
        list.add(new Product(5, "Maggi Noodles (Pack of 12)", "Food", 144.00, 80));
        list.add(new Product(6, "USB-C Charging Cable", "Electronics", 299.00, 100));
        list.add(new Product(7, "Wireless Earbuds", "Electronics", 1499.00, 25));
        list.add(new Product(8, "Phone Back Cover", "Electronics", 199.00, 75));
        list.add(new Product(9, "Cotton T-Shirt", "Clothing", 399.00, 45));
        list.add(new Product(10, "Formal Trousers", "Clothing", 899.00, 20));
        list.add(new Product(11, "Dove Shampoo (650ml)", "Personal Care", 349.00, 55));
        list.add(new Product(12, "Colgate Toothpaste (200g)", "Personal Care", 89.00, 90));
        return list;
    }
}
