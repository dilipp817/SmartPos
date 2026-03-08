package com.smartpos.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.smartpos.app.model.Bill;
import com.smartpos.app.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class BillActivity extends AppCompatActivity {

    private static List<CartItem> cartItems = new ArrayList<>();

    public static void setCartItems(List<CartItem> items) {
        cartItems = items;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Invoice");
        }

        Bill bill = new Bill("Walk-in Customer", cartItems, 18.0);

        TextView tvBillNumber = findViewById(R.id.tv_bill_number);
        TextView tvDateTime = findViewById(R.id.tv_bill_datetime);
        TextView tvCustomer = findViewById(R.id.tv_bill_customer);
        TextView tvSubtotal = findViewById(R.id.tv_bill_subtotal);
        TextView tvTax = findViewById(R.id.tv_bill_tax);
        TextView tvTotal = findViewById(R.id.tv_bill_total);
        RecyclerView rvItems = findViewById(R.id.rv_bill_items);
        Button btnNewBill = findViewById(R.id.btn_new_bill);

        tvBillNumber.setText("Bill No: " + bill.getBillNumber());
        tvDateTime.setText("Date: " + bill.getDateTime());
        tvCustomer.setText("Customer: " + bill.getCustomerName());
        tvSubtotal.setText(String.format("₹%.2f", bill.getSubtotal()));
        tvTax.setText(String.format("GST (%.0f%%): ₹%.2f", bill.getTaxRate(), bill.getTaxAmount()));
        tvTotal.setText(String.format("₹%.2f", bill.getTotal()));

        BillItemAdapter billAdapter = new BillItemAdapter(bill.getItems());
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(billAdapter);

        btnNewBill.setOnClickListener(v -> {
            cartItems.clear();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("cleared", true);
            startActivity(intent);
            finish();
        });
    }

    static class BillItemAdapter extends RecyclerView.Adapter<BillItemAdapter.BillItemViewHolder> {
        private List<CartItem> items;

        BillItemAdapter(List<CartItem> items) { this.items = items; }

        @NonNull
        @Override
        public BillItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
            return new BillItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BillItemViewHolder holder, int position) {
            CartItem item = items.get(position);
            holder.tvName.setText(item.getProduct().getName());
            holder.tvQtyPrice.setText(String.format("%d x ₹%.2f", item.getQuantity(), item.getProduct().getPrice()));
            holder.tvSubtotal.setText(String.format("₹%.2f", item.getSubtotal()));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class BillItemViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvQtyPrice, tvSubtotal;
            BillItemViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_bill_item_name);
                tvQtyPrice = itemView.findViewById(R.id.tv_bill_item_qty_price);
                tvSubtotal = itemView.findViewById(R.id.tv_bill_item_subtotal);
            }
        }
    }
}
