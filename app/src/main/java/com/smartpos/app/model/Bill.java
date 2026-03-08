package com.smartpos.app.model;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Bill {
    private String billNumber;
    private String customerName;
    private String dateTime;
    private List<CartItem> items;
    private double subtotal;
    private double taxRate;
    private double taxAmount;
    private double total;

    public Bill(String customerName, List<CartItem> items, double taxRate) {
        this.billNumber = generateBillNumber();
        this.customerName = customerName;
        this.items = items;
        this.taxRate = taxRate;
        this.dateTime = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(new Date());
        calculateAmounts();
    }

    private String generateBillNumber() {
        int num = new Random().nextInt(90000) + 10000;
        return "INV-" + num;
    }

    private void calculateAmounts() {
        subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getSubtotal();
        }
        taxAmount = subtotal * taxRate / 100.0;
        total = subtotal + taxAmount;
    }

    public String getBillNumber() { return billNumber; }
    public String getCustomerName() { return customerName; }
    public String getDateTime() { return dateTime; }
    public List<CartItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getTaxRate() { return taxRate; }
    public double getTaxAmount() { return taxAmount; }
    public double getTotal() { return total; }
}
