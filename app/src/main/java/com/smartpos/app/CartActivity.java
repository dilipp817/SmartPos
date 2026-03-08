package com.smartpos.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.smartpos.app.adapter.CartAdapter;
import com.smartpos.app.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private static List<CartItem> cart = new ArrayList<>();
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView tvSubtotal, tvEmptyCart;
    private Button btnProceed, btnClear;

    public static void setCart(List<CartItem> cartItems) {
        cart = cartItems;
    }

    public static List<CartItem> getCart() {
        return cart;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Shopping Cart");
        }

        recyclerView = findViewById(R.id.rv_cart);
        tvSubtotal = findViewById(R.id.tv_cart_subtotal);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        btnProceed = findViewById(R.id.btn_proceed_to_bill);
        btnClear = findViewById(R.id.btn_clear_cart);

        adapter = new CartAdapter(cart, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnProceed.setOnClickListener(v -> {
            if (cart.isEmpty()) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, BillActivity.class);
                BillActivity.setCartItems(new ArrayList<>(cart));
                startActivity(intent);
            }
        });

        btnClear.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Cart")
                    .setMessage("Are you sure you want to clear the cart?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        cart.clear();
                        adapter.notifyDataSetChanged();
                        updateSubtotal();
                        updateEmptyState();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("cleared", true);
                        setResult(RESULT_OK, resultIntent);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        updateSubtotal();
        updateEmptyState();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onQuantityIncreased(int position) {
        CartItem item = cart.get(position);
        if (item.getQuantity() < item.getProduct().getStock() + item.getQuantity()) {
            item.setQuantity(item.getQuantity() + 1);
            adapter.notifyItemChanged(position);
            updateSubtotal();
        } else {
            Toast.makeText(this, "No more stock available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQuantityDecreased(int position) {
        CartItem item = cart.get(position);
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            adapter.notifyItemChanged(position);
        } else {
            onItemRemoved(position);
            return;
        }
        updateSubtotal();
    }

    @Override
    public void onItemRemoved(int position) {
        cart.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, cart.size());
        updateSubtotal();
        updateEmptyState();
    }

    private void updateSubtotal() {
        double subtotal = 0;
        for (CartItem item : cart) subtotal += item.getSubtotal();
        tvSubtotal.setText(String.format("Subtotal: ₹%.2f", subtotal));
    }

    private void updateEmptyState() {
        if (cart.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnProceed.setEnabled(false);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnProceed.setEnabled(true);
        }
    }
}
